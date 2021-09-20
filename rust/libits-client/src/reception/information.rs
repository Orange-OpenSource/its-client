use std::str::FromStr;

use serde::{Deserialize, Serialize};

use crate::reception::mortal::Mortal;
use crate::reception::typed::Typed;
use crate::reception::Reception;

#[serde_with::skip_serializing_none]
#[derive(Clone, Debug, Default, PartialEq, Serialize, Deserialize)]
pub struct Information {
    #[serde(rename = "type")]
    pub type_field: String,
    pub version: String,
    pub instance_id: String,
    pub running: bool,
    pub timestamp: u128,
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
    pub role: String,
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
    quadkeys: Vec<String>,
}

#[serde_with::skip_serializing_none]
#[derive(Clone, Debug, Default, PartialEq, Serialize, Deserialize)]
pub struct Vertex {
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    coordinates: Vec<f32>,
}

impl Information {
    pub(crate) fn new() -> Self {
        Information {
            instance_id: "broker".to_string(),
            ..Default::default()
        }
    }

    pub(crate) fn instance_id_number(&self) -> u32 {
        let instance_id_split: Vec<&str> = self.instance_id.split("_").collect();
        // TODO generate a cache to not compute the same value each time again
        match instance_id_split.get(2) {
            Some(number) => u32::from_str(number).unwrap_or(31470),
            None => 31470,
        }
    }
}

impl Typed for Information {
    fn get_type() -> String {
        "info".to_string()
    }
}

impl Mortal for Information {
    fn timeout(&self) -> u128 {
        self.timestamp + self.validity_duration as u128 * 1000
    }

    fn terminate(&mut self) {
        self.validity_duration = 0
    }

    fn terminated(&self) -> bool {
        self.expired()
    }
}

impl Reception for Information {}
