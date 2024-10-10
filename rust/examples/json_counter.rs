/*
 * Software Name : libits-client
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 * Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) library based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
 */

use std::any::Any;
use std::fmt::{Display, Formatter};
use std::fs;
use std::path::Path;
use std::str::FromStr;

use clap::{Arg, Command};
use flexi_logger::{with_thread, Cleanup, Criterion, Logger, Naming, WriteMode};
use ini::Ini;
use libits::client::configuration::Configuration;
use libits::transport::mqtt::mqtt_client::MqttClient;
use libits::transport::mqtt::mqtt_router::MqttRouter;
use libits::transport::mqtt::topic::Topic;
use log::{error, info};
use rumqttc::v5::mqttbytes::v5::Publish;

#[derive(Clone, Default, Debug, Hash, PartialEq, Eq)]
struct StrTopic {
    topic: String,
}
impl Display for StrTopic {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.topic)
    }
}
impl FromStr for StrTopic {
    type Err = std::str::Utf8Error;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        Ok(StrTopic {
            topic: String::from(s),
        })
    }
}
impl Topic for StrTopic {
    fn as_route(&self) -> String {
        String::from("no_routing")
    }
}

#[tokio::main(flavor = "multi_thread")]
async fn main() {
    let matches = Command::new("ITS CopyCat client")
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

    let mut configuration = Configuration::try_from(
        Ini::load_from_file(Path::new(
            matches.get_one::<String>("config-file-path").unwrap(),
        ))
        .expect("Failed to load config file as Ini"),
    )
    .expect("Failed to create Configuration from loaded Ini");

    let log_path = &configuration
        .get::<String>(Some("log"), "path")
        .unwrap_or("log".to_string());
    let log_path = Path::new(log_path);
    if !log_path.is_dir() {
        if let Err(error) = fs::create_dir(log_path) {
            panic!("Unable to create the log directory: {}", error);
        }
    }
    let _logger = match Logger::try_with_env_or_str("info") {
        Ok(logger) => {
            match logger
                .log_to_stdout()
                .write_mode(WriteMode::Async)
                .format_for_files(with_thread)
                .append()
                .rotate(
                    Criterion::Size(2_000_000),
                    Naming::Timestamps,
                    Cleanup::KeepLogAndCompressedFiles(5, 30),
                )
                .print_message()
                .start()
            {
                Ok(logger_handle) => {
                    info!("logger ready on {}", log_path.to_str().unwrap());
                    logger_handle
                }
                Err(error) => panic!("Logger starting failed with {:?}", error),
            }
        }
        Err(error) => panic!("Logger initialization failed with {:?}", error),
    };

    configuration
        .mqtt_options
        .set_max_packet_size(Some(256_000));

    let (mut client, mut event_loop) = MqttClient::new(&configuration.mqtt_options);
    let mut router = MqttRouter::default();

    router.add_route(
        StrTopic::from_str("#").unwrap(),
        |publish: Publish| -> Option<Box<dyn Any + Send + 'static>> {
            if let Ok(payload) = std::str::from_utf8(publish.payload.as_ref()) {
                if serde_json::from_str::<String>(payload).is_ok() {
                    Some(Box::new(Ok::<(), &'static str>(())))
                } else {
                    Some(Box::new(Err::<(), &'static str>(
                        "Failed to parse payload as JSON",
                    )))
                }
            } else {
                Some(Box::new(Err::<(), &'static str>(
                    "Failed to parse payload as UTF-8",
                )))
            }
        },
    );

    client.subscribe(&["#".to_string()]).await;

    let mut total: u128 = 0;
    let mut json: u128 = 0;

    loop {
        match event_loop.poll().await {
            Ok(event) => {
                if let Some((_, result)) = router.handle_event::<StrTopic>(event) {
                    let result = result.downcast::<Result<(), &'static str>>();
                    if result.is_ok() {
                        json += 1;
                    }
                }
                total += 1;

                if total % 1000 == 0 {
                    println!("Received {} messages including {} as JSON", total, json);
                }
            }
            Err(e) => {
                error!("Connection error received: {:?}", e);
                info!("Exiting...");
                break;
            }
        }
    }
}
