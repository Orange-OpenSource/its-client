// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2023 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas BUFFON <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use crate::transport::mqtt::geo_topic::GeoTopicError;
use std::{cmp, fmt, hash, str};

#[derive(Debug, Default, Clone)]
pub(crate) enum Queue {
    #[default]
    In,
    Out,
}

impl fmt::Display for Queue {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{}",
            match self {
                Queue::In => "inQueue".to_string(),
                Queue::Out => "outQueue".to_string(),
            }
        )
    }
}

impl From<&str> for Queue {
    fn from(s: &str) -> Self {
        match s {
            "inQueue" => Queue::In,
            "outQueue" => Queue::Out,
            element => panic!(
                "Unable to convert from the element {} as a Queue, use from_str instead",
                element
            ),
        }
    }
}

impl From<String> for Queue {
    fn from(s: String) -> Self {
        Queue::from(s.as_str())
    }
}

impl hash::Hash for Queue {
    fn hash<H: hash::Hasher>(&self, state: &mut H) {
        self.to_string().hash(state);
    }
}

impl cmp::PartialEq for Queue {
    fn eq(&self, other: &Self) -> bool {
        self.to_string() == other.to_string()
    }
}

impl str::FromStr for Queue {
    type Err = GeoTopicError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "inQueue" | "outQueue" => Ok(Queue::from(s)),
            element => Err(GeoTopicError::UnknownQueue(element.to_string())),
        }
    }
}
