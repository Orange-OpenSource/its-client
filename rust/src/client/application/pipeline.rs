/*
 * Software Name : libits
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 * Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) library based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
 */

use crate::client::application::analyzer::Analyzer;
use crate::client::configuration::Configuration;
use crate::exchange::cause::Cause;
use crate::exchange::message::information::Information;
use crate::exchange::sequence_number::SequenceNumber;
use crate::exchange::Exchange;
use crate::monitor::trace_exchange;
use crate::transport::mqtt::mqtt_client::{listen, MqttClient};
use crate::transport::mqtt::mqtt_router;
use crate::transport::mqtt::topic::Topic;
use crate::transport::packet::Packet;
use crate::transport::payload::Payload;
use crossbeam_channel::{unbounded, Receiver};
use log::{debug, error, info, trace, warn};
use rumqttc::v5::{Event, EventLoop};
use serde::de::DeserializeOwned;
use std::any::Any;
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
    Receiver<Packet<T, Exchange>>,
    Receiver<(Packet<T, Exchange>, Option<Cause>)>,
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
    Receiver<Packet<T, Exchange>>,
    Receiver<(Packet<T, Exchange>, Option<Cause>)>,
    Receiver<Packet<T, Information>>,
    JoinHandle<()>,
);

pub async fn run<A, C, T>(
    configuration: Arc<Configuration>,
    context: Arc<RwLock<C>>,
    sequence_number: Arc<RwLock<SequenceNumber>>,
    subscription_list: &[T],
) where
    A: Analyzer<T, C>,
    T: Topic + 'static,
    C: Send + Sync + 'static,
{
    let mut thread_count: usize = 1;
    {
        let node_configuration = configuration
            .node
            .as_ref()
            .expect("Node configuration is required for analysis")
            .read()
            .unwrap();

        if let Some(value) = node_configuration.thread_count {
            thread_count = value;
        }
    }
    info!("Analysis thread count set to: {}", thread_count);

    let (mut mqtt_client, event_loop) = MqttClient::new(&configuration.mqtt_options);
    mqtt_client_subscribe(subscription_list, &mut mqtt_client).await;

    let (event_receiver, mqtt_client_listen_handle) = mqtt_client_listen_thread(event_loop);
    let (item_receiver, monitoring_receiver, information_receiver, mqtt_router_dispatch_handle) =
        mqtt_router_dispatch_thread(subscription_list.to_vec(), event_receiver);

    let monitor_reception_handle = monitor_thread(
        "received_on".to_string(),
        configuration.clone(),
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
            info!("starting analyser generation...");
            trace!("analyser generation closure entering...");
            let mut analyser = A::new(configuration_clone, context_clone, seq_num_clone);
            for item in rx {
                for publish_item in analyser.analyze(item.clone()) {
                    let cause = Cause::from_exchange(&(item.payload));
                    match tx.send((publish_item, cause)) {
                        Ok(()) => trace!("analyser sent"),
                        Err(error) => {
                            error!("stopped to send analyser: {}", error);
                            break;
                        }
                    }
                }
                trace!("analyser generation closure finished");
            }
        });
    }

    let (publish_item_receiver, publish_monitoring_receiver, filter_handle) =
        filter_thread::<T>(configuration.clone(), analyser_receiver);

    let reader_configure_handle =
        reader_configure_thread(configuration.clone(), information_receiver);

    let monitor_publish_handle = monitor_thread(
        "sent_on".to_string(),
        configuration,
        publish_monitoring_receiver,
    );

    mqtt_client_publish(publish_item_receiver, &mut mqtt_client).await;

    debug!("mqtt_client_listen_handler joining...");
    mqtt_client_listen_handle.await.unwrap();
    debug!("mqtt_router_dispatch_handler joining...");
    mqtt_router_dispatch_handle.join().unwrap();
    debug!("monitor_reception_handle joining...");
    monitor_reception_handle.join().unwrap();
    debug!("reader_configure_handler joining...");
    reader_configure_handle.join().unwrap();
    debug!("analyser_generate_handler joining...");
    analysis_pool.join();
    debug!("filter_handle joining...");
    filter_handle.join().unwrap();
    debug!("monitor_publish_handle joining...");
    monitor_publish_handle.join().unwrap();

    warn!("loop done");
    tokio::time::sleep(Duration::from_secs(5)).await;
}

