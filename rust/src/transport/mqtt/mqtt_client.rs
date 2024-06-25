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

use crate::transport::mqtt::topic::Topic;
use crate::transport::packet::Packet;
use crate::transport::payload::Payload;

use crossbeam_channel::Sender;
use log::{debug, error, info, trace, warn};
use rumqttc::v5::mqttbytes::v5::Filter;
use rumqttc::v5::mqttbytes::QoS;
use rumqttc::v5::{AsyncClient, Event, EventLoop, MqttOptions};

pub(crate) struct MqttClient {
    client: AsyncClient,
}

impl<'client> MqttClient {
    pub fn new(options: &MqttOptions) -> (Self, EventLoop) {
        let (client, event_loop) = AsyncClient::new(options.clone(), 1000);
        (MqttClient { client }, event_loop)
    }

    pub async fn subscribe(&mut self, topic_list: &[String]) {
        match self
            .client
            .subscribe_many(
                topic_list
                    .iter()
                    .map(|topic| Filter::new(topic.clone(), QoS::AtMostOnce))
                    .collect::<Vec<Filter>>(),
            )
            .await
        {
            Ok(()) => debug!("sent subscriptions"),
            Err(e) => error!(
                "failed to send subscriptions, is the connection close? \nError: {:?}",
                e
            ),
        };
    }

    pub async fn publish<T: Topic, P: Payload>(&self, item: Packet<T, P>) {
        let payload = serde_json::to_string(&item.payload).unwrap();
        match self
            .client
            .publish(item.topic.to_string(), QoS::ExactlyOnce, false, payload)
            .await
        {
            Ok(()) => {
                trace!("sent publish");
            }
            Err(e) => error!(
                "Failed to send publish, is the connection close? \nError: {:?}",
                e
            ),
        };
    }
}

pub async fn listen(mut event_loop: EventLoop, sender: Sender<Event>) {
    info!("listening started");
    let mut listening = true;
    while listening {
        match event_loop.poll().await {
            Ok(event) => match sender.send(event) {
                Ok(()) => trace!("item sent"),
                Err(error) => {
                    error!("stopped to send item: {}", error);
                    listening = false;
                }
            },
            Err(error) => {
                error!("stopped to receive event: {:?}", error);
                listening = false;
            }
        }
    }
    warn!("listening done");
}
