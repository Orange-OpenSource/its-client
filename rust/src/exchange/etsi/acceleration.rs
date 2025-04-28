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

/// Represents an Acceleration according to an ETSI standard.
///
/// This message is used to describe an acceleration.
/// It implements the schema defined in the CAM version 2.2.0.
///
/// # Fields
///
/// - `value`: The acceleration.
/// - `confidence`: The confidence of the acceleration.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Acceleration {
    /// Acceleration value in centimeters per second squared.
    pub value: i16,
    /// Confidence level for the acceleration.
    pub confidence: u8,
}
