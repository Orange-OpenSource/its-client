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

/// Represents a CAM message in ASN.1 binary format.
///
/// When `message_format` is "asn1", the message content is a base64-encoded binary payload.
#[derive(Clone, Debug, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CamBinaryPayload {
    /// ASN.1 PDU version string.
    pub version: String,
    /// Base64-encoded ASN.1 binary payload.
    pub payload: String,
}

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

    CAMBinary(CamBinaryPayload),
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
            Self::CAMBinary(v) => v,
            Self::CAM113(v) => v,
        }
    }
}

impl Content for CamBinaryPayload {
    fn get_type(&self) -> &str {
        "cam"
    }

    fn appropriate(&mut self, _timestamp: u64, _new_station_id: u32) {
        // Binary payloads cannot be modified
    }

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError> {
        Err(ContentError::NotAMortal("CamBinaryPayload"))
    }

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError> {
        Err(ContentError::NotAMortal("CamBinaryPayload"))
    }
}
