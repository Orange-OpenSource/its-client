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

use crate::transport::mqtt::str_topic::{StrTopic, StrTopicError};
use crate::transport::mqtt::topic::Topic;
use std::fmt::{Display, Formatter};
use std::str::FromStr;

/// Represents a routed topic as a string.
///
/// The ROUTE_LEVEL const generic parameter represents the route level:
/// - 255 means no specific route level (use the full topic as route)
/// - Any other value (0-254) represents the number of levels to use for routing
#[derive(Clone, Debug, Hash, PartialEq, Eq)]
pub struct RoutedStrTopic<const ROUTE_LEVEL: u8 = 255> {
    /// Topic as a StrTopic.
    topic: StrTopic,
}

impl<const ROUTE_LEVEL: u8> RoutedStrTopic<ROUTE_LEVEL> {
    /// Returns the route level
    pub fn route_level(self) -> u8 {
        ROUTE_LEVEL
    }

    /// Updates the topic with a new value
    pub fn replace_at(&mut self, level: u8, value: &str) -> Result<(), StrTopicError> {
        self.topic.replace_at(level, value)
    }
}

impl<const ROUTE_LEVEL: u8> Default for RoutedStrTopic<ROUTE_LEVEL> {
    fn default() -> Self {
        Self {
            topic: StrTopic::default(),
        }
    }
}

impl<const ROUTE_LEVEL: u8> Display for RoutedStrTopic<ROUTE_LEVEL> {
    /// Formats the `RoutedStrTopic` for display.
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

impl<const ROUTE_LEVEL: u8> FromStr for RoutedStrTopic<ROUTE_LEVEL> {
    type Err = std::str::Utf8Error;

    /// Creates a `RoutedStrTopic` from a string slice.
    ///
    /// # Arguments
    ///
    /// * `s` - String slice.
    ///
    /// # Returns
    ///
    /// A result containing the `RoutedStrTopic` or an error.
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match StrTopic::from_str(s) {
            Ok(topic) => Ok(RoutedStrTopic { topic }),
            Err(e) => Err(e),
        }
    }
}

