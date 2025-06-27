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

/// Represents a LongitudinalLanePosition according to an ETSI standard.
///
/// This message is used to describe a longitudinal lane position.
/// It implements the schema defined in the [CPM version 2.1.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cpm/cpm_schema_2-1-0.json#L1441
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct LongitudinalLanePosition {
    /// Longitudinal lane position value.
    pub value: u16,
    /// Confidence level for the longitudinal lane position.
    pub confidence: u16,
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::longitudinal_lane_position::LongitudinalLanePosition;

    #[test]
    fn test_deserialize_longitudinal_lane_position() {
        let data = r#"{
            "value": 32767,
            "confidence": 1023
        }"#;

        match serde_json::from_str::<LongitudinalLanePosition>(data) {
            Ok(object) => {
                assert_eq!(object.value, 32767);
                assert_eq!(object.confidence, 1023);
            }
            Err(e) => panic!("Failed to deserialize a LongitudinalLanePosition: '{}'", e),
        }
    }
}
