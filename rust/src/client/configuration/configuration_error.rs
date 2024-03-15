// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2023 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas BUFFON <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use thiserror::Error;

#[derive(Error, Debug)]
pub enum ConfigurationError {
    #[error("Could not found field '{0}'")]
    FieldNotFound(&'static str),
    #[error("Cannot parse '{0}' due to invalid file type")]
    InvalidFileType(String),
    #[error("Configuration missing mandatory field {0} in section {1}")]
    MissingMandatoryField(&'static str, &'static str),
    #[error("Configuration missing mandatory section: {0}")]
    MissingMandatorySection(&'static str),
    #[error("No custom settings found in configuration")]
    NoCustomSettings,
    #[error("Could not found section '{0}'")]
    SectionNotFound(&'static str),
    #[error("Could parse value of field '{0}' as a '{1}'")]
    TypeError(&'static str, &'static str),
    #[error("Username provided with no password")]
    NoPassword,
}
