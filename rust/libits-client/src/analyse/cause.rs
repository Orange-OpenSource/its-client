// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
use crate::reception::exchange::message::Message;
use crate::reception::exchange::Exchange;
use std::fmt::Formatter;

pub struct Cause {
    pub m_type: String,
    pub id: String,
}

impl std::fmt::Display for Cause {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "/cause_type:{}/cause_id:{}", self.m_type, self.id)
    }
}

impl Cause {
    fn new(m_type: String, id: String) -> Self {
        Self { m_type, id }
    }

    pub fn from_exchange(exchange: &Exchange) -> Option<Cause> {
        return match &exchange.message {
            Message::CAM(message) => Some(Cause::new(
                exchange.type_field.clone(),
                format!("{}/{}", message.station_id, message.generation_delta_time),
            )),
            Message::CPM(message) => Some(Cause::new(
                exchange.type_field.clone(),
                format!("{}/{}", message.station_id, message.generation_delta_time),
            )),
            _ => None,
        };
    }
}
