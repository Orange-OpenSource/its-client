// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
use serde::{Deserialize, Serialize};

use crate::analyse::configuration::Configuration;
use crate::reception::exchange::collective_perception_message::CollectivePerceptionMessage;
use crate::reception::exchange::cooperative_awareness_message::CooperativeAwarenessMessage;
use crate::reception::exchange::decentralized_environmental_notification_message::DecentralizedEnvironmentalNotificationMessage;
use crate::reception::exchange::mobile::Mobile;
use crate::reception::mortal::{etsi_timestamp, Mortal};
use crate::reception::typed::Typed;

#[derive(Clone, Debug, Hash, PartialEq, Serialize, Deserialize)]
#[serde(untagged)]
pub enum Message {
    CAM(CooperativeAwarenessMessage),
    DENM(DecentralizedEnvironmentalNotificationMessage),
    CPM(CollectivePerceptionMessage),
}

impl Message {
    pub fn get_type(&self) -> String {
        match self {
            // FIXME find how to call get_type() on any Message
            Message::CAM(_) => CooperativeAwarenessMessage::get_type(),
            Message::DENM(_) => DecentralizedEnvironmentalNotificationMessage::get_type(),
            Message::CPM(_) => CollectivePerceptionMessage::get_type(),
        }
    }

    pub fn appropriate(&mut self, configuration: &Configuration, timestamp: u128) -> u32 {
        match self {
            // FIXME find how to change the fileds on any Message
            Message::CAM(ref mut message) => {
                let station_id = configuration.station_id(Some(message.station_id));
                message.station_id = station_id;
                // TODO update the generation delta time
                station_id
            }
            Message::DENM(ref mut message) => {
                let station_id = configuration.station_id(Some(message.station_id));
                message.station_id = station_id;
                // FIXME find why the serde Serializer can't match the u128
                message.management_container.reference_time = etsi_timestamp(timestamp) as u64;
                station_id
            }
            Message::CPM(ref mut message) => {
                let station_id = configuration.station_id(Some(message.station_id));
                message.station_id = station_id;
                // TODO update the generation delta time
                station_id
            }
        }
    }

    pub fn as_mobile(&self) -> Result<&dyn Mobile, &'static str> {
        match self {
            Message::CAM(cam) => Ok(cam),
            Message::CPM(cpm) => match &cpm.station_data_container {
                Some(container) => match container.originating_vehicle_container {
                    Some(_) => Ok(cpm),
                    None => Err("RSU originating CPM is not a mobile"),
                },
                None => Err("No station data container; cannot convert as Mobile"),
            },
            Message::DENM(denm) => Ok(denm),
        }
    }
}

impl Mortal for Message {
    fn timeout(&self) -> u128 {
        if let Message::DENM(message) = self {
            return message.timeout();
        }
        // TODO implement a timeout on the cam and cpm
        0
    }

    fn terminate(&mut self) {
        if let Message::DENM(message) = self {
            message.terminate();
        }
        // TODO implement a terminate on the cam and cpm
    }

    fn terminated(&self) -> bool {
        if let Message::DENM(message) = self {
            return message.terminated();
        }
        // TODO implement a timeout on the cam and cpm
        false
    }
}
