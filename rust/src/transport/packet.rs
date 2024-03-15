// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use crate::exchange::Exchange;

use crate::transport::mqtt::topic::Topic;

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct Packet<T>
where
    T: Topic,
{
    pub topic: T,
    pub exchange: Exchange,
}

impl<T: Topic> Packet<T> {
    pub fn new(topic: T, exchange: Exchange) -> Self {
        Packet { topic, exchange }
    }
}
