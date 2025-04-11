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
use libits::client::configuration::configuration_error::ConfigurationError;
use libits::client::configuration::{Configuration, create_stdout_logger};
use libits::transport::mqtt::mqtt_client::MqttClient;
use libits::transport::mqtt::mqtt_router::MqttRouter;
use libits::transport::mqtt::str_topic::StrTopic;
#[cfg(feature = "telemetry")]
use libits::transport::telemetry::init_tracer;
use log::{debug, error, info};
use rumqttc::v5::mqttbytes::v5::{Publish, PublishProperties};

/// Main function for the ITS Collector client.
/// Initializes the logger, loads the configuration, sets up the MQTT client and router,
/// subscribes to topics, and processes incoming MQTT events.
#[tokio::main(flavor = "multi_thread")]
async fn main() {
    // Parse command line arguments
    let matches = Command::new("ITS Collector client")
        .version("0.1.0")
        .author("Frederic Gardes <frederic.gardes@orange.com>")
        .about("Collector example store the messages using exporters")
        .arg(
            Arg::new("config-file-path")
                .short('c')
                .long("config")
                .default_value("examples/config.ini")
                .help("Path to the configuration file"),
        )
        .get_matches();

    // Initialize logger
    let _logger = create_stdout_logger().expect("Logger initialization failed");

    // Load and parse configuration file
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

    // Initialize MQTT client and event loop
    let (mut client, mut event_loop) = MqttClient::new(&configuration.mqtt.mqtt_options);
    let mut router = MqttRouter::default();
    router.add_route(StrTopic::from_str("#").unwrap(), str_route);

    // Initialize telemetry if the feature is enabled
    #[cfg(feature = "telemetry")]
    init_tracer(&configuration.telemetry, "collector").expect("Failed to init telemetry");

    // Subscribe to all topics
    client.subscribe(&["#".to_string()]).await;

    // Get exporter configuration
    let exporter = ExporterConfiguration::try_from(&configuration).unwrap_or_default();
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

    // Event loop to process incoming MQTT events
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

/// Configuration for the exporter.
#[derive(Clone, Debug, Default)]
pub struct ExporterConfiguration {
    pub stdout: bool,
    pub file: bool,
    pub mqtt: bool,
}

impl TryFrom<&Configuration> for ExporterConfiguration {
    type Error = ConfigurationError;

    /// Tries to create an `ExporterConfiguration` from a `Configuration`.
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

/// Route handler for processing incoming MQTT messages.
/// Converts the payload to a UTF-8 string and returns it along with the publish properties.
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
mod tests {
    use super::*;
    use rumqttc::v5::mqttbytes::v5::{Publish, PublishProperties};

    #[test]
    fn str_route_valid_utf8_payload() {
        let payload = b"valid utf8 string".to_vec();
        let publish = Publish {
            payload: payload.into(),
            properties: Some(PublishProperties::default()),
            ..Default::default()
        };
        let result = str_route(publish);
        assert!(result.is_some());
        let (boxed_payload, _) = result.unwrap();
        assert_eq!(
            *boxed_payload.downcast_ref::<String>().unwrap(),
            "valid utf8 string"
        );
    }

    #[test]
    fn str_route_invalid_utf8_payload() {
        let payload = vec![0, 159, 146, 150]; // Invalid UTF-8 sequence
        let publish = Publish {
            payload: payload.into(),
            properties: Some(PublishProperties::default()),
            ..Default::default()
        };
        let result = str_route(publish);
        assert!(result.is_some());
        let (boxed_payload, _) = result.unwrap();
        assert_eq!(
            *boxed_payload.downcast_ref::<String>().unwrap(),
            "Failed to parse payload as UTF-8"
        );
    }

    #[test]
    fn str_route_with_large_payload() {
        let payload = vec![b'a'; 256_000]; // Large payload
        let publish = Publish {
            payload: payload.into(),
            properties: Some(PublishProperties::default()),
            ..Default::default()
        };
        let result = str_route(publish);
        assert!(result.is_some());
        let (boxed_payload, _) = result.unwrap();
        assert_eq!(
            boxed_payload.downcast_ref::<String>().unwrap().len(),
            256_000
        );
    }

    #[test]
    fn str_route_with_special_characters() {
        let payload = b"special characters: \xF0\x9F\x98\x81".to_vec(); // UTF-8 encoded emoji
        let publish = Publish {
            payload: payload.into(),
            properties: Some(PublishProperties::default()),
            ..Default::default()
        };
        let result = str_route(publish);
        assert!(result.is_some());
        let (boxed_payload, _) = result.unwrap();
        assert_eq!(
            *boxed_payload.downcast_ref::<String>().unwrap(),
            "special characters: \u{1F601}"
        );
    }

    #[test]
    fn get_exporter_configuration_defaults() {
        //FIXME implements Default into Configuration
        let mut ini = Ini::new();
        ini.with_section(Some("mqtt"))
            .set("host", "localhost")
            .set("client_id", "client-id")
            .set("port", "1883");
        let configuration =
            Configuration::try_from(ini).expect("Failed to create empty configuration");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap_or_default();
        assert!(!exporter_config.stdout);
        assert!(!exporter_config.file);
        assert!(!exporter_config.mqtt);
    }

    #[test]
    fn get_exporter_configuration_from_valid_config() {
        let mut ini = Ini::new();
        ini.with_section(Some("mqtt"))
            .set("host", "localhost")
            .set("client_id", "client-id")
            .set("port", "1883");
        let mut configuration =
            Configuration::try_from(ini).expect("Failed to create empty configuration");
        configuration.set(Some(EXPORTER_SECTION), "stdout", "true");
        configuration.set(Some(EXPORTER_SECTION), "file", "true");
        configuration.set(Some(EXPORTER_SECTION), "mqtt", "true");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap_or_default();
        assert!(exporter_config.stdout);
        assert!(exporter_config.file);
        assert!(exporter_config.mqtt);
    }

    #[test]
    fn get_exporter_configuration_from_invalid_config() {
        let mut ini = Ini::new();
        ini.with_section(Some("mqtt"))
            .set("host", "localhost")
            .set("client_id", "client-id")
            .set("port", "1883");
        let mut configuration =
            Configuration::try_from(ini).expect("Failed to create empty configuration");
        configuration.set(Some(EXPORTER_SECTION), "stdout", "invalid");
        configuration.set(Some(EXPORTER_SECTION), "file", "invalid");
        configuration.set(Some(EXPORTER_SECTION), "mqtt", "invalid");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap_or_default();
        assert!(!exporter_config.stdout);
        assert!(!exporter_config.file);
        assert!(!exporter_config.mqtt);
    }
    #[test]
    fn get_exporter_configuration_with_missing_section() {
        let mut ini = Ini::new();
        ini.with_section(Some("mqtt"))
            .set("host", "localhost")
            .set("client_id", "client-id")
            .set("port", "1883");
        let configuration = Configuration::try_from(ini).expect("Failed to create configuration");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap_or_default();
        assert!(!exporter_config.stdout);
        assert!(!exporter_config.file);
        assert!(!exporter_config.mqtt);
    }

    #[test]
    fn get_exporter_configuration_with_partial_section() {
        let mut ini = Ini::new();
        ini.with_section(Some("mqtt"))
            .set("host", "localhost")
            .set("client_id", "client-id")
            .set("port", "1883");
        ini.with_section(Some(EXPORTER_SECTION))
            .set("stdout", "true");
        let configuration = Configuration::try_from(ini).expect("Failed to create configuration");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap_or_default();
        assert!(exporter_config.stdout);
        assert!(!exporter_config.file);
        assert!(!exporter_config.mqtt);
    }
}
