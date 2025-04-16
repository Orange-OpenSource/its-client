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

use std::hash;

use crate::exchange::etsi::decentralized_environmental_notification_message::RelevanceDistance::{
    LessThan5Km, LessThan10Km, LessThan50m, LessThan100m, LessThan200m, LessThan500m,
    LessThan1000m, Over10Km,
};
use crate::exchange::etsi::reference_position::{PositionConfidence, ReferencePosition};
use crate::exchange::etsi::{PathPoint, etsi_now, heading_from_etsi, speed_from_etsi};
use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::mortal::Mortal;
use crate::mobility::mobile::Mobile;
use crate::mobility::position::Position;

use serde::{Deserialize, Serialize};

/// Represents a Decentralized Environmental Notification Message (DENM) according to ETSI standard.
///
/// This message is used to describe detected road hazards and traffic conditions.
/// The structure follows the JSON schema version 2.1.0 specification.
///
/// # Fields
///
/// * `protocol_version` - The version of the protocol used (default: 2)
/// * `station_id` - Unique identifier for the ITS station (0..4294967295)
/// * `management_container` - Contains basic information about the DENM
/// * `situation_container` - Optional container with details about the event type
/// * `location_container` - Optional container with position-related information
/// * `alacarte_container` - Optional container with additional specific information
#[derive(Debug, Default, Clone, Serialize, Deserialize)]
pub struct DecentralizedEnvironmentalNotificationMessage {
    #[serde(default = "default_protocol_version")]
    pub protocol_version: u8, // 0..255
    pub station_id: u32, // 0..4294967295
    pub management_container: ManagementContainer,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub situation_container: Option<SituationContainer>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub location_container: Option<LocationContainer>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub alacarte_container: Option<AlacarteContainer>,
}

fn default_protocol_version() -> u8 {
    2
}

/// Contains management information for the DENM
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ManagementContainer {
    pub action_id: ActionId,
    pub detection_time: u64, // 0..4398046511103
    pub reference_time: u64, // 0..4398046511103
    #[serde(skip_serializing_if = "Option::is_none")]
    pub termination: Option<u8>, // 0..1
    pub event_position: ReferencePosition,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub awareness_distance: Option<u8>, // 0..7
    #[serde(skip_serializing_if = "Option::is_none")]
    pub relevance_traffic_direction: Option<u8>, // 0..3
    #[serde(skip_serializing_if = "Option::is_none")]
    #[serde(default)] // This will use Default::default() which returns None
    pub validity_duration: Option<u32>, // 0..86400, default 600
    #[serde(skip_serializing_if = "Option::is_none")]
    pub transmission_interval: Option<u16>, // 1..10000
    #[serde(skip_serializing_if = "Option::is_none")]
    pub station_type: Option<u8>, // 0..255, default 0
    #[serde(skip_serializing_if = "Option::is_none")]
    pub confidence: Option<PositionConfidence>,
}

/// Contains situation information about the detected event
#[serde_with::skip_serializing_none]
#[derive(Debug, Default, Clone, Serialize, Deserialize)]
pub struct SituationContainer {
    /// Quality of the information (0..7)
    #[serde(skip_serializing_if = "Option::is_none")]
    #[serde(
        deserialize_with = "deserialize_information_quality",
        serialize_with = "serialize_information_quality"
    )]
    pub information_quality: Option<u8>,
    pub event_type: EventType,
    pub linked_cause: Option<EventType>,
}

fn deserialize_information_quality<'de, D>(deserializer: D) -> Result<Option<u8>, D::Error>
where
    D: serde::Deserializer<'de>,
{
    use serde::de::Error;
    let value = Option::<u8>::deserialize(deserializer)?;
    if let Some(quality) = value {
        if quality > 7 {
            return Err(Error::custom("information_quality must be between 0 and 7"));
        }
    }
    Ok(value)
}

fn serialize_information_quality<S>(value: &Option<u8>, serializer: S) -> Result<S::Ok, S::Error>
where
    S: serde::Serializer,
{
    use serde::ser::Error;
    if let Some(quality) = value {
        if *quality > 7 {
            return Err(Error::custom("information_quality must be between 0 and 7"));
        }
        serializer.serialize_some(quality)
    } else {
        serializer.serialize_none()
    }
}

