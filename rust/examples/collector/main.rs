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
use std::str::FromStr;
mod collector_str_topic;

use crate::collector_str_topic::CollectorStrTopic;
use chrono::Local;
use clap::{Arg, Command};
use ini::Ini;
use libits::client::configuration::Configuration;
use libits::client::configuration::configuration_error::ConfigurationError;
use libits::client::configuration::mqtt_configuration::MqttConfiguration;
use libits::client::logger::create_stdout_logger;
use libits::transport::mqtt::mqtt_client::MqttClient;
use libits::transport::mqtt::mqtt_router::MqttRouter;
use libits::transport::mqtt::routed_str_topic::RoutedStrTopic;
use libits::transport::packet::Packet;
#[cfg(feature = "telemetry")]
use libits::transport::telemetry::init_tracer;
use log::{debug, error, info, trace};
use rumqttc::v5::mqttbytes::v5::{Publish, PublishProperties};
use std::any::Any;
use std::fs::{File, OpenOptions, create_dir_all};
use std::io::Write;
use std::path::Path;
use std::path::PathBuf;

/// Main function for the ITS Collector client.
/// Initialises the logger, loads the configuration, sets up the MQTT client and router,
/// subscribes to topics and processes incoming MQTT events.
#[tokio::main(flavor = "multi_thread")]
async fn main() {
    // Parse command line arguments
    let matches = Command::new("ITS Collector client")
        .version("0.2.0")
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

    // Initialise telemetry if the feature is enabled
    #[cfg(feature = "telemetry")]
    init_tracer(&configuration.telemetry, "collector").expect("Failed to init telemetry");

    // Get receiver configuration
    let receiver = ReceiverConfiguration::try_from(&configuration).unwrap_or_default();

    // Log information about the receiver
    info!(
        "Receiver on {:?}{}",
        receiver.topic_list,
        match receiver.route_level {
            Some(route_level) => format!(" with the route level {}", route_level),
            None => "".to_string(),
        }
    );

    // Get exporter configuration
    let exporter = ExporterConfiguration::try_from(&configuration).unwrap_or_default();

    // Log information about each exporter
    info!(
        "Exporter stdout {}",
        if exporter.stdout.is_some() {
            "activated"
        } else {
            "deactivated"
        }
    );
    info!(
        "Exporter file {}",
        if let Some(file_exporter) = &exporter.file {
            format!(
                "activated on {} with switch each {} lines",
                file_exporter.directory, file_exporter.max_line_count
            )
        } else {
            "deactivated".to_string()
        }
    );
    info!(
        "Exporter mqtt {}",
        if let Some(mqtt_exporter) = &exporter.mqtt {
            let (host, port) = mqtt_exporter.configuration.mqtt_options.broker_address();
            format!("activated on {host}:{port}")
        } else {
            "deactivated".to_string()
        }
    );

    // Initialise MQTT client and event loop for subscriptions
    let mut router = MqttRouter::default();
    let route_level: u8 = receiver.route_level.unwrap_or(255);
    for topic in receiver.topic_list.iter() {
        match create_collector_str_topic_with_level!(topic, route_level) {
            Ok(topic) => router.add_route(topic, str_route),
            Err(_) => {
                error!("Failed to parse the topic {topic}")
            }
        }
    }

    // manage the file exporter
    let mut log_file = exporter.file.as_ref().map(LogFile::new);

    // manage the mqtt exporter
    let (mut publish_client, topic_level_update_list) = match &exporter.mqtt {
        Some(configuration) => {
            // Initialise MQTT client and event loop for publishing (outside the loop)
            debug!(
                "Connecting for publish to {}:{}",
                &configuration.configuration.mqtt_options.broker_address().0,
                &configuration.configuration.mqtt_options.broker_address().1
            );
            let (client, mut event_loop) =
                MqttClient::new(&configuration.configuration.mqtt_options);
            tokio::spawn(async move {
                info!("Publish event loop started");
                let mut retry_delay = tokio::time::Duration::from_millis(1_000);
                let max_retry_delay = tokio::time::Duration::from_millis(60_000);

                loop {
                    match event_loop.poll().await {
                        Ok(event) => {
                            debug!("Publish event: {event:?}");
                            // Reset retry delay on successful event
                            retry_delay = tokio::time::Duration::from_millis(1_000);
                        }
                        Err(e) => {
                            error!("Publish event loop error: {e:?}");
                            info!("Retrying publish connection in {:?}", retry_delay);
                            tokio::time::sleep(retry_delay).await;

                            // Exponential backoff
                            retry_delay = std::cmp::min(retry_delay * 2, max_retry_delay);
                        }
                    }
                }
            });
            (Some(client), &configuration.topic_level_update_list)
        }
        None => {
            debug!("No MQTT exporter configured, skipping publish connection");
            (None, &vec![])
        }
    };

    // Initialise MQTT client and event loop for subscription (outside the loop)
    let (mut subscribe_client, mut subscribe_event_loop) =
        MqttClient::new(&configuration.mqtt.mqtt_options);
    // Subscribe
    subscribe_client
        .subscribe(receiver.topic_list.as_ref())
        .await;
    // Event loop to process incoming MQTT events
    let mut retry_delay = tokio::time::Duration::from_millis(1_000);
    let max_retry_delay = tokio::time::Duration::from_millis(60_000);

    // Receive and export events into a loop
    loop {
        match subscribe_event_loop.poll().await {
            Ok(event) => {
                // Reset retry delay on successful event
                retry_delay = tokio::time::Duration::from_millis(1_000);

                if let Some((mut topic, result)) =
                    handle_event_with_level!(router, event, route_level)
                {
                    debug!("Event on {topic}");
                    match result.0.downcast_ref::<String>() {
                        Some(payload) => {
                            // process stdout exporter
                            if exporter.stdout.is_some() {
                                println!("{payload}");
                            }
                            // process file exporter
                            if let Some(file_exporter) = &exporter.file
                                && let Some(ref mut current_log_file) = log_file
                            {
                                // Write the payload to the file
                                current_log_file.write(payload);
                                // Check if the file has reached the maximum number of lines
                                if current_log_file.inserted_line_number
                                    >= file_exporter.max_line_count
                                {
                                    // Compress the file
                                    current_log_file.compress();
                                    // Create a new one
                                    log_file = Some(LogFile::new(file_exporter));
                                }
                            }
                            // process mqtt exporter
                            if exporter.mqtt.is_some()
                                && let Some(ref mut current_publish_client) = publish_client
                            {
                                // Apply all topic level updates if configured
                                for (level, value) in topic_level_update_list {
                                    match topic.replace_at(*level, value) {
                                        Ok(_) => trace!(" {value} applied at topic level {level}."),
                                        Err(e) => error!(
                                            "Failed to update topic level {} with value {}: {:?}",
                                            level, value, e
                                        ),
                                    };
                                }

                                // Create a Packet from the payload string
                                let packet = Packet::<CollectorStrTopic, String> {
                                    topic,
                                    payload: payload.to_string(),
                                    properties: PublishProperties::default(),
                                };

                                trace!("Start packet publishing...");
                                current_publish_client.publish(packet).await;
                                trace!("Packet published");
                            }
                        }
                        None => error!("Failed to downcast payload to String"),
                    }
                }
            }
            Err(e) => {
                error!("Subscribe connection error received: {e:?}");
                info!("Retrying subscribe connection in {:?}", retry_delay);
                tokio::time::sleep(retry_delay).await;
                // Exponential backoff
                retry_delay = std::cmp::min(retry_delay * 2, max_retry_delay);
                // Try to resubscribe
                subscribe_client
                    .subscribe(receiver.topic_list.as_ref())
                    .await
            }
        }
    }
}

