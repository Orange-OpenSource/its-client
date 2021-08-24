use std::fs;
use std::path::Path;

use clap::{App, Arg};
use flexi_logger::{with_thread, Cleanup, Criterion, FileSpec, Logger, Naming, WriteMode};
use log::{info, warn};

#[cfg(feature = "copycat")]
use libits_copycat::CopyCat;

#[tokio::main(flavor = "multi_thread")]
async fn main() {
    let log_directory = Path::new("log");
    if !log_directory.is_dir() {
        if let Err(error) = fs::create_dir(log_directory) {
            panic!("Unable to create the log directory: {}", error);
        }
    }
    let _logger = match Logger::try_with_env_or_str("info") {
        Ok(logger) => {
            match logger
                .log_to_file(
                    FileSpec::default()
                        .directory(log_directory)
                        .suppress_timestamp(),
                ) // write logs to file
                .write_mode(WriteMode::Async)
                .format_for_files(with_thread)
                .append() // do not truncate the log file when the program is restarted
                .rotate(
                    // If the program runs long enough,
                    Criterion::Size(2_000_000),
                    Naming::Timestamps,
                    Cleanup::KeepLogAndCompressedFiles(5, 30),
                )
                .print_message()
                .start()
            {
                Ok(logger_handle) => {
                    info!("logger ready");
                    logger_handle
                }
                Err(error) => panic!("Logger starting failed with {:?}", error),
            }
        }
        Err(error) => panic!("Logger initialization failed with {:?}", error),
    };

    let matches = App::new("ITS Client")
        .version("1.0.1")
        .author("Frederic Gardes <frederic.gardes@orange.com>")
        .about("ITS Client. Connect to MQTT broker, read CAM's, CPM's and DENM's, and produce DENM's if needed.")
        .arg(
            Arg::with_name("mqtt-host")
                .short("H")
                .long("mqtt-host")
                .value_name("HOST_IP")
                .help("The IP address of the MQTT broker")
                .default_value("127.0.0.1")
                .takes_value(true),
        )
        .arg(
            Arg::with_name("mqtt-port")
                .short("P")
                .long("mqtt-port")
                .value_name("PORT")
                .help("The PORT of the MQTT broker")
                .default_value("1883")
                .takes_value(true),
        )
        .arg(
            Arg::with_name("mqtt-username")
                .short("u")
                .long("mqtt-username")
                .value_name("USERNAME")
                .help("User login for the MQTT broker")
                .takes_value(true),
        )
        .arg(
            Arg::with_name("mqtt-password")
                .short("p")
                .long("mqtt-password")
                .value_name("PASSWORD")
                .help("User password for the MQTT broker")
                .takes_value(true),
        )
        .arg(
            Arg::with_name("mqtt-client-id")
                .long("mqtt-client-id")
                .value_name("CLIENT_ID")
                .help("MQTT client identifier. Must be unique by broker")
                .takes_value(true),
        )
        .arg(
            Arg::with_name("root-topic")
                .short("t")
                .long("root-topic")
                .value_name("TOPIC")
                .help("root topic for messages")
                .default_value("5GCroCo/outQueue")
                .takes_value(true),
        )
        .get_matches();

    let mqtt_host = matches.value_of("mqtt-host").unwrap();
    let mqtt_port: u16 = match matches.value_of("mqtt-port").unwrap().parse::<u16>() {
        Ok(p) => p,
        Err(e) => {
            panic!("MQTT port should be a positive integer. \nError {}", e);
        }
    };
    let mqtt_username = matches.value_of("mqtt-username");
    let mqtt_password = matches.value_of("mqtt-password");
    let mqtt_client_id = matches.value_of("mqtt-client-id").unwrap();
    let mqtt_root_topic = matches.value_of("root-topic").unwrap();

    info!(
        "Starting Mqtt client with args: \n  \
        mqtt_host: {} \n  \
        mqtt_port: {} \n  \
        mqtt_client_id: {} \n  \
        mqtt_username: {} \n \
        mqtt_root_topic: {}",
        mqtt_host,
        mqtt_port,
        mqtt_client_id,
        mqtt_username.unwrap_or(""),
        mqtt_root_topic,
    );
    if mqtt_password.is_some() {
        info!("  mqtt_password: **** :)");
    } else {
        info!("  mqtt_password: No password provided");
    }

    #[cfg(feature = "copycat")]
    libits_client::pipeline::run::<CopyCat>(
        mqtt_host,
        mqtt_port,
        mqtt_client_id,
        mqtt_username,
        mqtt_password,
        mqtt_root_topic,
    )
    .await;

    warn!("ITS client done");
}
