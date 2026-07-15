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

#[path = "../database.rs"]
mod database;
mod mqtt_reader;

use anyhow::Result;
use clap::{Arg, Command};
use ini::Ini;
use libits::client::configuration::mqtt_configuration::MqttConfiguration;
use libits::client::logger::create_stdout_logger;
use std::path::Path;

const DISPLAY_SECTION: &str = "display";
const RECEIVER_SECTION: &str = "receiver";
const MQTT_SECTION: &str = "mqtt";

#[tokio::main]
async fn main() -> Result<()> {
    let _logger = create_stdout_logger().expect("Logger initialization failed");

    let matches = Command::new("Display Ingestor")
        .version("0.1.0")
        .about("Subscribes to MQTT and ingests messages into a SQLite database")
        .arg(
            Arg::new("config-file-path")
                .short('c')
                .long("config")
                .value_name("CONFIG_FILE_PATH")
                .default_value("examples/config.ini")
                .help("Path to the configuration file"),
        )
        .get_matches();

    let config_path = matches.get_one::<String>("config-file-path").unwrap();
    let ini = Ini::load_from_file(Path::new(config_path))
        .unwrap_or_else(|error| panic!("Failed to load config file {}: {}", config_path, error));

    // Build the MQTT options straight from the [mqtt] section instead of the full
    // library configuration, which would also require unrelated mandatory sections
    // (e.g. [mobility]) that a display-only config does not provide.
    let mqtt_properties = ini
        .section(Some(MQTT_SECTION))
        .unwrap_or_else(|| panic!("Missing [{}] section in {}", MQTT_SECTION, config_path));
    let mqtt_configuration = MqttConfiguration::try_from(mqtt_properties).unwrap_or_else(|error| {
        panic!(
            "Invalid [{}] configuration in {}: {}",
            MQTT_SECTION, config_path, error
        )
    });

    // Only read the settings that are specific to the display example.
    let display_section = ini
        .section(Some(DISPLAY_SECTION))
        .unwrap_or_else(|| panic!("Missing [{}] section in {}", DISPLAY_SECTION, config_path));

    let db_path = display_section
        .get("db_path")
        .unwrap_or_else(|| panic!("Missing 'db_path' in [{}] section", DISPLAY_SECTION))
        .to_string();

    let zoom: u8 = display_section
        .get("zoom")
        .and_then(|value| value.parse().ok())
        .unwrap_or(18);

    let topics: Vec<String> = ini
        .section(Some(RECEIVER_SECTION))
        .and_then(|section| section.get("topic_list"))
        .map(|value| {
            value
                .split(',')
                .map(|topic| topic.trim().trim_matches('"').to_string())
                .filter(|topic| !topic.is_empty())
                .collect::<Vec<String>>()
        })
        .filter(|topics| !topics.is_empty())
        .unwrap_or_else(|| vec!["#".to_string()]);

    let config = mqtt_reader::MqttIngestorConfig {
        mqtt_options: mqtt_configuration.mqtt_options,
        topics,
        zoom,
        db_path,
    };

    mqtt_reader::run_mqtt_ingestor(config).await?;

    Ok(())
}
