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

use std::fmt::{Debug, Display};
use std::hash::Hash;
use std::str::FromStr;

pub trait Topic:
    Default + Debug + Display + Clone + FromStr + ToString + Hash + PartialEq + Eq + Send + Sync
{
    /// Returns the topic part that can be used to route messages
    ///
    /// This is used to route the messages you receive from the broker
    /// Imagine your V2X client sends CAM messages on a topic like the following
    /// `/root/cam/client_1/anything`
    /// You might want to route it differently following the receiving application
    /// If you want to route the message using the message type this method should return `/root/cam`
    /// If you want to route the messages using the client this method should return `/root/cam/client_1`
    fn as_route(&self) -> String;
}
