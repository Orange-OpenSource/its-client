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
}

impl<T: Topic, P: Payload> Packet<T, P> {
    pub fn new(topic: T, payload: P) -> Self {
        Packet { topic, payload }
    }
}
