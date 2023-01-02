// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
use std::sync::mpsc::{channel, Receiver, TryRecvError};
use std::sync::Arc;

use log::{debug, info, trace, warn};
use timer::MessageTimer;

use libits_client::analyse::analyser::Analyser;
use libits_client::analyse::configuration::Configuration;
use libits_client::analyse::item::Item;
use libits_client::reception::exchange::Exchange;
use libits_client::reception::mortal::now;

pub struct CopyCat {
    configuration: Arc<Configuration>,
    item_receiver: Receiver<Item<Exchange>>,
    timer: MessageTimer<Item<Exchange>>,
}

impl Analyser for CopyCat {
    fn new(configuration: Arc<Configuration>) -> Self
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

    fn analyze(&mut self, item: Item<Exchange>) -> Vec<Item<Exchange>> {
        let mut item_to_publish = Vec::new();
        let component_name = self.configuration.component_name(None);

        trace!("item received: {:?}", item);

        // 1- delay the storage of the new item
        match item.reception.message.as_mobile() {
            Ok(mobile_message) => {
                if item.reception.source_uuid == component_name || mobile_message.stopped() {
                    info!(
                        "we received an item as itself {} or stopped: we don't copy cat",
                        item.reception.source_uuid
                    );
                } else {
                    info!(
                        "we start to schedule {} from {}",
                        &mobile_message.mobile_id(),
                        item.reception.source_uuid
                    );
                    // FIXME adding `as_mobile()` method forced me to clone the item here
                    //       as a quick solve; to be investigated...
                    let guard = self
                        .timer
                        .schedule_with_delay(chrono::Duration::seconds(3), item.clone());
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
                            let mut own_exchange = item.reception.clone();
                            info!(
                                "we treat the scheduled item {} {} from {}",
                                data_found,
                                &mobile_message.mobile_id(),
                                item.reception.source_uuid
                            );
                            let timestamp = now();
                            own_exchange.appropriate(&self.configuration, timestamp);
                            let mut own_topic = item.topic.clone();
                            own_topic.appropriate(&self.configuration);
                            item_to_publish.push(Item::new(own_topic, own_exchange));
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
        // 3- send the copy cat items
        item_to_publish
    }
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
