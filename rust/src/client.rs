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

/// Structures, functions and traits in this mod are made to create applications that analyze
/// messages to either send new messages (e.g. creating DENM on odd behaviour from CAMs)
/// or to create/store data (e.g. counting pedestrian, vehicles, etc. in a specific area)
#[cfg(feature = "mobility")]
pub mod application;
pub mod configuration;
