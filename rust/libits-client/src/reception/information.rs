use std::str::FromStr;

use serde::{Deserialize, Serialize};

use crate::mqtt::topic::geo_extension::GeoExtension;
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
    pub instance_type: String,
    pub central_instance_id: Option<String>,
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

    pub fn is_in_region_of_responsibility(&self, geo_extension: GeoExtension) -> bool {
        if let Some(service_area) = &self.service_area {
            return service_area.quadkeys.iter().any(|quadkey| {
                if let Ok(service_area_geo_extension) = GeoExtension::from_str(quadkey) {
                    return geo_extension <= service_area_geo_extension;
                }
                false
            });
        }
        false
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

#[cfg(test)]
mod tests {
    use crate::mqtt::topic::geo_extension::GeoExtension;
    use crate::reception::information::{Information, ServiceArea};
    use std::str::FromStr;

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

    #[test]
    fn test_new() {
        assert_eq!(
            Information::new(),
            Information {
                type_field: "".to_string(),
                version: "".to_string(),
                instance_id: "broker".to_string(),
                instance_type: "".to_string(),
                central_instance_id: None,
                running: false,
                timestamp: 0,
                validity_duration: 0,
                public_ip_address: vec![],
                mqtt_ip: vec![],
                mqtt_tls_ip: vec![],
                http_proxy: vec![],
                ntp_servers: vec![],
                domain_name_servers: vec![],
                gelf_loggers: vec![],
                udp_loggers: vec![],
                fbeat_loggers: vec![],
                service_area: None,
                cells_id: vec![],
            }
        );
    }

    #[test]
    fn test_good_instance_id_number() {
        assert_eq!(
            Information {
                instance_id: "corp_role_32".to_string(),
                ..Default::default()
            }
            .instance_id_number(),
            32
        );
    }

    #[test]
    fn test_default_instance_id_number() {
        assert_eq!(Information::new().instance_id_number(), 31470);
    }

    #[test]
    fn test_bad_instance_id_number() {
        assert_eq!(
            Information {
                instance_id: "corp_32".to_string(),
                ..Default::default()
            }
            .instance_id_number(),
            31470
        );
        assert_eq!(
            Information {
                instance_id: "32".to_string(),
                ..Default::default()
            }
            .instance_id_number(),
            31470
        );
        assert_eq!(
            Information {
                instance_id: "".to_string(),
                ..Default::default()
            }
            .instance_id_number(),
            31470
        );
    }

    #[test]
    fn test_is_in_central_region_of_responsibility() {
        let information = generate_central_information();
        assert!(
            information.is_in_region_of_responsibility(GeoExtension::from_str("12020").unwrap()),
        );
        assert!(
            information.is_in_region_of_responsibility(GeoExtension::from_str("120203").unwrap()),
        );
        assert!(
            information.is_in_region_of_responsibility(GeoExtension::from_str("1202033").unwrap()),
        );
    }

    #[test]
    fn test_is_in_edge_region_of_responsibility() {
        let information = generate_edge_information();
        assert!(information
            .is_in_region_of_responsibility(GeoExtension::from_str("12020322313301023").unwrap()),);
        assert!(information
            .is_in_region_of_responsibility(GeoExtension::from_str("120203223133010232").unwrap()),);
        assert!(information.is_in_region_of_responsibility(
            GeoExtension::from_str("1202032231330102321013").unwrap()
        ),);
    }

    #[test]
    fn test_is_not_in_edge_region_of_responsibility() {
        let information = generate_edge_information();
        assert!(!information
            .is_in_region_of_responsibility(GeoExtension::from_str("1202032231330102").unwrap()));
        assert!(!information.is_in_region_of_responsibility(
            GeoExtension::from_str("1202032231330102103102").unwrap()
        ),);
        assert!(!information
            .is_in_region_of_responsibility(GeoExtension::from_str("12020322313300132").unwrap()),);
    }
}
