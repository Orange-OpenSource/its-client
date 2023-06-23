// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
use std::{cmp, hash};

use serde::{Deserialize, Serialize};

use crate::reception::exchange::mobile::Mobile;
use crate::reception::exchange::{PathHistory, PositionConfidence, ReferencePosition};
use crate::reception::mortal::{etsi_now, timestamp, Mortal};
use crate::reception::typed::Typed;

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct DecentralizedEnvironmentalNotificationMessage {
    pub protocol_version: u8,
    pub station_id: u32,
    pub management_container: ManagementContainer,
    pub situation_container: Option<SituationContainer>,
    pub location_container: Option<LocationContainer>,
    pub alacarte_container: Option<AlacarteContainer>,
}

#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ManagementContainer {
    pub action_id: ActionId,
    pub detection_time: u64,
    pub reference_time: u64,
    pub termination: Option<u8>,
    pub event_position: ReferencePosition,
    pub relevance_distance: Option<u8>,
    pub relevance_traffic_direction: Option<u8>,
    pub validity_duration: Option<u32>,
    pub transmission_interval: Option<u16>,
    pub station_type: Option<u8>,
    pub confidence: Option<PositionConfidence>,
}

#[derive(Default, Debug, Clone, PartialEq, Eq, Serialize, Deserialize, Hash)]
pub struct ActionId {
    pub originating_station_id: u32,
    pub sequence_number: u16,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct SituationContainer {
    pub information_quality: Option<u8>,
    pub event_type: EventType,
    pub linked_cause: Option<EventType>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct LocationContainer {
    pub event_speed: Option<u16>,
    pub event_position_heading: Option<u16>,
    pub traces: Vec<Trace>,
    pub road_type: Option<u8>,
    pub confidence: Option<LocationContainerConfidence>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct AlacarteContainer {
    pub lane_position: Option<i8>,
    pub positioning_solution: Option<u8>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct EventType {
    pub cause: u8,
    pub subcause: Option<u8>,
}

#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct Trace {
    #[serde(rename = "path_history")]
    pub path_history: Vec<PathHistory>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Serialize, Deserialize)]
pub struct LocationContainerConfidence {
    pub speed: Option<u8>,
    pub heading: Option<u8>,
}

impl DecentralizedEnvironmentalNotificationMessage {
    /// Create a new DecentralizedEnvironmentalNotificationMessage of cause 94 (Stationary Vehicle).
    ///
    /// # Arguments
    ///
    /// * `station_id`: the station id
    /// * `originating_station_id`: the originating station id
    /// * `event_position`: the reference position of the event
    /// * `sequence_number`: the sequence number
    /// * `etsi_timestamp`: the timestamp on ETSI format
    /// * `event_position_heading`: the heading of the reference position of he event
    ///
    /// returns: DecentralizedEnvironmentalNotificationMessage
    ///  The Stationary Vehicle DENM built using the provided elements.
    /// # Examples
    ///
    /// use crate::reception::exchange::reference_position::ReferencePosition;
    //  use crate::reception::mortal::{etsi_timestamp, now};

    //  let station_id = 4567;
    //  let originating_station_id = 1230;
    //  let event_position = ReferencePosition::default();
    //  let previous_sequence_number = 10;
    /// let reference_timestamp = now() as u64;
    //  let event_position_heading = Some(3000);

    /// let denm = DecentralizedEnvironmentalNotificationMessage::new_stationary_vehicle(
    //         station_id,
    //         originating_station_id,
    //         event_position,
    //         previous_sequence_number,
    //         reference_timestamp,
    //         event_position_heading,
    //     );
    ///
    pub fn new_stationary_vehicle(
        station_id: u32,
        originating_station_id: u32,
        event_position: ReferencePosition,
        sequence_number: u16,
        etsi_timestamp: u128,
        event_position_heading: Option<u16>,
    ) -> Self {
        Self::new(
            station_id,
            originating_station_id,
            event_position,
            sequence_number,
            etsi_timestamp,
            94,
            Option::Some(0), // FIXME remove it when the gateway will accept it
            None,
            None,
            Some(0),
            event_position_heading,
        )
    }

    #[allow(clippy::too_many_arguments)]
    pub fn new_traffic_condition(
        station_id: u32,
        originating_station_id: u32,
        event_position: ReferencePosition,
        sequence_number: u16,
        etsi_timestamp: u128,
        subcause: Option<u8>,
        relevance_distance: Option<u8>,
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
            relevance_distance,
            relevance_traffic_direction,
            event_speed,
            event_position_heading,
        )
    }

    #[allow(clippy::too_many_arguments)]
    pub fn new(
        station_id: u32,
        originating_station_id: u32,
        event_position: ReferencePosition,
        sequence_number: u16,
        etsi_timestamp: u128,
        cause: u8,
        subcause: Option<u8>,
        relevance_distance: Option<u8>,
        relevance_traffic_direction: Option<u8>,
        event_speed: Option<u16>,
        event_position_heading: Option<u16>,
    ) -> Self {
        Self {
            protocol_version: 2,
            station_id,
            management_container: ManagementContainer {
                action_id: ActionId {
                    originating_station_id,
                    sequence_number,
                },
                // FIXME find why the serde Serializer can't match the u128
                detection_time: etsi_timestamp as u64,
                reference_time: etsi_timestamp as u64,
                event_position,
                // 10 seconds to reduce the TTL
                validity_duration: Option::Some(10),
                station_type: Option::Some(5),
                relevance_distance,
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

    pub fn is_stationary_vehicle(&self) -> bool {
        self.situation_container.is_some()
            && 94 == self.situation_container.as_ref().unwrap().event_type.cause
    }

    pub fn is_traffic_condition(&self) -> bool {
        self.situation_container.is_some()
            && 1 == self.situation_container.as_ref().unwrap().event_type.cause
    }
}

impl hash::Hash for DecentralizedEnvironmentalNotificationMessage {
    fn hash<H: hash::Hasher>(&self, state: &mut H) {
        self.management_container.hash(state);
    }
}

impl cmp::Eq for DecentralizedEnvironmentalNotificationMessage {}

impl cmp::PartialEq for DecentralizedEnvironmentalNotificationMessage {
    fn eq(&self, other: &Self) -> bool {
        self.management_container == other.management_container
    }
}

impl Mobile for DecentralizedEnvironmentalNotificationMessage {
    fn mobile_id(&self) -> u32 {
        self.station_id
    }

    fn position(&self) -> &ReferencePosition {
        &self.management_container.event_position
    }

    fn speed(&self) -> Option<u16> {
        if let Some(location_container) = &self.location_container {
            return location_container.event_speed;
        }
        None
    }

    fn heading(&self) -> Option<u16> {
        if let Some(location_container) = &self.location_container {
            return location_container.event_position_heading;
        }
        None
    }
}

impl Mortal for DecentralizedEnvironmentalNotificationMessage {
    fn timeout(&self) -> u128 {
        timestamp(self.management_container.detection_time as u128)
            + (self
                .management_container
                .validity_duration
                .unwrap_or_default()
                * 1000) as u128
    }

    fn terminate(&mut self) {
        self.management_container.termination = Some(0);
        self.management_container.detection_time = etsi_now() as u64;
        self.management_container.validity_duration = Some(10);
    }

    fn terminated(&self) -> bool {
        self.management_container.termination.is_some()
    }
}

impl Typed for DecentralizedEnvironmentalNotificationMessage {
    fn get_type() -> String {
        "denm".to_string()
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
            relevance_distance: Default::default(),
            relevance_traffic_direction: Default::default(),
            validity_duration: Some(600),
            transmission_interval: Default::default(),
            station_type: Default::default(),
            confidence: Default::default(),
        }
    }
}

impl cmp::PartialEq for ManagementContainer {
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
    use crate::reception::exchange::decentralized_environmental_notification_message::DecentralizedEnvironmentalNotificationMessage;
    use crate::reception::exchange::reference_position::ReferencePosition;
    use crate::reception::mortal::{etsi_timestamp, now};

    #[test]
    fn create_new_stationary_vehicle() {
        let station_id = 4567;
        let originating_station_id = 1230;
        let event_position = ReferencePosition::default();
        let sequence_number = 10;
        let reference_timestamp = now();
        let event_position_heading = Some(3000);
        std::thread::sleep(std::time::Duration::from_secs(1));

        let denm = DecentralizedEnvironmentalNotificationMessage::new_stationary_vehicle(
            station_id,
            originating_station_id,
            //assumed clone, to compare with further
            event_position.clone(),
            sequence_number,
            reference_timestamp,
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
        assert_eq!(
            denm.management_container.reference_time,
            reference_timestamp as u64
        );

        let etsi_ref_time = etsi_timestamp(reference_timestamp) as u64;
        assert!(denm.management_container.detection_time >= etsi_ref_time + 1000);
    }
}
