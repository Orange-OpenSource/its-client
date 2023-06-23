// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
use std::any::Any;
use std::collections::HashMap;
use std::sync::mpsc::{channel, Receiver};
use std::sync::Arc;
use std::thread;
use std::thread::JoinHandle;
use std::time::Duration;

use log::{debug, error, info, trace, warn};
use rumqttc::{Event, EventLoop, MqttOptions, Publish};
use serde::de::DeserializeOwned;

use crate::analyse::analyser::Analyser;
use crate::analyse::cause::Cause;
use crate::analyse::configuration::Configuration;
use crate::analyse::item::Item;
use crate::monitor;
use crate::mqtt::mqtt_client::{listen, Client};
use crate::mqtt::mqtt_router;

use crate::reception::exchange::Exchange;
use crate::reception::information::Information;
use crate::reception::typed::Typed;
use crate::reception::Reception;

/// Struct holding the results of the MQTT dispatcher thread initialization
///
/// Holding:
/// - one [exchange][1] channel receiver
/// - one [exchange][1]/cause channel receiver for monitoring reception
/// - one [information][2] channel receiver
/// - the [join handle][3] to manage the thread's termination
///
/// [1]: Exchange
/// [2]: Information
/// [3]: JoinHandle
type DispatcherPipes = (
    Receiver<Item<Exchange>>,
    Receiver<(Item<Exchange>, Option<Cause>)>,
    Receiver<Item<Information>>,
    JoinHandle<()>,
);

/// Struct holding the result of the exchange analysis thread initialization
///
/// Holding:
/// - the [exchange][1] channel receiver that will be fed with the exchanges to send
/// - the [join handle][2] to manage the thread's termination
///
/// [1]: Exchange
/// [2]: JoinHandle
type AnalyzerPipes = (Receiver<(Item<Exchange>, Option<Cause>)>, JoinHandle<()>);

/// Struct holding the result of the output exchanges filter thread initialization
///
/// Holding:
/// - one [exchange][1] channel receiver for exchange sending monitoring
/// - one [exchange][1]/cause channel receiver for exchange MQTT publishing
/// - the [join handle][2] to manage the tread's termination
///
/// [1]: Exchange
/// [2]: JoinHandle
type FilterPipes = (
    Receiver<Item<Exchange>>,
    Receiver<(Item<Exchange>, Option<Cause>)>,
    JoinHandle<()>,
);

pub async fn run<T: Analyser>(
    mqtt_options: MqttOptions,
    mqtt_root_topic: &str,
    region_of_responsibility: bool,
    custom_settings: HashMap<String, String>,
) {
    loop {
        // build the shared topic list
        let topic_list = vec![
            format!("{}/v2x/cam", mqtt_root_topic),
            format!("{}/v2x/cpm", mqtt_root_topic),
            format!("{}/v2x/denm", mqtt_root_topic),
            format!("{}/v2x/map", mqtt_root_topic),
            format!("{}/v2x/spat", mqtt_root_topic),
            format!("{}/info", mqtt_root_topic),
        ];

        //initialize the client
        let (mut client, event_loop) = Client::new(&mqtt_options);

        let configuration = Arc::new(Configuration::new(
            mqtt_options.client_id(),
            region_of_responsibility,
            custom_settings.clone(),
        ));

        // subscribe
        mqtt_client_subscribe(&topic_list, &mut client).await;
        // receive
        let (event_receiver, mqtt_client_listen_handle) = mqtt_client_listen_thread(event_loop);
        // dispatch
        let (item_receiver, monitoring_receiver, information_receiver, mqtt_router_dispatch_handle) =
            mqtt_router_dispatch_thread(topic_list, event_receiver);

        // in parallel, monitor exchanges reception
        let monitor_reception_handle = monitor_thread(
            "received_on".to_string(),
            configuration.clone(),
            monitoring_receiver,
        );
        // in parallel, analyse exchanges
        let (analyser_item_receiver, analyser_generate_handle) =
            analyser_generate_thread::<T>(configuration.clone(), item_receiver);

        // read information
        let reader_configure_handle =
            reader_configure_thread(configuration.clone(), information_receiver);

        // filter exchanges on region of responsibility
        let (publish_item_receiver, publish_monitoring_receiver, filter_handle) =
            filter_thread::<T>(configuration.clone(), analyser_item_receiver);

        // in parallel, monitor exchanges publish
        let monitor_publish_handle = monitor_thread(
            "sent_on".to_string(),
            configuration,
            publish_monitoring_receiver,
        );
        // in parallel send
        mqtt_client_publish(publish_item_receiver, &mut client).await;

        debug!("mqtt_client_listen_handler joining...");
        mqtt_client_listen_handle.await.unwrap();
        debug!("mqtt_router_dispatch_handler joining...");
        mqtt_router_dispatch_handle.join().unwrap();
        debug!("monitor_reception_handle joining...");
        monitor_reception_handle.join().unwrap();
        debug!("reader_configure_handler joining...");
        reader_configure_handle.join().unwrap();
        debug!("analyser_generate_handler joining...");
        analyser_generate_handle.join().unwrap();
        debug!("filter_handle joining...");
        filter_handle.join().unwrap();
        debug!("monitor_publish_handle joining...");
        monitor_publish_handle.join().unwrap();

        warn!("loop done");
        tokio::time::sleep(Duration::from_secs(5)).await;
    }
}