// Define a struct to hold the file and its path
struct LogFile {
    file: File,
    path: PathBuf,
    inserted_line_number: u32,
}

impl LogFile {
    /// Creates a new log file and returns a `LogFile` instance.
    fn new(exporter_file: &FileExporterConfiguration) -> Self {
        let file_dir = PathBuf::from(&exporter_file.directory);
        create_dir_all(&file_dir).expect("Failed to create log directory");
        let timestamp = Local::now().format("%Y%m%d_%H%M%S_%3f");
        let file_path = file_dir.join(format!("collector_{timestamp}.log"));
        let file = OpenOptions::new()
            .create(true)
            .append(true)
            .open(&file_path)
            .expect("Failed to open log file");
        LogFile {
            file,
            path: file_path,
            inserted_line_number: 0,
        }
    }

    /// Compresses the given log file by creating a `.tar.gz` archive and removing the original file.
    ///
    /// # Arguments
    ///
    /// * `self` - A mutable reference to the `LogFile` to be compressed.
    ///
    /// # Panics
    ///
    /// This function will panic if:
    /// - Flushing or syncing the file fails.
    /// - Creating the archive file fails.
    /// - Adding the file to the archive fails.
    /// - Removing the original file fails.
    fn compress(&mut self) {
        // Finish on the current file
        self.file.flush().expect("Failed to flush log file");
        self.file.sync_all().expect("Failed to sync log file");
        // Create an archive name
        let archive_path = self.path.with_extension("tar.gz");
        // Create a tar.gz archive
        let tar_gz = File::create(&archive_path).expect("Failed to create archive");
        let enc = flate2::GzBuilder::new().write(tar_gz, flate2::Compression::default());
        let mut tar = tar::Builder::new(enc);
        tar.append_path_with_name(&self.path, self.path.file_name().unwrap())
            .expect("Failed to add file to archive");
        tar.finish().expect("Failed to finish archive");
        // Remove the original file
        std::fs::remove_file(&self.path).expect("Failed to remove original file");
    }

