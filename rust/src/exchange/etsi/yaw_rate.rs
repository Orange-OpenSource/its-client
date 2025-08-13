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

/// Represents a Yaw Rate according to an ETSI standard.
///
/// This message is used to describe a yaw rate.
/// It implements the schema defined in the [CAM version 2.2.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cam/cam_schema_2-2-0.json#L978
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct YawRate {
    /// Yaw rate value.
    pub value: i16,
    /// Confidence level for the yaw rate.
    pub confidence: u8,
}
