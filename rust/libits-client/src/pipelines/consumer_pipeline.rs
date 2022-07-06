// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use std::collections::HashMap;
use std::sync::mpsc::Receiver;
use std::sync::Arc;
use std::thread;
use std::thread::JoinHandle;
use std::time::Duration;

use log::{debug, info, trace, warn};

use crate::analyse::analyser::Analyser;
use crate::analyse::configuration::Configuration;
use crate::analyse::item::Item;
use crate::mqtt::mqtt_client;
use crate::pipelines::{monitor_thread, mqtt_client_listen_thread, mqtt_client_subscribe, mqtt_router_dispatch_thread, reader_configure_thread};
use crate::reception::exchange::Exchange;

pub async fn run<T: Analyser<()>>(
    mqtt_host: &str,
    mqtt_port: u16,
    mqtt_client_id: &str,
    mqtt_username: Option<&str>,
    mqtt_password: Option<&str>,
    mqtt_root_topic: &str,
    region_of_responsibility: bool,
    custom_settings: HashMap<String, String>,
) {
    loop {
        // build the shared topic list
        let topic_list = vec![
            format!("{}/v2x/cam", mqtt_root_topic),
            format!("{}/v2x/cpm", mqtt_root_topic),
            format!("{}/v2x/denm", mqtt_root_topic),
            format!("{}/info", mqtt_root_topic),
        ];

        //initialize the client
        let (mut client, event_loop) = mqtt_client::Client::new(
            mqtt_host,
            mqtt_port,
            mqtt_client_id,
            mqtt_username,
            mqtt_password,
        );

        let configuration = Arc::new(Configuration::new(
            mqtt_client_id.to_string(),
            region_of_responsibility,
            custom_settings.clone(),
        ));

        // subscribe
        mqtt_client_subscribe(&topic_list, &mut client).await;

        // receive
        let (event_receiver, mqtt_client_listen_handle) = mqtt_client_listen_thread(event_loop);
        // dispatch
        let (item_receiver, monitoring_receiver, information_receiver, mqtt_router_dispatch_handle) =
            mqtt_router_dispatch_thread(topic_list, event_receiver);

        // in parallel, monitor exchanges reception
        let monitor_reception_handle = monitor_thread(
            "received_on".to_string(),
            configuration.clone(),
            monitoring_receiver,
        );

        // in parallel, analyse exchanges
        let analyser_generate_handle =
            analyser_generate_thread::<T>(configuration.clone(), item_receiver);

        // read information
        let reader_configure_handle =
            reader_configure_thread(configuration.clone(), information_receiver);

        debug!("mqtt_client_listen_handler joining...");
        mqtt_client_listen_handle.await.unwrap();
        debug!("mqtt_router_dispatch_handler joining...");
        mqtt_router_dispatch_handle.join().unwrap();
        debug!("monitor_reception_handle joining...");
        monitor_reception_handle.join().unwrap();
        debug!("reader_configure_handler joining...");
        reader_configure_handle.join().unwrap();
        debug!("analyser_generate_handler joining...");
        analyser_generate_handle.join().unwrap();

        warn!("loop done");
        tokio::time::sleep(Duration::from_secs(5)).await;
    }
}

fn analyser_generate_thread<T: Analyser<()>>(
    configuration: Arc<Configuration>,
    exchange_receiver: Receiver<Item<Exchange>>,
) -> JoinHandle<()> {
    info!("starting analyser generation...");
    let handle = thread::Builder::new()
        .name("analyser-generator".into())
        .spawn(move || {
            trace!("analyser generation closure entering...");
            //initialize the analyser
            let mut analyser = T::new(configuration);
            for item in exchange_receiver {
                analyser.analyze(item.clone());
            }
        })
        .unwrap();
    info!("analyser generation started");
    handle
}






