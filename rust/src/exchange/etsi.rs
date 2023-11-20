// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2023 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas BUFFON <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use serde::{Deserialize, Serialize};

pub mod cooperative_awareness_message;
pub mod reference_position;

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

/// Converts heading from decidegrees to radians
fn heading_from_etsi(decidegrees: u16) -> f64 {
    (f64::from(decidegrees) / 10.).to_radians()
}

/// Converts heading from radians to decidegrees
fn heading_to_etsi(radians: f64) -> u16 {
    ((radians.to_degrees() * 10_f64) % 3600.) as u16
}

/// Converts speed from cm/s to m/s
fn speed_from_etsi(cm_per_sec: u16) -> f64 {
    f64::from(cm_per_sec) / 100.
}

/// Converts speed from m/s to cm/s
fn speed_to_etsi(meters_per_sec: f64) -> u16 {
    (meters_per_sec * 100.) as u16
}

/// Converts acceleration from dm/s² to m/s²
fn acceleration_from_etsi(dm_per_sec_2: i16) -> f64 {
    f64::from(dm_per_sec_2) / 10.
}

/// Converts acceleration from m/s² to dm/s²
fn acceleration_to_etsi(m_per_s_2: f64) -> i16 {
    (m_per_s_2 * 10.) as i16
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::{
        acceleration_from_etsi, acceleration_to_etsi, heading_from_etsi, heading_to_etsi,
        speed_from_etsi, speed_to_etsi,
    };
    use std::f64::consts::PI;

    macro_rules! test_from_etsi {
        ($func:ident, $test_name:ident, $value:expr, $expected:expr) => {
            #[test]
            fn $test_name() {
                let epsilon = 1e-11;

                let as_si = $func($value);
                let delta = (as_si - $expected).abs();

                assert!(
                    delta < epsilon,
                    "Actual: {} (expected: {})",
                    as_si,
                    $expected
                );
            }
        };
    }
    test_from_etsi!(heading_from_etsi, north_0_deg_heading_from_etsi, 0, 0_f64);
    test_from_etsi!(
        heading_from_etsi,
        north_3600_deg_heading_from_etsi,
        3600,
        2. * PI
    );
    test_from_etsi!(heading_from_etsi, east_heading_from_etsi, 900, PI / 2.);
    test_from_etsi!(
        heading_from_etsi,
        west_heading_from_etsi,
        2700,
        3. * PI / 2.
    );
    test_from_etsi!(heading_from_etsi, south_heading_from_etsi, 1800, PI);
    test_from_etsi!(speed_from_etsi, nul_speed_from_etsi, 0, 0.);
    test_from_etsi!(speed_from_etsi, non_nul_speed_from_etsi, 2753, 27.53);
    test_from_etsi!(speed_from_etsi, max_speed_from_etsi, u16::MAX, 655.35);
    test_from_etsi!(acceleration_from_etsi, nul_acceleration_from_etsi, 0, 0.);
    test_from_etsi!(
        acceleration_from_etsi,
        negative_acceleration_from_etsi,
        -100,
        -10.
    );
    test_from_etsi!(
        acceleration_from_etsi,
        positive_acceleration_from_etsi,
        123,
        12.3_f64
    );
    test_from_etsi!(
        acceleration_from_etsi,
        min_acceleration_from_etsi,
        -160,
        -16_f64
    );
    test_from_etsi!(
        acceleration_from_etsi,
        max_acceleration_from_etsi,
        161,
        16.1_f64
    );

    macro_rules! test_to_etsi {
        ($func:ident, $test_name:ident, $value:expr, $expected:expr) => {
            #[test]
            fn $test_name() {
                let as_etsi = $func($value);

                assert_eq!(as_etsi, $expected);
            }
        };
    }
    test_to_etsi!(heading_to_etsi, north_0_deg_heading_to_etsi, 0_f64, 0);
    test_to_etsi!(heading_to_etsi, north_3600_deg_heading_to_etsi, 2. * PI, 0);
    test_to_etsi!(heading_to_etsi, east_heading_to_etsi, PI / 2., 900);
    test_to_etsi!(heading_to_etsi, west_heading_to_etsi, 3. * PI / 2., 2700);
    test_to_etsi!(heading_to_etsi, south_heading_to_etsi, PI, 1800);
    test_to_etsi!(speed_to_etsi, nul_speed_to_etsi, 0., 0);
    test_to_etsi!(speed_to_etsi, non_nul_speed_to_etsi, 27.53, 2753);
    test_to_etsi!(speed_to_etsi, extra_decimal_speed_to_etsi, 34.123456, 3412);
    test_to_etsi!(speed_to_etsi, max_speed_to_etsi, 655.35, u16::MAX);
    test_to_etsi!(acceleration_to_etsi, nul_acceleration_to_etsi, 0., 0);
    test_to_etsi!(
        acceleration_to_etsi,
        negative_acceleration_to_etsi,
        -10_f64,
        -100
    );
    test_to_etsi!(
        acceleration_to_etsi,
        positive_acceleration_to_etsi,
        12.3_f64,
        123
    );
    test_to_etsi!(
        acceleration_to_etsi,
        min_acceleration_to_etsi,
        -16_f64,
        -160
    );
    test_to_etsi!(
        acceleration_to_etsi,
        max_acceleration_to_etsi,
        16.1_f64,
        161
    );
}
