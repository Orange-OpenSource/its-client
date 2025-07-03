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

use serde::{Deserialize, Serialize};

/// Represents a Steering Wheel Angle according to an ETSI standard.
///
/// This message is used to describe a steering wheel angle.
/// It implements the schema defined in the CAM version 2.2.0.
///
/// # Fields
///
/// - `value`: The steering wheel angle.
/// - `confidence`: The confidence of the steering wheel angle.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct SteeringWheelAngle {
    /// Steering wheel angle value.
    pub value: i16,
    /// Confidence level for the steering wheel angle.
    pub confidence: u8,
}