/// Contains location-related information about the event
#[serde_with::skip_serializing_none]
#[derive(Debug, Default, Clone, Serialize, Deserialize)]
pub struct LocationContainer {
    /// Speed at the event location in 0.01 m/s
    pub event_speed: Option<u16>,
    /// Heading at the event location in 0.1 degrees
    pub event_position_heading: Option<u16>,
    /// List of traces leading to the event position
    #[serde(default)]
    pub detection_zones_to_event_position: Vec<Trace>,
    /// Type of road where the event occurred
    pub road_type: Option<u8>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct AlacarteContainer {
    pub lane_position: Option<i8>,
    pub positioning_solution: Option<u8>,
}

/// Represents an action identifier
#[derive(Clone, Debug, Default, Hash, Eq, PartialEq, Serialize, Deserialize)]
pub struct ActionId {
    pub originating_station_id: u32, // 0..4294967295
    pub sequence_number: u16,        // 0..65535
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct EventType {
    pub cause: u8,
    pub subcause: Option<u8>,
}

#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct Trace {
    pub path: Vec<PathPoint>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct LocationContainerConfidence {
    pub speed: Option<u8>,
    pub heading: Option<u8>,
}

#[repr(u8)]
pub enum RelevanceTrafficDirection {
    AllTrafficDirection = 0,
    UpstreamTraffic,
    DownstreamTraffic,
    OppositeTraffic,
}
impl From<RelevanceTrafficDirection> for u8 {
    fn from(val: RelevanceTrafficDirection) -> Self {
        val as u8
    }
}

#[repr(u8)]
pub enum RelevanceDistance {
    LessThan50m = 0,
    LessThan100m,
    LessThan200m,
    LessThan500m,
    LessThan1000m,
    LessThan5Km,
    LessThan10Km,
    Over10Km,
}
impl From<RelevanceDistance> for u8 {
    fn from(val: RelevanceDistance) -> Self {
        val as u8
    }
}
impl From<f64> for RelevanceDistance {
    fn from(value: f64) -> Self {
        match value {
            _ if value < 50. => LessThan50m,
            _ if value < 100. => LessThan100m,
            _ if value < 200. => LessThan200m,
            _ if value < 500. => LessThan500m,
            _ if value < 1000. => LessThan1000m,
            _ if value < 5_000. => LessThan5Km,
            _ if value < 10_000. => LessThan10Km,
            _ => Over10Km,
        }
    }
}

impl DecentralizedEnvironmentalNotificationMessage {
    pub fn new_stationary_vehicle(
        station_id: u32,
        originating_station_id: u32,
        event_position: ReferencePosition,
        sequence_number: u16,
        etsi_timestamp: u64,
        event_position_heading: Option<u16>,
    ) -> Self {
        Self::new(
            station_id,
            originating_station_id,
            event_position,
            sequence_number,
            etsi_timestamp,
            94,
            Some(0),
            None,
            None,
            Some(0),
            event_position_heading,
            Some(10),
            Some(200),
        )
    }

    #[allow(clippy::too_many_arguments)]
    pub fn new_traffic_condition(
        station_id: u32,
        originating_station_id: u32,
        event_position: ReferencePosition,
        sequence_number: u16,
        etsi_timestamp: u64,
        subcause: Option<u8>,
        awareness_distance: Option<u8>,
        relevance_traffic_direction: Option<u8>,
        event_speed: Option<u16>,
        event_position_heading: Option<u16>,
    ) -> Self {
        Self::new(
            station_id,
            originating_station_id,
            event_position,
            sequence_number,
            etsi_timestamp,
            1,
            subcause,
            awareness_distance,
            relevance_traffic_direction,
            event_speed,
            event_position_heading,
            Some(10),
            Some(200),
        )
    }

    #[allow(clippy::too_many_arguments)]
    pub fn new_collision_risk(
        station_id: u32,
        originating_station_id: u32,
        event_position: ReferencePosition,
        sequence_number: u16,
        etsi_timestamp: u64,
        subcause: Option<u8>,
        awareness_distance: Option<u8>,
        relevance_traffic_direction: Option<u8>,
        event_speed: Option<u16>,
        event_position_heading: Option<u16>,
    ) -> Self {
        // collisionRisk
        Self::new(
            station_id,
            originating_station_id,
            event_position,
            sequence_number,
            etsi_timestamp,
            97,
            subcause,
            awareness_distance,
            relevance_traffic_direction,
            event_speed,
            event_position_heading,
            Some(2),
            Some(200),
        )
    }

