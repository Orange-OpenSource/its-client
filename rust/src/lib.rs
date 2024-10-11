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

use std::time::{SystemTime, UNIX_EPOCH};

pub mod client;
#[cfg(feature = "mobility")]
pub mod exchange;
#[cfg(feature = "mobility")]
pub mod mobility;
#[cfg(feature = "mobility")]
pub(crate) mod monitor;
pub mod transport;

pub fn now() -> u64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_millis() as u64
}
