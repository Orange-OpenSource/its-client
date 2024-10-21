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

use rumqttc::v5::MqttOptions;
use rumqttc::{TlsConfiguration, Transport};

pub mod mqtt_client;
pub mod mqtt_router;
pub mod topic;

#[cfg(feature = "geo_routing")]
pub mod geo_topic;

pub(crate) fn configure_transport(
    use_tls: bool,
    use_websocket: bool,
    mqtt_options: &mut MqttOptions,
) {
    match (use_tls, use_websocket) {
        (true, true) => {
            println!("Transport: MQTT over WebSocket; TLS enabled");
            mqtt_options.set_transport(Transport::Wss(TlsConfiguration::default()));
        }
        (true, false) => {
            println!("Transport: standard MQTT; TLS enabled");
            mqtt_options.set_transport(Transport::Tls(TlsConfiguration::default()));
        }
        (false, true) => {
            println!("Transport: MQTT over WebSocket; TLS disabled");
            mqtt_options.set_transport(Transport::Ws);
        }
        (false, false) => println!("Transport: standard MQTT; TLS disabled"),
    }
}
