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

pub mod content;
pub mod content_error;
pub mod information;

use crate::exchange::etsi::collective_perception_message::CollectivePerceptionMessage;
use crate::exchange::etsi::cooperative_awareness_message::CooperativeAwarenessMessage;
use crate::exchange::etsi::cooperative_awareness_message_113::CooperativeAwarenessMessage113;
use crate::exchange::etsi::decentralized_environmental_notification_message::DecentralizedEnvironmentalNotificationMessage;
use crate::exchange::etsi::map_extended_message::MAPExtendedMessage;
use crate::exchange::etsi::signal_phase_and_timing_extended_message::SignalPhaseAndTimingExtendedMessage;
use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
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
    MAPEM(MAPExtendedMessage),
    SPATEM(SignalPhaseAndTimingExtendedMessage),

    CAM113(CooperativeAwarenessMessage113),
}

impl Message {
    pub fn as_content(&mut self) -> &mut dyn Content {
        match self {
            Self::CAM(v) => v,
            Self::CPM(v) => v,
            Self::DENM(v) => v,
            Self::MAPEM(v) => v,
            Self::SPATEM(v) => v,
            Self::CAM113(v) => v,
        }
    }
}
