// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2023 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas BUFFON <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use crate::client::configuration::Configuration;
use crate::exchange::Exchange;
use crate::transport::mqtt::topic::Topic;
use crate::transport::packet::Packet;

use crate::exchange::sequence_number::SequenceNumber;
use std::sync::{Arc, RwLock};

/// Structures implementing this trait can be used in [crate::client::application::pipeline::run]
/// to treat messages and eventually send or update other ones
///
/// Analyzer implementing structs must be able to run in parallel to be able to treat messages
/// faster than they arrive
/// All members are thus shared using [Arc] and [RwLock] when they can be modified by the analyzer
///
/// Example:
/// ```
/// use std::fmt::{Display, Formatter};
/// use std::str::FromStr;
/// use std::sync::{Arc, RwLock};
/// use libits::client::application::analyzer::Analyzer;
/// use libits::client::configuration::Configuration;use libits::exchange::Exchange;
/// use libits::exchange::message::Message;
/// use libits::exchange::sequence_number::SequenceNumber;
/// use libits::transport::mqtt::topic::Topic;
/// use libits::transport::packet::Packet;
///
/// struct Counts {
///     pub pedestrians: u32,
///     pub vehicles: u32,
/// }
///
/// struct CounterAnalyzer {
///     configuration: Arc<Configuration>,
///     context: Arc<RwLock<Counts>>,
/// }
///
/// #[derive(Clone, Default, Debug, PartialEq, Eq, Hash)]
/// struct StringTopic {
///     topic: String,
/// }
/// impl FromStr for StringTopic {
///     type Err = ();
///     fn from_str(s: &str) -> Result<Self, Self::Err> {
///         Ok(Self { topic: String::from(s)})
///     }
/// }
/// impl Display for StringTopic { ///
///     fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
///         write!(f, "{}", self.topic)
///     }
///  }
/// impl Topic for StringTopic {
///     fn as_route(&self) -> String {
///         self.topic.to_string()
///     }
/// }
///
/// impl Analyzer<StringTopic, Counts> for CounterAnalyzer {
///     fn new(configuration: Arc<Configuration>, context: Arc<RwLock<Counts>>, _: Arc<RwLock<SequenceNumber>>) -> Self where Self: Sized {
///         Self {
///             configuration,
///             context,
///         }
///     }
///
///     fn analyze(&mut self, packet: Packet<StringTopic, Exchange>) -> Vec<Packet<StringTopic, Exchange>> {
///         match packet.payload.message {
///             Message::CAM(cam) => {
///                 if let Some(station_type) = cam.basic_container.station_type {
///                     match station_type {
///                         1 => self.context.write().unwrap().pedestrians += 1,
///                         5 | 6 | 7 => self.context.write().unwrap().vehicles += 1,
///                         _ => ()
///                     }
///                 }
///             }
///             _ => ()
///         }
///
///         Vec::new()
///     }
/// }
/// ```
pub trait Analyzer<T: Topic, C> {
    fn new(
        configuration: Arc<Configuration>,
        context: Arc<RwLock<C>>,
        sequence_number: Arc<RwLock<SequenceNumber>>,
    ) -> Self
    where
        Self: Sized;

    fn analyze(&mut self, packet: Packet<T, Exchange>) -> Vec<Packet<T, Exchange>>;
}
