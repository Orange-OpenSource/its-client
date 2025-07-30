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

/// Represents a Reference Position according to an ETSI standard.
///
/// This message is used to describe a position.
/// It implements the schema defined in the [CAM version 2.2.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cam/cam_schema_2-2-0.json#L828
#[derive(Clone, Default, Debug, Eq, Hash, PartialEq, Serialize, Deserialize)]
pub struct ReferencePosition {
    /// Latitude in tenths of microdegrees.
    #[serde(default = "default_latitude")]
    pub latitude: i32,
    /// Longitude in tenths of microdegrees.
    #[serde(default = "default_longitude")]
    pub longitude: i32,
    /// Altitude in centimeters.
    pub altitude: Altitude,
    /// Confidence ellipse for the position.
    pub position_confidence_ellipse: PositionConfidenceEllipse,
}

#[derive(Clone, Debug, Default, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct Altitude {
    /// Altitude value in centimeters.
    #[serde(default = "default_altitude")]
    pub value: i32,
    /// Confidence level for the altitude.
    #[serde(default = "default_confidence")]
    pub confidence: u8,
}

/// Represents the position confidence in a reference position
#[derive(Clone, Debug, Default, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct PositionConfidenceEllipse {
    /// Semi-major axis of the ellipse in centimeters.
    #[serde(default = "default_semi_major")]
    pub semi_major: u16,
    /// Semi-minor axis of the ellipse in centimeters.
    #[serde(default = "default_semi_minor")]
    pub semi_minor: u16,
    /// Orientation of the semi-major axis in centidegrees.
    #[serde(default = "default_semi_major_orientation")]
    pub semi_major_orientation: u16,
}

