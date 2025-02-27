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

//! Generic IoT3 messaging and telemetry for V2X
//!
//! MQTT
//! ----
//!
//! Generic MQTT exchanges can be managed using both [MqttClient][1] to publish and subscribe and
//! [MqttRouter][2] to handle messages using different callbacks depending the [Topic][3] a message is
//! received on.
//!
//! If the `telemetry` feature is enabled:
//! - message reception and publishing will be traced[^1]
//! - the [W3C Context][4] will be injected as an MQTTv5 user property before publishing a message
//!
//! Telemetry
//! ---------
//!
//! **Available with the `telemetry` feature**
//!
//! Provides functions to send traces to an OpenTelemetry collector
//!
//! V2X
//! ---
//!
//! **Available with the `mobility` feature**
//!
//! Provides traits and functions to create applications that will listen to MQTT messages
//!
//! ### Geo routing
//!
//! **Available with the `geo_routing` feature**
//!
//! Provides topic management using queue system and quadkey positioning
//!
//! [^1]: For now, two traces are sent, but the aim is to send a single trace carrying (at least)
//!       two spans: one for reception and one for publishing
//!
//! [1]: transport::mqtt::mqtt_client::MqttClient
//! [2]: transport::mqtt::mqtt_router::MqttRouter
//! [3]: transport::mqtt::topic::Topic
//! [4]: https://www.w3.org/TR/trace-context/

use std::time::{SystemTime, UNIX_EPOCH};

/// Client implementation and configuration
///
/// Provides structs to load configuration from file or bootstrap sequence and traits and functions
/// to implement V2X application
pub mod client;

/// V2X and ETSI messages definition
#[cfg(feature = "mobility")]
pub mod exchange;

/// Generic mobility structs and functions
///
/// Structs and helper functions to manipulate objects on a geodesic referential computing distance,
/// bearing, ...
///
/// Everything here is using [SI units][1]
///
/// [1]: https://en.wikipedia.org/wiki/International_System_of_Units
#[cfg(feature = "mobility")]
pub mod mobility;

#[cfg(feature = "mobility")]
pub(crate) mod monitor;

/// MQTT and Telemetry clients implementation
pub mod transport;

/// Returns the current UTC timestamp in milliseconds
pub fn now() -> u64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_millis() as u64
}
