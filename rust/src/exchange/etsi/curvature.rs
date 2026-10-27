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

use super::confidence;
use serde::{Deserialize, Serialize};

/// Represents a Curvature according to an ETSI standard.
///
/// This message is used to describe curvature.
/// It implements the schema defined in the [CAM version 2.2.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cam/cam_schema_2-2-0.json#L955
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Curvature {
    /// Curvature value.
    pub value: i16,
    /// Confidence level for the curvature.
    pub confidence: CurvatureConfidence,
}

#[derive(Debug, Default, Clone, Copy, Hash, PartialEq, Eq, Serialize)]
#[serde(into = "u8")]
pub enum CurvatureConfidence {
    /// Curvature confidence levels.
    OnePerMeter0_00002 = 0,
    OnePerMeter0_0001 = 1,
    OnePerMeter0_0005 = 2,
    OnePerMeter0_002 = 3,
    OnePerMeter0_01 = 4,
    OnePerMeter0_1 = 5,
    OutOfRange = 6,
    #[default]
    Unavailable = 7,
}

impl<'de> Deserialize<'de> for CurvatureConfidence {
    fn deserialize<D: serde::Deserializer<'de>>(deserializer: D) -> Result<Self, D::Error> {
        confidence::deserialize::<u8, _, 0, 7>(deserializer).map(Self::from)
    }
}

impl From<CurvatureConfidence> for u8 {
    fn from(value: CurvatureConfidence) -> Self {
        value as u8
    }
}

impl From<u8> for CurvatureConfidence {
    fn from(value: u8) -> Self {
        match value {
            0 => CurvatureConfidence::OnePerMeter0_00002,
            1 => CurvatureConfidence::OnePerMeter0_0001,
            2 => CurvatureConfidence::OnePerMeter0_0005,
            3 => CurvatureConfidence::OnePerMeter0_002,
            4 => CurvatureConfidence::OnePerMeter0_01,
            5 => CurvatureConfidence::OnePerMeter0_1,
            6 => CurvatureConfidence::OutOfRange,
            _ => CurvatureConfidence::Unavailable,
        }
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::curvature::{Curvature, CurvatureConfidence};

    #[test]
    fn test_deserialize_curvature() {
        let data = r#"{
            "value": 1023,
            "confidence": 7
        }"#;

        match serde_json::from_str::<Curvature>(data) {
            Ok(object) => {
                assert_eq!(object.value, 1023);
                assert_eq!(object.confidence, CurvatureConfidence::Unavailable);
            }
            Err(e) => panic!("Failed to deserialize a Curvature: '{e}'"),
        }
    }
}
