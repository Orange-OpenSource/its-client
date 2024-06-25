/*
 * Software Name : libits
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 * Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) library based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
 */

use std::collections::HashMap;

use log::{error, info, trace, warn};
use rumqttc::v5::mqttbytes::v5::Publish;
use rumqttc::v5::{Event, Incoming};

use crate::transport::mqtt::topic::Topic;
use std::any::{type_name, Any};
use std::str::from_utf8;

type BoxedReception = Box<dyn Any + 'static + Send>;

type BoxedCallback = Box<dyn Fn(Publish) -> Option<BoxedReception>>;

pub(crate) struct MqttRouter {
    route_map: HashMap<String, BoxedCallback>,
}

impl MqttRouter {
    pub(crate) fn new() -> MqttRouter {
        MqttRouter {
            route_map: HashMap::new(),
        }
    }

    pub(crate) fn add_route<T, C>(&mut self, topic: T, callback: C)
    where
        T: Topic,
        C: Fn(Publish) -> Option<BoxedReception> + 'static,
    {
        self.route_map.insert(topic.as_route(), Box::new(callback));
        info!("Registered route for topic: {}", topic.as_route());
    }

    pub(crate) fn handle_event<T: Topic>(&mut self, event: Event) -> Option<(T, BoxedReception)> {
        match event {
            Event::Incoming(incoming) => match incoming {
                Incoming::Publish(publish) => {
                    match from_utf8(&publish.topic) {
                        Ok(str_topic) => {
                            trace!(
                                "Publish received for the packet {:?} on the topic {}",
                                publish.pkid,
                                str_topic,
                            );

                            match T::from_str(str_topic) {
                                Ok(topic) => match self.route_map.get(&topic.as_route()) {
                                    Some(callback) => {
                                        if let Some(reception) = callback(publish) {
                                            return Some((topic, reception));
                                        }
                                    }
                                    None => {
                                        warn!("No route found for topic '{}'", topic);
                                    }
                                },
                                // FIXME how to print this error ?
                                Err(_error) => {
                                    error!("Failed to create {} from string", type_name::<T>(),)
                                }
                            };
                        }
                        Err(e) => {
                            warn!("Failed to parse topic as UTF-8: {:?}", e);
                        }
                    }
                }
                Incoming::PubAck(packet) => {
                    trace!("Publish Ack received for the packet {:?}", packet)
                }
                Incoming::PubRec(packet) => {
                    trace!("Publish Rec received for the packet {:?}", packet)
                }
                Incoming::PubRel(packet) => {
                    trace!("Publish Rel received for the packet {:?}", packet)
                }
                Incoming::PubComp(packet) => {
                    trace!("Publish Comp received for the packet {:?}", packet)
                }
                Incoming::SubAck(suback) => trace!(
                    "Subscription Ack received for the packet {:?}: {:?}",
                    suback.pkid,
                    suback.return_codes
                ),
                Incoming::UnsubAck(packet) => {
                    trace!("Unsubscription Ack received for the packet {:?}", packet)
                }
                Incoming::ConnAck(packet) => {
                    trace!("Con Ack Ack received for the packet {:?}", packet)
                }
                Incoming::Subscribe(packet) => {
                    trace!("Subscribe received for the packet {:?}", packet)
                }
                Incoming::Unsubscribe(packet) => {
                    trace!("Unsubscribe received for the packet {:?}", packet)
                }
                Incoming::PingReq(packet) => {
                    trace!("Ping request received: {:?}", packet)
                }
                Incoming::PingResp(packet) => {
                    trace!("Ping response received: {:?}", packet)
                }
                // FIXME log about last will and login
                Incoming::Connect(packet, _last_will, _login) => {
                    info!("Connect received for the packet {:?}", packet)
                }
                Incoming::Disconnect(packet) => {
                    info!("Disconnect received: {:?}", packet)
                }
            },
            Event::Outgoing(outgoing) => trace!("outgoing: {:?}", outgoing),
        }
        None
    }
}
