// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas BUFFON <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use crate::client::configuration::Configuration;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::mortal::Mortal;
use crate::mobility::mobile::Mobile;
use enum_dispatch::enum_dispatch;

#[enum_dispatch(Message)]
pub trait Content {
    fn get_type(&self) -> &str;

    fn appropriate(&mut self, configuration: &Configuration, timestam: u64);

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError>;

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError>;
}
