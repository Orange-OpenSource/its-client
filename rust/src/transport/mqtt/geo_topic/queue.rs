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

use crate::transport::mqtt::geo_topic::GeoTopicError;
use std::{fmt, hash, str};

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
                "Unable to convert from the element {element} as a Queue, use from_str instead"
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

impl PartialEq for Queue {
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