    pub fn update_collision_risk(
        mut denm: Self,
        event_position: ReferencePosition,
        etsi_timestamp: u64,
        awareness_distance: Option<u8>,
        relevance_traffic_direction: Option<u8>,
        event_speed: Option<u16>,
        event_position_heading: Option<u16>,
    ) -> Self {
        // collisionRisk
        denm.management_container.event_position = event_position;
        denm.management_container.reference_time = etsi_timestamp;
        denm.management_container.awareness_distance = awareness_distance;
        denm.management_container.relevance_traffic_direction = relevance_traffic_direction;
        if event_speed.is_some() || event_position_heading.is_some() {
            denm.location_container = Option::from(LocationContainer {
                event_speed,
                event_position_heading,
                ..Default::default()
            });
        }
        denm
    }

    #[allow(clippy::too_many_arguments)]
    pub(crate) fn new(
        station_id: u32,
        originating_station_id: u32,
        event_position: ReferencePosition,
        sequence_number: u16,
        etsi_timestamp: u64,
        cause: u8,
        subcause: Option<u8>,
        awareness_distance: Option<u8>,
        relevance_traffic_direction: Option<u8>,
        event_speed: Option<u16>,
        event_position_heading: Option<u16>,
        validity_duration: Option<u32>,
        transmission_interval: Option<u16>,
    ) -> Self {
        Self {
            protocol_version: 2,
            station_id,
            management_container: ManagementContainer {
                action_id: ActionId {
                    originating_station_id,
                    sequence_number,
                },
                detection_time: etsi_timestamp,
                reference_time: etsi_now(),
                event_position,
                validity_duration,
                transmission_interval,
                station_type: Some(5),
                awareness_distance,
                relevance_traffic_direction,
                ..Default::default()
            },
            situation_container: Option::from(SituationContainer {
                event_type: EventType { cause, subcause },
                ..Default::default()
            }),
            location_container: Option::from(LocationContainer {
                event_speed,
                event_position_heading,
                ..Default::default()
            }),
            ..Default::default()
        }
    }

    pub fn update_information_quality(&mut self, information_quality: u8) {
        let situation_container = self.situation_container.clone();
        match situation_container {
            Some(mut situation_container) => {
                situation_container.information_quality = Some(information_quality);
                self.situation_container = Some(situation_container);
            }
            None => {
                self.situation_container = Option::from(SituationContainer {
                    information_quality: Some(information_quality),
                    ..Default::default()
                })
            }
        }
    }

    pub fn is_stationary_vehicle(&self) -> bool {
        self.situation_container.is_some()
            && 94 == self.situation_container.as_ref().unwrap().event_type.cause
    }

    pub fn is_traffic_condition(&self) -> bool {
        self.situation_container.is_some()
            && 1 == self.situation_container.as_ref().unwrap().event_type.cause
    }

    pub fn is_collision_risk(&self) -> bool {
        self.situation_container.is_some()
            && 97 == self.situation_container.as_ref().unwrap().event_type.cause
    }
}

impl hash::Hash for DecentralizedEnvironmentalNotificationMessage {
    fn hash<H: hash::Hasher>(&self, state: &mut H) {
        self.management_container.hash(state);
    }
}

impl Eq for DecentralizedEnvironmentalNotificationMessage {}

impl PartialEq for DecentralizedEnvironmentalNotificationMessage {
    fn eq(&self, other: &Self) -> bool {
        self.management_container == other.management_container
    }
}

impl Mobile for DecentralizedEnvironmentalNotificationMessage {
    fn id(&self) -> u32 {
        self.station_id
    }

    fn position(&self) -> Position {
        self.management_container.event_position.as_position()
    }

    fn speed(&self) -> Option<f64> {
        if let Some(location_container) = &self.location_container {
            location_container.event_speed.map(speed_from_etsi)
        } else {
            None
        }
    }

