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

use crate::exchange::Exchange;
use crate::exchange::message::Message;
use std::fmt::Formatter;

/// Cause is hereby used pass information to the [monitoring function][1] about which message a DENM
/// was emitted from
///
/// If the `analyze(...)` call of an Analyzer implementation return some DENM to send, the info in
/// the message that has provided to the method will be used to build a Caused that will allow to
/// tell that this DENM was detected using this message
///
/// [1]: crate::monitor::trace_exchange
pub(crate) struct Cause {
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
        match &exchange.message {
            Message::CAM(message) => Some(Cause::new(
                exchange.type_field.clone(),
                format!("{}/{}", message.station_id, message.generation_delta_time),
            )),
            Message::CPM(message) => Some(Cause::new(
                exchange.type_field.clone(),
                format!("{}/{}", message.station_id, message.generation_delta_time),
            )),
            _ => None,
        }
    }
}
