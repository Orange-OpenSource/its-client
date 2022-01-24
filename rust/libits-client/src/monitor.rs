// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
use crate::analyse::cause::Cause;
use crate::reception::exchange::message::Message;
use crate::reception::exchange::Exchange;
use crate::reception::mortal::now;

// TODO implement the Rust macro monitor!
pub fn monitor(
    exchange: &Exchange,
    cause: Option<Cause>,
    direction: &str,
    component: String,
    partner: String,
) {
    match &exchange.message {
        // FIXME find how to call position() on any Message implementing Mobile
        Message::CAM(message) => {
            // log to monitoring platform
            println!(
                "{} {} {} {} {}/{} at {}",
                component,
                exchange.type_field,
                direction,
                partner,
                message.station_id,
                message.generation_delta_time,
                now()
            );
        }
        Message::DENM(message) => {
            // log to monitoring platform
            println!(
                "{} {} {} {} {}/{}/{}/{}/{}{} at {}",
                component,
                exchange.type_field,
                direction,
                partner,
                message.station_id,
                message
                    .management_container
                    .action_id
                    .originating_station_id,
                message.management_container.action_id.sequence_number,
                message.management_container.reference_time,
                message.management_container.detection_time,
                get_cause_str(cause),
                now()
            );
        }
        Message::CPM(message) => {
            // log to monitoring platform
            println!(
                "{} {} {} {} {}/{} at {}",
                component,
                exchange.type_field,
                direction,
                partner,
                message.station_id,
                message.generation_delta_time,
                now()
            );
        }
    };
}

fn get_cause_str(cause: Option<Cause>) -> String {
    return match cause {
        Some(cause) => format!("/cause_type:{}/cause_id:{}", cause.m_type, cause.id),
        None => String::new(),
    };
}