fn filter_thread<T>(
    _configuration: Arc<Configuration>,
    exchange_receiver: Receiver<(Packet<T, Exchange>, Option<Cause>)>,
) -> FilterPipes<T>
where
    T: Topic + 'static,
{
    info!("starting filtering...");
    let (publish_sender, publish_receiver) = unbounded();
    let (monitoring_sender, monitoring_receiver) = unbounded();
    let handle = thread::Builder::new()
        .name("filter".into())
        .spawn(move || {
            trace!("filter closure entering...");
            for tuple in exchange_receiver {
                let item = tuple.0;
                let cause = tuple.1;

                // FIXME Topic does not hold geo_extension anymore
                //assumed clone, we just send the GeoExtension
                // if configuration.is_in_region_of_responsibility(item.topic.geo_extension.clone()) {
                //assumed clone, we send to 2 channels
                match publish_sender.send(item.clone()) {
                    Ok(()) => trace!("publish sent"),
                    Err(error) => {
                        error!("stopped to send publish: {}", error);
                        break;
                    }
                }
                match monitoring_sender.send((item, cause)) {
                    Ok(()) => trace!("monitoring sent"),
                    Err(error) => {
                        error!("stopped to send monitoring: {}", error);
                        break;
                    }
                }
                // }
                trace!("filter closure finished");
            }
        })
        .unwrap();
    info!("filter started");
    (publish_receiver, monitoring_receiver, handle)
}

fn monitor_thread<T>(
    direction: String,
    configuration: Arc<Configuration>,
    exchange_receiver: Receiver<(Packet<T, Exchange>, Option<Cause>)>,
) -> JoinHandle<()>
where
    T: Topic + 'static,
{
    info!("starting monitor reception thread...");
    let handle = thread::Builder::new()
        .name("monitor-reception".into())
        .spawn(move || {
            trace!("monitor reception entering...");

            for tuple in exchange_receiver {
                let packet = tuple.0;
                let cause = tuple.1;

                let node_configuration = configuration
                    .node
                    .as_ref()
                    .expect("Pipeline requires NodeConfiguration")
                    .read()
                    .unwrap();

                if let Some(gateway_component_name) = node_configuration.gateway_component_name() {
                    trace_exchange(
                        &packet.payload,
                        cause,
                        direction.as_str(),
                        configuration.component_name(None),
                        format!(
                            "{}/{}/{}",
                            gateway_component_name,
                            packet.topic.as_route(),
                            packet.payload.source_uuid
                        ),
                    );
                } else {
                    info!("Cannot trace exchange, missing gateway component name in node configuration");
                }
            }
        })
        .unwrap();
    info!("monitor reception thread started");
    handle
}

fn mqtt_client_listen_thread(
    event_loop: EventLoop,
) -> (Receiver<Event>, tokio::task::JoinHandle<()>) {
    info!("Starting MQTT listening thread...");
    let (event_sender, event_receiver) = unbounded();
    let handle = tokio::task::spawn(async move {
        trace!("mqtt client listening closure entering...");
        listen(event_loop, event_sender).await;
        trace!("mqtt client listening closure finished");
    });
    info!("MQTT listening thread started!");
    (event_receiver, handle)
}

fn reader_configure_thread<T>(
    configuration: Arc<Configuration>,
    information_receiver: Receiver<Packet<T, Information>>,
) -> JoinHandle<()>
where
    T: Topic + 'static,
{
    info!("Starting configuration reader thread...");
    let handle = thread::Builder::new()
        .name("reader-configurator".into())
        .spawn(move || {
            trace!("reader configuration closure entering...");
            for packet in information_receiver {
                info!(
                    "we received an information on the topic {}: {:?}",
                    packet.topic, packet.payload
                );

                configuration
                    .node
                    .as_ref()
                    .expect("Node app requires node configuration")
                    .write()
                    .unwrap()
                    .update(packet.payload);
            }
            trace!("reader configuration closure finished");
        })
        .unwrap();
    info!("Configuration reader thread started!");
    handle
}

