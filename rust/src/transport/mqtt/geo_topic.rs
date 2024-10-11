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
use log::{error, warn};
use std::fmt;
use std::fmt::{Debug, Display};
use std::hash::{Hash, Hasher};
use std::str::FromStr;

use crate::client::configuration::Configuration;
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

/// Orange V2X platform implementation of [Topic]
///
/// FIXME info messages does not contains the `server` part and it requires if/else management
#[derive(Clone, Debug, Default)]
pub struct GeoTopic {
    project: String,
    queue: Queue,
    server: String,
    message_type: MessageType,
    uuid: String,
    pub geo_extension: Quadkey,
}

impl GeoTopic {
    // FIXME use outside configuration for project & so
    pub fn denm(component_name: &str, geo_extension: Quadkey) -> Self {
        Self {
            project: "5GCroCo".to_string(),
            queue: Queue::In,
            server: "v2x".to_string(),
            message_type: MessageType::DENM,
            uuid: component_name.to_string(),
            geo_extension,
        }
    }

    // TODO find a better way to appropriate
    pub fn appropriate(&mut self, configuration: &Configuration) {
        self.uuid = configuration.component_name(None);
        self.queue = Queue::In;
    }
}

impl Topic for GeoTopic {
    fn as_route(&self) -> String {
        if self.message_type == MessageType::INFO {
            format!("{}/{}/{}", self.project, self.queue, self.message_type)
        } else {
            format!(
                "{}/{}/{}/{}",
                self.project, self.queue, self.server, self.message_type
            )
        }
    }
}

impl Hash for GeoTopic {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.project.hash(state);
        self.queue.hash(state);
        self.server.hash(state);
        self.message_type.hash(state);
        self.uuid.hash(state);
        self.geo_extension.hash(state);
    }
}

impl PartialEq for GeoTopic {
    fn eq(&self, other: &Self) -> bool {
        self.project == other.project
            && self.queue == other.queue
            && self.server == other.server
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
                error!("We can't compare the topic with a bad string: {}", error);
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
                "Unable to convert the String {} as a Topic: {}, use from_str instead",
                topic, error
            ),
        }
    }
}

impl FromStr for GeoTopic {
    type Err = GeoTopicError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        if s.contains("info") {
            s.trim_matches('/').split('/').enumerate().try_fold(
                GeoTopic::default(),
                |mut topic_struct, (i, element)| {
                    match i {
                        // project
                        0 => topic_struct.project = element.to_string(),
                        // queue
                        1 => topic_struct.queue = Queue::from_str(element)?,
                        // message type
                        2 => topic_struct.message_type = MessageType::from_str(element)?,
                        // uuid
                        3 => topic_struct.uuid = element.to_string(),
                        // TODO use geo_extension FromStr trait instead
                        // geo extension
                        _n => match Tile::from_str(element) {
                            Ok(tile) => topic_struct.geo_extension.push(tile),
                            Err(e) => {
                                warn!("{}", e);
                                return Err(GeoTopicError::InvalidTile(element.to_string()));
                            }
                        },
                    }
                    Ok(topic_struct)
                },
            )
        } else {
            s.trim_matches('/').split('/').enumerate().try_fold(
                GeoTopic::default(),
                |mut topic_struct, (i, element)| {
                    match i {
                        // project
                        0 => topic_struct.project = element.to_string(),
                        // queue
                        1 => topic_struct.queue = Queue::from_str(element)?,
                        // server
                        2 => topic_struct.server = element.to_string(),
                        // message type
                        3 => topic_struct.message_type = MessageType::from_str(element)?,
                        // uuid
                        4 => topic_struct.uuid = element.to_string(),
                        // TODO use geo_extension FromStr trait instead
                        // geo extension
                        _n => match Tile::from_str(element) {
                            Ok(tile) => topic_struct.geo_extension.push(tile),
                            Err(e) => {
                                warn!("{}", e);
                                return Err(GeoTopicError::InvalidTile(element.to_string()));
                            }
                        },
                    }
                    Ok(topic_struct)
                },
            )
        }
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

    #[test]
    fn test_cam_topic_from_str() {
        let topic_string = "5GCroCo/outQueue/v2x/cam/car_1/0/1/2/3";

        match GeoTopic::from_str(topic_string) {
            Ok(topic) => {
                assert_eq!(topic.project, "5GCroCo".to_string());
                assert_eq!(topic.queue, Queue::Out);
                assert_eq!(topic.server, "v2x".to_string());
                assert_eq!(topic.message_type, MessageType::CAM);
                assert_eq!(topic.uuid, "car_1".to_string());
                assert_eq!(topic.geo_extension.tiles.len(), 4);
                for i in 0..4 {
                    assert_eq!(topic.geo_extension.tiles[i], Tile::from(i as u8));
                }
            }
            Err(e) => panic!("Failed to create GeoTopic from string: {}", e),
        }
    }

    #[test]
    fn test_denm_topic_from_str() {
        let topic_string =
            "5GCroCo/outQueue/v2x/denm/wse_app_bcn1/1/2/0/2/2/2/2/3/3/0/0/3/2/0/2/0/1/0/1/0/3/1/";

        match GeoTopic::from_str(topic_string) {
            Ok(topic) => {
                assert_eq!(topic.project, "5GCroCo".to_string());
                assert_eq!(topic.queue, Queue::Out);
                assert_eq!(topic.server, "v2x".to_string());
                assert_eq!(topic.message_type, MessageType::DENM);
                assert_eq!(topic.uuid, "wse_app_bcn1".to_string());
                assert_eq!(topic.geo_extension.tiles.len(), 22);
            }
            Err(e) => panic!("Failed to create GeoTopic from string: {}", e),
        }
    }

    #[test]
    fn test_info_topic_from_str() {
        let topic_string = "5GCroCo/outQueue/info/broker";

        match GeoTopic::from_str(topic_string) {
            Ok(topic) => {
                assert_eq!(topic.project, "5GCroCo".to_string());
                assert_eq!(topic.queue, Queue::Out);
                assert!(topic.server.is_empty());
                assert_eq!(topic.message_type, MessageType::INFO);
                assert_eq!(topic.uuid, "broker".to_string());
                assert_eq!(topic.geo_extension.tiles.len(), 0);
            }
            Err(e) => panic!("Failed to create GeoTopic from string: {}", e),
        }
    }

    #[test]
    fn test_in_queue_cam_topic_from_str() {
        let topic_string = "5GCroCo/inQueue/v2x/cam/car_1/0/1/2/3";

        match GeoTopic::from_str(topic_string) {
            Ok(topic) => {
                assert_eq!(topic.project, "5GCroCo".to_string());
                assert_eq!(topic.queue, Queue::In);
                assert_eq!(topic.server, "v2x".to_string());
                assert_eq!(topic.message_type, MessageType::CAM);
                assert_eq!(topic.uuid, "car_1".to_string());
                assert_eq!(topic.geo_extension.tiles.len(), 4);
                for i in 0..4 {
                    assert_eq!(topic.geo_extension.tiles[i], Tile::from(i as u8));
                }
            }
            Err(e) => panic!("Failed to create GeoTopic from string: {}", e),
        }
    }
}