#[derive(Clone, Default, Debug, Eq, Hash, PartialEq, Serialize, Deserialize)]
pub struct DeltaReferencePosition {
    /// Delta latitude in tenths of microdegrees.
    #[serde(default = "default_delta_latitude")]
    pub delta_latitude: i32,
    /// Delta longitude in tenths of microdegrees.
    #[serde(default = "default_delta_longitude")]
    pub delta_longitude: i32,
    /// Delta altitude in centimeters.
    #[serde(default = "default_delta_altitude")]
    pub delta_altitude: i16,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PathPoint {
    /// Reference position of the path point.
    pub path_position: DeltaReferencePosition,

    // optional fields
    /// Delata time in milliseconds since the last path point.
    pub path_delta_time: Option<u16>,
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

fn default_latitude() -> i32 {
    900000001
}
fn default_longitude() -> i32 {
    1800000001
}

fn default_altitude() -> i32 {
    800001
}

fn default_confidence() -> u8 {
    15
}

fn default_semi_major() -> u16 {
    4095
}
fn default_semi_minor() -> u16 {
    4095
}
fn default_semi_major_orientation() -> u16 {
    3601
}

fn default_delta_latitude() -> i32 {
    131072
}

fn default_delta_longitude() -> i32 {
    default_delta_latitude()
}

fn default_delta_altitude() -> i16 {
    12800
}

impl ReferencePosition {
    pub fn as_position(&self) -> Position {
        Position {
            latitude: coordinate_from_etsi(self.latitude),
            longitude: coordinate_from_etsi(self.longitude),
            altitude: altitude_from_etsi(self.altitude.value),
        }
    }
}

impl From<Position> for ReferencePosition {
    fn from(position: Position) -> Self {
        ReferencePosition {
            latitude: coordinate_to_etsi(position.latitude),
            longitude: coordinate_to_etsi(position.longitude),
            altitude: Altitude {
                value: altitude_to_etsi(position.altitude),
                ..Default::default()
            },
            ..Default::default()
        }
    }
}

impl fmt::Display for ReferencePosition {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "(lat: {} / lon: {} / alt: {}(prec. {:?}) / conf: {:?})",
            self.latitude,
            self.longitude,
            self.altitude.value,
            self.altitude.confidence,
            self.position_confidence_ellipse
        )
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::reference_position::{
        Altitude, DeltaReferencePosition, PositionConfidenceEllipse, ReferencePosition,
        altitude_from_etsi, altitude_to_etsi, coordinate_from_etsi, coordinate_to_etsi,
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
    fn coordinates_from_etsi_accurate() {
        use std::f64::consts::PI;

        let test_cases = vec![
            (0, 0.0),
            (10000000, PI / 180.0),
            (-10000000, -PI / 180.0),
            (900000000, PI / 2.0),
            (-900000000, -PI / 2.0),
            (1800000000, PI),
            (-1800000000, -PI),
            (450000000, PI / 4.0),
            (-450000000, -PI / 4.0),
            (1350000000, 3.0 * PI / 4.0),
            (-1350000000, -3.0 * PI / 4.0),
        ];

        for (microdegree_tenths, expected_radians) in test_cases {
            let radians = coordinate_from_etsi(microdegree_tenths);
            assert!(
                (radians - expected_radians).abs() <= 1e-9,
                "Expected {expected_radians} radians, but got {radians} from {microdegree_tenths} microdegree_tenths"
            );
        }
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
            "Current: {altitude_in_meters} (Expected: {expected_altitude})"
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
            altitude: Altitude {
                value: 16880,
                ..Default::default()
            },
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
            altitude: Altitude {
                value: 16880,
                ..Default::default()
            },
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

    #[test]
    fn test_deserialize_pos_confidence_ellipse() {
        let data = r#"{
            "semi_major": 4095,
            "semi_minor": 0,
            "semi_major_orientation": 3601
        }"#;

        match serde_json::from_str::<PositionConfidenceEllipse>(data) {
            Ok(position) => {
                assert_eq!(position.semi_major, 4095);
                assert_eq!(position.semi_minor, 0);
                assert_eq!(position.semi_major_orientation, 3601);
            }
            Err(e) => panic!("Failed to deserialize a PositionConfidenceEllipse: '{e}'"),
        }
    }

    #[test]
    fn test_deserialize_altitude() {
        let data = r#"{
            "value": 800001,
            "confidence": 15
        }"#;

        match serde_json::from_str::<Altitude>(data) {
            Ok(object) => {
                assert_eq!(object.value, 800001);
                assert_eq!(object.confidence, 15);
            }
            Err(e) => panic!("Failed to deserialize an Altitude: '{e}'"),
        }
    }

    #[test]
    fn test_deserialize_reference_position() {
        let data = r#"{
            "latitude": 900000001,
            "longitude": -1800000000,
            "position_confidence_ellipse": {
                "semi_major": 4095,
                "semi_minor": 0,
                "semi_major_orientation": 3601
            },
            "altitude": {
                "value": 800001,
                "confidence": 15
            }
        }"#;

        match serde_json::from_str::<ReferencePosition>(data) {
            Ok(object) => {
                assert_eq!(object.latitude, 900000001);
                assert_eq!(object.longitude, -1800000000);
                assert_eq!(
                    object.position_confidence_ellipse,
                    PositionConfidenceEllipse {
                        semi_major: 4095,
                        semi_minor: 0,
                        semi_major_orientation: 3601,
                    }
                );
                assert_eq!(
                    object.altitude,
                    Altitude {
                        value: 800001,
                        confidence: 15,
                    }
                );
            }
            Err(e) => panic!("Failed to deserialize a ReferencePosition: '{e}'"),
        }
    }

    #[test]
    fn test_deserialize_delta_reference_position() {
        let data = r#"{
            "delta_latitude": 131072,
            "delta_longitude": -131071,
            "delta_altitude": 12800
        }"#;

        match serde_json::from_str::<DeltaReferencePosition>(data) {
            Ok(object) => {
                assert_eq!(object.delta_latitude, 131072);
                assert_eq!(object.delta_longitude, -131071);
                assert_eq!(object.delta_altitude, 12800);
            }
            Err(e) => panic!("Failed to deserialize a DeltaReferencePosition: '{e}'"),
        }
    }
}
