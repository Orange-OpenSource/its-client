/*
 * Software Name : libits-client
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 */

use crate::client::application::json_analyzer::JsonAnalyzer;
use crate::client::configuration::Configuration;
use crate::exchange::Exchange;
use crate::exchange::cause::Cause;
use crate::exchange::message::information::Information;
use crate::exchange::sequence_number::SequenceNumber;
use crate::monitor::trace_exchange;
use crate::transport::mqtt::mqtt_client::MqttClient;
use crate::transport::mqtt::mqtt_router;
use crate::transport::mqtt::topic::Topic;
use crate::transport::packet::Packet;
use crossbeam_channel::{Receiver, unbounded};
use log::{debug, error, info, trace, warn};
use rumqttc::{Event, PublishProperties};
use serde_json::Value;
use std::sync::{Arc, RwLock};
use std::thread;
use std::thread::JoinHandle;
use std::time::Duration;

/// Struct holding the result of the output exchanges filter thread initialization
///
/// Holding:
/// - one [exchange][1] channel receiver for exchange sending monitoring
/// - one [exchange][1]/cause channel receiver for exchange MQTT publishing
/// - the [join handle][2] to manage the tread's termination
///
/// [1]: Exchange
/// [2]: JoinHandle
type FilterPipes<T> = (
    Receiver<Packet<T, Value>>,
    Receiver<(Packet<T, Value>, Option<Cause>)>,
    JoinHandle<()>,
);

/// Struct holding the result of the output exchanges router dispatch thread initialization
///
/// Holding:
/// - the [exchange][1] channel receiver to provide to the analysis threads
/// - the [exchange][1]/cause channel receiver to provide to the monitoring thread
/// - the [information][2] channel receiver to provide to configuration updater thread
/// - the [join handle][3] to manage the thread's termination
///
/// [1]: Exchange
/// [2]: Information
/// [3]: JoinHandle
type DispatchPipes<T> = (
    Receiver<Packet<T, Value>>,
    Receiver<(Packet<T, Value>, Option<Cause>)>,
    Receiver<Packet<T, Information>>,
    JoinHandle<()>,
);

pub async fn run<A, C, InTopic, OutTopic>(
    configuration: Arc<Configuration>,
    context: Arc<RwLock<C>>,
    sequence_number: Arc<RwLock<SequenceNumber>>,
    subscription_list: &[InTopic],
) where
    A: JsonAnalyzer<InTopic, OutTopic, C>,
    InTopic: Topic + 'static,
    OutTopic: Topic + 'static,
    C: Send + Sync + 'static,
{
    let thread_count = configuration.mobility.thread_count;
    info!("Analysis thread count set to: {thread_count}");
    let information = Arc::new(RwLock::new(Information::default()));

    loop {
        let (mut mqtt_client, event_loop) = MqttClient::new(&configuration.mqtt);
        mqtt_client_subscribe(subscription_list, &mut mqtt_client).await;

        let (event_receiver, mqtt_client_listen_handle) = mqtt_client_listen_thread(event_loop);
        let (item_receiver, monitoring_receiver, information_receiver, mqtt_router_dispatch_handle) =
            mqtt_router_dispatch_thread(subscription_list.to_vec(), event_receiver);

        let monitor_reception_handle = monitor_thread(
            "received_on".to_string(),
            // assumed clone
            configuration.mobility.source_uuid.clone(),
            // assumed clone, only on the Arc, not on the RwLock
            information.clone(),
            monitoring_receiver,
        );

        let analysis_pool = threadpool::ThreadPool::with_name("Analysis".to_string(), thread_count);

        let (analyser_sender, analyser_receiver) = unbounded();
        for _ in 0..thread_count {
            let rx = item_receiver.clone();
            let tx = analyser_sender.clone();
            let configuration_clone = configuration.clone();
            let context_clone = context.clone();
            let seq_num_clone = sequence_number.clone();
            analysis_pool.execute(move || {
                info!("Starting analyser generation...");
                trace!("Analyser generation closure entering...");
                let mut analyser = A::new(configuration_clone, context_clone, seq_num_clone);
                loop {
                    match rx.recv() {
                        Ok(item) => {
                            for publish_item in analyser.analyze(item.clone()) {
                                let cause = Cause::from_json_value(&(item.payload));
                                if let Err(error) = tx.send((publish_item, cause)) {
                                    error!("Stopped to send analyser: {error}");
                                    // break is not enough here as it only exits the for when we
                                    // need to exit the loop it is in, so use return instead
                                    return;
                                }
                            }
                            trace!("Analyser generation closure finished");
                        }
                        Err(recv_error) => {
                            info!("Exiting analysis thread: {recv_error}");
                            break;
                        }
                    }
                }
            });
        }
        // Drop the original sender as only clones in threads remains
        drop(analyser_sender);

        let (publish_item_receiver, publish_monitoring_receiver, filter_handle) =
            filter_thread::<OutTopic>(configuration.clone(), analyser_receiver);

        // assumed clone, only on the Arc, not on the RwLock
        let information_handle = information_thread(information.clone(), information_receiver);

        let monitor_publish_handle = monitor_thread(
            "sent_on".to_string(),
            // assumed clone
            configuration.mobility.source_uuid.clone(),
            // assumed clone, only on the Arc, not on the RwLock
            information.clone(),
            publish_monitoring_receiver,
        );

        mqtt_client_publish(publish_item_receiver, &mut mqtt_client).await;

        debug!("Start mqtt_client_listen_handler joining...");
        mqtt_client_listen_handle.await.unwrap();
        debug!("Start mqtt_router_dispatch_handler joining...");
        mqtt_router_dispatch_handle.join().unwrap();
        debug!("Start monitor_reception_handle joining...");
        monitor_reception_handle.join().unwrap();
        debug!("Start reader_configure_handler joining...");
        information_handle.join().unwrap();
        debug!("Start analyser_generate_handler joining...");
        analysis_pool.join();
        debug!("Start filter_handle joining...");
        filter_handle.join().unwrap();
        debug!("Start monitor_publish_handle joining...");
        monitor_publish_handle.join().unwrap();

        warn!("Loop done");
        tokio::time::sleep(Duration::from_secs(5)).await;
    }
}

