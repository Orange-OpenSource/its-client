use std::collections::HashMap;

use log::{debug, error, info};
use rumqttc::{Event, Incoming, Publish};

use crate::mqtt::topic::Topic;
use std::any::Any;
use std::str::FromStr;

type BoxedReception = Box<dyn Any + 'static + Send>;

type BoxedCallback = Box<dyn Fn(Publish) -> Option<BoxedReception>>;

pub(crate) struct Router {
    route_map: HashMap<String, BoxedCallback>,
}

impl Router {
    //
    pub(crate) fn new() -> Router {
        Router {
            route_map: HashMap::new(),
        }
    }

    //
    pub(crate) fn add_route<R, C>(&mut self, route: R, callback: C)
    where
        R: Into<String>,
        C: Fn(Publish) -> Option<BoxedReception> + 'static,
    {
        self.route_map.insert(route.into(), Box::new(callback));
        debug!("route added");
    }

    //
    // pub(crate) fn get_topic_list(&self) -> Vec<String> {
    //     self.route_map.keys().fold(Vec::new(), |mut route_list, route| {
    //         route_list.push(route.to_string());
    //         route_list
    //     })
    // }

    //
    pub(crate) fn handle_event(&mut self, event: Event) -> Option<(Topic, BoxedReception)> {
        match event {
            Event::Incoming(incoming) => match incoming {
                // Incoming publish from the broker
                Incoming::Publish(publish) => {
                    debug!(
                        "Publish received for the packet {:?} on the topic {}",
                        publish.pkid, publish.topic
                    );
                    // add manually the v2x server for the info message type: not provided :(
                    match Topic::from_str(publish.topic.replace("info", "v2x/info").as_ref()) {
                        Ok(topic) => match self
                            .route_map
                            .get(topic.project_base().replace("v2x/info", "info").as_str())
                        {
                            Some(callback) => {
                                if let Some(reception) = callback(publish) {
                                    return Some((topic, reception));
                                }
                            }
                            None => error!("a route hasn't got its destination"),
                        },
                        Err(error) => error!("unable to create a topic: {}", error),
                    };
                }
                // Incoming pub ack from the broker
                Incoming::PubAck(packet) => {
                    debug!("Publish Ack received for the packet {:?}", packet)
                }
                // Incoming pubrec from the broker
                Incoming::PubRec(packet) => {
                    debug!("Publish Rec received for the packet {:?}", packet)
                }
                // Incoming pubrel from the broker
                Incoming::PubRel(packet) => {
                    debug!("Publish Rel received for the packet {:?}", packet)
                }
                // Incoming pubcomp from the broker
                Incoming::PubComp(packet) => {
                    debug!("Publish Comp received for the packet {:?}", packet)
                }
                // Incoming sub ack from the broker
                Incoming::SubAck(suback) => debug!(
                    "Subscription Ack received for the packet {:?}: {:?}",
                    suback.pkid, suback.return_codes
                ),
                // Incoming unsub ack from the broker
                Incoming::UnsubAck(packet) => {
                    debug!("Unsubscription Ack received for the packet {:?}", packet)
                }
                // Incoming con ack ack from the broker
                Incoming::ConnAck(packet) => {
                    debug!("Con Ack Ack received for the packet {:?}", packet)
                }
                // Incoming subscribe from the broker
                Incoming::Subscribe(packet) => {
                    debug!("Subscribe received for the packet {:?}", packet)
                }
                // Incoming unsubscribe from the broker
                Incoming::Unsubscribe(packet) => {
                    debug!("Unsubscribe received for the packet {:?}", packet)
                }
                // Incoming ping request packet
                Incoming::PingReq => {
                    debug!("Ping request received")
                }
                // Incoming ping response packet
                Incoming::PingResp => {
                    debug!("Ping response received")
                }
                // Incoming connect packet
                Incoming::Connect(packet) => {
                    info!("Connect received for the packet {:?}", packet)
                }
                // Incoming disconnect packet
                Incoming::Disconnect => {
                    info!("Disconnect received")
                }
            },
            Event::Outgoing(outgoing) => debug!("outgoing: {:?}", outgoing),
        }
        Option::None
    }
}
