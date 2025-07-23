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

/// Represents a Vehicle Length according to an ETSI standard.
///
/// This message is used to describe a vehicle length.
/// It implements the schema defined in the [CAM version 2.2.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cam/cam_schema_2-2-0.json#L907
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct VehicleLength {
    /// Length of the vehicle in decimeters.
    pub value: u16,
    /// Confidence level for the vehicle length.
    pub confidence: TrailerPresence,
}

/// Enum representing the presence of a trailer.
/// Includes various states such as no trailer, trailer with known/unknown length, or unavailable.
#[derive(Debug, Default, Clone, Copy, Hash, PartialEq, Eq, Serialize, Deserialize)]
#[serde(from = "u8", into = "u8")]
pub enum TrailerPresence {
    /// No trailer is present.
    #[default]
    NoTrailerPresent = 0,
    /// Trailer is present with a known length.
    TrailerPresentWithKnownLength = 1,
    /// Trailer is present with an unknown length.
    TrailerPresentWithUnknownLength = 2,
    /// Trailer presence is unknown.
    Unknown = 3,
    /// Trailer presence information is unavailable.
    Unavailable = 4,
}

impl TrailerPresence {
    /// Attempts to convert a `u8` value into a `TrailerPresence` enum.
    /// Returns an error if the value is invalid.
    fn try_from(value: u8) -> Result<TrailerPresence, String> {
        match value {
            0 => Ok(TrailerPresence::NoTrailerPresent),
            1 => Ok(TrailerPresence::TrailerPresentWithKnownLength),
            2 => Ok(TrailerPresence::TrailerPresentWithUnknownLength),
            3 => Ok(TrailerPresence::Unknown),
            4 => Ok(TrailerPresence::Unavailable),
            _ => Err(format!("Invalid trailer presence value: {value}")),
        }
    }
}

impl From<u8> for TrailerPresence {
    /// Converts a `u8` value into a `TrailerPresence` enum.
    /// Defaults to `TrailerPresence::Unavailable` if the value is invalid.
    fn from(value: u8) -> Self {
        Self::try_from(value).unwrap_or(TrailerPresence::Unavailable)
    }
}

impl From<TrailerPresence> for u8 {
    fn from(trailer_presence: TrailerPresence) -> Self {
        trailer_presence as u8
    }
}
