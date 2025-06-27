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

use crate::exchange::etsi::reference_position::{
    DeltaReferencePosition, PathPoint, ReferencePosition,
};
use crate::exchange::etsi::{etsi_now, heading_from_etsi, speed_from_etsi, timestamp_to_etsi};
use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::mortal::Mortal;
use crate::mobility::mobile::Mobile;
use crate::mobility::position::Position;

use crate::exchange::etsi::cause_code::CauseCode;
use crate::exchange::etsi::heading::Heading;
use crate::exchange::etsi::speed::Speed;
use serde::{Deserialize, Serialize};

/// Represents a Decentralized Environmental Notification Message (DENM) according to an ETSI standard.
///
/// This message is used to describe detected road hazards and traffic conditions.
/// It implements the schema defined in the [DENM version 2.2.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/denm/denm_schema_2-2-0.json
#[serde_with::skip_serializing_none]
#[derive(Debug, Default, Clone, Serialize, Deserialize)]
pub struct DecentralizedEnvironmentalNotificationMessage {
    /// Protocol version (mandatory).
    pub protocol_version: u8,
    /// Unique identifier for the station (mandatory).
    pub station_id: u32,
    /// Container with management information about the notification (mandatory)
    pub management: ManagementContainer,

    /// Contains situation information about the detected event (optional)
    pub situation: Option<SituationContainer>,
    /// Contains location-related information about the event (optional)
    pub location: Option<LocationContainer>,
    /// Contains additional specific information (optional)
    pub alacarte: Option<AlacarteContainer>,
}

/// Contains management information for the DENM
#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ManagementContainer {
    pub action_id: ActionId,
    pub detection_time: u64,
    pub reference_time: u64,
    pub event_position: ReferencePosition,
    pub station_type: u8,

    pub termination: Option<u8>,
    pub awareness_distance: Option<Distance>,
    pub traffic_direction: Option<TrafficDirection>,
    pub validity_duration: Option<u32>,
    pub transmission_interval: Option<u16>,
}

/// Contains situation information about the detected event
#[serde_with::skip_serializing_none]
#[derive(Debug, Default, Clone, Serialize, Deserialize)]
pub struct SituationContainer {
    pub information_quality: u8,
    pub event_type: CauseCode,

    pub linked_cause: Option<CauseCode>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub event_zone: Vec<EventPoint>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub linked_denms: Vec<ActionId>,
    pub event_end: Option<i16>,
}

#[serde_with::skip_serializing_none]
#[derive(Debug, Default, Clone, Serialize, Deserialize)]
pub struct EventPoint {
    pub event_position: DeltaReferencePosition,
    // TODO: implements an InformationQuality enum
    pub information_quality: u8,

    pub event_delta_time: Option<u16>,
}

/// Contains location-related information about the event
#[serde_with::skip_serializing_none]
#[derive(Debug, Default, Clone, Serialize, Deserialize)]
pub struct LocationContainer {
    /// Speed at the event location in cm/s
    pub event_speed: Option<Speed>,
    /// Heading at the event location in 0.1 degrees
    pub event_position_heading: Option<Heading>,
    /// List of traces leading to the event position (optional, even mandatory into the specification)
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub detection_zones_to_event_position: Vec<Trace>,
    /// Type of road where the event occurred
    // TODO: implements a RoadType enum
    pub road_type: Option<u8>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct AlacarteContainer {
    pub lane_position: Option<i8>,
    // TODO: implements a PositioningSolution enum
    pub positioning_solution: Option<u8>,
}

/// Represents an action identifier
#[derive(Clone, Debug, Default, Hash, Eq, PartialEq, Serialize, Deserialize)]
pub struct ActionId {
    pub originating_station_id: u32,
    pub sequence_number: u16,
}

#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct Trace {
    pub path: Vec<PathPoint>,
}

#[derive(Default, Debug, Clone, Copy, Hash, PartialEq, Eq, Serialize, Deserialize)]
#[serde(from = "u8", into = "u8")]
pub enum TrafficDirection {
    #[default]
    AllTrafficDirection = 0,
    UpstreamTraffic = 1,
    DownstreamTraffic = 2,
    OppositeTraffic = 3,
}

