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

/// Represents an ObjectDimension according to an ETSI standard.
///
/// This message is used to describe an object dimension.
/// It implements the schema defined in the [CPM version 2.1.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cpm/cpm_schema_2-1-0.json#L1395
#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct ObjectDimension {
    /// Object dimension value.
    pub value: u16,
    /// Confidence level for the object dimension.
    #[serde(
        serialize_with = "confidence::serialize::<_, _, 1, 32>",
        deserialize_with = "confidence::deserialize::<_, _, 1, 32>"
    )]
    pub confidence: u8,
}

impl Default for ObjectDimension {
    fn default() -> Self {
        Self {
            value: 0,
            confidence: 32,
        }
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::object_dimension::ObjectDimension;

    #[test]
    fn test_deserialize_object_dimension() {
        let data = r#"{
            "value": 256,
            "confidence": 32
        }"#;

        match serde_json::from_str::<ObjectDimension>(data) {
            Ok(object) => {
                assert_eq!(object.value, 256);
                assert_eq!(object.confidence, 32);
            }
            Err(e) => panic!("Failed to deserialize an ObjectDimension: '{e}'"),
        }
    }
}
