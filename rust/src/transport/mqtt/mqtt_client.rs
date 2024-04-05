// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use crate::transport::mqtt::topic::Topic;
use crate::transport::packet::Packet;
use log::{debug, error, info, trace, warn};
use rumqttc::{AsyncClient, Event, EventLoop, MqttOptions, QoS, SubscribeFilter};

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
                    .map(|topic| SubscribeFilter::new(topic.clone(), QoS::AtMostOnce))
                    .collect::<Vec<SubscribeFilter>>(),
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

    pub async fn publish<T: Topic>(&self, item: Packet<T>) {
        let payload = serde_json::to_string(&item.exchange).unwrap();
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

pub async fn listen(mut event_loop: EventLoop, sender: std::sync::mpsc::Sender<Event>) {
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
