// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2023 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas BUFFON <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

pub mod content;
pub mod content_error;
pub mod information;

use crate::client::configuration::Configuration;
use crate::exchange::etsi::collective_perception_message::CollectivePerceptionMessage;
use crate::exchange::etsi::cooperative_awareness_message::CooperativeAwarenessMessage;
use crate::exchange::etsi::decentralized_environmental_notification_message::DecentralizedEnvironmentalNotificationMessage;
use crate::exchange::etsi::map_extended_message::MAPExtendedMessage;
use crate::exchange::etsi::signal_phase_and_timing_extended_message::SignalPhaseAndTimingExtendedMessage;
use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::message::information::BoxedInformation;
use crate::exchange::mortal::Mortal;
use crate::mobility::mobile::Mobile;
use enum_dispatch::enum_dispatch;
use serde::{Deserialize, Serialize};

#[enum_dispatch]
#[allow(clippy::upper_case_acronyms)]
#[derive(Clone, Debug, PartialEq, Serialize, Deserialize)]
#[serde(untagged)]
pub enum Message {
    CAM(CooperativeAwarenessMessage),
    CPM(CollectivePerceptionMessage),
    DENM(DecentralizedEnvironmentalNotificationMessage),
    INFO(BoxedInformation),
    MAPEM(MAPExtendedMessage),
    SPATEM(SignalPhaseAndTimingExtendedMessage),
}

impl Message {
    pub fn as_content(&mut self) -> &mut dyn Content {
        match self {
            Self::CAM(v) => v,
            Self::CPM(v) => v,
            Self::DENM(v) => v,
            Self::INFO(v) => v,
            Self::MAPEM(v) => v,
            Self::SPATEM(v) => v,
        }
    }
}
