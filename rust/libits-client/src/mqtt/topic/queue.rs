// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
use crate::mqtt::topic::parse_error::ParseError;
use std::{cmp, convert, fmt, hash, str};

#[derive(Debug, Clone)]
pub(crate) enum Queue {
    // To V2X server
    In,
    // From V2X server
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

impl Default for Queue {
    fn default() -> Self {
        Queue::In
    }
}

impl convert::From<&str> for Queue {
    fn from(s: &str) -> Self {
        match s {
            "inQueue" => Queue::In,
            "outQueue" => Queue::Out,
            // no Result on the From trait : use FromStr trait instead
            element => panic!(
                "Unable to convert from the element {} as a Queue, use from_str instead",
                element
            ),
        }
    }
}

impl convert::From<String> for Queue {
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
    type Err = ParseError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "inQueue" | "outQueue" => Ok(Queue::from(s)),
            element => Err(ParseError {
                element: element.to_string(),
            }),
        }
    }
}