fn mqtt_client_listen_thread(
    event_loop: EventLoop,
) -> (Receiver<Event>, tokio::task::JoinHandle<()>) {
    info!("starting mqtt client listening...");
    let (event_sender, event_receiver) = channel();
    let handle = tokio::task::spawn(async move {
        trace!("mqtt client listening closure entering...");
        listen(event_loop, event_sender).await;
        trace!("mqtt client listening closure finished");
    });
    info!("mqtt client listening started");
    (event_receiver, handle)
}

fn mqtt_router_dispatch_thread(
    topic_list: Vec<String>,
    event_receiver: Receiver<Event>,
    // FIXME manage a Box into the Exchange to use a unique object Trait instead
) -> DispatcherPipes {
    info!("starting mqtt router dispatching...");
    let (exchange_sender, exchange_receiver) = channel();
    let (monitoring_sender, monitoring_receiver) = channel();
    let (information_sender, information_receiver) = channel();

    let handle = thread::Builder::new()
        .name("mqtt-router-dispatcher".into())
        .spawn(move || {
            trace!("mqtt router dispatching closure entering...");
            //initialize the router
            let router = &mut mqtt_router::Router::new();

            for topic in topic_list.iter() {
                match topic {
                    info_topic if info_topic.contains(Information::get_type().as_str()) => {
                        router.add_route(info_topic, deserialize::<Information>);
                    }
                    _ => router.add_route(topic, deserialize::<Exchange>),
                }
            }

            for event in event_receiver {
                match router.handle_event(event) {
                    Some((topic, reception)) => {
                        // TODO use the From Trait
                        if reception.is::<Exchange>() {
                            if let Ok(boxed_exchange) = reception.downcast::<Exchange>() {
                                let item = Item {
                                    topic,
                                    reception: *boxed_exchange,
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
                        } else if let Ok(boxed_information) = reception.downcast::<Information>() {
                            match information_sender.send(Item {
                                topic,
                                reception: *boxed_information,
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

fn monitor_thread(
    direction: String,
    configuration: Arc<Configuration>,
    exchange_receiver: Receiver<(Item<Exchange>, Option<Cause>)>,
) -> JoinHandle<()> {
    info!("starting monitor reception thread...");
    let handle = thread::Builder::new()
        .name("monitor-reception".into())
        .spawn(move || {
            trace!("monitor reception entering...");
            for tuple in exchange_receiver {
                let publish_item = tuple.0;
                let cause = tuple.1;
                // monitor
                monitor::monitor(
                    &publish_item.reception,
                    cause,
                    direction.as_str(),
                    // assumed clone, we preserve it for the topics
                    configuration.component_name(None),
                    format!(
                        "{}/{}/{}",
                        configuration.gateway_component_name(),
                        publish_item.topic.project_base(),
                        publish_item.reception.source_uuid
                    ),
                );
            }
        })
        .unwrap();
    info!("monitor reception thread started");
    handle
}

fn analyser_generate_thread<T: Analyser>(
    configuration: Arc<Configuration>,
    exchange_receiver: Receiver<Item<Exchange>>,
) -> AnalyzerPipes {
    info!("starting analyser generation...");
    let (analyser_sender, analyser_receiver) = channel();
    let handle = thread::Builder::new()
        .name("analyser-generator".into())
        .spawn(move || {
            trace!("analyser generation closure entering...");
            //initialize the analyser
            let mut analyser = T::new(configuration);
            for item in exchange_receiver {
                for publish_item in analyser.analyze(item.clone()) {
                    let cause = Cause::from_exchange(&(item.reception));
                    match analyser_sender.send((publish_item, cause)) {
                        Ok(()) => trace!("analyser sent"),
                        Err(error) => {
                            error!("stopped to send analyser: {}", error);
                            break;
                        }
                    }
                }
                trace!("analyser generation closure finished");
            }
        })
        .unwrap();
    info!("analyser generation started");
    (analyser_receiver, handle)
}

fn filter_thread<T: Analyser>(
    configuration: Arc<Configuration>,
    exchange_receiver: Receiver<(Item<Exchange>, Option<Cause>)>,
) -> FilterPipes {
    info!("starting filtering...");
    let (publish_sender, publish_receiver) = channel();
    let (monitoring_sender, monitoring_receiver) = channel();
    let handle = thread::Builder::new()
        .name("filter".into())
        .spawn(move || {
            trace!("filter closure entering...");
            for tuple in exchange_receiver {
                let item = tuple.0;
                let cause = tuple.1;

                //assumed clone, we just send the GeoExtension
                if configuration.is_in_region_of_responsibility(item.topic.geo_extension.clone()) {
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
                }
                trace!("filter closure finished");
            }
        })
        .unwrap();
    info!("filter started");
    (publish_receiver, monitoring_receiver, handle)
}

fn reader_configure_thread(
    configuration: Arc<Configuration>,
    information_receiver: Receiver<Item<Information>>,
) -> JoinHandle<()> {
    info!("starting reader configuration...");
    let handle = thread::Builder::new()
        .name("reader-configurator".into())
        .spawn(move || {
            trace!("reader configuration closure entering...");
            for item in information_receiver {
                info!(
                    "we received an information on the topic {}: {:?}",
                    item.topic, item.reception
                );
                configuration.update(item.reception);
            }
            trace!("reader configuration closure finished");
        })
        .unwrap();
    info!("reader configuration started");
    handle
}

fn deserialize<T>(publish: Publish) -> Option<Box<dyn Any + 'static + Send>>
where
    T: DeserializeOwned + Reception + 'static + Send,
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
    Option::None
}

async fn mqtt_client_subscribe(topic_list: &[String], client: &mut Client) {
    info!("mqtt client subscribing starting...");
    let mut topic_subscription_list = Vec::new();

    for topic in topic_list.iter() {
        match topic {
            info_topic if info_topic.contains(Information::get_type().as_str()) => {
                topic_subscription_list.push(format!("{}/broker", info_topic));
            }
            _ => topic_subscription_list.push(format!("{}/+/#", topic)),
        }
    }

    // NOTE: we share the topic list with the dispatcher
    client.subscribe(topic_subscription_list).await;
    info!("mqtt client subscribing finished");
}

async fn mqtt_client_publish(publish_item_receiver: Receiver<Item<Exchange>>, client: &mut Client) {
    info!("mqtt client publishing starting...");
    for item in publish_item_receiver {
        debug!("we received a publish");
        client.publish(item).await;
        debug!("we forwarded the publish");
    }
    info!("mqtt client publishing finished");
}
