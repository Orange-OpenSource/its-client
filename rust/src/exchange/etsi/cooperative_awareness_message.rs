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

use crate::exchange::etsi::reference_position::ReferencePosition;
use crate::exchange::etsi::{
    acceleration_from_etsi, heading_from_etsi, speed_from_etsi, PathHistory, PositionConfidence,
};
use crate::mobility::mobile::Mobile;
use std::any::type_name;

use crate::client::configuration::Configuration;
use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::message::content_error::ContentError::NotAMortal;
use crate::exchange::mortal::Mortal;
use crate::mobility::position::Position;
use serde::{Deserialize, Serialize};

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CooperativeAwarenessMessage {
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
    pub station_type: Option<u8>,
    pub reference_position: ReferencePosition,
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
pub struct LowFrequencyContainer {
    pub vehicle_role: Option<u8>,
    pub exterior_lights: String,
    pub path_history: Vec<PathHistory>,
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

impl Mobile for CooperativeAwarenessMessage {
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

impl Content for CooperativeAwarenessMessage {
    fn get_type(&self) -> &str {
        "cam"
    }

    /// TODO implement this (issue [#96](https://github.com/Orange-OpenSource/its-client/issues/96))
    fn appropriate(&mut self, configuration: &Configuration, _timestamp: u64) {
        let station_id = configuration
            .node
            .as_ref()
            .unwrap()
            .read()
            .unwrap()
            .station_id(Some(self.station_id));
        self.station_id = station_id;
        // TODO update the generation delta time
    }

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError> {
        Ok(self)
    }

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError> {
        Err(NotAMortal(type_name::<CooperativeAwarenessMessage>()))
    }
}
