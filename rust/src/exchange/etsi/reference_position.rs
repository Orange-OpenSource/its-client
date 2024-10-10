/*
 * Software Name : libits
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 * Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) library based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
 */

use core::fmt;

use crate::mobility::position::Position;
use serde::{Deserialize, Serialize};

const COORDINATE_SIGNIFICANT_DIGIT: u8 = 7;
const ALTITUDE_SIGNIFICANT_DIGIT: u8 = 2;

#[derive(Clone, Default, Debug, Eq, Hash, PartialEq, Serialize, Deserialize)]
pub struct ReferencePosition {
    /// Latitude in tenths of microdegree
    pub latitude: i32,
    /// Longitude in tenths of microdegree
    pub longitude: i32,
    /// Altitude in centimeters
    pub altitude: i32,
}

impl ReferencePosition {
    pub fn as_position(&self) -> Position {
        Position {
            latitude: coordinate_from_etsi(self.latitude),
            longitude: coordinate_from_etsi(self.longitude),
            altitude: altitude_from_etsi(self.altitude),
        }
    }
}

impl From<Position> for ReferencePosition {
    fn from(position: Position) -> Self {
        ReferencePosition {
            latitude: coordinate_to_etsi(position.latitude),
            longitude: coordinate_to_etsi(position.longitude),
            altitude: altitude_to_etsi(position.altitude),
        }
    }
}

impl fmt::Display for ReferencePosition {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "(lat: {} / lon: {} / alt: {})",
            self.latitude, self.longitude, self.altitude,
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
fn coordinate_to_etsi(radians: f64) -> i32 {
    let degrees = radians.to_degrees();
    (degrees * f64::from(10i32.pow(u32::from(COORDINATE_SIGNIFICANT_DIGIT)))) as i32
}

/// Converts altitude from centimeters to meters
pub(crate) fn altitude_from_etsi(centimeters: i32) -> f64 {
    f64::from(centimeters) / 10f64.powf(f64::from(ALTITUDE_SIGNIFICANT_DIGIT))
}

/// Converts altitude from meters to centimeters
fn altitude_to_etsi(meters: f64) -> i32 {
    (meters * 10_f64.powf(f64::from(ALTITUDE_SIGNIFICANT_DIGIT))) as i32
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::reference_position::{
        altitude_from_etsi, altitude_to_etsi, coordinate_from_etsi, coordinate_to_etsi,
        ReferencePosition,
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
    fn reference_position_from_position() {
        let position = Position {
            latitude: 48.8417860_f64.to_radians(),
            longitude: 2.3678940_f64.to_radians(),
            altitude: 168.80,
        };
        let expected_reference_position = ReferencePosition {
            latitude: 488417860,
            longitude: 23678940,
            altitude: 16880,
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