/// Output packet filter thread
///
/// This thread filters the packets returned by the analyzer to check if they have to be sent or not
/// This was first in place due to RoR[^1] management
///
/// [^1]: Region of Responsibility
fn filter_thread<T>(
    _configuration: Arc<Configuration>,
    exchange_receiver: Receiver<(Packet<T, Value>, Option<Cause>)>,
) -> FilterPipes<T>
where
    T: Topic + 'static,
{
    info!("Starting filtering...");
    let (publish_sender, publish_receiver) = unbounded();
    let (monitoring_sender, monitoring_receiver) = unbounded();
    let handle = thread::Builder::new()
        .name("filter".into())
        .spawn(move || {
            trace!("Filter closure entering...");
            loop {
                match exchange_receiver.recv() {
                    Ok(tuple) => {
                        let item = tuple.0;
                        let cause = tuple.1;

                        // FIXME Topic does not hold geo_extension anymore
                        //assumed clone, we just send the GeoExtension
                        // if configuration.is_in_region_of_responsibility(item.topic.geo_extension.clone()) {
                        //assumed clone, we send to 2 channels
                        if let Err(error) = publish_sender.send(item.clone()) {
                            error!("Stopped to send publish: {error}");
                            // Use return instead of break as we need to exit
                            // the entire thread function, not just the loop
                            return;
                        }
                        if let Err(error) = monitoring_sender.send((item, cause)) {
                            error!("Stopped to send monitoring: {error}");
                            // Use return instead of break as we need to exit
                            // the entire thread function, not just the loop
                            return;
                        }
                        trace!("Filter closure finished");
                    }
                    Err(recv_error) => {
                        info!("Exiting filter thread: {recv_error}");
                        break;
                    }
                }
            }
        })
        .unwrap();
    info!("Filter started");
    (publish_receiver, monitoring_receiver, handle)
}

fn monitor_thread<T>(
    direction: String,
    source_uuid: String,
    information: Arc<RwLock<Information>>,
    exchange_receiver: Receiver<(Packet<T, Value>, Option<Cause>)>,
) -> JoinHandle<()>
where
    T: Topic + 'static,
{
    info!("Starting monitor reception thread...");
    let handle = thread::Builder::new()
        .name("monitor-reception".into())
        .spawn(move || {
            trace!("Monitor reception entering...");
            for tuple in exchange_receiver {
                // FIXME check for exchanges
                let packet = tuple.0;
                let cause = tuple.1;
                let information_instance_id = &information.read().unwrap().instance_id;

                if let Ok(exchange) = serde_json::from_value::<Exchange>(packet.payload) {
                    trace_exchange(
                        &exchange,
                        cause,
                        direction.as_str(),
                        source_uuid.as_str(),
                        format!(
                            "{}/{}/{}",
                            information_instance_id,
                            packet.topic.as_route(),
                            exchange.source_uuid
                        ),
                    );
                } else {
                    trace!("Payload is not an exchange, cannot trace");
                }
            }
        })
        .unwrap();
    info!("Monitor reception thread started");
    handle
}

fn mqtt_client_listen_thread(
    event_loop: EventLoop,
) -> (Receiver<Event>, tokio::task::JoinHandle<()>) {
    info!("Starting MQTT listening thread...");
    let (event_sender, event_receiver) = unbounded();
    let handle = tokio::task::spawn(async move {
        trace!("MQTT listening closure entering...");
        listen(event_loop, event_sender).await;
        trace!("MQTT listening closure finished");
    });
    info!("MQTT listening thread started");
    (event_receiver, handle)
}

fn information_thread<T>(
    information: Arc<RwLock<Information>>,
    information_receiver: Receiver<Packet<T, Information>>,
) -> JoinHandle<()>
where
    T: Topic + 'static,
{
    info!("Starting configuration reader thread...");
    let handle = thread::Builder::new()
        .name("reader-configurator".into())
        .spawn(move || {
            trace!("Reader configuration closure entering...");
            for packet in information_receiver {
                info!("We received a new information");
                debug!(
                    "Information on the topic {}: {:?}",
                    packet.topic, packet.payload
                );
                information.write().unwrap().replace(packet.payload);
            }
            trace!("Reader configuration closure finished");
        })
        .unwrap();
    info!("Configuration reader thread started");
    handle
}

