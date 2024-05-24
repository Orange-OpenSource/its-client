// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2023 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas BUFFON <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use crate::transport::mqtt::geo_topic::GeoTopicError;
use std::{fmt, hash, str};

#[derive(Debug, Default, Clone)]
#[allow(clippy::upper_case_acronyms)]
pub(crate) enum MessageType {
    #[default]
    Any,
    CAM,
    DENM,
    CPM,
    INFO,
    MAP,
    SPAT,
}

impl fmt::Display for MessageType {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{}",
            match self {
                MessageType::Any => "+".to_string(),
                MessageType::CAM => "cam".to_string(),
                MessageType::DENM => "denm".to_string(),
                MessageType::CPM => "cpm".to_string(),
                MessageType::INFO => "info".to_string(),
                MessageType::MAP => "map".to_string(),
                MessageType::SPAT => "spat".to_string(),
            }
        )
    }
}

impl PartialEq for MessageType {
    fn eq(&self, other: &Self) -> bool {
        self.to_string() == other.to_string()
    }
}

impl From<&str> for MessageType {
    fn from(s: &str) -> Self {
        match s {
            "+" => MessageType::Any,
            "cam" => MessageType::CAM,
            "denm" => MessageType::DENM,
            "cpm" => MessageType::CPM,
            "info" => MessageType::INFO,
            "map" => MessageType::MAP,
            "spat" => MessageType::SPAT,
            element => panic!(
                "Unable to convert from the element {} as a MessageType, use from_str instead",
                element
            ),
        }
    }
}

impl From<String> for MessageType {
    fn from(s: String) -> Self {
        MessageType::from(s.as_str())
    }
}

impl hash::Hash for MessageType {
    fn hash<H: hash::Hasher>(&self, state: &mut H) {
        self.to_string().hash(state);
    }
}

impl str::FromStr for MessageType {
    type Err = GeoTopicError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "+" | "cam" | "denm" | "cpm" | "info" | "map" | "spat" => Ok(MessageType::from(s)),
            element => Err(GeoTopicError::UnknownMessageType(element.to_string())),
        }
    }
}
