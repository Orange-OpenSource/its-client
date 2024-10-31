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

use std::fs;
use std::path::Path;
use std::sync::mpsc::{channel, Receiver, TryRecvError};
use std::sync::{Arc, RwLock};

use clap::{Arg, Command};
use flexi_logger::{
    with_thread, Cleanup, Criterion, Duplicate, FileSpec, Logger, Naming, WriteMode,
};
use ini::Ini;
use libits::client::application::analyzer::Analyzer;
use libits::client::application::pipeline;
use libits::client::configuration::Configuration;
use libits::exchange::sequence_number::SequenceNumber;
use libits::exchange::Exchange;
use libits::now;
use libits::transport::mqtt::geo_topic::GeoTopic;
use libits::transport::packet::Packet;
use log::{debug, info, warn};
use timer::MessageTimer;

#[cfg(feature = "telemetry")]
use libits::transport::telemetry::init_tracer;

pub struct CopyCat {
    configuration: Arc<Configuration>,
    item_receiver: Receiver<Packet<GeoTopic, Exchange>>,
    timer: MessageTimer<Packet<GeoTopic, Exchange>>,
}

#[derive(Default)]
struct NoContext {}

impl Analyzer<GeoTopic, NoContext> for CopyCat {
    fn new(
        configuration: Arc<Configuration>,
        _context: Arc<RwLock<NoContext>>,
        _: Arc<RwLock<SequenceNumber>>,
    ) -> Self
    where
        Self: Sized,
    {
        let (tx, item_receiver) = channel();
        let timer = timer::MessageTimer::new(tx);
        Self {
            configuration,
            item_receiver,
            timer,
        }
    }

    fn analyze(
        &mut self,
        mut packet: Packet<GeoTopic, Exchange>,
    ) -> Vec<Packet<GeoTopic, Exchange>> {
        let mut item_to_publish = Vec::new();
        let component_name = self.configuration.component_name(None);

        debug!("item received: {:?}", packet);

        let clone = packet.clone();
        let content = packet.payload.message.as_content();

        // 1- delay the storage of the new item
        match content.as_mobile() {
            Ok(mobile_message) => {
                let speed = mobile_message.speed().unwrap_or_default();
                if packet.payload.source_uuid == component_name || speed <= 0.5 {
                    info!(
                        "we received an item as itself {} or stopped: we don't copy cat",
                        packet.payload.source_uuid
                    );
                } else {
                    info!(
                        "we start to schedule {} from {}",
                        &mobile_message.id(),
                        packet.payload.source_uuid
                    );

                    let guard = self
                        .timer
                        .schedule_with_delay(chrono::Duration::seconds(3), clone);
                    guard.ignore();
                    debug!("scheduling done");
                }

                // 2- create the copy cat items for each removed delayed item
                let mut data_found = 0;
                while data_found >= 0 {
                    match self.item_receiver.try_recv() {
                        Ok(item) => {
                            data_found += 1;

                            //assumed clone, we create a new item
                            let mut own_exchange = item.payload.clone();
                            info!(
                                "we treat the scheduled item {} {} from {}",
                                data_found,
                                &mobile_message.id(),
                                item.payload.source_uuid
                            );
                            let timestamp = now();

                            own_exchange.appropriate(&self.configuration, timestamp);

                            let mut own_topic = item.topic.clone();
                            own_topic.appropriate(&self.configuration);
                            item_to_publish.push(Packet::new(own_topic, own_exchange));

                            debug!("item scheduled published");
                        }
                        Err(e) => match e {
                            TryRecvError::Empty => {
                                debug!("delayed channel empty, we stop");
                                data_found = -1;
                            }
                            TryRecvError::Disconnected => {
                                warn!("delayed channel disconnected, we stop");
                                data_found = -1;
                            }
                        },
                    }
                }
            }
            Err(e) => warn!("{}", e),
        }

        item_to_publish
    }
}

#[tokio::main(flavor = "multi_thread")]
async fn main() {
    let matches = Command::new("ITS CopyCat client")
        .version("0.2.3")
        .author("Frederic Gardes <frederic.gardes@orange.com>")
        .about(
            "CopyCat example creates 3 second delayed clones of incoming messages from MQTT broker",
        )
        .arg(
            Arg::new("config-file-path")
                .short('c')
                .long("config")
                .value_name("CONFIG_FILE_PATH")
                .default_value("examples/config.ini")
                .help("Path to the configuration file"),
        )
        .arg(
            Arg::new("mqtt-username")
                .short('u')
                .long("username")
                .value_name("MQTT_USERNAME")
                .help("Username used to connect to the MQTT broker"),
        )
        .arg(
            Arg::new("mqtt-password")
                .short('p')
                .long("password")
                .required(false)
                .value_name("MQTT_PASSWORD")
                .help("Password to use to connect to the MQTT broker"),
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
                .log_to_file(FileSpec::default().directory(log_path).suppress_timestamp())
                .write_mode(WriteMode::Async)
                .duplicate_to_stdout(Duplicate::All)
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

    let context = NoContext::default();
    let topics = vec![
        GeoTopic::from("default/outQueue/v2x/cam"),
        GeoTopic::from("default/outQueue/v2x/cpm"),
        GeoTopic::from("default/outQueue/v2x/denm"),
        GeoTopic::from("default/outQueue/v2x/cam"),
        GeoTopic::from("default/outQueue/info"),
    ];

    if let Some(username) = matches.get_one::<String>("mqtt-username") {
        let password = matches.get_one::<String>("mqtt-password");
        if password.is_none() {
            warn!("MQTT username provided with no password");
        }

        configuration
            .mqtt_options
            .set_credentials(username, password.unwrap_or(&String::new()));
    }

    #[cfg(feature = "telemetry")]
    init_tracer(&configuration.telemetry, "copycat").expect("Failed to init telemetry");

    pipeline::run::<CopyCat, NoContext, GeoTopic>(
        Arc::new(configuration),
        Arc::new(RwLock::new(context)),
        Arc::new(RwLock::new(SequenceNumber::new(u16::MAX.into()))),
        &topics,
    )
    .await;

    info!("CopyCat example exited");
}

#[cfg(test)]
mod tests {
    use std::sync::mpsc::channel;

    #[test]
    fn test_timer_schedule_with_delay() {
        let (tx, rx) = channel();
        let timer = timer::MessageTimer::new(tx);
        let _guard = timer.schedule_with_delay(chrono::Duration::seconds(3), 3);

        rx.recv().unwrap();
        println!("This code has been executed after 3 seconds");
    }
}
