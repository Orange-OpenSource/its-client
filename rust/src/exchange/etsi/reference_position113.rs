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

use core::fmt;

use crate::exchange::etsi::reference_position::{
    altitude_from_etsi, altitude_to_etsi, coordinate_from_etsi, coordinate_to_etsi,
};
use crate::mobility::position::Position;
use serde::{Deserialize, Serialize};

/// Represents a Reference Position according to an ETSI standard.
///
/// This message is used to describe a position.
/// It implements the schema defined in the [CAM version 1.1.3][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cam/cam_schema_1-1-3.json
#[derive(Clone, Default, Debug, Eq, Hash, PartialEq, Serialize, Deserialize)]
pub struct ReferencePosition113 {
    /// Latitude in ETSI format (1/10^7 degrees).
    #[serde(default = "default_latitude")]
    pub latitude: i32,
    /// Longitude in ETSI format (1/10^7 degrees).
    #[serde(default = "default_longitude")]
    pub longitude: i32,
    /// Altitude in ETSI format (1/1000 meters).
    #[serde(default = "default_altitude")]
    pub altitude: i32,
}

fn default_latitude() -> i32 {
    900000001
}
fn default_longitude() -> i32 {
    1800000001
}
fn default_altitude() -> i32 {
    800001
}

impl ReferencePosition113 {
    pub fn as_position(&self) -> Position {
        Position {
            latitude: coordinate_from_etsi(self.latitude),
            longitude: coordinate_from_etsi(self.longitude),
            altitude: altitude_from_etsi(self.altitude),
        }
    }
}

impl From<Position> for ReferencePosition113 {
    fn from(position: Position) -> Self {
        ReferencePosition113 {
            latitude: coordinate_to_etsi(position.latitude),
            longitude: coordinate_to_etsi(position.longitude),
            altitude: altitude_to_etsi(position.altitude),
        }
    }
}

impl fmt::Display for ReferencePosition113 {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "(lat: {} / lon: {} / alt: {})",
            self.latitude, self.longitude, self.altitude,
        )
    }
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PositionConfidence {
    pub position_confidence_ellipse: Option<PositionConfidenceEllipse>,
    pub altitude: Option<u8>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PositionConfidenceEllipse {
    pub semi_major_confidence: Option<u16>,
    pub semi_minor_confidence: Option<u16>,
    pub semi_major_orientation: Option<u16>,
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::reference_position113::ReferencePosition113;
    use crate::mobility::position::Position;

    #[test]
    fn reference_position_113_as_position() {
        let reference_position = ReferencePosition113 {
            latitude: 488417860,
            longitude: 23678940,
            altitude: 16880,
        };
        let expected_position = Position {
            latitude: 48.8417860_f64.to_radians(),
            longitude: 2.3678940_f64.to_radians(),
            altitude: 168.80,
        };

        let position = reference_position.as_position();

        assert!(
            (position.latitude - expected_position.latitude).abs() <= 1e-11,
            "Actual latitude: {} (Expected: {}",
            position.latitude,
            expected_position.latitude,
        );
        assert!(
            (position.longitude - expected_position.longitude).abs() <= 1e-11,
            "Actual longitude: {} (Expected: {}",
            position.longitude,
            expected_position.longitude,
        );
        assert!(
            (position.altitude - expected_position.altitude).abs() <= 1e-11,
            "Actual altitude: {} (Expected: {}",
            position.altitude,
            expected_position.altitude,
        );
    }

    #[test]
    fn reference_position_113_from_position() {
        let position = Position {
            latitude: 48.8417860_f64.to_radians(),
            longitude: 2.3678940_f64.to_radians(),
            altitude: 168.80,
        };
        let expected_reference_position = ReferencePosition113 {
            latitude: 488417860,
            longitude: 23678940,
            altitude: 16880,
        };

        let reference_position = ReferencePosition113::from(position);

        assert_eq!(
            reference_position.latitude,
            expected_reference_position.latitude
        );
        assert_eq!(
            reference_position.longitude,
            expected_reference_position.longitude
        );
        assert_eq!(
            reference_position.altitude,
            expected_reference_position.altitude
        );
    }
}
