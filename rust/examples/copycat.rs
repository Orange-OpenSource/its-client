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

use std::path::Path;
use std::sync::mpsc::{Receiver, TryRecvError, channel};
use std::sync::{Arc, RwLock};

use clap::{Arg, Command};
use ini::Ini;
use libits::client::application::analyzer::Analyzer;
use libits::client::application::pipeline;
use libits::client::configuration::Configuration;
use libits::client::logger::create_stdout_logger;
use libits::exchange::Exchange;
use libits::exchange::sequence_number::SequenceNumber;
use libits::now;
use libits::transport::mqtt::geo_topic::GeoTopic;
use libits::transport::packet::Packet;
#[cfg(feature = "telemetry")]
use libits::transport::telemetry::init_tracer;
use log::{debug, info, warn};
use timer::MessageTimer;

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
        let timer = MessageTimer::new(tx);
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

        debug!("Item received: {:?}", packet);

        let clone = packet.clone();
        let content = packet.payload.message.as_content();

        // 1- delay the storage of the new item
        match content.as_mobile() {
            Ok(mobile_message) => {
                if packet.payload.source_uuid == component_name {
                    info!(
                        "We received an item as itself {} : we don't copy cat",
                        packet.payload.source_uuid
                    );
                } else {
                    let speed = mobile_message.speed().unwrap_or_default();
                    if speed <= 0.5 {
                        info!(
                            "We received an item from {} as stopped: we don't copy cat",
                            packet.payload.source_uuid
                        );
                    } else {
                        info!(
                            "We start to schedule from {} ({})",
                            packet.payload.source_uuid,
                            &mobile_message.id(),
                        );

                        let guard = self
                            .timer
                            .schedule_with_delay(chrono::Duration::seconds(3), clone);
                        guard.ignore();
                        debug!("Scheduling done");
                    }
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
                                "We treat the scheduled item {} from {} ({})",
                                data_found,
                                item.payload.source_uuid,
                                &mobile_message.id(),
                            );
                            let timestamp = now();

                            own_exchange.appropriate(&self.configuration, timestamp);

                            let mut own_topic = item.topic.clone();
                            own_topic.appropriate(&self.configuration);
                            item_to_publish.push(Packet::new(own_topic, own_exchange));

                            debug!("Item scheduled published");
                        }
                        Err(e) => match e {
                            TryRecvError::Empty => {
                                debug!("Delayed channel empty, we stop");
                                data_found = -1;
                            }
                            TryRecvError::Disconnected => {
                                warn!("Delayed channel disconnected, we stop");
                                data_found = -1;
                            }
                        },
                    }
                }
            }
            Err(e) => {
                warn!("Unable to get the Mobile in the content");
                debug!("Content error: {e:?}");
            }
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
        .get_matches();

    let configuration = Configuration::try_from(
        Ini::load_from_file(Path::new(
            matches.get_one::<String>("config-file-path").unwrap(),
        ))
        .expect("Failed to load config file as Ini"),
    )
    .expect("Failed to create Configuration from loaded Ini");

    let _logger = create_stdout_logger().expect("Logger initialization failed");

    let context = NoContext::default();
    let topics = vec![
        GeoTopic::from("default/outQueue/v2x/cam"),
        GeoTopic::from("default/outQueue/v2x/cpm"),
        GeoTopic::from("default/outQueue/v2x/denm"),
        GeoTopic::from("default/outQueue/v2x/info"),
    ];

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
