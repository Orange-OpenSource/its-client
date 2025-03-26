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

use clap::{Arg, Command};
use ini::Ini;
use libits::client::configuration::configuration_error::ConfigurationError;
use libits::client::configuration::{Configuration, create_stdout_logger};
use libits::transport::mqtt::mqtt_client::MqttClient;
use libits::transport::mqtt::mqtt_router::MqttRouter;
use libits::transport::mqtt::str_topic::StrTopic;
#[cfg(feature = "telemetry")]
use libits::transport::telemetry::init_tracer;
use log::{debug, error, info};
use rumqttc::v5::mqttbytes::v5::{Publish, PublishProperties};
use std::str::FromStr;

#[tokio::main(flavor = "multi_thread")]
async fn main() {
    let matches = Command::new("ITS Collector client")
        .version("0.1.0")
        .author("Frederic Gardes <frederic.gardes@orange.com>")
        .about("Collector example store the messages using exporters")
        .arg(
            Arg::new("config-file-path")
                .short('c')
                .long("config")
                .value_name("CONFIG_FILE_PATH")
                .default_value("examples/config.ini")
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

    router.add_route(StrTopic::from_str("#").unwrap(), str_route);

    #[cfg(feature = "telemetry")]
    init_tracer(&configuration.telemetry, "collector").expect("Failed to init telemetry");

    client.subscribe(&["#".to_string()]).await;

    let exporter = get_exporter_configuration(&configuration);
    info!(
        "Exporter stdout {}, file {} and mqtt {}",
        if exporter.stdout {
            "activated"
        } else {
            "deactivated"
        },
        if exporter.file {
            "activated"
        } else {
            "deactivated"
        },
        if exporter.mqtt {
            "activated"
        } else {
            "deactivated"
        }
    );
    loop {
        match event_loop.poll().await {
            Ok(event) => {
                if let Some((topic, result)) = router.handle_event::<StrTopic>(event) {
                    debug!("Event on {topic}");
                    match result.0.downcast_ref::<String>() {
                        Some(payload) => {
                            if exporter.stdout {
                                println!("{}", payload);
                            }
                            if exporter.file {
                                unimplemented!("file exporter not implemented")
                            }
                            if exporter.mqtt {
                                unimplemented!("mqtt exporter not implemented")
                            }
                        }
                        None => error!("Failed to downcast payload to String"),
                    }
                }
            }
            Err(e) => {
                error!("Connection error received: {:?}", e);
                info!("Exiting...");
                break;
            }
        }
    }

    info!("Collector example exited");
}

const EXPORTER_SECTION: &str = "exporter";

#[derive(Clone, Debug, Default)]
pub struct ExporterConfiguration {
    pub stdout: bool,
    pub file: bool,
    pub mqtt: bool,
}

impl TryFrom<&Configuration> for ExporterConfiguration {
    type Error = ConfigurationError;

    fn try_from(configuration: &Configuration) -> Result<Self, Self::Error> {
        Ok(ExporterConfiguration {
            stdout: configuration
                .get::<bool>(Some(EXPORTER_SECTION), "stdout")
                .unwrap_or(false),
            file: configuration
                .get::<bool>(Some(EXPORTER_SECTION), "file")
                .unwrap_or(false),
            mqtt: configuration
                .get::<bool>(Some(EXPORTER_SECTION), "mqtt")
                .unwrap_or(false),
        })
    }
}

pub fn get_exporter_configuration(configuration: &Configuration) -> ExporterConfiguration {
    ExporterConfiguration::try_from(configuration).unwrap_or_default()
}

pub fn str_route(publish: Publish) -> Option<(Box<dyn Any + 'static + Send>, PublishProperties)> {
    if let Ok(payload) = std::str::from_utf8(publish.payload.as_ref()) {
        Some((
            Box::new(payload.to_string()),
            publish.properties.unwrap_or_default(),
        ))
    } else {
        Some((
            Box::new("Failed to parse payload as UTF-8".to_string()),
            publish.properties.unwrap_or_default(),
        ))
    }
}

#[cfg(test)]
mod tests {}
