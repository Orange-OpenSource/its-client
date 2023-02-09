// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
pub mod geo_extension;
mod message_type;
mod parse_error;
mod queue;

use std::{fmt, hash, str, str::FromStr};

use log::error;

use crate::analyse::configuration::Configuration;
use crate::mqtt::topic::geo_extension::{GeoExtension, Tile};
use crate::mqtt::topic::message_type::MessageType;
use crate::mqtt::topic::parse_error::ParseError;
use crate::mqtt::topic::queue::Queue;

#[derive(Default, Debug, Clone)]
// TODO implement a generic to manage a subscription with wild cards differently of a publish
pub struct Topic {
    // Project name at the topic root
    project: String,

    // Base topic
    queue: Queue,
    server: String,
    message_type: MessageType,

    // Source / destination extension: userUUID, roadUUID, oemUUID, appUUID, or +
    uuid: String,

    // GeoExtension
    pub geo_extension: GeoExtension,
}

impl Topic {
    pub(crate) fn new<Q, T>(
        queue: Option<Q>,
        message_type: Option<T>,
        uuid: Option<String>,
        geo_extension: Option<GeoExtension>,
    ) -> Topic
    where
        Q: Into<Queue> + Default,
        T: Into<MessageType> + Default,
    {
        Topic {
            project: "5GCroCo".to_string(),
            queue: match queue {
                Some(into_queue) => into_queue.into(),
                None => Queue::default(),
            },
            server: "v2x".to_string(),
            message_type: match message_type {
                Some(into_queue) => into_queue.into(),
                None => MessageType::default(),
            },
            uuid: uuid.unwrap_or_else(|| "+".to_string()),
            geo_extension: geo_extension.unwrap_or_default(),
        }
    }

    pub fn new_denm(component_name: String, geo_extension: &GeoExtension) -> Topic {
        Topic::new(
            Some("inQueue".to_string()),
            Some("denm".to_string()),
            Some(component_name),
            // assumed clone, we build a new topic
            Some(geo_extension.clone()),
        )
    }

    pub fn project_base(&self) -> String {
        format!(
            "{}/{}/{}/{}",
            self.project, self.queue, self.server, self.message_type
        )
    }

    // TODO find a better way to appropriate
    pub fn appropriate(&mut self, configuration: &Configuration) {
        self.uuid = configuration.component_name(None);
        self.queue = Queue::In;
    }
}

impl hash::Hash for Topic {
    fn hash<H: hash::Hasher>(&self, state: &mut H) {
        self.project.hash(state);
        self.queue.hash(state);
        self.server.hash(state);
        self.message_type.hash(state);
        self.uuid.hash(state);
        self.geo_extension.hash(state);
    }
}

impl PartialEq for Topic {
    fn eq(&self, other: &Self) -> bool {
        self.project == other.project
            && self.queue == other.queue
            && self.server == other.server
            && self.message_type == other.message_type
            && self.uuid == other.uuid
            && self.geo_extension == other.geo_extension
    }
}

impl Eq for Topic {}

impl PartialEq<String> for Topic {
    fn eq(&self, other: &String) -> bool {
        match Topic::from_str(other) {
            Ok(topic) => self == &topic,
            Err(error) => {
                error!("We can't compare the topic with a bad string: {}", error);
                false
            }
        }
    }
}

impl From<String> for Topic {
    fn from(topic: String) -> Self {
        Topic::from(topic.as_str())
    }
}

impl From<&str> for Topic {
    fn from(topic: &str) -> Self {
        match Topic::from_str(topic) {
            Ok(topic) => topic,
            Err(error) => panic!(
                "Unable to convert the String {} as a Topic: {}, use from_str instead",
                topic, error
            ),
        }
    }
}

impl FromStr for Topic {
    type Err = ParseError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        s.trim_matches('/').split('/').enumerate().try_fold(
            Topic::default(),
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
                    _n => {
                        let result = Tile::from_str(element)?;
                        topic_struct.geo_extension.tiles.push(result)
                    }
                }
                Ok(topic_struct)
            },
        )
    }
}

impl fmt::Display for Topic {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{}/{}{}",
            self.project_base(),
            self.uuid,
            self.geo_extension
        )
    }
}

#[cfg(test)]
mod tests {
    use crate::mqtt::topic::geo_extension::Tile;
    use crate::mqtt::topic::message_type::MessageType;
    use crate::mqtt::topic::queue::Queue;
    use crate::mqtt::topic::Topic;
    use std::str::FromStr;

    #[test]
    fn test_cam_topic_from_str() {
        let topic_string = "5GCroCo/outQueue/v2x/cam/car_1/0/1/2/3";
        let topic_result = Topic::from_str(topic_string);
        assert!(topic_result.is_ok());
        let topic = topic_result.unwrap();
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

    #[test]
    fn test_denm_topic_from_str() {
        let topic_string =
            "5GCroCo/outQueue/v2x/denm/wse_app_bcn1/1/2/0/2/2/2/2/3/3/0/0/3/2/0/2/0/1/0/1/0/3/1/";
        let topic_result = Topic::from_str(topic_string);
        assert!(topic_result.is_ok());
        let topic = topic_result.unwrap();
        assert_eq!(topic.project, "5GCroCo".to_string());
        assert_eq!(topic.queue, Queue::Out);
        assert_eq!(topic.server, "v2x".to_string());
        assert_eq!(topic.message_type, MessageType::DENM);
        assert_eq!(topic.uuid, "wse_app_bcn1".to_string());
        assert_eq!(topic.geo_extension.tiles.len(), 22);
    }

    #[test]
    fn test_info_topic_from_str() {
        let topic_string = "5GCroCo/outQueue/v2x/info/broker";
        let topic_result = Topic::from_str(topic_string);
        assert!(topic_result.is_ok());
        let topic = topic_result.unwrap();
        assert_eq!(topic.project, "5GCroCo".to_string());
        assert_eq!(topic.queue, Queue::Out);
        assert_eq!(topic.server, "v2x".to_string());
        assert_eq!(topic.message_type, MessageType::INFO);
        assert_eq!(topic.uuid, "broker".to_string());
        assert_eq!(topic.geo_extension.tiles.len(), 0);
    }

    #[test]
    fn test_in_queue_cam_topic_from_str() {
        let topic_string = "5GCroCo/inQueue/v2x/cam/car_1/0/1/2/3";
        let topic_result = Topic::from_str(topic_string);
        assert!(topic_result.is_ok());
        let topic = topic_result.unwrap();
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
}