    fn write(&mut self, payload: &str) {
        writeln!(self.file, "{payload}").expect("Failed to write to log file");
        self.inserted_line_number += 1;
    }
}

const RECEIVER_SECTION: &str = "receiver";

/// Configuration for the exporter.
#[derive(Clone, Debug)]
pub struct ReceiverConfiguration {
    /// List of topics to subscribe to, default to ["#"]
    pub topic_list: Vec<String>,
    /// Route topic level number kept to create a Route
    pub route_level: Option<u8>,
}

impl Default for ReceiverConfiguration {
    fn default() -> Self {
        Self {
            topic_list: vec!["#".to_string()],
            route_level: None,
        }
    }
}

impl TryFrom<&Configuration> for ReceiverConfiguration {
    type Error = ConfigurationError;

    /// Tries to create a `ReceiverConfiguration` from a `Configuration`.
    ///
    /// # Arguments
    ///
    /// * `configuration` - Configuration to create the receiver configuration from.
    ///
    /// # Returns
    ///
    /// A result containing the `ReceiverConfiguration` or an error.
    fn try_from(configuration: &Configuration) -> Result<Self, Self::Error> {
        match &configuration.custom_settings {
            Some(_custom_settings) => {
                // Try to get the topic list from configuration using the new get_list method
                let topic_list =
                    match configuration.get_list::<String>(Some(RECEIVER_SECTION), "topic_list") {
                        Ok(topics) => topics,
                        Err(_) => {
                            // If not found or error, use the default value
                            return Ok(ReceiverConfiguration::default());
                        }
                    };
                let route_level = configuration
                    .get::<u8>(Some(RECEIVER_SECTION), "route_level")
                    .ok();
                if topic_list.is_empty() {
                    Ok(ReceiverConfiguration::default())
                } else {
                    Ok(ReceiverConfiguration {
                        topic_list,
                        route_level,
                    })
                }
            }
            None => Ok(ReceiverConfiguration::default()),
        }
    }
}

const EXPORTER_SECTION: &str = "exporter";

/// Configuration for the exporter.
#[derive(Clone, Debug, Default)]
pub struct ExporterConfiguration {
    /// Stdout exporter configuration
    pub stdout: Option<StdoutExporterConfiguration>,
    /// File exporter configuration
    pub file: Option<FileExporterConfiguration>,
    /// MQTT exporter configuration
    pub mqtt: Option<MQTTExporterConfiguration>,
}

