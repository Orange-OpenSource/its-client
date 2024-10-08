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

use opentelemetry::propagation::{Extractor, Injector};
use rumqttc::v5::mqttbytes::v5::PublishProperties;
use std::fmt::Debug;

use crate::transport::mqtt::topic::Topic;
use crate::transport::payload::Payload;

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct Packet<T, P>
where
    T: Topic,
    P: Payload,
{
    pub topic: T,
    pub payload: P,
    pub properties: PublishProperties,
}

impl<T: Topic, P: Payload> Packet<T, P> {
    pub fn new(topic: T, payload: P) -> Self {
        Self {
            topic,
            payload,
            properties: PublishProperties::default(),
        }
    }
}

impl<T: Topic, P: Payload> Injector for Packet<T, P> {
    fn set(&mut self, key: &str, value: String) {
        self.properties
            .user_properties
            .push((key.to_string(), value));
    }
}

impl<T: Topic, P: Payload> Extractor for Packet<T, P> {
    fn get(&self, key: &str) -> Option<&str> {
        self.properties
            .user_properties
            .iter()
            .find(|(k, _)| key == k)
            .map(|(_, value)| value.as_str())
    }

    fn keys(&self) -> Vec<&str> {
        self.properties
            .user_properties
            .iter()
            .map(|(key, _)| key.as_str())
            .collect::<Vec<&str>>()
    }
}
