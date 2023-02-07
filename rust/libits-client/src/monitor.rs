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
        Message::CAM(cam) => {
            // log to monitoring platform
            println!(
                "{} {} {} {} {}/{} at {}",
                component,
                exchange.type_field,
                direction,
                partner,
                cam.station_id,
                cam.generation_delta_time,
                now()
            );
        }
        Message::DENM(denm) => {
            // log to monitoring platform
            println!(
                "{} {} {} {} {}/{}/{}/{}/{}{} at {}",
                component,
                exchange.type_field,
                direction,
                partner,
                denm.station_id,
                denm.management_container.action_id.originating_station_id,
                denm.management_container.action_id.sequence_number,
                denm.management_container.reference_time,
                denm.management_container.detection_time,
                get_cause_str(cause),
                now()
            );
        }
        Message::CPM(cpm) => {
            // log to monitoring platform
            println!(
                "{} {} {} {} {}/{} at {}",
                component,
                exchange.type_field,
                direction,
                partner,
                cpm.station_id,
                cpm.generation_delta_time,
                now()
            );
        }
        Message::MAPEM(map) => {
            println!(
                "{} {} {} {} {}/{}/{} at {}",
                component,
                exchange.type_field,
                direction,
                partner,
                map.sending_station_id.unwrap_or_default(),
                map.id,
                map.region.unwrap_or_default(),
                now()
            );
        }
        Message::SPATEM(spat) => {
            println!(
                "{} {} {} {} {}/{}/{} at {}",
                component,
                exchange.type_field,
                direction,
                partner,
                spat.sending_station_id.unwrap_or_default(),
                spat.id,
                spat.region.unwrap_or_default(),
                now()
            );
        }
    };
}

fn get_cause_str(cause: Option<Cause>) -> String {
    match cause {
        Some(cause) => format!("/cause_type:{}/cause_id:{}", cause.m_type, cause.id),
        None => String::new(),
    }
}
