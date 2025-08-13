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
/// It implements the schema defined in the [CAM version 2.2.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cam/cam_schema_2-2-0.json#L931
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Acceleration {
    /// Acceleration value in centimeters per second squared.
    pub value: i16,
    /// Confidence level for the acceleration.
    pub confidence: u8,
}

/// Represents an Acceleration Magnitude according to an ETSI standard.
/// It's an acceleration vector in a polar or cylindrical coordinate system.
/// This message is used to describe an acceleration magnitude.
/// It implements the schema defined in the [CAM version 2.2.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cam/cam_schema_2-2-0.json#L422
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct AccelerationMagnitude {
    /// Acceleration magnitude value.
    pub value: u8,
    /// Confidence level for the acceleration magnitude.
    pub confidence: u8,
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::acceleration::{Acceleration, AccelerationMagnitude};

    #[test]
    fn test_deserialize_acceleration() {
        let data = r#"{
            "value": 161,
            "confidence": 102
        }"#;

        match serde_json::from_str::<Acceleration>(data) {
            Ok(object) => {
                assert_eq!(object.value, 161);
                assert_eq!(object.confidence, 102);
            }
            Err(e) => panic!("Failed to deserialize an Acceleration: '{e}'"),
        }
    }

    #[test]
    fn test_deserialize_acceleration_magnitude() {
        let data = r#"{
            "value": 161,
            "confidence": 102
        }"#;

        match serde_json::from_str::<AccelerationMagnitude>(data) {
            Ok(object) => {
                assert_eq!(object.value, 161);
                assert_eq!(object.confidence, 102);
            }
            Err(e) => panic!("Failed to deserialize an Acceleration Magnitude: '{e}'"),
        }
    }
}