async fn mqtt_client_subscribe<T: Topic>(topic_list: &[T], client: &mut MqttClient) {
    info!("mqtt client subscribing starting...");
    let mut topic_subscription_list = topic_list.iter().map(|t| t.to_string()).collect::<Vec<_>>();

    for topic in topic_subscription_list.iter_mut() {
        match topic {
            info_topic if info_topic.contains(Information::TYPE) => {
                info_topic.push_str("/broker");
            }
            topic => topic.push_str("/+/#"),
        }
    }

    // NOTE: we share the topic list with the dispatcher
    client.subscribe(&topic_subscription_list).await;
    info!("mqtt client subscribing finished");
}

async fn mqtt_client_publish<T, P>(
    publish_item_receiver: Receiver<Packet<T, P>>,
    client: &mut MqttClient,
) where
    T: Topic,
    P: Payload,
{
    info!("Starting MQTT publishing thread...");
    for item in publish_item_receiver {
        debug!("Packet to publish...");
        client.publish(item).await;
        debug!("Packet published!");
    }
    info!("MQTT publishing thread stopping");
}

fn mqtt_router_dispatch_thread<T>(
    topic_list: Vec<T>,
    event_receiver: Receiver<Event>,
    // FIXME manage a Box into the Exchange to use a unique object Trait instead
) -> DispatchPipes<T>
where
    T: Topic + 'static,
{
    info!("starting mqtt router dispatching...");
    let (exchange_sender, exchange_receiver) = unbounded();
    let (monitoring_sender, monitoring_receiver) = unbounded();
    let (information_sender, information_receiver) = unbounded();

    let handle = thread::Builder::new()
        .name("mqtt-router-dispatcher".into())
        .spawn(move || {
            trace!("mqtt router dispatching closure entering...");
            //initialize the router
            let router = &mut mqtt_router::MqttRouter::new();

            for topic in topic_list.iter() {
                match topic {
                    info_topic if info_topic.to_string().contains(Information::TYPE) => {
                        router.add_route(info_topic.clone(), deserialize::<Information>);
                    }
                    _ => router.add_route(topic.clone(), deserialize::<Exchange>),
                }
            }

            for event in event_receiver {
                match router.handle_event(event) {
                    Some((topic, reception)) => {
                        // TODO use the From Trait
                        if reception.is::<Exchange>() {
                            if let Ok(exchange) = reception.downcast::<Exchange>() {
                                let item = Packet {
                                    topic,
                                    payload: *exchange,
                                };
                                //assumed clone, we send to 2 channels
                                match monitoring_sender.send((item.clone(), None)) {
                                    Ok(()) => trace!("mqtt monitoring sent"),
                                    Err(error) => {
                                        error!("stopped to send mqtt monitoring: {}", error);
                                        break;
                                    }
                                }
                                match exchange_sender.send(item) {
                                    Ok(()) => trace!("mqtt exchange sent"),
                                    Err(error) => {
                                        error!("stopped to send mqtt exchange: {}", error);
                                        break;
                                    }
                                }
                            }
                        } else if let Ok(information) = reception.downcast::<Information>() {
                            match information_sender.send(Packet {
                                topic,
                                payload: *information,
                            }) {
                                Ok(()) => trace!("mqtt information sent"),
                                Err(error) => {
                                    error!("stopped to send mqtt information: {}", error);
                                    break;
                                }
                            }
                        }
                    }
                    None => trace!("no mqtt response to send"),
                }
            }
            trace!("mqtt router dispatching closure finished");
        })
        .unwrap();
    info!("mqtt router dispatching started");
    (
        exchange_receiver,
        monitoring_receiver,
        information_receiver,
        handle,
    )
}

fn deserialize<T>(
    publish: rumqttc::v5::mqttbytes::v5::Publish,
) -> Option<Box<dyn Any + 'static + Send>>
where
    T: DeserializeOwned + Payload + 'static + Send,
{
    // Incoming publish from the broker
    match String::from_utf8(publish.payload.to_vec()) {
        Ok(message) => {
            let message_str = message.as_str();
            match serde_json::from_str::<T>(message_str) {
                Ok(message) => {
                    trace!("message parsed");
                    return Some(Box::new(message));
                }
                Err(e) => warn!("parse error({}) on: {}", e, message_str),
            }
        }
        Err(e) => warn!("format error: {}", e),
    }
    None
}
