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
use crate::exchange::message::information::Information;
use crate::transport::mqtt::mqtt_client::{MqttClient, listen};
use crate::transport::mqtt::mqtt_router::BoxedReception;
use crate::transport::mqtt::topic::Topic;
use crate::transport::packet::Packet;
use crate::transport::payload::Payload;
use crossbeam_channel::{Receiver, unbounded};
use log::{debug, info, trace, warn};
use rumqttc::{Event, EventLoop};
use serde::de::DeserializeOwned;
use std::sync::Arc;
use std::sync::RwLock;
use std::thread;
use std::thread::JoinHandle;

pub mod exchange_pipeline;
pub mod json_pipeline;

pub(crate) fn mqtt_client_listen_thread(
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

fn deserialize<T>(publish: rumqttc::Publish) -> Option<BoxedReception>
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