#[derive(Clone, Debug, Default)]
pub struct StdoutExporterConfiguration {}

#[derive(Clone, Debug, Default)]
pub struct FileExporterConfiguration {
    /// Output directory where the generated file will be available
    pub directory: String,
    /// Maximum number of lines written before to compress the file
    pub max_line_count: u32,
}

#[derive(Clone, Debug, Default)]
pub struct MQTTExporterConfiguration {
    /// MQTT configuration
    pub configuration: MqttConfiguration,
    /// Topic level update list
    pub topic_level_update_list: Vec<(u8, String)>,
}

impl TryFrom<&Configuration> for ExporterConfiguration {
    type Error = ConfigurationError;

    /// Tries to create an `ExporterConfiguration` from a `Configuration`.
    ///
    /// # Arguments
    ///
    /// * `configuration` - Configuration to create the exporter configuration from.
    ///
    /// # Returns
    ///
    /// A result containing the `ExporterConfiguration` or an error.
    fn try_from(configuration: &Configuration) -> Result<Self, Self::Error> {
        match &configuration.custom_settings {
            Some(custom_settings) => Ok(ExporterConfiguration {
                stdout: match configuration.get::<bool>(Some(EXPORTER_SECTION), "stdout") {
                    Ok(true) => Some(StdoutExporterConfiguration {}),
                    Ok(false) => None,
                    Err(e) => {
                        info!("Exporter stdout not configured: {e}");
                        None
                    }
                },
                file: match configuration.get::<bool>(Some(EXPORTER_SECTION), "file") {
                    Ok(true) => Some(FileExporterConfiguration {
                        directory: configuration
                            .get::<String>(Some(EXPORTER_SECTION), "file_directory")
                            .unwrap_or("/data/collector".to_string()),
                        max_line_count: configuration
                            .get(Some(EXPORTER_SECTION), "file_nb_line")
                            .unwrap_or(10000),
                    }),
                    Ok(false) => None,
                    Err(e) => {
                        info!("Exporter file not configured: {e}");
                        None
                    }
                },
                mqtt: match configuration.get::<bool>(Some(EXPORTER_SECTION), "mqtt") {
                    Ok(true) => {
                        let exporter_section = match custom_settings.section(Some(EXPORTER_SECTION))
                        {
                            Some(section) => section,
                            None => {
                                info!(" Exporter mqtt not configured: no section found");
                                return Err(ConfigurationError::NoCustomSettings);
                            }
                        };

                        // Parse topic_level_update_list if present
                        let topic_level_update_list = configuration
                            .get_list::<String>(Some(EXPORTER_SECTION), "topic_level_update_list")
                            .map(|topic_level_update| {
                                topic_level_update
                                    .iter()
                                    .filter_map(|pair| {
                                        let parts: Vec<&str> = pair.split('=').collect();
                                        if parts.len() == 2 {
                                            parts[0]
                                                .trim()
                                                .parse::<u8>()
                                                .ok()
                                                .map(|level| (level, parts[1].trim().to_string()))
                                        } else {
                                            None
                                        }
                                    })
                                    .collect::<Vec<(u8, String)>>()
                            })
                            .unwrap_or_default();

                        Some(MQTTExporterConfiguration {
                            configuration: match MqttConfiguration::try_from(exporter_section) {
                                Ok(config) => config,
                                Err(e) => {
                                    info!(" Exporter mqtt not configured: {e}");
                                    return Err(e);
                                }
                            },
                            topic_level_update_list,
                        })
                    }
                    Ok(false) => None,
                    Err(e) => {
                        info!("Exporter mqtt not configured: {e}");
                        None
                    }
                },
            }),
            None => {
                info!("No custom settings found in configuration");
                Err(ConfigurationError::NoCustomSettings)
            }
        }
    }
}

