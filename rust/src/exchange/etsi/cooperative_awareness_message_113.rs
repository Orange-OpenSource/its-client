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

use crate::exchange::etsi::reference_position::ReferencePosition113;
use crate::exchange::etsi::{
    PathPoint, PositionConfidence, acceleration_from_etsi, heading_from_etsi, speed_from_etsi,
    timestamp_to_generation_delta_time,
};
use crate::mobility::mobile::Mobile;
use std::any::type_name;

use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::message::content_error::ContentError::NotAMortal;
use crate::exchange::mortal::Mortal;
use crate::mobility::position::Position;
use serde::{Deserialize, Serialize};

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

impl Mobile for CooperativeAwarenessMessage113 {}

impl Content for CooperativeAwarenessMessage113 {}