#[derive(Default, Debug, Clone, Copy, Hash, PartialEq, Eq, Serialize, Deserialize)]
#[serde(from = "u8", into = "u8")]
pub enum Distance {
    #[default]
    LessThan50m = 0,
    LessThan100m = 1,
    LessThan200m = 2,
    LessThan500m = 3,
    LessThan1000m = 4,
    LessThan5Km = 5,
    LessThan10Km = 6,
    Over10Km = 7,
}

impl DecentralizedEnvironmentalNotificationMessage {
    pub fn new_stationary_vehicle(
        station_id: u32,
        originating_station_id: u32,
        event_position: ReferencePosition,
        sequence_number: u16,
        etsi_timestamp: u64,
        event_position_heading: Option<Heading>,
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
            Some(Speed {
                value: 0,
                ..Default::default()
            }),
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
        awareness_distance: Option<Distance>,
        traffic_direction: Option<TrafficDirection>,
        event_speed: Option<Speed>,
        event_position_heading: Option<Heading>,
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
            traffic_direction,
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
        awareness_distance: Option<Distance>,
        traffic_direction: Option<TrafficDirection>,
        event_speed: Option<Speed>,
        event_position_heading: Option<Heading>,
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
            traffic_direction,
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
        awareness_distance: Option<Distance>,
        traffic_direction: Option<TrafficDirection>,
        event_speed: Option<Speed>,
        event_position_heading: Option<Heading>,
    ) -> Self {
        // collisionRisk
        denm.management.event_position = event_position;
        denm.management.reference_time = etsi_timestamp;
        denm.management.awareness_distance = awareness_distance;
        denm.management.traffic_direction = traffic_direction;

        if event_speed.is_some() || event_position_heading.is_some() {
            denm.location = Option::from(LocationContainer {
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
        awareness_distance: Option<Distance>,
        traffic_direction: Option<TrafficDirection>,
        event_speed: Option<Speed>,
        event_position_heading: Option<Heading>,
        validity_duration: Option<u32>,
        transmission_interval: Option<u16>,
    ) -> Self {
        Self {
            protocol_version: 2,
            station_id,
            management: ManagementContainer {
                action_id: ActionId {
                    originating_station_id,
                    sequence_number,
                },
                detection_time: etsi_timestamp,
                reference_time: etsi_now(),
                event_position,
                validity_duration,
                transmission_interval,
                station_type: 5,
                awareness_distance,
                traffic_direction,
                ..Default::default()
            },
            situation: Option::from(SituationContainer {
                event_type: CauseCode { cause, subcause },
                ..Default::default()
            }),
            location: Option::from(LocationContainer {
                event_speed,
                event_position_heading,
                ..Default::default()
            }),
            ..Default::default()
        }
    }

    /// Creates an updated copy of the provided DENM
    pub fn update(mut self, detection_time: u64, mobile: &dyn Mobile) -> Self {
        self.management.detection_time = timestamp_to_etsi(detection_time);
        self.management.reference_time = etsi_now();
        self.management.event_position = ReferencePosition::from(mobile.position());

        self
    }

    pub fn update_information_quality(&mut self, information_quality: u8) {
        let situation = self.situation.clone();
        match situation {
            Some(mut situation) => {
                situation.information_quality = information_quality;
                self.situation = Some(situation);
            }
            None => {
                self.situation = Option::from(SituationContainer {
                    information_quality,
                    ..Default::default()
                })
            }
        }
    }

    pub fn is_stationary_vehicle(&self) -> bool {
        self.situation.is_some() && 94 == self.situation.as_ref().unwrap().event_type.cause
    }

    pub fn is_traffic_condition(&self) -> bool {
        self.situation.is_some() && 1 == self.situation.as_ref().unwrap().event_type.cause
    }

    pub fn is_collision_risk(&self) -> bool {
        self.situation.is_some() && 97 == self.situation.as_ref().unwrap().event_type.cause
    }
}

impl hash::Hash for DecentralizedEnvironmentalNotificationMessage {
    fn hash<H: hash::Hasher>(&self, state: &mut H) {
        self.management.hash(state);
    }
}

impl Eq for DecentralizedEnvironmentalNotificationMessage {}

impl PartialEq for DecentralizedEnvironmentalNotificationMessage {
    fn eq(&self, other: &Self) -> bool {
        self.management == other.management
    }
}

impl Mobile for DecentralizedEnvironmentalNotificationMessage {
    fn id(&self) -> u32 {
        self.station_id
    }

    fn position(&self) -> Position {
        self.management.event_position.as_position()
    }

    fn speed(&self) -> Option<f64> {
        match &self.location {
            Some(location) => location
                .event_speed
                .as_ref()
                .map(|event_speed| speed_from_etsi(event_speed.value)),
            None => None,
        }
    }

    fn heading(&self) -> Option<f64> {
        match &self.location {
            Some(location) => location
                .event_position_heading
                .as_ref()
                .map(|event_position_heading| heading_from_etsi(event_position_heading.value)),
            None => None,
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
        self.management.detection_time = timestamp;
        // reference_time different for each message
        self.management.reference_time = timestamp;
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
        self.management.reference_time
            + u64::from(self.management.validity_duration.unwrap_or_default() * 1000)
    }

    fn terminate(&mut self) {
        self.management.termination = Some(0);
        self.management.detection_time = etsi_now();
        self.management.reference_time = etsi_now();
        self.management.validity_duration = Some(10);
    }

    fn terminated(&self) -> bool {
        self.management.termination.is_some()
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
            traffic_direction: Default::default(),
            validity_duration: Some(600),
            transmission_interval: Default::default(),
            station_type: Default::default(),
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

impl From<TrafficDirection> for u8 {
    fn from(val: TrafficDirection) -> Self {
        val as u8
    }
}

impl From<u8> for TrafficDirection {
    fn from(val: u8) -> Self {
        match val {
            0 => TrafficDirection::AllTrafficDirection,
            1 => TrafficDirection::UpstreamTraffic,
            2 => TrafficDirection::DownstreamTraffic,
            3 => TrafficDirection::OppositeTraffic,
            _ => TrafficDirection::default(), // Default case
        }
    }
}

impl From<Distance> for u8 {
    fn from(val: Distance) -> Self {
        val as u8
    }
}

impl From<u8> for Distance {
    fn from(value: u8) -> Self {
        match value {
            0 => Distance::LessThan50m,
            1 => Distance::LessThan100m,
            2 => Distance::LessThan200m,
            3 => Distance::LessThan500m,
            4 => Distance::LessThan1000m,
            5 => Distance::LessThan5Km,
            6 => Distance::LessThan10Km,
            7 => Distance::Over10Km,
            _ => Distance::default(),
        }
    }
}

impl From<f64> for Distance {
    fn from(value: f64) -> Self {
        match value {
            _ if value < 50. => Distance::LessThan50m,
            _ if value < 100. => Distance::LessThan100m,
            _ if value < 200. => Distance::LessThan200m,
            _ if value < 500. => Distance::LessThan500m,
            _ if value < 1000. => Distance::LessThan1000m,
            _ if value < 5_000. => Distance::LessThan5Km,
            _ if value < 10_000. => Distance::LessThan10Km,
            _ => Distance::default(),
        }
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::decentralized_environmental_notification_message::{
        ActionId, AlacarteContainer, CauseCode, DecentralizedEnvironmentalNotificationMessage,
        Distance, EventPoint, LocationContainer, ManagementContainer, SituationContainer, Trace,
        TrafficDirection,
    };
    use crate::exchange::etsi::heading::Heading;
    use crate::exchange::etsi::reference_position::{
        Altitude, DeltaReferencePosition, PathPoint, PositionConfidenceEllipse, ReferencePosition,
    };
    use crate::exchange::etsi::speed::Speed;
    use crate::exchange::etsi::{etsi_now, timestamp_to_etsi};
    use crate::exchange::mortal::Mortal;
    use crate::now;

    fn minimal_denm() -> &'static str {
        r#"{
            "protocol_version": 255,
            "station_id": 4294967295,
            "management": {
                 "action_id": {
                    "originating_station_id": 4294967295,
                    "sequence_number": 65535
                },
                "detection_time": 4398046511103,
                "reference_time": 4398046511103,
                "event_position": {
                    "latitude": 900000001,
                    "longitude": 1800000001,
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
                    },
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 0,
                        "semi_major_orientation": 3601
                    }
                },
                "station_type": 15                
            }
        }"#
    }

    fn standard_denm() -> &'static str {
        r#"{
            "protocol_version": 255,
            "station_id": 4294967295,
            "management": {
                 "action_id": {
                    "originating_station_id": 4294967295,
                    "sequence_number": 65535
                },
                "detection_time": 4398046511103,
                "reference_time": 4398046511103,
                "event_position": {
                    "latitude": 900000001,
                    "longitude": 1800000001,
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
                    },
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 0,
                        "semi_major_orientation": 3601
                    }
                },
                "station_type": 15,
                "validity_duration": 86400
            },
            "situation": {
                "information_quality": 7,
                "event_type": {
                    "cause": 94,
                    "subcause": 2
                }
            }
        }"#
    }

    fn full_denm() -> &'static str {
        r#"{
            "protocol_version": 255,
            "station_id": 4294967295,
            "management": {
                 "action_id": {
                    "originating_station_id": 4294967295,
                    "sequence_number": 65535
                },
                "detection_time": 4398046511103,
                "reference_time": 4398046511103,
                "event_position": {
                    "latitude": 900000001,
                    "longitude": 1800000001,
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
                    },
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 0,
                        "semi_major_orientation": 3601
                    }
                },
                "station_type": 15,
                "termination": 1,
                "awareness_distance": 7,
                "traffic_direction": 3,
                "validity_duration": 86400,
                "transmission_interval": 10000
            },
            "situation": {
                "information_quality": 7,
                "event_type": {
                    "cause": 94,
                    "subcause": 2
                },
                "linked_cause": {
                    "cause": 95,
                    "subcause": 3
                },
                "event_zone": [
                    {
                        "event_position": {
                            "delta_latitude": 131072,
                            "delta_longitude": -131071,
                            "delta_altitude": 12800
                        },
                        "information_quality": 7,
                        "event_delta_time": 65535
                    },
                    {
                        "event_position": {
                            "delta_latitude": -131071,
                            "delta_longitude": 131072,
                            "delta_altitude": -12700
                        },
                        "information_quality": 0,
                        "event_delta_time": 0
                    }
                ],
                "linked_denms": [
                    {
                        "originating_station_id": 4294967295,
                        "sequence_number": 65535
                    },
                    {
                        "originating_station_id": 0,
                        "sequence_number": 0
                    }
                ],
                "event_end": 8191
            },
            "location": {
                "event_speed": {
                    "value": 16383,
                    "confidence": 127
                },
                "event_position_heading": {
                    "value": 3601,
                    "confidence": 127
                },
                "detection_zones_to_event_position": [
                    {
                        "path": [
                            {
                                "path_position": {
                                    "delta_latitude": 131072,
                                    "delta_longitude": -131071,
                                    "delta_altitude": 12800
                                },
                                "path_delta_time": 65535
                            },
                            {
                                "path_position": {
                                    "delta_latitude": -131071,
                                    "delta_longitude": 131072,
                                    "delta_altitude": -12700
                                },
                                "path_delta_time": 0
                            }
                        ]
                    },
                    {
                        "path": [
                            {
                                "path_position": {
                                    "delta_latitude": 65036,
                                    "delta_longitude": 65036,
                                    "delta_altitude": 6400
                                },
                                "path_delta_time": 32518
                            },
                            {
                                "path_position": {
                                    "delta_latitude": 32518,
                                    "delta_longitude": 32518,
                                    "delta_altitude": 3200
                                },
                                "path_delta_time": 16259
                            }
                        ]
                    }
                ],
                "road_type": 3
            },
            "alacarte": {
                "lane_position": 14,
                "positioning_solution": 6
            }
        }"#
    }

    #[test]
    fn test_deserialize_minimal_denm() {
        let data = minimal_denm();

        let denm =
            serde_json::from_str::<DecentralizedEnvironmentalNotificationMessage>(data).unwrap();

        // minimal
        assert_eq!(denm.protocol_version, 255);
        assert_eq!(denm.station_id, 4294967295);
        assert_eq!(denm.management.action_id.originating_station_id, 4294967295);
        assert_eq!(denm.management.action_id.sequence_number, 65535);
        assert_eq!(denm.management.detection_time, 4398046511103);
        assert_eq!(denm.management.reference_time, 4398046511103);
        assert_eq!(denm.management.event_position.latitude, 900000001);
        assert_eq!(denm.management.event_position.longitude, 1800000001);
        assert_eq!(denm.management.event_position.altitude.value, 800001);
        assert_eq!(denm.management.event_position.altitude.confidence, 15);
        assert_eq!(
            denm.management
                .event_position
                .position_confidence_ellipse
                .semi_major,
            4095
        );
        assert_eq!(
            denm.management
                .event_position
                .position_confidence_ellipse
                .semi_minor,
            0
        );
        assert_eq!(
            denm.management
                .event_position
                .position_confidence_ellipse
                .semi_major_orientation,
            3601
        );
        assert_eq!(denm.management.station_type, 15);

        // no situation, location or alacarte
        assert!(denm.situation.is_none());
        assert!(denm.location.is_none());
        assert!(denm.alacarte.is_none());
    }

    #[test]
    fn test_deserialize_standard_denm() {
        let data = standard_denm();

        let denm =
            serde_json::from_str::<DecentralizedEnvironmentalNotificationMessage>(data).unwrap();

        // minimal
        assert_eq!(denm.protocol_version, 255);
        assert_eq!(denm.station_id, 4294967295);
        assert_eq!(denm.management.action_id.originating_station_id, 4294967295);
        assert_eq!(denm.management.action_id.sequence_number, 65535);
        assert_eq!(denm.management.detection_time, 4398046511103);
        assert_eq!(denm.management.reference_time, 4398046511103);
        assert_eq!(denm.management.event_position.latitude, 900000001);
        assert_eq!(denm.management.event_position.longitude, 1800000001);
        assert_eq!(denm.management.event_position.altitude.value, 800001);
        assert_eq!(denm.management.event_position.altitude.confidence, 15);
        assert_eq!(
            denm.management
                .event_position
                .position_confidence_ellipse
                .semi_major,
            4095
        );
        assert_eq!(
            denm.management
                .event_position
                .position_confidence_ellipse
                .semi_minor,
            0
        );
        assert_eq!(
            denm.management
                .event_position
                .position_confidence_ellipse
                .semi_major_orientation,
            3601
        );
        assert_eq!(denm.management.station_type, 15);

        //standard
        assert_eq!(denm.management.validity_duration, Some(86400));
        assert!(denm.situation.is_some());
        let situation = denm.situation.as_ref().unwrap();
        assert_eq!(situation.information_quality, 7);
        assert_eq!(situation.event_type.cause, 94);
        assert_eq!(situation.event_type.subcause, Some(2));

        // no situation details
        assert!(situation.linked_cause.is_none());
        assert!(situation.event_zone.is_empty());
        assert!(situation.linked_denms.is_empty());
        assert!(situation.event_end.is_none());

        // no location or alacarte
        assert!(denm.location.is_none());
        assert!(denm.alacarte.is_none());
    }

    #[test]
    fn test_deserialize_full_denm() {
        let data = full_denm();

        let denm =
            serde_json::from_str::<DecentralizedEnvironmentalNotificationMessage>(data).unwrap();

        // minimal
        assert_eq!(denm.protocol_version, 255);
        assert_eq!(denm.station_id, 4294967295);
        assert_eq!(denm.management.action_id.originating_station_id, 4294967295);
        assert_eq!(denm.management.action_id.sequence_number, 65535);
        assert_eq!(denm.management.detection_time, 4398046511103);
        assert_eq!(denm.management.reference_time, 4398046511103);
        assert_eq!(denm.management.event_position.latitude, 900000001);
        assert_eq!(denm.management.event_position.longitude, 1800000001);
        assert_eq!(denm.management.event_position.altitude.value, 800001);
        assert_eq!(denm.management.event_position.altitude.confidence, 15);
        assert_eq!(
            denm.management
                .event_position
                .position_confidence_ellipse
                .semi_major,
            4095
        );
        assert_eq!(
            denm.management
                .event_position
                .position_confidence_ellipse
                .semi_minor,
            0
        );
        assert_eq!(
            denm.management
                .event_position
                .position_confidence_ellipse
                .semi_major_orientation,
            3601
        );
        assert_eq!(denm.management.station_type, 15);

        //standard
        assert_eq!(denm.management.validity_duration, Some(86400));
        assert!(denm.situation.is_some());
        let situation = denm.situation.as_ref().unwrap();
        assert_eq!(situation.information_quality, 7);
        assert_eq!(situation.event_type.cause, 94);
        assert_eq!(situation.event_type.subcause, Some(2));

        // full
        assert!(situation.linked_cause.is_some());
        let situation_linked_cause = situation.linked_cause.as_ref().unwrap();
        assert_eq!(situation_linked_cause.cause, 95);
        assert_eq!(situation_linked_cause.subcause, Some(3));
        assert_eq!(situation.event_zone.len(), 2);
        assert_eq!(
            situation.event_zone[0].event_position.delta_latitude,
            131072
        );
        assert_eq!(
            situation.event_zone[0].event_position.delta_longitude,
            -131071
        );
        assert_eq!(situation.event_zone[0].event_position.delta_altitude, 12800);
        assert_eq!(situation.event_zone[0].information_quality, 7);
        assert_eq!(situation.event_zone[0].event_delta_time, Some(65535));
        assert_eq!(
            situation.event_zone[1].event_position.delta_latitude,
            -131071
        );
        assert_eq!(
            situation.event_zone[1].event_position.delta_longitude,
            131072
        );
        assert_eq!(
            situation.event_zone[1].event_position.delta_altitude,
            -12700
        );
        assert_eq!(situation.event_zone[1].information_quality, 0);
        assert_eq!(situation.event_zone[1].event_delta_time, Some(0));

        assert_eq!(situation.linked_denms.len(), 2);
        assert_eq!(situation.linked_denms[0].originating_station_id, 4294967295);
        assert_eq!(situation.linked_denms[0].sequence_number, 65535);
        assert_eq!(situation.linked_denms[1].originating_station_id, 0);
        assert_eq!(situation.linked_denms[1].sequence_number, 0);
        assert_eq!(situation.event_end, Some(8191));
        assert!(denm.location.is_some());
        let location = denm.location.as_ref().unwrap();
        assert!(location.event_speed.is_some());
        let location_event_speed = location.event_speed.as_ref().unwrap();
        assert_eq!(location_event_speed.value, 16383);
        assert_eq!(location_event_speed.confidence, 127);
        assert!(location.event_position_heading.is_some());
        let location_event_position_heading = location.event_position_heading.as_ref().unwrap();
        assert_eq!(location_event_position_heading.value, 3601);
        assert_eq!(location_event_position_heading.confidence, 127);
        assert_eq!(location.detection_zones_to_event_position.len(), 2);
        assert_eq!(location.detection_zones_to_event_position[0].path.len(), 2);
        assert_eq!(
            location.detection_zones_to_event_position[0].path[0]
                .path_position
                .delta_latitude,
            131072
        );
        assert_eq!(
            location.detection_zones_to_event_position[0].path[0]
                .path_position
                .delta_longitude,
            -131071
        );
        assert_eq!(
            location.detection_zones_to_event_position[0].path[0]
                .path_position
                .delta_altitude,
            12800
        );
        assert_eq!(
            location.detection_zones_to_event_position[0].path[0].path_delta_time,
            Some(65535)
        );
        assert_eq!(
            location.detection_zones_to_event_position[0].path[1]
                .path_position
                .delta_latitude,
            -131071
        );
        assert_eq!(
            location.detection_zones_to_event_position[0].path[1]
                .path_position
                .delta_longitude,
            131072
        );
        assert_eq!(
            location.detection_zones_to_event_position[0].path[1]
                .path_position
                .delta_altitude,
            -12700
        );
        assert_eq!(
            location.detection_zones_to_event_position[0].path[1].path_delta_time,
            Some(0)
        );
        assert_eq!(location.detection_zones_to_event_position[1].path.len(), 2);
        assert_eq!(
            location.detection_zones_to_event_position[1].path[0]
                .path_position
                .delta_latitude,
            65036
        );
        assert_eq!(
            location.detection_zones_to_event_position[1].path[0]
                .path_position
                .delta_longitude,
            65036
        );
        assert_eq!(
            location.detection_zones_to_event_position[1].path[0]
                .path_position
                .delta_altitude,
            6400
        );
        assert_eq!(
            location.detection_zones_to_event_position[1].path[0].path_delta_time,
            Some(32518)
        );
        assert_eq!(
            location.detection_zones_to_event_position[1].path[1]
                .path_position
                .delta_latitude,
            32518
        );
        assert_eq!(
            location.detection_zones_to_event_position[1].path[1]
                .path_position
                .delta_longitude,
            32518
        );
        assert_eq!(
            location.detection_zones_to_event_position[1].path[1]
                .path_position
                .delta_altitude,
            3200
        );
        assert_eq!(
            location.detection_zones_to_event_position[1].path[1].path_delta_time,
            Some(16259)
        );
        assert_eq!(location.road_type, Some(3));
        assert!(denm.alacarte.is_some());
        let alacarte = denm.alacarte.as_ref().unwrap();
        assert_eq!(alacarte.lane_position, Some(14));
        assert_eq!(alacarte.positioning_solution, Some(6));
    }

    #[test]
    fn test_reserialize_minimal_denm() {
        let data = minimal_denm();

        let denm =
            serde_json::from_str::<DecentralizedEnvironmentalNotificationMessage>(data).unwrap();
        let serialized = serde_json::to_string(&denm).unwrap();
        assert_eq!(
            serialized,
            data.replace("\n", "").replace(" ", "").replace("\t", "")
        );
    }

    #[test]
    fn test_reserialize_standard_denm() {
        let data = standard_denm();

        let denm =
            serde_json::from_str::<DecentralizedEnvironmentalNotificationMessage>(data).unwrap();
        let serialized = serde_json::to_string(&denm).unwrap();
        assert_eq!(
            serialized,
            data.replace("\n", "").replace(" ", "").replace("\t", "")
        );
    }

    #[test]
    fn test_reserialize_full_denm() {
        let data = full_denm();

        let denm =
            serde_json::from_str::<DecentralizedEnvironmentalNotificationMessage>(data).unwrap();
        let serialized = serde_json::to_string(&denm).unwrap();
        assert_eq!(
            serialized,
            data.replace("\n", "").replace(" ", "").replace("\t", "")
        );
    }

    #[test]
    fn create_new_stationary_vehicle() {
        let station_id = 4567;
        let originating_station_id = 1230;
        let event_position = ReferencePosition::default();
        let sequence_number = 10;
        let detection_time = etsi_now();
        let event_position_heading = Some(Heading {
            value: 3000,
            ..Default::default()
        });
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
            denm.management.action_id.originating_station_id,
            originating_station_id
        );
        assert_eq!(denm.management.event_position, event_position);
        assert_eq!(denm.management.action_id.sequence_number, sequence_number);

        assert_eq!(denm.management.detection_time, detection_time);
        assert!(denm.management.detection_time <= denm.management.reference_time);
    }

    #[test]
    fn information_quality_update() {
        let mut denm = DecentralizedEnvironmentalNotificationMessage::default();

        assert!(denm.situation.is_none());

        denm.update_information_quality(5);
        assert!(denm.situation.is_some());
        let situation = denm.situation.unwrap();
        assert_eq!(situation.information_quality, 5);
    }

    #[test]
    fn correct_timeout() {
        let now = now();
        let denm = DecentralizedEnvironmentalNotificationMessage {
            management: ManagementContainer {
                reference_time: timestamp_to_etsi(now) + 500,
                detection_time: timestamp_to_etsi(now),
                validity_duration: Some(10),
                ..Default::default()
            },
            ..Default::default()
        };

        assert!(denm.remaining_time() <= 10);
        assert_eq!(denm.timeout() - denm.management.reference_time, 10_000);
    }

    #[test]
    fn serialize_minimal_denm() {
        let denm = DecentralizedEnvironmentalNotificationMessage {
            protocol_version: 2,
            station_id: 12345,
            management: ManagementContainer {
                action_id: ActionId {
                    originating_station_id: 54321,
                    sequence_number: 1,
                },
                detection_time: 1000,
                reference_time: 1000,
                event_position: ReferencePosition {
                    latitude: 900000001,
                    longitude: 1800000001,
                    altitude: Altitude {
                        value: 800001,
                        confidence: 15,
                    },
                    position_confidence_ellipse: PositionConfidenceEllipse {
                        semi_major: 4095,
                        semi_minor: 0,
                        semi_major_orientation: 3601,
                    },
                },
                station_type: 5,
                ..Default::default()
            },
            ..Default::default()
        };

        let json = serde_json::to_value(&denm).unwrap();
        assert_eq!(json["protocol_version"], 2);
        assert_eq!(json["station_id"], 12345);
        assert!(json.get("situation").is_none());
    }

    #[test]
    fn serialize_full_denm() {
        let denm = DecentralizedEnvironmentalNotificationMessage {
            protocol_version: 2,
            station_id: 12345,
            management: ManagementContainer {
                action_id: ActionId {
                    originating_station_id: 54321,
                    sequence_number: 1,
                },
                detection_time: 1000,
                reference_time: 1000,
                event_position: ReferencePosition {
                    latitude: 900000001,
                    longitude: 1800000001,
                    altitude: Altitude {
                        value: 800001,
                        confidence: 15,
                    },
                    position_confidence_ellipse: PositionConfidenceEllipse {
                        semi_major: 4095,
                        semi_minor: 0,
                        semi_major_orientation: 3601,
                    },
                },
                station_type: 5,
                validity_duration: Some(600),
                termination: Some(0),
                awareness_distance: Some(Distance::LessThan5Km),
                traffic_direction: Some(TrafficDirection::AllTrafficDirection),
                transmission_interval: Some(1000),
            },
            situation: Some(SituationContainer {
                information_quality: 7,
                event_type: CauseCode {
                    cause: 94,
                    subcause: Some(2),
                },
                linked_cause: None,
                event_zone: vec![EventPoint {
                    event_position: DeltaReferencePosition {
                        delta_latitude: 0,
                        delta_longitude: 0,
                        delta_altitude: 0,
                    },
                    information_quality: 5,
                    event_delta_time: Some(100),
                }],
                linked_denms: vec![ActionId {
                    originating_station_id: 54321,
                    sequence_number: 1,
                }],
                event_end: Some(10),
            }),
            location: Some(LocationContainer {
                event_speed: Some(Speed {
                    value: 100,
                    confidence: 5,
                }),
                event_position_heading: Some(Heading {
                    value: 900,
                    confidence: 3,
                }),
                detection_zones_to_event_position: vec![Trace {
                    path: vec![PathPoint {
                        path_position: DeltaReferencePosition {
                            delta_latitude: 0,
                            delta_longitude: 0,
                            delta_altitude: 0,
                        },
                        path_delta_time: Some(100),
                    }],
                }],
                road_type: Some(0),
            }),
            alacarte: Some(AlacarteContainer {
                lane_position: Some(1),
                positioning_solution: Some(1),
            }),
        };

        let json = serde_json::to_value(&denm).unwrap();
        assert_eq!(json["protocol_version"], 2);
        assert_eq!(json["situation"]["information_quality"], 7);
        assert_eq!(json["location"]["event_speed"]["value"], 100);
    }
}
