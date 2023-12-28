// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use crate::exchange::message::content::Content;
use crate::exchange::mortal::Mortal;
use crate::mobility::mobile::Mobile;
use std::any::type_name;
use std::ops::{Deref, DerefMut};

use crate::exchange::message::content_error::ContentError;
use crate::exchange::message::content_error::ContentError::{NotAMobile, NotAMortal};
use crate::transport::payload::Payload;
use serde::{Deserialize, Serialize};

/// Client or server information message
///
/// The message carries information about an instance involved in V2X message exchanges
/// It can be either a server hosting a broker and/or application(s) that consume/produce messages
/// or a client sending messages (OBU/RSU)
///
/// The corresponding JSON schema of this message struct can be found in this projects [schema directory][1]
///
/// [1]: https://github.com/Orange-OpenSource/its-client/tree/master/schema
#[serde_with::skip_serializing_none]
#[derive(Clone, Debug, Default, PartialEq, Serialize, Deserialize)]
pub struct Information {
    #[serde(rename = "type")]
    pub type_field: String,
    pub version: String,
    pub instance_id: String,
    pub instance_type: String,
    pub central_instance_id: Option<String>,
    pub running: bool,
    pub timestamp: u64,
    pub validity_duration: u32,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    public_ip_address: Vec<String>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    mqtt_ip: Vec<String>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    mqtt_tls_ip: Vec<String>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    http_proxy: Vec<String>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    ntp_servers: Vec<String>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    domain_name_servers: Vec<String>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    gelf_loggers: Vec<String>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    udp_loggers: Vec<String>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    fbeat_loggers: Vec<String>,
    pub service_area: Option<ServiceArea>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    cells_id: Vec<u32>,
}

#[serde_with::skip_serializing_none]
#[derive(Clone, Debug, Default, PartialEq, Serialize, Deserialize)]
pub struct ServiceArea {
    #[serde(rename = "type")]
    pub type_field: String,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    coordinates: Vec<f32>,
    radius: Option<u32>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    vertices: Vec<Vertex>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub quadkeys: Vec<String>,
}

#[serde_with::skip_serializing_none]
#[derive(Clone, Debug, Default, PartialEq, Serialize, Deserialize)]
pub struct Vertex {
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    coordinates: Vec<f32>,
}

impl Information {
    pub const TYPE: &'static str = "info";
}

impl Content for Information {
    fn get_type(&self) -> &str {
        Self::TYPE
    }

    fn appropriate(&mut self) {
        todo!()
    }

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError> {
        Err(NotAMobile(type_name::<Information>()))
    }

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError> {
        Err(NotAMortal(type_name::<Information>()))
    }
}

impl Mortal for Information {
    fn timeout(&self) -> u64 {
        self.timestamp + u64::from(self.validity_duration) * 1000_u64
    }

    fn terminate(&mut self) {
        self.validity_duration = 0
    }

    fn terminated(&self) -> bool {
        self.expired()
    }
}

impl Payload for Information {}

/// Making Information as a [Message][1] enum variant triggers Clippy's [large enum variant][2] warning
/// All other variant are going to be used more than this one so box it to avoid making the enum size
/// grow unnecessarily
///
/// As enum variant are required to implement specific traits here is declared a dedicated type to
/// impl these traits
///
/// [1]: crate::exchange::message::Message
/// [2]: https://rust-lang.github.io/rust-clippy/master/index.html#/large_enum_variant
pub type BoxedInformation = Box<Information>;
impl Content for BoxedInformation {
    fn get_type(&self) -> &str {
        (*self).deref().get_type()
    }

    fn appropriate(&mut self) {
        (*self).deref_mut().appropriate();
    }

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError> {
        (*self).deref().as_mobile()
    }

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError> {
        (*self).deref().as_mortal()
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::message::information::{Information, ServiceArea};

    fn generate_central_information() -> Information {
        Information {
            instance_id: "corp_role_32".to_string(),
            service_area: Some(ServiceArea {
                quadkeys: vec!["12020".to_string()],
                ..Default::default()
            }),
            ..Default::default()
        }
    }

    fn generate_edge_information() -> Information {
        Information {
            instance_id: "corp_role_32".to_string(),
            service_area: Some(ServiceArea {
                quadkeys: vec![
                    "1202032231330103".to_string(),
                    "12020322313211".to_string(),
                    "12020322313213".to_string(),
                    "12020322313302".to_string(),
                    "12020322313230".to_string(),
                    "12020322313221".to_string(),
                    "12020322313222".to_string(),
                    "120203223133032".to_string(),
                    "120203223133030".to_string(),
                    "120203223133012".to_string(),
                    "120203223133003".to_string(),
                    "120203223133002".to_string(),
                    "120203223133000".to_string(),
                    "120203223132103".to_string(),
                    "120203223132121".to_string(),
                    "120203223132123".to_string(),
                    "120203223132310".to_string(),
                    "120203223132311".to_string(),
                    "120203223132122".to_string(),
                    "120203223132033".to_string(),
                    "120203223132032".to_string(),
                    "120203223132023".to_string(),
                    "120203223132201".to_string(),
                    "120203223132203".to_string(),
                    "120203223132202".to_string(),
                    "120203223123313".to_string(),
                    "120203223123331".to_string(),
                    "120203223123333".to_string(),
                    "120203223132230".to_string(),
                    "12020322313300133".to_string(),
                    "12020322313301022".to_string(),
                    "12020322313301023".to_string(),
                ],
                ..Default::default()
            }),
            ..Default::default()
        }
    }
}
