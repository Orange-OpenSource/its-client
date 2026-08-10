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

use crate::mobility::position::Position;

/// Describes a mobile at a position at a moment in time
///
/// All attributes are expressed in SI units
pub trait Mobile {
    fn id(&self) -> u32;

    /// Returns the mobile's position
    fn position(&self) -> Position;

    /// Returns the mobile's speed in m/s
    fn speed(&self) -> Option<f64>;

    /// Returns the mobile's heading in radians
    fn heading(&self) -> Option<f64>;

    /// Returns ths mobile's acceleration in m/s²
    fn acceleration(&self) -> Option<f64>;

    /// Returns the mean position confidence in centimeters.
    ///
    /// Computed from the confidence ellipse of the reference position.
    fn position_confidence(&self) -> f64;
}
