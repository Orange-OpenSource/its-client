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

use rumqttc::v5::MqttOptions;
use rumqttc::{TlsConfiguration, Transport};

pub(crate) mod mqtt_client;
pub(crate) mod mqtt_router;
pub mod topic;

#[cfg(feature = "geo_routing")]
pub mod geo_topic;

pub(crate) fn configure_transport(
    tls_configuration: Option<TlsConfiguration>,
    use_websocket: bool,
    mqtt_options: &mut MqttOptions,
) {
    match (tls_configuration, use_websocket) {
        (Some(tls), true) => {
            println!("Transport: MQTT over WebSocket; TLS enabled");
            mqtt_options.set_transport(Transport::Wss(tls));
        }
        (Some(tls), false) => {
            println!("Transport: standard MQTT; TLS enabled");
            mqtt_options.set_transport(Transport::Tls(tls));
        }
        (None, true) => {
            println!("Transport: MQTT over WebSocket; TLS disabled");
            mqtt_options.set_transport(Transport::Ws);
        }
        (None, false) => println!("Transport: standard MQTT; TLS disabled"),
    }
}

pub(crate) fn configure_tls(
    ca_path: &str,
    alpn: Option<Vec<Vec<u8>>>,
    client_auth: Option<(Vec<u8>, Vec<u8>)>,
) -> TlsConfiguration {
    let ca: Vec<u8> = std::fs::read(ca_path).expect("Failed to read TLS certificate");

    TlsConfiguration::Simple {
        ca,
        alpn,
        client_auth,
    }
}

#[cfg(test)]
mod tests {
    use crate::transport::mqtt::configure_tls;

    #[test]
    #[should_panic]
    fn configure_tls_with_invalid_path_should_return_error() {
        let _ = configure_tls("unextisting/path", None, None);
    }
}
