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

use crate::mobility::position::Position;
use serde::{Deserialize, Serialize};

const COORDINATE_SIGNIFICANT_DIGIT: u8 = 7;
const ALTITUDE_SIGNIFICANT_DIGIT: u8 = 2;

/// Represents a reference position with confidence information
#[derive(Clone, Default, Debug, Eq, Hash, PartialEq, Serialize, Deserialize)]
pub struct ReferencePosition {
    #[serde(default = "default_latitude")]
    pub latitude: i32, // -900000000..900000001
    #[serde(default = "default_longitude")]
    pub longitude: i32, // -1800000000..1800000001

    #[serde(skip_serializing_if = "Option::is_none")]
    pub altitude: Option<Altitude>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub confidence: Option<PositionConfidence>,
}

fn default_latitude() -> i32 {
    900000001
}
fn default_longitude() -> i32 {
    1800000001
}

#[derive(Clone, Debug, Default, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct Altitude {
    #[serde(default = "default_altitude")]
    pub value: i32, // -100000..800001
    #[serde(skip_serializing_if = "Option::is_none")]
    pub confidence: Option<u8>, // 0..15
}

fn default_altitude() -> i32 {
    800001
}

/// Represents the position confidence in a reference position
#[derive(Clone, Debug, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct PositionConfidence {
    #[serde(default = "default_semi_major_confidence")]
    pub semi_major_confidence: u16, // 0..4095, default 4095
    #[serde(default = "default_semi_minor_confidence")]
    pub semi_minor_confidence: u16, // 0..4095, default 4095
    #[serde(default = "default_semi_major_orientation")]
    pub semi_major_orientation: u16, // 0..3601, default 3601
}

fn default_semi_major_confidence() -> u16 {
    4095
}
fn default_semi_minor_confidence() -> u16 {
    4095
}
fn default_semi_major_orientation() -> u16 {
    3601
}

impl ReferencePosition {
    pub fn as_position(&self) -> Position {
        Position {
            latitude: coordinate_from_etsi(self.latitude),
            longitude: coordinate_from_etsi(self.longitude),
            altitude: altitude_from_etsi(self.altitude.as_ref().map(|a| a.value).unwrap_or(0)),
        }
    }
}

impl From<Position> for ReferencePosition {
    fn from(position: Position) -> Self {
        ReferencePosition {
            latitude: coordinate_to_etsi(position.latitude),
            longitude: coordinate_to_etsi(position.longitude),
            altitude: Some(Altitude {
                value: altitude_to_etsi(position.altitude),
                ..Default::default()
            }),
            ..Default::default()
        }
    }
}

impl fmt::Display for ReferencePosition {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let altitude = self.altitude.as_ref().unwrap();
        write!(
            f,
            "(lat: {} / lon: {} / alt: {}(prec. {:?}) / conf: {:?})",
            self.latitude, self.longitude, altitude.value, altitude.confidence, self.confidence
        )
    }
}

/// Converts a coordinate from tenths of microdegree to radians
pub(crate) fn coordinate_from_etsi(microdegree_tenths: i32) -> f64 {
    let degrees =
        f64::from(microdegree_tenths) / 10f64.powf(f64::from(COORDINATE_SIGNIFICANT_DIGIT));
    degrees.to_radians()
}

/// Converts a coordinate from radians to tenths of microdegree
pub(crate) fn coordinate_to_etsi(radians: f64) -> i32 {
    let degrees = radians.to_degrees();
    (degrees * f64::from(10i32.pow(u32::from(COORDINATE_SIGNIFICANT_DIGIT)))) as i32
}

/// Converts altitude from centimeters to meters
pub(crate) fn altitude_from_etsi(centimeters: i32) -> f64 {
    f64::from(centimeters) / 10f64.powf(f64::from(ALTITUDE_SIGNIFICANT_DIGIT))
}

/// Converts altitude from meters to centimeters
pub(crate) fn altitude_to_etsi(meters: f64) -> i32 {
    (meters * 10_f64.powf(f64::from(ALTITUDE_SIGNIFICANT_DIGIT))) as i32
}

#[derive(Clone, Default, Debug, Eq, Hash, PartialEq, Serialize, Deserialize)]
pub struct ReferencePosition113 {
    #[serde(default = "default_latitude")]
    pub latitude: i32, // -900000000..900000001
    #[serde(default = "default_longitude")]
    pub longitude: i32, // -1800000000..1800000001
    #[serde(default = "default_altitude")]
    pub altitude: i32, // -100000..800001
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

impl ReferencePosition113 {
    pub fn as_position(&self) -> Position {
        Position {
            latitude: coordinate_from_etsi(self.latitude),
            longitude: coordinate_from_etsi(self.longitude),
            altitude: altitude_from_etsi(self.altitude),
        }
    }
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct DeltaReferencePosition {
    pub delta_latitude: i32,
    pub delta_longitude: i32,
    pub delta_altitude: i32,
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::reference_position::{
        Altitude, ReferencePosition, altitude_from_etsi, altitude_to_etsi, coordinate_from_etsi,
        coordinate_to_etsi,
    };
    use crate::mobility::position::Position;

    #[test]
    fn coordinates_from_etsi() {
        let latitude: i32 = 488417860;
        let longitude: i32 = 23678940;
        let expected_latitude = 48.8417860_f64.to_radians();
        let expected_longitude = 2.3678940_f64.to_radians();

        let latitude_as_radians = coordinate_from_etsi(latitude);
        let longitude_as_radians = coordinate_from_etsi(longitude);

        assert!((latitude_as_radians - expected_latitude).abs() <= 1e-11);
        assert!((longitude_as_radians - expected_longitude).abs() <= 1e-11);
    }

    #[test]
    fn coordinates_to_etsi() {
        let latitude = 48.8417860_f64.to_radians();
        let longitude = 2.3678940_f64.to_radians();
        let expected_latitude: i32 = 488417860;
        let expected_longitude: i32 = 23678940;

        let latitude_as_etsi = coordinate_to_etsi(latitude);
        let longitude_as_etsi = coordinate_to_etsi(longitude);

        assert_eq!(latitude_as_etsi, expected_latitude);
        assert_eq!(longitude_as_etsi, expected_longitude);
    }

    #[test]
    fn altitude_from_etsi_to_si() {
        let altitude: i32 = 16880;
        let expected_altitude: f64 = 168.80;

        let altitude_in_meters = altitude_from_etsi(altitude);

        assert!(
            (altitude_in_meters - expected_altitude).abs() <= 1e-11,
            "Current: {} (Expected: {})",
            altitude_in_meters,
            expected_altitude,
        );
    }

    #[test]
    fn altitude_from_si_to_etsi() {
        let altitude: f64 = 168.80;
        let expected_altitude: i32 = 16880;

        let altitude_in_centimeters = altitude_to_etsi(altitude);

        assert_eq!(altitude_in_centimeters, expected_altitude);
    }

    #[test]
    fn reference_position_as_position() {
        let reference_position = ReferencePosition {
            latitude: 488417860,
            longitude: 23678940,
            altitude: Some(Altitude {
                value: 16880,
                ..Default::default()
            }),
            ..Default::default()
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
    fn reference_position_from_position() {
        let position = Position {
            latitude: 48.8417860_f64.to_radians(),
            longitude: 2.3678940_f64.to_radians(),
            altitude: 168.80,
        };
        let expected_reference_position = ReferencePosition {
            latitude: 488417860,
            longitude: 23678940,
            altitude: Some(Altitude {
                value: 16880,
                ..Default::default()
            }),
            ..Default::default()
        };

        let reference_position = ReferencePosition::from(position);

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
