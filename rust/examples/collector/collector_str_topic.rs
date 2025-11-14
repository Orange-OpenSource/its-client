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
use libits::transport::mqtt::routed_str_topic::RoutedStrTopic;
use libits::transport::mqtt::str_topic::StrTopicError;
use libits::transport::mqtt::topic::Topic;
use std::str::FromStr;

/// Represents a 5-level string topic collector, supporting various levels of specificity.
///
/// This enum is used to define a categorised topic collector for different levels of string topics.
/// Each level corresponds to a more granular or specific topic hierarchy, allowing for structured
/// and organised handling of string-based topics.
///
/// # Variants
///
///
/// - `Level1(RoutedStrTopic<1>)`:
///   Represents the first-level specific topic.
///
/// - `Level2(RoutedStrTopic<2>)`:
///   Represents the second-level specific topic.
///
/// - `Level3(RoutedStrTopic<3>)`:
///   Represents the third-level specific topic.
///
/// - `Level4(RoutedStrTopic<4>)`:
///   Represents the fourth-level specific topic.
///
/// - `Level5(RoutedStrTopic<5>)`:
///   Represents the fifth-level specific topic.
///
/// - `Default(RoutedStrTopic)`:
///   Represents a default or unbounded topic, which may not adhere to any specific level hierarchy.
///
/// # Derives
///
/// The enum derives the following traits:
///
/// - `Debug`: Allows the enum to be formatted using the `{:?}` formatter, useful for debugging purposes.
/// - `Clone`: Enables the enum and its variants to be cloned.
/// - `PartialEq`: Allows comparison for equality between instances of the enum.
/// - `Eq`: Enforces equality constraints to ensure instances can be treated as strictly equal.
/// - `Hash`: Allows the enum and its variants to be hashed, useful when storing it in hash-based collections such as `HashSet` or `HashMap`.
///
/// # Example Usage
///
/// ```
/// use crate::CollectorStrTopic;
/// use crate::RoutedStrTopic; // Assuming RoutedStrTopic is defined elsewhere.
///
/// let topic = CollectorStrTopic::Level2(RoutedStrTopic::<2>);
/// println!("{:?}", topic);
/// ```
#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub enum CollectorStrTopic {
    Level1(RoutedStrTopic<1>),
    Level2(RoutedStrTopic<2>),
    Level3(RoutedStrTopic<3>),
    Level4(RoutedStrTopic<4>),
    Level5(RoutedStrTopic<5>),
    Default(RoutedStrTopic),
}

impl CollectorStrTopic {
    pub fn replace_at(&mut self, level: u8, value: &str) -> Result<(), StrTopicError> {
        match self {
            CollectorStrTopic::Level1(topic) => topic.replace_at(level, value),
            CollectorStrTopic::Level2(topic) => topic.replace_at(level, value),
            CollectorStrTopic::Level3(topic) => topic.replace_at(level, value),
            CollectorStrTopic::Level4(topic) => topic.replace_at(level, value),
            CollectorStrTopic::Level5(topic) => topic.replace_at(level, value),
            CollectorStrTopic::Default(topic) => topic.replace_at(level, value),
        }
    }
}

impl std::fmt::Display for CollectorStrTopic {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            CollectorStrTopic::Level1(topic) => write!(f, "{}", topic),
            CollectorStrTopic::Level2(topic) => write!(f, "{}", topic),
            CollectorStrTopic::Level3(topic) => write!(f, "{}", topic),
            CollectorStrTopic::Level4(topic) => write!(f, "{}", topic),
            CollectorStrTopic::Level5(topic) => write!(f, "{}", topic),
            CollectorStrTopic::Default(topic) => write!(f, "{}", topic),
        }
    }
}

impl Default for CollectorStrTopic {
    fn default() -> Self {
        CollectorStrTopic::Default(RoutedStrTopic::default())
    }
}

impl FromStr for CollectorStrTopic {
    type Err = std::str::Utf8Error;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        // We use Default variant when parsing from string
        // since we don't know the desired level at parse time
        Ok(CollectorStrTopic::Default(RoutedStrTopic::from_str(s)?))
    }
}

// Implement the Topic trait for CollectorStrTopic
impl Topic for CollectorStrTopic {
    fn as_route(&self) -> String {
        match self {
            CollectorStrTopic::Level1(topic) => topic.as_route(),
            CollectorStrTopic::Level2(topic) => topic.as_route(),
            CollectorStrTopic::Level3(topic) => topic.as_route(),
            CollectorStrTopic::Level4(topic) => topic.as_route(),
            CollectorStrTopic::Level5(topic) => topic.as_route(),
            CollectorStrTopic::Default(topic) => topic.as_route(),
        }
    }
}

