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

use crate::exchange::etsi::cooperative_awareness_message::CooperativeAwarenessMessage;
use crate::exchange::message::content::Content;
use crate::mobility::mobile::Mobile;
use enum_dispatch::enum_dispatch;
use serde::{Deserialize, Serialize};

#[derive(Clone, Debug, PartialEq, Serialize, Deserialize)]
#[enum_dispatch]
#[allow(clippy::upper_case_acronyms)]
enum Message {
    CAM(CooperativeAwarenessMessage),
}
