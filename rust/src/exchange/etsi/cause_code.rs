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

/// Represents a CauseCode V1 according to an ETSI standard.
///
/// This message is used to describe a cause code.
/// It implements the schema defined in the [DENM version 2.2.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/denm/denm_schema_2-2-0.json#L538
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CauseCode {
    /// The cause code, represented as an unsigned 8-bit integer.
    // TODO create a CauseCode enum to represent the cause codes
    pub cause: u8,
    /// The subcause code, represented as an optional unsigned 8-bit integer.
    pub subcause: Option<u8>,
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::cause_code::CauseCode;

    #[test]
    fn test_deserialize_cause_code() {
        let data = r#"{
            "cause": 100,
            "subcause": 255
        }"#;

        match serde_json::from_str::<CauseCode>(data) {
            Ok(object) => {
                assert_eq!(object.cause, 100);
                assert_eq!(object.subcause, Some(255));
            }
            Err(e) => panic!("Failed to deserialize a CauseCode: '{e}'"),
        }
    }
}
