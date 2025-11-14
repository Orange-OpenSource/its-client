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
use thiserror::Error;

/// Error during topic manipulation
#[derive(Error, Debug, PartialEq)]
pub enum StrTopicError {
    #[error("Cannot update topic at level 0")]
    LevelZero,
    #[error("Cannot update topic at level {0}: level is too high")]
    LevelTooHigh(u8),
}

/// Represents a topic as a string.
#[derive(Clone, Default, Debug, Hash, PartialEq, Eq)]
pub struct StrTopic {
    /// Topic as a string.
    topic: String,
}

impl StrTopic {
    /// Returns the topic parts
    pub fn parts(&self) -> Vec<&str> {
        self.topic.split('/').collect()
    }

    /// Updates the topic with a new value at the specified level
    pub fn replace_at(&mut self, level: u8, value: &str) -> Result<(), StrTopicError> {
        if level == 0 {
            return Err(StrTopicError::LevelZero);
        }
        let mut parts = self.parts();
        if level as usize <= parts.len() {
            parts[level as usize - 1] = value;
            self.topic = parts.join("/");
            Ok(())
        } else {
            Err(StrTopicError::LevelTooHigh(level))
        }
    }
}

impl Display for StrTopic {
    /// Formats the `StrTopic` for display.
    ///
    /// # Arguments
    ///
    /// * `f` - Formatter.
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
    /// * `s` - String slice.
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
        // Assumed clone is inexpensive
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
    /// * `topic` - Topic string.
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
        assert_eq!(format!("{topic}"), "test/topic");
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

    #[test]
    fn str_topic_not_replace_at_at_level_0() {
        let mut topic = create_str_topic("a/b/c/d");
        let result = topic.replace_at(0, "x");
        assert!(result.is_err());
        assert_eq!(result.unwrap_err(), StrTopicError::LevelZero);
        assert_eq!(topic.to_string(), "a/b/c/d");
    }

    #[test]
    fn str_topic_replace_at_at_first_level() {
        let mut topic = create_str_topic("a/b/c/d");
        let result = topic.replace_at(1, "x");
        assert!(result.is_ok());
        assert_eq!(topic.to_string(), "x/b/c/d");
    }

    #[test]
    fn str_topic_replace_at_at_level_2() {
        let mut topic = create_str_topic("a/b/c/d");
        let result = topic.replace_at(2, "x");
        assert!(result.is_ok());
        assert_eq!(topic.to_string(), "a/x/c/d");
    }

    #[test]
    fn str_topic_replace_at_at_level_3() {
        let mut topic = create_str_topic("a/b/c/d");
        let result = topic.replace_at(3, "x");
        assert!(result.is_ok());
        assert_eq!(topic.to_string(), "a/b/x/d");
    }

    #[test]
    fn str_topic_replace_at_at_last_level() {
        let mut topic = create_str_topic("a/b/c/d");
        let result = topic.replace_at(4, "x");
        assert!(result.is_ok());
        assert_eq!(topic.to_string(), "a/b/c/x");
    }

    #[test]
    fn str_topic_not_replace_at_at_level_too_high() {
        let mut topic = create_str_topic("a/b/c/d");
        let result = topic.replace_at(5, "x");
        assert!(result.is_err());
        assert_eq!(result.unwrap_err(), StrTopicError::LevelTooHigh(5));
        assert_eq!(topic.to_string(), "a/b/c/d");
    }
}
