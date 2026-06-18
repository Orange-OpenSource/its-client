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
use log::{error, info};
use rumqttc::v5::MqttOptions;
use rumqttc::{TlsConfiguration, Transport};
use std::fs::File;
use std::io;
use std::io::Read;

pub mod mqtt_client;
pub mod mqtt_router;
pub mod topic;

#[cfg(feature = "geo_routing")]
pub mod geo_topic;
pub mod routed_str_topic;
pub mod str_topic;

/// Fills MqttOptions with the proper transport
///
/// Based on the provided arguments the best transport configuration is set.
///
/// TLS configuration, if enabled, will give priority to client certificate
/// presence, and if not rely only on server certificate.
///
/// On top of TLS the transport is configured to use web socket or not.
///
/// * `use_tls`          - whether to enable TLS or not
/// * `use_websocket`    - wether to use websocket or not
/// * `mqtt_options`     - the [MqttOptions] struct to edit
/// * `ca_file`          - certificate of the authority that signed the client certificate
/// * `client_cert_file` - client certificate
/// * `client_key_file`  - client certificate's public key
pub(crate) fn configure_transport(
    use_tls: bool,
    use_websocket: bool,
    mqtt_options: &mut MqttOptions,
    ca_file: Option<String>,
    client_cert_file: Option<String>,
    client_key_file: Option<String>,
) {
    match (use_tls, use_websocket) {
        (true, _) => match (ca_file, client_cert_file, client_key_file) {
            (Some(ca), Some(client_cert), Some(client_key)) => {
                info!(
                    "Configuring TLS with client certificate ({})",
                    if use_websocket { "WSS" } else { "MQTTS" }
                );
                match tls_from_client_auth(ca, client_cert, client_key, use_websocket) {
                    Ok(transport) => {
                        mqtt_options.set_transport(transport);
                    }
                    Err(e) => {
                        error!("Failed to configure TLS transport: {}", e);
                    }
                }
            }
            (_, _, _) => {
                if use_websocket {
                    info!("Transport: MQTT over WebSocket; TLS enabled");
                    mqtt_options.set_transport(Transport::Wss(TlsConfiguration::default()));
                } else {
                    info!("Transport: standard MQTT; TLS enabled");
                    mqtt_options.set_transport(Transport::Tls(TlsConfiguration::default()));
                }
            }
        },
        (false, true) => {
            info!("Transport: MQTT over WebSocket; TLS disabled");
            mqtt_options.set_transport(Transport::Ws);
        }
        (false, false) => info!("Transport: standard MQTT; TLS disabled"),
    }
}

fn tls_from_client_auth(
    ca_file: String,
    client_cert_file: String,
    client_key_file: String,
    use_websocket: bool,
) -> io::Result<Transport> {
    let mut ca: Vec<u8> = Vec::new();
    let mut ca_file = File::open(ca_file)?;
    ca_file.read_to_end(&mut ca)?;

    let mut client_cert: Vec<u8> = Vec::new();
    let mut client_cert_file = File::open(client_cert_file)?;
    client_cert_file.read_to_end(&mut client_cert)?;

    let mut client_key: Vec<u8> = Vec::new();
    let mut client_key_file = File::open(client_key_file)?;
    client_key_file.read_to_end(&mut client_key)?;

    if use_websocket {
        Ok(Transport::wss(ca, Some((client_key, client_cert)), None))
    } else {
        Ok(Transport::tls(ca, Some((client_cert, client_key)), None))
    }
}