async fn mqtt_client_subscribe<T: Topic>(topic_list: &[T], client: &mut MqttClient) {
    info!("MQTT client subscribing starting...");
    let topic_subscription_list: Vec<_> = topic_list
        .iter()
        .map(|t| {
            format!(
                "{}{}",
                t,
                // TODO challenge if we can switch to a standard GeoTopic (adding a uuid as last required part) to simplify the code
                if t.to_string().contains(Information::TYPE) {
                    "/#"
                } else {
                    "/+/#"
                }
            )
        })
        .collect();

    // NOTE: we share the topic list with the dispatcher
    client.subscribe(&topic_subscription_list).await;
    info!("MQTT client subscribing finished");
}

async fn mqtt_client_publish<T, P>(
    publish_item_receiver: Receiver<Packet<T, P>>,
    client: &mut MqttClient,
) where
    T: Topic,
    P: Payload,
{
    info!("Starting MQTT publishing thread...");

    loop {
        match publish_item_receiver.recv() {
            Ok(packet) => {
                debug!("Start packet publishing...");
                client.publish(packet).await;
                debug!("Packet published");
            }
            Err(recv_err) => {
                info!("Exiting MQTT publish thread: {recv_err}");
                break;
            }
        }
    }
    info!("MQTT publishing thread stopped");
}

/// Incoming packets dispatching thread
fn mqtt_router_dispatch_thread<T>(
    topic_list: Vec<T>,
    event_receiver: Receiver<Event>,
    // FIXME manage a Box into the Exchange to use a unique object Trait instead
) -> DispatchPipes<T>
where
    T: Topic + 'static,
{
    info!("Starting mqtt router dispatching...");
    let (exchange_sender, exchange_receiver) = unbounded();
    let (monitoring_sender, monitoring_receiver) = unbounded();
    let (information_sender, information_receiver) = unbounded();

    let handle = thread::Builder::new()
        .name("mqtt-router-dispatcher".into())
        .spawn(move || {
            trace!("MQTT router dispatching closure entering...");
            //initialize the router
            let router = &mut mqtt_router::MqttRouter::default();

            for topic in topic_list.iter() {
                match topic {
                    info_topic if info_topic.to_string().contains(Information::TYPE) => {
                        router.add_route(info_topic.clone(), deserialize::<Information>);
                    }
                    _ => router.add_route(topic.clone(), deserialize::<Value>),
                }
            }

            loop {
                match event_receiver.recv() {
                    Ok(event) => match router.handle_event(event) {
                        Some((topic, (reception, properties))) => {
                            trace!("Topic: {topic}");
                            if reception.is::<Information>() {
                                if let Ok(information) = reception.downcast::<Information>() {
                                    match information_sender.send(Packet {
                                        topic,
                                        payload: *information,
                                        properties: PublishProperties::default(),
                                    }) {
                                        Ok(()) => trace!("MQTT information sent"),
                                        Err(error) => {
                                            error!("Stopped to send mqtt information: {error}");
                                            return;
                                        }
                                    }
                                }
                            } else {
                                if let Ok(value) = reception.downcast::<Value>() {
                                    let item = Packet {
                                        topic,
                                        payload: *value,
                                        properties,
                                    };
                                    match exchange_sender.send(item.clone()) {
                                        Ok(()) => trace!("MQTT exchange sent"),
                                        Err(error) => {
                                            error!("Stopped to send mqtt exchange: {error}");
                                            return;
                                        }
                                    }
                                    match monitoring_sender.send((item, None)) {
                                        Ok(()) => trace!("MQTT monitoring sent"),
                                        Err(error) => {
                                            error!("Stopped to send mqtt monitoring: {error}");
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        None => trace!("No mqtt response to send"),
                    },
                    Err(recv_err) => {
                        info!("Exiting MQTT routing thread: {recv_err}");
                        break;
                    }
                }
            }
            trace!("MQTT router dispatching closure finished");
        })
        .unwrap();
    info!("MQTT router dispatching started");
    (
        exchange_receiver,
        monitoring_receiver,
        information_receiver,
        handle,
    )
}

fn deserialize<T>(publish: Publish) -> Option<BoxedReception>
where
    T: DeserializeOwned + Payload + 'static + Send,
{
    // Incoming publish from the broker
    match String::from_utf8(publish.payload.to_vec()) {
        Ok(message) => {
            let message_str = message.as_str();
            match serde_json::from_str::<T>(message_str) {
                Ok(message) => {
                    trace!("Message parsed");
                    return Some((Box::new(message), publish.properties.unwrap_or_default()));
                }
                Err(e) => warn!("Parse error({e}) on: {message_str}"),
            }
        }
        Err(e) => warn!("Format error: {e}"),
    }
    None
}