/// A macro to create a `CollectorStrTopic` instance from a string slice (`&str`) and a specific level.
///
/// This macro provides a safe way to parse a topic string into a strongly-typed representation
/// of `CollectorStrTopic` at a given level. Each level corresponds to a specific specialization
/// of a `RoutedStrTopic<N>` type, where `N` is the level (from 0 to 5).
///
/// # Parameters
/// - `$topic_str`: A string slice (`&str`) representing the topic to be parsed.
/// - `$level`: An integer (`u8`) specifying the level of the `RoutedStrTopic`. Supported levels are from
///   1 to 5, and any other level falls back to the `Default` variant.
///
/// # Behavior
/// - At levels 1 through 5, it attempts to parse the `$topic_str` using the respective
///   `RoutedStrTopic<N>::from_str` implementation for the given level.
/// - If `$level` is outside the range of 1 to 5, it defaults to parsing the topic as a
///   `RoutedStrTopic::Default`.
/// - The resulting `RoutedStrTopic` is wrapped in the corresponding enum variant of `CollectorStrTopic`
///   (e.g., `CollectorStrTopic::Level1` for `level = 1`).
///
/// # Return Value
/// - Returns a `Result<CollectorStrTopic, E>` where `E` is the type of error returned by the
///   `from_str` implementation, if the parsing fails.
///
/// # Examples
/// ```rust
/// use your_crate::create_collector_str_topic_with_level;
///
/// let topic_str = "example_topic";
/// let level = 1;
///
/// let result = create_collector_str_topic_with_level!(topic_str, level);
/// match result {
///     Ok(collector_topic) => println!("Parsed topic for level {}: {:?}", level, collector_topic),
///     Err(e) => eprintln!("Error parsing topic: {}", e),
/// }
/// ```
///
/// In the example above, `topic_str` is parsed as a `RoutedStrTopic<1>` and wrapped in
/// `CollectorStrTopic::Level1`. Adjusting `level` in the macro invocation determines
/// which level-specific `RoutedStrTopic` is created.
///
/// # Notes
/// - Ensure that the provided `$topic_str` is valid for the specified `$level`, as
///   parsing errors may occur if they are not compatible.
/// - The macro is exported with `#[macro_export]`, making it available externally
///   when the defining crate is included as a dependency.
///
/// # Errors
/// - If `$topic_str` cannot be parsed into the requested `RoutedStrTopic<N>` type (or the default type),
///   the macro returns the error generated by the `from_str` function.
#[macro_export]
macro_rules! create_collector_str_topic_with_level {
    ($topic_str:expr, $level:expr) => {
        match $level {
            1 => RoutedStrTopic::<1>::from_str($topic_str).map(CollectorStrTopic::Level1),
            2 => RoutedStrTopic::<2>::from_str($topic_str).map(CollectorStrTopic::Level2),
            3 => RoutedStrTopic::<3>::from_str($topic_str).map(CollectorStrTopic::Level3),
            4 => RoutedStrTopic::<4>::from_str($topic_str).map(CollectorStrTopic::Level4),
            5 => RoutedStrTopic::<5>::from_str($topic_str).map(CollectorStrTopic::Level5),
            _ => RoutedStrTopic::from_str($topic_str).map(CollectorStrTopic::Default),
        }
    };
}

/// Macro: `handle_event_with_level`
///
/// This macro is designed to handle events by routing them based on their level.
/// It matches the provided level against specific constants (1 through 5), and uses the appropriate
/// type parameter `RoutedStrTopic<N>` to handle the event.
///
/// # Syntax
/// ```rust
/// handle_event_with_level!(router, event, level);
/// ```
///
/// # Parameters
/// - `$router`: This represents the router instance which needs to process the event.
/// - `$event`: The event object that needs to be handled.
/// - `$level`: An integer value representing the level of the event. This controls the type of
///   `RoutedStrTopic<N>` used for the handler. Levels are as follows:
///   - `1` corresponds to `RoutedStrTopic<1>`
///   - `2` corresponds to `RoutedStrTopic<2>`
///   - `3` corresponds to `RoutedStrTopic<3>`
///   - `4` corresponds to `RoutedStrTopic<4>`
///   - `5` corresponds to `RoutedStrTopic<5>`
///   - Any other level will default to `RoutedStrTopic`.
///
/// # Behaviour
/// The macro uses a `match` expression to determine the appropriate type parameter for `RoutedStrTopic<N>`
/// based on the provided `$level`:
/// - If `$level` matches `1` through `5`, the macro invokes `$router.handle_event::<RoutedStrTopic<N>>($event)`,
///   where `N` corresponds to the level.
/// - If `$level` does not match any of these values, the default case will invoke `$router.handle_event::<RoutedStrTopic>($event)`.
///
/// # Example
/// ```rust
/// // Using the macro to handle an event
/// handle_event_with_level!(router, some_event, 2); // Uses RoutedStrTopic<2>
/// handle_event_with_level!(router, some_event, 10); // Defaults to RoutedStrTopic
/// ```
///
/// This provides a convenient way to manage event handling based on different levels uniformly.
#[macro_export]
macro_rules! handle_event_with_level {
    ($router:expr, $event:expr, $level:expr) => {
        match $level {
            1 => $router
                .handle_event::<RoutedStrTopic<1>>($event)
                .map(|(topic, data)| (CollectorStrTopic::Level1(topic), data)),
            2 => $router
                .handle_event::<RoutedStrTopic<2>>($event)
                .map(|(topic, data)| (CollectorStrTopic::Level2(topic), data)),
            3 => $router
                .handle_event::<RoutedStrTopic<3>>($event)
                .map(|(topic, data)| (CollectorStrTopic::Level3(topic), data)),
            4 => $router
                .handle_event::<RoutedStrTopic<4>>($event)
                .map(|(topic, data)| (CollectorStrTopic::Level4(topic), data)),
            5 => $router
                .handle_event::<RoutedStrTopic<5>>($event)
                .map(|(topic, data)| (CollectorStrTopic::Level5(topic), data)),
            _ => $router
                .handle_event::<RoutedStrTopic>($event)
                .map(|(topic, data)| (CollectorStrTopic::Default(topic), data)),
        }
    };
}
