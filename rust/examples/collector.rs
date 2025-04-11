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

use chrono::Local;
use clap::{Arg, Command};
use ini::Ini;
use libits::client::configuration::Configuration;
use libits::client::configuration::configuration_error::ConfigurationError;
use libits::client::logger::create_stdout_logger;
use libits::transport::mqtt::mqtt_client::MqttClient;
use libits::transport::mqtt::mqtt_router::MqttRouter;
use libits::transport::mqtt::str_topic::StrTopic;
#[cfg(feature = "telemetry")]
use libits::transport::telemetry::init_tracer;
use log::{debug, error, info, warn};
use rumqttc::v5::mqttbytes::v5::{Publish, PublishProperties};
use std::fs::{File, OpenOptions, create_dir_all};
use std::io::Write;
use std::path::PathBuf;

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

    // Initialize MQTT client and event loop
    let mut router = MqttRouter::default();
    router.add_route(StrTopic::from_str("#").unwrap(), str_route);

    // Initialize telemetry if the feature is enabled
    #[cfg(feature = "telemetry")]
    init_tracer(&configuration.telemetry, "collector").expect("Failed to init telemetry");

    // Get exporter configuration
    let exporter = ExporterConfiguration::try_from(&configuration).unwrap_or_default();

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
        if exporter.mqtt.is_some() {
            "activated"
        } else {
            "deactivated"
        }
    );

    // Initialize MQTT client and event loop
    let (mut client, mut event_loop) = MqttClient::new(&configuration.mqtt.mqtt_options);

    // Subscribe to all topics
    client.subscribe(&["#".to_string()]).await;

    // manger the log file
    let mut log_file = exporter.file.as_ref().map(LogFile::new);

    // Event loop to process incoming MQTT events
    loop {
        match event_loop.poll().await {
            Ok(event) => {
                if let Some((topic, result)) = router.handle_event::<StrTopic>(event) {
                    debug!("Event on {topic}");
                    match result.0.downcast_ref::<String>() {
                        Some(payload) => {
                            if exporter.stdout.is_some() {
                                println!("{}", payload);
                            }
                            if let Some(file_exporter) = &exporter.file {
                                if let Some(ref mut current_log_file) = log_file {
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
                            }
                            if exporter.mqtt.is_some() {
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

// Define a struct to hold the file and its path
struct LogFile {
    file: File,
    path: PathBuf,
    inserted_line_number: u16,
}

impl LogFile {
    /// Creates a new log file and returns a `LogFile` instance.
    fn new(exporter_file: &FileExporterConfiguration) -> Self {
        let file_dir = PathBuf::from(&exporter_file.directory);
        create_dir_all(&file_dir).expect("Failed to create log directory");
        let timestamp = Local::now().format("%Y%m%d_%H%M%S_%3f");
        let file_path = file_dir.join(format!("collector_{}.log", timestamp));
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
        // Create archive name
        let archive_path = self.path.with_extension("tar.gz");
        // Create tar.gz archive
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
        writeln!(self.file, "{}", payload).expect("Failed to write to log file");
        self.inserted_line_number += 1;
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
    pub max_line_count: u16,
}

#[derive(Clone, Debug, Default)]
pub struct MQTTExporterConfiguration {}

impl TryFrom<&Configuration> for ExporterConfiguration {
    type Error = ConfigurationError;

    /// Tries to create an `ExporterConfiguration` from a `Configuration`.
    ///
    /// # Arguments
    ///
    /// * `configuration` - The configuration to create the exporter configuration from.
    ///
    /// # Returns
    ///
    /// A result containing the `ExporterConfiguration` or an error.
    fn try_from(configuration: &Configuration) -> Result<Self, Self::Error> {
        Ok(ExporterConfiguration {
            stdout: match configuration.get::<bool>(Some(EXPORTER_SECTION), "stdout") {
                Ok(true) => Some(StdoutExporterConfiguration {}),
                Ok(false) => None,
                Err(e) => {
                    warn!(" Exporter stdout not configured: {}", e);
                    None
                }
            },
            file: match configuration.get::<bool>(Some(EXPORTER_SECTION), "file") {
                Ok(true) => Some(FileExporterConfiguration {
                    directory: configuration
                        .get::<String>(Some(EXPORTER_SECTION), "file_directory")
                        .unwrap_or("/data/collector".to_string()),
                    max_line_count: configuration
                        .get::<u16>(Some(EXPORTER_SECTION), "file_nb_line")
                        .unwrap_or(10000),
                }),
                Ok(false) => None,
                Err(e) => {
                    warn!(" Exporter file not configured: {}", e);
                    None
                }
            },
            mqtt: match configuration.get::<bool>(Some(EXPORTER_SECTION), "mqtt") {
                Ok(true) => Some(MQTTExporterConfiguration {}),
                Ok(false) => None,
                Err(e) => {
                    warn!(" Exporter mqtt not configured: {}", e);
                    None
                }
            },
        })
    }
}

/// Route handler for processing incoming MQTT messages.
/// Converts the payload to a UTF-8 string and returns it along with the publish properties.
///
/// # Arguments
///
/// * `publish` - The MQTT publish message.
///
/// # Returns
///
/// An optional tuple containing the payload as a boxed `Any` and the publish properties.
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
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(!exporter_config.stdout.is_some());
        assert!(!exporter_config.file.is_some());
        assert!(!exporter_config.mqtt.is_some());
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
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(exporter_config.stdout.is_some());
        assert!(exporter_config.file.is_some());
        assert!(exporter_config.mqtt.is_some());
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
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(!exporter_config.stdout.is_some());
        assert!(!exporter_config.file.is_some());
        assert!(!exporter_config.mqtt.is_some());
    }
    #[test]
    fn get_exporter_configuration_with_missing_section() {
        let mut ini = Ini::new();
        ini.with_section(Some("mqtt"))
            .set("host", "localhost")
            .set("client_id", "client-id")
            .set("port", "1883");
        let configuration = Configuration::try_from(ini).expect("Failed to create configuration");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(!exporter_config.stdout.is_some());
        assert!(!exporter_config.file.is_some());
        assert!(!exporter_config.mqtt.is_some());
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
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        assert!(exporter_config.stdout.is_some());
        assert!(!exporter_config.file.is_some());
        assert!(!exporter_config.mqtt.is_some());
    }

    #[test]
    fn get_exporter_configuration_with_default_file_fields() {
        let mut ini = Ini::new();
        ini.with_section(Some("mqtt"))
            .set("host", "localhost")
            .set("client_id", "client-id")
            .set("port", "1883");
        ini.with_section(Some(EXPORTER_SECTION)).set("file", "true");
        let configuration = Configuration::try_from(ini).expect("Failed to create configuration");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        let file_exporter = exporter_config.file.unwrap();
        assert_eq!(file_exporter.directory, "/data/collector");
        assert_eq!(file_exporter.max_line_count, 10000);
    }

    #[test]
    fn get_exporter_configuration_with_file_fields() {
        let mut ini = Ini::new();
        ini.with_section(Some("mqtt"))
            .set("host", "localhost")
            .set("client_id", "client-id")
            .set("port", "1883");
        ini.with_section(Some(EXPORTER_SECTION))
            .set("file", "true")
            .set("file_directory", "/custom/path")
            .set("file_nb_line", "5000");
        let configuration = Configuration::try_from(ini).expect("Failed to create configuration");
        let exporter_config = ExporterConfiguration::try_from(&configuration).unwrap();
        let file_exporter = exporter_config.file.unwrap();
        assert_eq!(file_exporter.directory, "/custom/path");
        assert_eq!(file_exporter.max_line_count, 5000);
    }
}
