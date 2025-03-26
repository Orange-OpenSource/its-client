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

use crate::transport::mqtt::topic::Topic;
use std::fmt::{Display, Formatter};
use std::str::FromStr;

/// Represents a topic as a string.
#[derive(Clone, Default, Debug, Hash, PartialEq, Eq)]
pub struct StrTopic {
    topic: String,
}

impl Display for StrTopic {
    /// Formats the `StrTopic` for display.
    ///
    /// # Arguments
    ///
    /// * `f` - The formatter.
    ///
    /// # Returns
    ///
    /// A result indicating success or failure.
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.topic)
    }
}

impl FromStr for StrTopic {
    type Err = std::str::Utf8Error;

    /// Creates a `StrTopic` from a string slice.
    ///
    /// # Arguments
    ///
    /// * `s` - The string slice.
    ///
    /// # Returns
    ///
    /// A result containing the `StrTopic` or an error.
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        Ok(StrTopic {
            topic: String::from(s),
        })
    }
}

impl Topic for StrTopic {
    /// Returns the topic as a route.
    ///
    /// # Returns
    ///
    /// A string representing the route.
    fn as_route(&self) -> String {
        // Assume the topic is the route
        // Assumed clone is cheap
        self.topic.clone()
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::str::FromStr;

    /// Helper function to create a `StrTopic`.
    ///
    /// # Arguments
    ///
    /// * `topic` - The topic string.
    ///
    /// # Returns
    ///
    /// A `StrTopic` instance.
    fn create_str_topic(topic: &str) -> StrTopic {
        StrTopic {
            topic: topic.to_string(),
        }
    }

    #[test]
    fn str_topic_display() {
        let topic = create_str_topic("test/topic");
        assert_eq!(format!("{}", topic), "test/topic");
    }

    #[test]
    fn str_topic_from_str_valid() {
        let topic = StrTopic::from_str("test/topic").unwrap();
        assert_eq!(topic, create_str_topic("test/topic"));
    }

    #[test]
    fn str_topic_from_str_empty() {
        let topic = StrTopic::from_str("").unwrap();
        assert_eq!(topic, create_str_topic(""));
    }

    #[test]
    fn str_topic_as_route() {
        let topic = create_str_topic("test/route");
        assert_eq!(topic.as_route(), "test/route");
    }
}
