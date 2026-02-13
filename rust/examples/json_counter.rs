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

use std::any::Any;
use std::path::Path;
use std::str::FromStr;

use clap::{Arg, Command};
use ini::Ini;
use libits::client::configuration::Configuration;
use libits::client::logger::create_stdout_logger;
use libits::transport::mqtt::mqtt_client::MqttClient;
use libits::transport::mqtt::mqtt_router::MqttRouter;
use libits::transport::mqtt::str_topic::StrTopic;
use log::{error, info};
use rumqttc::v5::mqttbytes::v5::{Publish, PublishProperties};

#[tokio::main(flavor = "multi_thread")]
async fn main() {
    let matches = Command::new("ITS JSON Counter client")
        .version("0.1.0")
        .author("Nicolas Buffon <nicolas.buffon@orange.com>")
        .about("MQTT example counting message that contain JSON payload")
        .arg(
            Arg::new("config-file-path")
                .short('c')
                .long("config")
                .default_value("examples/config.ini")
                .value_name("CONFIG_FILE_PATH")
                .help("Path to the configuration file"),
        )
        .get_matches();

    let _logger = create_stdout_logger().expect("Logger initialization failed");

    let mut configuration = Configuration::try_from(
        Ini::load_from_file(Path::new(
            matches.get_one::<String>("config-file-path").unwrap(),
        ))
        .expect("Failed to load config file as Ini"),
    )
    .expect("Failed to create Configuration from loaded Ini");
    configuration
        .mqtt
        .mqtt_options
        .set_max_packet_size(Some(256_000));

    let (mut client, mut event_loop) = MqttClient::new(&configuration.mqtt.mqtt_options);
    let mut router = MqttRouter::default();

    router.add_route(StrTopic::from_str("#").unwrap(), json_route);

    client.subscribe(&["#".to_string()]).await;

    let mut total: u128 = 0;
    let mut json: u128 = 0;

    loop {
        match event_loop.poll().await {
            Ok(event) => {
                if let Some((_, result)) = router.handle_event::<StrTopic>(event) {
                    let result = result.0.downcast::<Result<(), &'static str>>();
                    if result.is_ok() {
                        json += 1;
                    }
                }
                total += 1;

                if total.is_multiple_of(1000) {
                    println!("Received {total} messages including {json} as JSON");
                }
            }
            Err(e) => {
                error!("Connection error received: {e:?}");
                info!("Exiting...");
                break;
            }
        }
    }
}

pub fn json_route(publish: Publish) -> Option<(Box<dyn Any + 'static + Send>, PublishProperties)> {
    if let Ok(payload) = std::str::from_utf8(publish.payload.as_ref()) {
        if serde_json::from_str::<String>(payload).is_ok() {
            Some((
                Box::new(Ok::<(), &'static str>(())),
                publish.properties.unwrap_or_default(),
            ))
        } else {
            Some((
                Box::new(Err::<(), &'static str>("Failed to parse payload as JSON")),
                publish.properties.unwrap_or_default(),
            ))
        }
    } else {
        Some((
            Box::new(Err::<(), &'static str>("Failed to parse payload as UTF-8")),
            publish.properties.unwrap_or_default(),
        ))
    }
}
