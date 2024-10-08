/*
 * Software Name : libits
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 * Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) library based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
 */

use crate::mobility::position::Position;

/// Describes a mobile at a moment in time
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
}
