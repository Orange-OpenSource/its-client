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

use crate::client::configuration::Configuration;
use crate::transport::mqtt::topic::Topic;
use crate::transport::packet::Packet;

use crate::exchange::sequence_number::SequenceNumber;
use serde_json::Value;
use std::sync::{Arc, RwLock};

/// Trait allowing to analyze JSON payload packets
///
/// **Generics**
///
/// - InTopic: expected [Topic] type for analyzed packets
/// - OutTopic: [Topic] to publish created packets on
/// - C: structure type for shared context
///
pub trait JsonAnalyzer<InTopic: Topic, OutTopic: Topic, C> {
    /// Create a new JsonAnalyzer
    ///
    /// **Arguments**
    ///
    /// - configuration:   Loaded [Configuration] holding configuration dedicated to the analyzing
    ///                    application (see [Configuration::custom_settings])
    ///
    /// - context:         Shared reference to the context structure that will be shared by all the
    ///                    analyzer's instances if multi-threading is enabled in configuration
    ///                    (see `thread_count` in [MobilityConfiguration][1])
    ///
    /// - sequence_number: Shared reference to the [SequenceNumber] manager shared between all
    ///                    analyzer's instances if multi-threading is enabled in configuration
    ///                    (see `thread_count` in [MobilityConfiguration][1])
    ///                    _Note: this field is only relevant for [DENM][2]
    ///                    generating analyzers_
    ///
    /// [1]: crate::client::configuration::mobility_configuration::MobilityConfiguration
    /// [2]: crate::exchange::etsi::decentralized_environmental_notification_message::DecentralizedEnvironmentalNotificationMessage
    #[allow(rustdoc::private_intra_doc_links)]
    fn new(
        configuration: Arc<Configuration>,
        context: Arc<RwLock<C>>,
        sequence_number: Arc<RwLock<SequenceNumber>>,
    ) -> Self
    where
        Self: Sized;

    fn analyze(&mut self, packet: Packet<InTopic, Value>) -> Vec<Packet<OutTopic, Value>>;
}
