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

use crate::mobility::mobile::Mobile;
use std::any::type_name;

use crate::exchange::etsi::cooperative_awareness_message::ExteriorLights;
use crate::exchange::etsi::reference_position113::{PositionConfidence, ReferencePosition113};
use crate::exchange::etsi::{
    acceleration_from_etsi, heading_from_etsi, speed_from_etsi, timestamp_to_generation_delta_time,
};
use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::message::content_error::ContentError::NotAMortal;
use crate::exchange::mortal::Mortal;
use crate::mobility::position::Position;
use serde::{Deserialize, Serialize};

/// Represents a Cooperative Awareness Message (CAM) according to an ETSI standard.
///
/// This message is used to describe the information about itself.
/// It implements the schema defined in the [CAM version 1.1.3][1].
///
/// # Fields
///
/// - `protocol_version`: Version of the protocol used
/// - `station_id`: Unique identifier for the station sending the message
/// - `generation_delta_time`: Time difference since the last generation of the message
/// - `basic_container`: Contains basic information about the station
/// - `high_frequency_container`: Contains high-frequency data such as speed, heading, and acceleration
/// - `low_frequency_container`: Container for low-frequency data such as vehicle role and path history
/// - `special_vehicle_container`: Container for special vehicle data
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cam/cam_schema_1-1-3.json
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CooperativeAwarenessMessage113 {
    pub protocol_version: u8,
    pub station_id: u32,
    pub generation_delta_time: u16,
    pub basic_container: BasicContainer,
    pub high_frequency_container: HighFrequencyContainer,

    pub low_frequency_container: Option<LowFrequencyContainer>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct BasicContainer {
    pub reference_position: ReferencePosition113,

    pub station_type: Option<u8>,
    pub confidence: Option<PositionConfidence>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct HighFrequencyContainer {
    pub heading: Option<u16>,
    pub speed: Option<u16>,
    pub drive_direction: Option<u8>,
    pub vehicle_length: Option<u16>,
    pub vehicle_width: Option<u16>,
    pub curvature: Option<i16>,
    pub curvature_calculation_mode: Option<u8>,
    pub longitudinal_acceleration: Option<i16>,
    pub yaw_rate: Option<i16>,
    pub acceleration_control: Option<String>,
    pub lane_position: Option<i8>,
    pub lateral_acceleration: Option<i16>,
    pub vertical_acceleration: Option<i16>,
    pub confidence: Option<HighFrequencyConfidence>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct HighFrequencyConfidence {
    pub heading: Option<u8>,
    pub speed: Option<u8>,
    pub vehicle_length: Option<u8>,
    pub yaw_rate: Option<u8>,
    pub longitudinal_acceleration: Option<u8>,
    pub curvature: Option<u8>,
    pub lateral_acceleration: Option<u8>,
    pub vertical_acceleration: Option<u8>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct LowFrequencyContainer {
    pub vehicle_role: Option<u8>,
    pub exterior_lights: ExteriorLights,
    pub path_history: Vec<PathHistory>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PathHistory {
    pub path_position: PathPosition,
    pub path_delta_time: Option<u16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PathPosition {
    pub delta_latitude: Option<i32>,
    pub delta_longitude: Option<i32>,
    pub delta_altitude: Option<i32>,
}

impl Mobile for CooperativeAwarenessMessage113 {
    fn id(&self) -> u32 {
        self.station_id
    }

    fn position(&self) -> Position {
        self.basic_container.reference_position.as_position()
    }

    fn speed(&self) -> Option<f64> {
        self.high_frequency_container.speed.map(speed_from_etsi)
    }

    fn heading(&self) -> Option<f64> {
        self.high_frequency_container.heading.map(heading_from_etsi)
    }

    fn acceleration(&self) -> Option<f64> {
        self.high_frequency_container
            .longitudinal_acceleration
            .map(acceleration_from_etsi)
    }
}

impl Content for CooperativeAwarenessMessage113 {
    fn get_type(&self) -> &str {
        "cam"
    }

    fn appropriate(&mut self, timestamp: u64, new_station_id: u32) {
        self.station_id = new_station_id;
        self.generation_delta_time = timestamp_to_generation_delta_time(timestamp);
    }

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError> {
        Ok(self)
    }

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError> {
        Err(NotAMortal(type_name::<CooperativeAwarenessMessage113>()))
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::cooperative_awareness_message_113::{
        BasicContainer, CooperativeAwarenessMessage113,
    };
    use crate::exchange::etsi::reference_position113::ReferencePosition113;
    use crate::mobility::mobile::Mobile;

    #[test]
    fn test_cooperative_awareness_message_113_as_position() {
        let reference_position = ReferencePosition113 {
            latitude: 123456789,
            longitude: 987654321,
            altitude: 1000,
        };
        let cam = CooperativeAwarenessMessage113 {
            basic_container: BasicContainer {
                reference_position,
                ..Default::default()
            },
            ..Default::default()
        };

        let position = cam.position();
        assert_eq!(position.latitude, 12.3456789_f64.to_radians());
        assert_eq!(position.longitude, 98.7654321_f64.to_radians());
        assert_eq!(position.altitude, 10.00);
    }
}