/// Route handler for processing incoming MQTT messages.
/// Converts the payload to a UTF-8 string and returns it along with the Publish properties.
///
/// # Arguments
///
/// * `publish` - MQTT published message.
///
/// # Returns
///
/// An optional tuple containing the payload as a boxed `Any` and the Publish properties.
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
    use crate::{
        EXPORTER_SECTION, ExporterConfiguration, FileExporterConfiguration, LogFile,
        ReceiverConfiguration, str_route,
    };
    use libits::client::configuration::Configuration;
    use rumqttc::Transport;
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
    fn exporter_configuration_default() {
        let exporter_configuration = ExporterConfiguration::default();
        assert!(exporter_configuration.stdout.is_none());
        assert!(exporter_configuration.file.is_none());
        assert!(exporter_configuration.mqtt.is_none());
    }

    #[test]
    fn exporter_configuration_with_all_the_sections() {
        let mut configuration = Configuration::default();
        configuration.set(Some(EXPORTER_SECTION), "stdout", "true");
        configuration.set(Some(EXPORTER_SECTION), "file", "true");
        configuration.set(Some(EXPORTER_SECTION), "mqtt", "true");
        // if mqtt is true, we need to set the mqtt options
        configuration.set(Some(EXPORTER_SECTION), "host", "localhost");
        configuration.set(Some(EXPORTER_SECTION), "port", "1883");
        configuration.set(Some(EXPORTER_SECTION), "use_tls", "false");
        configuration.set(Some(EXPORTER_SECTION), "use_websocket", "false");
        configuration.set(Some(EXPORTER_SECTION), "client_id", "collector_client");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(exporter_config.stdout.is_some());
        assert!(exporter_config.file.is_some());
        assert!(exporter_config.mqtt.is_some());
    }

    #[test]
    fn exporter_configuration_with_invalid_sections() {
        let mut configuration = Configuration::default();
        configuration.set(Some(EXPORTER_SECTION), "stdout", "invalid");
        configuration.set(Some(EXPORTER_SECTION), "file", "invalid");
        configuration.set(Some(EXPORTER_SECTION), "mqtt", "invalid");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(!exporter_config.stdout.is_some());
        assert!(!exporter_config.file.is_some());
        assert!(!exporter_config.mqtt.is_some());
    }

    #[test]
    fn exporter_configuration_with_stdout_section() {
        let mut configuration = Configuration::default();
        configuration.set(Some(EXPORTER_SECTION), "stdout", "true");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(exporter_config.stdout.is_some());
        assert!(!exporter_config.file.is_some());
        assert!(!exporter_config.mqtt.is_some());
    }

    #[test]
    fn exporter_configuration_with_file_section() {
        let mut configuration = Configuration::default();
        configuration.set(Some(EXPORTER_SECTION), "file", "true");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(!exporter_config.stdout.is_some());
        assert!(exporter_config.file.is_some());
        assert!(!exporter_config.mqtt.is_some());
        let file_exporter = exporter_config.file.unwrap();
        assert_eq!(file_exporter.directory, "/data/collector");
        assert_eq!(file_exporter.max_line_count, 10000);
    }

    #[test]
    fn exporter_configuration_with_mqtt_section() {
        let mut configuration = Configuration::default();
        configuration.set(Some(EXPORTER_SECTION), "mqtt", "true");
        // if mqtt is true, we need to set the mqtt options
        configuration.set(Some(EXPORTER_SECTION), "host", "localhost");
        configuration.set(Some(EXPORTER_SECTION), "port", "1883");
        configuration.set(Some(EXPORTER_SECTION), "use_tls", "false");
        configuration.set(Some(EXPORTER_SECTION), "use_websocket", "false");
        configuration.set(Some(EXPORTER_SECTION), "client_id", "collector_client");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(!exporter_config.stdout.is_some());
        assert!(!exporter_config.file.is_some());
        assert!(exporter_config.mqtt.is_some());
    }

    #[test]
    fn exporter_configuration_with_custom_file_section() {
        let mut configuration = Configuration::default();
        configuration.set(Some(EXPORTER_SECTION), "file", "true");
        configuration.set(Some(EXPORTER_SECTION), "file_directory", "/custom/path");
        configuration.set(Some(EXPORTER_SECTION), "file_nb_line", "5000");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(!exporter_config.stdout.is_some());
        assert!(exporter_config.file.is_some());
        assert!(!exporter_config.mqtt.is_some());
        let file_exporter = exporter_config.file.unwrap();
        assert_eq!(file_exporter.directory, "/custom/path");
        assert_eq!(file_exporter.max_line_count, 5000);
    }

    #[test]
    fn exporter_configuration_with_custom_mqtt_section() {
        let mut configuration = Configuration::default();
        configuration.set(Some(EXPORTER_SECTION), "mqtt", "true");
        configuration.set(Some(EXPORTER_SECTION), "host", "localhost");
        configuration.set(Some(EXPORTER_SECTION), "port", "1883");
        configuration.set(Some(EXPORTER_SECTION), "use_tls", "true");
        configuration.set(Some(EXPORTER_SECTION), "use_websocket", "true");
        configuration.set(Some(EXPORTER_SECTION), "client_id", "collector_client");
        configuration.set(Some(EXPORTER_SECTION), "connection_timeout", "10");
        configuration.set(Some(EXPORTER_SECTION), "username", "user");
        configuration.set(Some(EXPORTER_SECTION), "password", "pwd");
        configuration.set(
            Some(EXPORTER_SECTION),
            "topic_level_update_list",
            "\"1=my-project\",\"2=client\"",
        );

        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(!exporter_config.stdout.is_some());
        assert!(!exporter_config.file.is_some());
        assert!(exporter_config.mqtt.is_some());
        let mqtt_exporter = exporter_config.mqtt.unwrap();
        assert_eq!(
            mqtt_exporter.configuration.mqtt_options.broker_address(),
            ("localhost".to_string(), 1883)
        );
        assert_eq!(
            mqtt_exporter.configuration.mqtt_options.client_id(),
            "collector_client"
        );
        assert_eq!(
            mqtt_exporter.configuration.mqtt_options.credentials(),
            Some(("user".to_string(), "pwd".to_string()))
        );
        assert!(matches!(
            mqtt_exporter.configuration.mqtt_options.transport(),
            Transport::Wss(_)
        ));
        assert_eq!(
            mqtt_exporter
                .configuration
                .mqtt_options
                .connection_timeout(),
            10
        );
        assert_eq!(
            mqtt_exporter.topic_level_update_list,
            vec![(1, "my-project".to_string()), (2, "client".to_string())]
        );
    }

    // Tests for ReceiverConfiguration
    #[test]
    fn receiver_configuration_default() {
        let receiver_config = ReceiverConfiguration::default();
        assert_eq!(receiver_config.topic_list, vec!["#".to_string()]);
        assert_eq!(receiver_config.route_level, None,);
    }

    #[test]
    fn receiver_configuration_with_topic_list() {
        let mut configuration = Configuration::default();
        configuration.set(Some("receiver"), "topic_list", "#,/topic/1,/topic/2");
        let receiver_config = ReceiverConfiguration::try_from(&configuration).unwrap();
        assert_eq!(
            receiver_config.topic_list,
            vec!["#", "/topic/1", "/topic/2"]
        );
        assert_eq!(receiver_config.route_level, None,);
    }

    #[test]
    fn receiver_configuration_with_route_level() {
        let mut configuration = Configuration::default();
        configuration.set(Some("receiver"), "route_level", "10");
        let receiver_config = ReceiverConfiguration::try_from(&configuration).unwrap();
        assert_eq!(receiver_config.topic_list, vec!["#".to_string()]);
        assert_eq!(receiver_config.route_level, None); // Should use default
    }

    #[test]
    fn receiver_configuration_with_topic_list_and_route_level() {
        let mut configuration = Configuration::default();
        configuration.set(Some("receiver"), "topic_list", "/topic/1,/topic/2");
        configuration.set(Some("receiver"), "route_level", "1");
        let receiver_config = ReceiverConfiguration::try_from(&configuration).unwrap();
        assert_eq!(receiver_config.topic_list, vec!["/topic/1", "/topic/2"]);
        assert_eq!(receiver_config.route_level, Some(1),);
    }

    #[test]
    fn receiver_configuration_empty_topic_list() {
        let mut configuration = Configuration::default();
        configuration.set(Some("receiver"), "topic_list", "");
        let receiver_config = ReceiverConfiguration::try_from(&configuration).unwrap();
        assert_eq!(receiver_config.topic_list, vec!["#"]); // Should use default
        assert_eq!(receiver_config.route_level, None,);
    }

    #[test]
    fn receiver_configuration_missing_field() {
        let configuration = Configuration::default();
        let receiver_config = ReceiverConfiguration::try_from(&configuration).unwrap();
        assert_eq!(receiver_config.topic_list, vec!["#"]); // Should use default
        assert_eq!(receiver_config.route_level, None,);
    }

    #[test]
    fn receiver_configuration_no_custom_settings() {
        let mut configuration = Configuration::default();
        configuration.custom_settings = None;
        let receiver_config = ReceiverConfiguration::try_from(&configuration).unwrap();
        assert_eq!(receiver_config.topic_list, vec!["#"]); // Should use default
        assert_eq!(receiver_config.route_level, None,);
    }

    // Test for LogFile functionality
    #[test]
    fn logfile_new_creates_file() {
        use tempfile::TempDir;

        let temp_dir = TempDir::new().unwrap();
        let file_config = FileExporterConfiguration {
            directory: temp_dir.path().to_string_lossy().to_string(),
            max_line_count: 10,
        };

        let log_file = LogFile::new(&file_config);
        assert!(log_file.path.exists());
        assert_eq!(log_file.inserted_line_number, 0);

        // Cleanup is automatic with TempDir
    }

    #[test]
    fn exporter_configuration_with_mqtt_section_missing_host() {
        let mut configuration = Configuration::default();
        configuration.set(Some(EXPORTER_SECTION), "mqtt", "true");
        let result = ExporterConfiguration::try_from(&configuration);
        assert!(result.is_err());
    }

    #[test]
    fn exporter_configuration_with_mqtt_section_and_invalid_topic_level_updates() {
        let mut configuration = Configuration::default();
        configuration.set(Some(EXPORTER_SECTION), "mqtt", "true");
        configuration.set(Some(EXPORTER_SECTION), "host", "localhost");
        configuration.set(Some(EXPORTER_SECTION), "port", "1883");
        configuration.set(Some(EXPORTER_SECTION), "use_tls", "false");
        configuration.set(Some(EXPORTER_SECTION), "use_websocket", "false");
        configuration.set(Some(EXPORTER_SECTION), "client_id", "test-client");
        configuration.set(
            Some(EXPORTER_SECTION),
            "topic_level_update_list",
            "invalid_format",
        );

        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(exporter_config.mqtt.is_some());
        assert!(
            exporter_config
                .mqtt
                .unwrap()
                .topic_level_update_list
                .is_empty()
        );
    }

    #[test]
    fn exporter_configuration_with_mqtt_section_and_empty_topic_level_updates() {
        let mut configuration = Configuration::default();
        configuration.set(Some(EXPORTER_SECTION), "mqtt", "true");
        configuration.set(Some(EXPORTER_SECTION), "host", "localhost");
        configuration.set(Some(EXPORTER_SECTION), "port", "1883");
        configuration.set(Some(EXPORTER_SECTION), "use_tls", "false");
        configuration.set(Some(EXPORTER_SECTION), "use_websocket", "false");
        configuration.set(Some(EXPORTER_SECTION), "client_id", "test-client");
        configuration.set(Some(EXPORTER_SECTION), "topic_level_update_list", "");

        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(exporter_config.mqtt.is_some());
        assert!(
            exporter_config
                .mqtt
                .unwrap()
                .topic_level_update_list
                .is_empty()
        );
    }
}
