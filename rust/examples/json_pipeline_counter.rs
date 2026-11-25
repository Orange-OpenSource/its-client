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
// This example's main function  does not compile when telemetry feature is enabled.
// A dedicated main is then used to tell so but it makes all imports unused
// Disable unused imports warnings if telemetry feature is enabled
#![cfg_attr(feature = "telemetry", allow(unused))]

use std::fmt::{Display, Formatter};
use std::path::Path;
use std::sync::{Arc, RwLock};

use clap::{Arg, Command};
use ini::Ini;
use libits::client::application::json_analyzer::JsonAnalyzer;
use libits::client::application::pipeline::json_pipeline;
use libits::client::configuration::Configuration;
use libits::client::logger::create_stdout_logger;
use libits::exchange::sequence_number::SequenceNumber;
use libits::transport::mqtt::mqtt_client::MqttClient;
use libits::transport::mqtt::str_topic::StrTopic;
use libits::transport::mqtt::topic::Topic;
use libits::transport::packet::Packet;
use log::{error, info};
use rumqttc::Event;
use serde_json::Value;
use std::str::FromStr;
use std::thread;
use std::time::Duration;

#[derive(Default)]
struct Counter {
    json: u128,
}

struct JsonAnalyzerExample {
    context: Arc<RwLock<Counter>>,
}

#[derive(Clone, Debug, Default, PartialEq, Eq, Hash)]
struct JsonExampleTopic {}
impl Topic for JsonExampleTopic {
    fn as_route(&self) -> String {
        "libits/example/json".to_string()
    }
}
impl FromStr for JsonExampleTopic {
    type Err = String;
    fn from_str(_: &str) -> Result<Self, Self::Err> {
        Ok(Self {})
    }
}
impl Display for JsonExampleTopic {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "libits/example")
    }
}

impl JsonAnalyzer<JsonExampleTopic, JsonExampleTopic, Counter> for JsonAnalyzerExample {
    fn new(
        _configuration: Arc<Configuration>,
        context: Arc<RwLock<Counter>>,
        _sequence_number: Arc<RwLock<SequenceNumber>>,
    ) -> Self {
        Self { context }
    }

    fn analyze(
        &mut self,
        packet: Packet<JsonExampleTopic, Value>,
    ) -> Vec<Packet<JsonExampleTopic, Value>> {
        let count = {
            let mut count = self.context.write().unwrap();
            count.json += 1;
            count.json
        };

        info!("JSON packet #{} received: {}", count, packet.payload);

        Vec::new()
    }
}

#[cfg(feature = "telemetry")]
fn main() {
    eprintln!("json_pipeline_counter is not supported with the telemetry feature");
}

#[cfg(not(feature = "telemetry"))]
#[tokio::main(flavor = "multi_thread")]
async fn main() {
    let matches = Command::new("ITS JSON pipeline counter client")
        .version("0.1.0")
        .about("MQTT example counting messages that contain JSON payload using the JSON pipeline")
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

    let topics = vec![JsonExampleTopic::default()];

    let mut send_configuration = configuration.clone();
    tokio::spawn(async move {
        send_configuration
            .mqtt
            .mqtt_options
            .set_client_id(format!("libits-example-{:?}", thread::current().id()));
        let (client, mut event_loop) = MqttClient::new(&send_configuration.mqtt.mqtt_options);
        loop {
            client
                .publish(Packet::new(
                    StrTopic::from_str("libits/example/json")
                        .expect("Failed to parse string as StrTopic"),
                    Value::from_str("{\"this_is_json\": true}")
                        .expect("Failed to parse JSON string as JSON"),
                ))
                .await;
            loop {
                match event_loop.poll().await {
                    Ok(Event::Outgoing(_)) => {
                        break;
                    }
                    Err(e) => {
                        error!("Connection error received: {e:?}");
                        info!("Exiting...");
                        break;
                    }
                    _ => (),
                }
                thread::sleep(Duration::from_millis(500));
            }
        }
    });

    json_pipeline::run::<JsonAnalyzerExample, Counter, JsonExampleTopic, JsonExampleTopic>(
        Arc::new(configuration),
        Arc::new(RwLock::new(Counter::default())),
        Arc::new(RwLock::new(SequenceNumber::new(u16::MAX.into()))),
        &topics,
    )
    .await;

    info!("JSON pipeline counter example exited");
}
