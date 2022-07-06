// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use std::any::Any;
use std::sync::Arc;
use std::sync::mpsc::{channel, Receiver};
use std::thread;
use std::thread::JoinHandle;
use log::{error, info, trace, warn};
use rumqttc::{Event, EventLoop};
use serde::de::DeserializeOwned;
use crate::analyse::cause::Cause;
use crate::analyse::configuration::Configuration;
use crate::analyse::item::Item;
use crate::monitor;
use crate::mqtt::mqtt_client::{Client, listen};
use crate::mqtt::mqtt_router;
use crate::reception::exchange::collective_perception_message::CollectivePerceptionMessage;
use crate::reception::exchange::cooperative_awareness_message::CooperativeAwarenessMessage;
use crate::reception::exchange::decentralized_environmental_notification_message::DecentralizedEnvironmentalNotificationMessage;
use crate::reception::exchange::Exchange;
use crate::reception::information::Information;
use crate::reception::Reception;
use crate::reception::typed::Typed;

pub mod producer_pipeline;
pub mod consumer_pipeline;

pub fn unbox<T>(value: Box<T>) -> T {
    *value
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

async fn mqtt_client_subscribe(topic_list: &Vec<String>, client: &mut Client) {
    info!("mqtt client subscribing starting...");
    // build the topic subscription list
    let mut topic_subscription_list = Vec::new();
    if let Some(cam_topic) = topic_list
        .iter()
        .find(|&r| r.contains(CooperativeAwarenessMessage::get_type().as_str()))
    {
        topic_subscription_list.push(format!("{}/+/#", cam_topic));
    }
    if let Some(denm_topic) = topic_list
        .iter()
        .find(|&r| r.contains(DecentralizedEnvironmentalNotificationMessage::get_type().as_str()))
    {
        topic_subscription_list.push(format!("{}/+/#", denm_topic));
    }
    if let Some(cpm_topic) = topic_list
        .iter()
        .find(|&r| r.contains(CollectivePerceptionMessage::get_type().as_str()))
    {
        topic_subscription_list.push(format!("{}/+/#", cpm_topic));
    }
    if let Some(info_topic) = topic_list
        .iter()
        .find(|&r| r.contains(Information::get_type().as_str()))
    {
        // The topic of the broker we are currently connected to
        // is always: "5GCroCo/backOutQueue/info/broker"
        topic_subscription_list.push(format!("{}/broker", info_topic));
    }

    // NOTE: we share the topic list with the dispatcher
    client.subscribe(topic_subscription_list).await;
    info!("mqtt client subscribing finished");
}

fn mqtt_router_dispatch_thread(
    topic_list: Vec<String>,
    event_receiver: Receiver<Event>,
    // FIXME manage a Box into the Exchange to use a unique object Trait instead
) -> (
    Receiver<Item<Exchange>>,
    Receiver<(Item<Exchange>, Option<Cause>)>,
    Receiver<Item<Information>>,
    JoinHandle<()>,
) {
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

            if let Some(cam_topic) = topic_list
                .iter()
                .find(|&r| r.contains(CooperativeAwarenessMessage::get_type().as_str()))
            {
                router.add_route(cam_topic, deserialize::<Exchange>);
            }
            if let Some(denm_topic) = topic_list.iter().find(|&r| {
                r.contains(DecentralizedEnvironmentalNotificationMessage::get_type().as_str())
            }) {
                router.add_route(denm_topic, deserialize::<Exchange>);
            }
            if let Some(cpm_topic) = topic_list
                .iter()
                .find(|&r| r.contains(CollectivePerceptionMessage::get_type().as_str()))
            {
                router.add_route(cpm_topic, deserialize::<Exchange>);
            }
            if let Some(info_topic) = topic_list
                .iter()
                .find(|&r| r.contains(Information::get_type().as_str()))
            {
                router.add_route(info_topic, deserialize::<Information>);
            }

            for event in event_receiver {
                match router.handle_event(event) {
                    Some((topic, reception)) => {
                        // TODO use the From Trait
                        if reception.is::<Exchange>() {
                            if let Ok(exchange) = reception.downcast::<Exchange>() {
                                let item = Item {
                                    topic,
                                    reception: unbox(exchange),
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
                            match information_sender.send(Item {
                                topic,
                                reception: unbox(information),
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

fn deserialize<T>(publish: rumqttc::Publish) -> Option<Box<dyn Any + 'static + Send>>
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