    fn heading(&self) -> Option<f64> {
        if let Some(location_container) = &self.location_container {
            location_container
                .event_position_heading
                .map(heading_from_etsi)
        } else {
            None
        }
    }

    fn acceleration(&self) -> Option<f64> {
        None
    }
}

impl Content for DecentralizedEnvironmentalNotificationMessage {
    fn get_type(&self) -> &str {
        "denm"
    }

    fn appropriate(&mut self, timestamp: u64, new_station_id: u32) {
        self.station_id = new_station_id;
        // we keep the action_id: originating_station_id and sequence_number remain unchanged
        // detection_time updated because values changed in the payload (anything else has been updated)
        self.management_container.detection_time = timestamp;
        // reference_time different for each message
        self.management_container.reference_time = timestamp;
    }

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError> {
        Ok(self)
    }

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError> {
        Ok(self)
    }
}

impl Mortal for DecentralizedEnvironmentalNotificationMessage {
    fn timeout(&self) -> u64 {
        self.management_container.reference_time
            + u64::from(
                self.management_container
                    .validity_duration
                    .unwrap_or_default()
                    * 1000,
            )
    }

    fn terminate(&mut self) {
        self.management_container.termination = Some(0);
        self.management_container.detection_time = etsi_now();
        self.management_container.reference_time = etsi_now();
        self.management_container.validity_duration = Some(10);
    }

    fn terminated(&self) -> bool {
        self.management_container.termination.is_some()
    }

