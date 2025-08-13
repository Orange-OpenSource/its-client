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

use crate::mobility::quadtree::quadkey::Quadkey;
use crate::mobility::quadtree::tile::Tile;
use crate::transport::mqtt::topic::Topic;
use log::{debug, error, warn};
use std::fmt;
use std::fmt::{Debug, Display};
use std::hash::{Hash, Hasher};
use std::str::FromStr;

use crate::client::configuration::Configuration;
use crate::client::configuration::geo_configuration::GeoConfiguration;
use crate::transport::mqtt::geo_topic::message_type::MessageType;
use crate::transport::mqtt::geo_topic::queue::Queue;
use thiserror::Error;

mod message_type;
mod queue;

/// An error which can be returned when parsing a Topic string.
#[derive(Error, Debug)]
pub enum GeoTopicError {
    #[error("Cannot parse topic with unknown queue '{0}'")]
    UnknownQueue(String),
    #[error("Cannot parse topic with unknown message type '{0}'")]
    UnknownMessageType(String),
    #[error("Cannot parse topic with invalid tile '{0}'")]
    InvalidTile(String),
}

/// Geo implementation of [Topic]
///
/// FIXME info messages does not contains the `suffix` part and it requires if/else management
#[derive(Clone, Debug, Default)]
pub struct GeoTopic {
    prefix: String,
    queue: Queue,
    suffix: String,
    message_type: MessageType,
    uuid: String,
    pub geo_extension: Quadkey,
}

impl GeoTopic {
    pub fn denm(
        configuration: &GeoConfiguration,
        component_name: &str,
        geo_extension: &Quadkey,
    ) -> Self {
        Self {
            prefix: String::from(&configuration.prefix),
            queue: Queue::In,
            suffix: String::from(&configuration.suffix),
            message_type: MessageType::DENM,
            uuid: component_name.to_string(),
            geo_extension: Quadkey::from(geo_extension),
        }
    }

    // TODO find a better way to appropriate
    pub fn appropriate(&mut self, configuration: &Configuration) {
        // assumed clone
        self.uuid = configuration.mobility.source_uuid.clone();
        self.queue = Queue::In;
    }
}

impl Topic for GeoTopic {
    fn as_route(&self) -> String {
        format!(
            "{}/{}/{}/{}",
            self.prefix, self.queue, self.suffix, self.message_type
        )
    }
}

impl Hash for GeoTopic {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.prefix.hash(state);
        self.queue.hash(state);
        self.suffix.hash(state);
        self.message_type.hash(state);
        self.uuid.hash(state);
        self.geo_extension.hash(state);
    }
}

impl PartialEq for GeoTopic {
    fn eq(&self, other: &Self) -> bool {
        self.prefix == other.prefix
            && self.queue == other.queue
            && self.suffix == other.suffix
            && self.message_type == other.message_type
            && self.uuid == other.uuid
            && self.geo_extension == other.geo_extension
    }
}

impl Eq for GeoTopic {}

impl PartialEq<String> for GeoTopic {
    fn eq(&self, other: &String) -> bool {
        match GeoTopic::from_str(other) {
            Ok(topic) => self == &topic,
            Err(error) => {
                error!("We can't compare the topic with a bad string: {error}");
                false
            }
        }
    }
}

impl From<String> for GeoTopic {
    fn from(topic: String) -> Self {
        GeoTopic::from(topic.as_str())
    }
}

impl From<&str> for GeoTopic {
    fn from(topic: &str) -> Self {
        match GeoTopic::from_str(topic) {
            Ok(topic) => topic,
            Err(error) => panic!(
                "Unable to convert the String {topic} as a Topic: {error}, use from_str instead"
            ),
        }
    }
}

impl FromStr for GeoTopic {
    type Err = GeoTopicError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        s.trim_matches('/').split('/').enumerate().try_fold(
            GeoTopic::default(),
            |mut topic_struct, (i, element)| {
                match i {
                    // prefix
                    0 => topic_struct.prefix = element.to_string(),
                    // queue
                    1 => topic_struct.queue = Queue::from_str(element)?,
                    // suffix
                    2 => topic_struct.suffix = element.to_string(),
                    // message type
                    3 => topic_struct.message_type = MessageType::from_str(element)?,
                    // uuid
                    4 => topic_struct.uuid = element.to_string(),
                    // TODO use geo_extension FromStr trait instead
                    // geo extension
                    _n => match Tile::from_str(element) {
                        Ok(tile) => topic_struct.geo_extension.push(tile),
                        Err(e) => {
                            warn!("Unable to parse the tile {element}");
                            debug!("Parsing error: {e}");
                            return Err(GeoTopicError::InvalidTile(element.to_string()));
                        }
                    },
                }
                Ok(topic_struct)
            },
        )
    }
}

