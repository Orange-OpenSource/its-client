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

/// Represents a Speed according to an ETSI standard.
///
/// This message is used to describe a speed.
/// It implements the schema defined in the CAM version 2.2.0.
///
/// # Fields
///
/// - `value`: The heading.
/// - `confidence`: The confidence of the heading.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Heading {
    /// Heading value in centidegrees.
    pub value: u16,
    /// Confidence level for the heading.
    pub confidence: u8,
}
