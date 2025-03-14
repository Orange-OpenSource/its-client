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
use libits::client::configuration::{create_stdout_logger, Configuration};
use libits::transport::mqtt::mqtt_client::MqttClient;
use libits::transport::mqtt::mqtt_router::MqttRouter;
use libits::transport::mqtt::topic::Topic;
#[cfg(feature = "telemetry")]
use libits::transport::telemetry::init_tracer;
use log::{debug, error, info};
use rumqttc::v5::mqttbytes::v5::{Publish, PublishProperties};
use std::fmt::{Display, Formatter};
use std::str::FromStr;

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
        .mqtt_options
        .set_max_packet_size(Some(256_000));

    let (mut client, mut event_loop) = MqttClient::new(&configuration.mqtt_options);
    let mut router = MqttRouter::default();

    router.add_route(
        StrTopic::from_str("#").unwrap(),
        |publish: Publish| -> Option<(Box<dyn Any + 'static + Send>, PublishProperties)> {
            if let Ok(payload) = std::str::from_utf8(publish.payload.as_ref()) {
                if serde_json::from_str::<String>(payload).is_ok() {
                    Some((
                        Box::new(Ok::<(), &'static str>),
                        publish.properties.unwrap_or_default(),
                    ))
                } else {
                    Some((
                        Box::new(Err::<(), &'static str>("Failed to parse payload as JSON")),
                        publish.properties.unwrap_or_default(),
                    ))
                }
            } else {
                Some((
                    Box::new(Err::<(), &'static str>("Failed to parse payload as UTF-8")),
                    publish.properties.unwrap_or_default(),
                ))
            }
        },
    );
    #[cfg(feature = "telemetry")]
    init_tracer(&configuration.telemetry, "copycat").expect("Failed to init telemetry");

    client.subscribe(&["#".to_string()]).await;

    loop {
        match event_loop.poll().await {
            Ok(event) => {
                if let Some((_, result)) = router.handle_event::<StrTopic>(event) {
                    // TODO: Handle the result
                    debug!("{:?}", result);
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

#[cfg(test)]
mod tests {}