impl Display for GeoTopic {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let s = format!("{}/{}{}", self.as_route(), self.uuid, self.geo_extension);
        write!(f, "{}", s.trim_matches('/'))
    }
}

#[cfg(test)]
mod tests {
    use crate::mobility::quadtree::tile::Tile;
    use crate::transport::mqtt::geo_topic::GeoTopic;
    use std::str::FromStr;

    use crate::transport::mqtt::geo_topic::message_type::MessageType;
    use crate::transport::mqtt::geo_topic::queue::Queue;

    /// Helper function to verify common GeoTopic parsing assertions
    fn assert_topic_parsed_correctly(
        topic: &GeoTopic,
        expected_prefix: &str,
        expected_queue: Queue,
        expected_suffix: &str,
        expected_message_type: MessageType,
        expected_uuid: &str,
        expected_tiles_len: usize,
    ) {
        assert_eq!(topic.prefix, expected_prefix.to_string());
        assert_eq!(topic.queue, expected_queue);
        assert_eq!(topic.suffix, expected_suffix.to_string());
        assert_eq!(topic.message_type, expected_message_type);
        assert_eq!(topic.uuid, expected_uuid.to_string());
        assert_eq!(topic.geo_extension.tiles.len(), expected_tiles_len);
    }

    #[test]
    fn test_cam_topic_from_str() {
        let topic_string = "5GCroCo/outQueue/v2x/cam/car_1/0/1/2/3";

        match GeoTopic::from_str(topic_string) {
            Ok(topic) => {
                assert_topic_parsed_correctly(
                    &topic,
                    "5GCroCo",
                    Queue::Out,
                    "v2x",
                    MessageType::CAM,
                    "car_1",
                    4,
                );
                for i in 0..4 {
                    assert_eq!(topic.geo_extension.tiles[i], Tile::from(i as u8));
                }
            }
            Err(e) => panic!("Failed to create GeoTopic from string: {e}"),
        }
    }

    #[test]
    fn test_denm_topic_from_str() {
        let topic_string =
            "5GCroCo/outQueue/v2x/denm/wse_app_bcn1/1/2/0/2/2/2/2/3/3/0/0/3/2/0/2/0/1/0/1/0/3/1/";

        match GeoTopic::from_str(topic_string) {
            Ok(topic) => {
                assert_topic_parsed_correctly(
                    &topic,
                    "5GCroCo",
                    Queue::Out,
                    "v2x",
                    MessageType::DENM,
                    "wse_app_bcn1",
                    22,
                );
            }
            Err(e) => panic!("Failed to create GeoTopic from string: {e}"),
        }
    }

    #[test]
    fn test_info_topic_from_str() {
        let topic_string = "5GCroCo/outQueue/v2x/info/broker";

        match GeoTopic::from_str(topic_string) {
            Ok(topic) => {
                assert_topic_parsed_correctly(
                    &topic,
                    "5GCroCo",
                    Queue::Out,
                    "v2x",
                    MessageType::INFO,
                    "broker",
                    0,
                );
            }
            Err(e) => panic!("Failed to create GeoTopic from string: {e}"),
        }
    }

    #[test]
    fn test_in_queue_cam_topic_from_str() {
        let topic_string = "5GCroCo/inQueue/v2x/cam/car_1/0/1/2/3";

        match GeoTopic::from_str(topic_string) {
            Ok(topic) => {
                assert_topic_parsed_correctly(
                    &topic,
                    "5GCroCo",
                    Queue::In,
                    "v2x",
                    MessageType::CAM,
                    "car_1",
                    4,
                );
                for i in 0..4 {
                    assert_eq!(topic.geo_extension.tiles[i], Tile::from(i as u8));
                }
            }
            Err(e) => panic!("Failed to create GeoTopic from string: {e}"),
        }
    }
}
