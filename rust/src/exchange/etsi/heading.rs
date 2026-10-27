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

/// Represents a Speed according to an ETSI standard.
///
/// This message is used to describe a speed.
/// It implements the schema defined in the [CAM version 2.2.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cam/cam_schema_2-2-0.json#L859
#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Heading {
    /// Heading value in centidegrees.
    pub value: u16,
    /// Confidence level for the heading.
    #[serde(
        serialize_with = "confidence::serialize::<_, _, 1, 127>",
        deserialize_with = "confidence::deserialize::<_, _, 1, 127>"
    )]
    pub confidence: u8,
}

impl Default for Heading {
    fn default() -> Self {
        Self {
            value: 0,
            confidence: 127,
        }
    }
}
