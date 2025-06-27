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

/// Represents a CartesianCoordinate according to an ETSI standard.
///
/// This message is used to describe a coordinate.
/// It implements the schema defined in the [CPM version 2.1.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cpm/cpm_schema_2-1-0.json#L1024
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Copy, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CartesianCoordinate {
    /// coordinate value.
    pub value: i32,
    /// Confidence level for the coordinate.
    pub confidence: u16,
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::coordinate::CartesianCoordinate;

    #[test]
    fn test_deserialize_angle() {
        let data = r#"{
            "value": 131071,
            "confidence": 4096
        }"#;

        match serde_json::from_str::<CartesianCoordinate>(data) {
            Ok(object) => {
                assert_eq!(object.value, 131071);
                assert_eq!(object.confidence, 4096);
            }
            Err(e) => panic!("Failed to deserialize a CartesianCoordinate: '{}'", e),
        }
    }
}