    fn remaining_time(&self) -> u64 {
        let now = etsi_now();
        if self.timeout() > now {
            (self.timeout() - now) / 1000
        } else {
            0
        }
    }
}

impl Default for ManagementContainer {
    fn default() -> Self {
        Self {
            action_id: Default::default(),
            detection_time: Default::default(),
            reference_time: Default::default(),
            termination: Default::default(),
            event_position: Default::default(),
            awareness_distance: Default::default(),
            relevance_traffic_direction: Default::default(),
            validity_duration: Some(600),
            transmission_interval: Default::default(),
            station_type: Default::default(),
            confidence: Default::default(),
        }
    }
}

impl PartialEq for ManagementContainer {
    fn eq(&self, other: &Self) -> bool {
        self.action_id == other.action_id
    }
}

impl hash::Hash for ManagementContainer {
    fn hash<H: hash::Hasher>(&self, state: &mut H) {
        self.action_id.hash(state);
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::decentralized_environmental_notification_message::{
        ActionId, AlacarteContainer, DecentralizedEnvironmentalNotificationMessage, EventType,
        LocationContainer, ManagementContainer, SituationContainer,
    };
    use crate::exchange::etsi::reference_position::{PositionConfidence, ReferencePosition};
    use crate::exchange::etsi::{etsi_now, timestamp_to_etsi};
    use crate::exchange::mortal::Mortal;
    use crate::now;
    use serde_json::json;

    #[test]
    fn create_new_stationary_vehicle() {
        let station_id = 4567;
        let originating_station_id = 1230;
        let event_position = ReferencePosition::default();
        let sequence_number = 10;
        let detection_time = etsi_now();
        let event_position_heading = Some(3000);
        std::thread::sleep(std::time::Duration::from_secs(1));

        let denm = DecentralizedEnvironmentalNotificationMessage::new_stationary_vehicle(
            station_id,
            originating_station_id,
            //assumed clone, to compare with further
            event_position.clone(),
            sequence_number,
            detection_time,
            event_position_heading,
        );
        assert_eq!(denm.station_id, station_id);
        assert_eq!(
            denm.management_container.action_id.originating_station_id,
            originating_station_id
        );
        assert_eq!(denm.management_container.event_position, event_position);
        assert_eq!(
            denm.management_container.action_id.sequence_number,
            sequence_number
        );

        assert_eq!(denm.management_container.detection_time, detection_time);
        assert!(
            denm.management_container.detection_time <= denm.management_container.reference_time
        );
    }

    #[test]
    fn information_quality_update() {
        let mut denm = DecentralizedEnvironmentalNotificationMessage::default();

        assert!(denm.situation_container.is_none());

        denm.update_information_quality(5);
        assert!(denm.situation_container.is_some());
        let situation_container = denm.situation_container.unwrap();
        assert_eq!(situation_container.information_quality, Some(5));
    }

    #[test]
    fn correct_timeout() {
        let now = now();
        let denm = DecentralizedEnvironmentalNotificationMessage {
            management_container: ManagementContainer {
                reference_time: timestamp_to_etsi(now) + 500,
                detection_time: timestamp_to_etsi(now),
                validity_duration: Some(10),
                ..Default::default()
            },
            ..Default::default()
        };

        assert!(denm.remaining_time() <= 10);
        assert_eq!(
            denm.timeout() - denm.management_container.reference_time,
            10_000
        );
    }

    #[test]
    fn serialize_minimal_denm() {
        let denm = DecentralizedEnvironmentalNotificationMessage {
            protocol_version: 2,
            station_id: 12345,
            management_container: ManagementContainer {
                action_id: ActionId {
                    originating_station_id: 54321,
                    sequence_number: 1,
                },
                detection_time: 1000,
                reference_time: 1000,
                event_position: ReferencePosition::default(),
                validity_duration: Some(600),
                termination: None,
                awareness_distance: None,
                relevance_traffic_direction: None,
                transmission_interval: None,
                station_type: None,
                confidence: None,
            },
            situation_container: None,
            location_container: None,
            alacarte_container: None,
        };

        let json = serde_json::to_value(&denm).unwrap();
        assert_eq!(json["protocol_version"], 2);
        assert_eq!(json["station_id"], 12345);
        assert!(json.get("situation_container").is_none());
    }

    #[test]
    fn serialize_full_denm() {
        let denm = DecentralizedEnvironmentalNotificationMessage {
            protocol_version: 2,
            station_id: 12345,
            management_container: ManagementContainer {
                action_id: ActionId {
                    originating_station_id: 54321,
                    sequence_number: 1,
                },
                detection_time: 1000,
                reference_time: 1000,
                event_position: ReferencePosition::default(),
                validity_duration: Some(600),
                termination: Some(0),
                awareness_distance: Some(5),
                relevance_traffic_direction: Some(0),
                transmission_interval: Some(1000),
                station_type: Some(5),
                confidence: Some(PositionConfidence {
                    semi_major_confidence: 100,
                    semi_minor_confidence: 100,
                    semi_major_orientation: 0,
                }),
            },
            situation_container: Some(SituationContainer {
                information_quality: Some(7),
                event_type: EventType {
                    cause: 94,
                    subcause: Some(2),
                },
                linked_cause: None,
            }),
            location_container: Some(LocationContainer {
                event_speed: Some(100),
                event_position_heading: Some(900),
                detection_zones_to_event_position: vec![],
                road_type: Some(0),
            }),
            alacarte_container: Some(AlacarteContainer {
                lane_position: Some(1),
                positioning_solution: Some(1),
            }),
        };

        let json = serde_json::to_value(&denm).unwrap();
        assert_eq!(json["protocol_version"], 2);
        assert_eq!(json["situation_container"]["information_quality"], 7);
        assert_eq!(json["location_container"]["event_speed"], 100);
    }

    #[test]
    fn deserialize_denm() {
        let json = json!({
            "protocol_version": 2,
            "station_id": 12345,
            "management_container": {
                "action_id": {
                    "originating_station_id": 54321,
                    "sequence_number": 1
                },
                "detection_time": 1000,
                "reference_time": 1000,
                "event_position": {
                    "latitude": 0,
                    "longitude": 0,
                    "altitude": {
                        "value": 0,
                        "confidence": 0
                    }
                },
                "validity_duration": 600
            }
        });

        let denm: DecentralizedEnvironmentalNotificationMessage =
            serde_json::from_value(json).unwrap();
        assert_eq!(denm.protocol_version, 2);
        assert_eq!(denm.station_id, 12345);
        assert_eq!(denm.management_container.validity_duration, Some(600));
    }

    #[test]
    fn validate_information_quality_bounds() {
        let denm = DecentralizedEnvironmentalNotificationMessage {
            situation_container: Some(SituationContainer {
                information_quality: Some(8), // Invalid value
                event_type: EventType {
                    cause: 94,
                    subcause: None,
                },
                linked_cause: None,
            }),
            ..Default::default()
        };

        let result = serde_json::to_string(&denm);
        assert!(result.is_err());
    }
}
