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

/// Represents an Angle according to an ETSI standard.
///
/// This message is used to describe an angle.
/// It implements the schema defined in the [CPM version 2.1.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cpm/cpm_schema_2-1-0.json#L946
#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Angle {
    /// Angle value.
    pub value: u16,
    /// Confidence level for the angle.
    #[serde(
        serialize_with = "confidence::serialize::<_, _, 1, 127>",
        deserialize_with = "confidence::deserialize::<_, _, 1, 127>"
    )]
    pub confidence: u8,
}

impl Default for Angle {
    fn default() -> Self {
        Self {
            value: 0,
            confidence: 127,
        }
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::angle::Angle;

    #[test]
    fn test_deserialize_angle() {
        let data = r#"{
            "value": 3601,
            "confidence": 127
        }"#;

        match serde_json::from_str::<Angle>(data) {
            Ok(object) => {
                assert_eq!(object.value, 3601);
                assert_eq!(object.confidence, 127);
            }
            Err(e) => panic!("Failed to deserialize an Angle: '{e}'"),
        }
    }
}