impl<const ROUTE_LEVEL: u8> Topic for RoutedStrTopic<ROUTE_LEVEL> {
    /// Returns the topic as a route.
    ///
    /// # Returns
    ///
    /// A string representing the route.
    fn as_route(&self) -> String {
        if ROUTE_LEVEL == 0 {
            // Route level 0 means an empty route
            String::new()
        } else {
            let string_topic = self.topic.to_string();
            // Use the specified number of levels
            let parts = self.topic.parts();
            if ROUTE_LEVEL as usize >= parts.len() {
                string_topic
            } else {
                parts[..ROUTE_LEVEL as usize].join("/")
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::str::FromStr;

    /// Helper function to create a `RoutedStrTopic`.
    ///
    /// # Arguments
    ///
    /// * `topic` - Topic string.
    ///
    /// # Returns
    ///
    /// A `RoutedStrTopic` instance.
    fn create_routed_str_topic(topic: &str) -> RoutedStrTopic {
        RoutedStrTopic {
            topic: StrTopic::from_str(topic).unwrap(),
        }
    }

    fn create_routed_str_topic_with_levels<const LEVEL: u8>(topic: &str) -> RoutedStrTopic<LEVEL> {
        RoutedStrTopic {
            topic: StrTopic::from_str(topic).unwrap(),
        }
    }

    #[test]
    fn routed_str_topic_display() {
        let topic = create_routed_str_topic("test/topic");
        assert_eq!(format!("{topic}"), "test/topic");
    }

    #[test]
    fn routed_str_topic_from_str_valid() {
        let topic = RoutedStrTopic::from_str("test/topic").unwrap();
        assert_eq!(topic, create_routed_str_topic("test/topic"));
    }

    #[test]
    fn routed_str_topic_from_str_empty() {
        let topic = RoutedStrTopic::from_str("").unwrap();
        assert_eq!(topic, create_routed_str_topic(""));
    }

    #[test]
    fn routed_str_topic_from_str_with_route_level_3() {
        let topic = RoutedStrTopic::<3>::from_str("a/b/c/d/e").unwrap();
        assert_eq!(topic.as_route(), "a/b/c");
        assert_eq!(topic.route_level(), 3);
    }

    #[test]
    fn routed_str_topic_const_generic_default() {
        let topic = RoutedStrTopic::<255>::default();
        assert_eq!(topic.route_level(), 255);
    }

    #[test]
    fn routed_str_topic_as_route() {
        let topic = create_routed_str_topic("test/route");
        assert_eq!(topic.as_route(), "test/route");
    }

    #[test]
    fn routed_str_topic_as_route_with_none_levels() {
        let topic = create_routed_str_topic_with_levels::<255>("a/b/c/d/e");
        assert_eq!(topic.as_route(), "a/b/c/d/e");
    }

    #[test]
    fn routed_str_topic_as_route_with_zero_levels() {
        let topic = create_routed_str_topic_with_levels::<0>("a/b/c/d/e");
        assert_eq!(topic.as_route(), "");
    }

    #[test]
    fn routed_str_topic_as_route_with_one_level() {
        let topic = create_routed_str_topic_with_levels::<1>("a/b/c/d/e");
        assert_eq!(topic.as_route(), "a");
    }

    #[test]
    fn routed_str_topic_as_route_with_three_levels() {
        let topic = create_routed_str_topic_with_levels::<3>("a/b/c/d/e");
        assert_eq!(topic.as_route(), "a/b/c");
    }

    #[test]
    fn routed_str_topic_as_route_with_more_levels_than_available() {
        let topic = create_routed_str_topic_with_levels::<10>("a/b/c");
        assert_eq!(topic.as_route(), "a/b/c");
    }

    #[test]
    fn routed_str_topic_as_route_single_level_topic() {
        let topic = create_routed_str_topic_with_levels::<1>("single");
        assert_eq!(topic.as_route(), "single");
    }

    #[test]
    fn routed_str_topic_as_route_single_level_topic_zero_levels() {
        let topic = create_routed_str_topic_with_levels::<0>("single");
        assert_eq!(topic.as_route(), "");
    }

    #[test]
    fn routed_str_topic_with_route_levels_constructor() {
        let topic = create_routed_str_topic_with_levels::<2>("a/b/c/d");
        assert_eq!(topic.to_string(), "a/b/c/d");
        assert_eq!(topic.as_route(), "a/b");
    }

    #[test]
    fn routed_str_topic_not_replace_at_at_level_0() {
        let mut topic = create_routed_str_topic("a/b/c/d");
        let result = topic.replace_at(0, "x");
        assert!(result.is_err());
        assert_eq!(result.unwrap_err(), StrTopicError::LevelZero);
        assert_eq!(topic.to_string(), "a/b/c/d");
    }

    #[test]
    fn routed_str_topic_replace_at_at_first_level() {
        let mut topic = create_routed_str_topic("a/b/c/d");
        let result = topic.replace_at(1, "x");
        assert!(result.is_ok());
        assert_eq!(topic.to_string(), "x/b/c/d");
    }

    #[test]
    fn routed_str_topic_replace_at_at_level_2() {
        let mut topic = create_routed_str_topic("a/b/c/d");
        let result = topic.replace_at(2, "x");
        assert!(result.is_ok());
        assert_eq!(topic.to_string(), "a/x/c/d");
    }

    #[test]
    fn routed_str_topic_replace_at_at_level_3() {
        let mut topic = create_routed_str_topic("a/b/c/d");
        let result = topic.replace_at(3, "x");
        assert!(result.is_ok());
        assert_eq!(topic.to_string(), "a/b/x/d");
    }

    #[test]
    fn routed_str_topic_replace_at_at_last_level() {
        let mut topic = create_routed_str_topic("a/b/c/d");
        let result = topic.replace_at(4, "x");
        assert!(result.is_ok());
        assert_eq!(topic.to_string(), "a/b/c/x");
    }

    #[test]
    fn routed_str_topic_not_replace_at_at_level_too_high() {
        let mut topic = create_routed_str_topic("a/b/c/d");
        let result = topic.replace_at(5, "x");
        assert!(result.is_err());
        assert_eq!(result.unwrap_err(), StrTopicError::LevelTooHigh(5));
        assert_eq!(topic.to_string(), "a/b/c/d");
    }
}
