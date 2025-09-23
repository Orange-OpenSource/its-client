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

use crate::exchange::mortal::Mortal;
use crate::transport::payload::Payload;
use serde::{Deserialize, Serialize};

/// Represents a client or server information message.
///
/// This message carries information about an instance involved in V2X message exchanges.
/// It can represent either:
/// - A server hosting a broker and/or applications that consume/produce messages.
/// - A client sending messages (e.g., OBU/RSU).
/// It implements the schema defined in the Information [Information version 2.1.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/information/information_schema_2-1-0.json
#[serde_with::skip_serializing_none]
#[derive(Clone, Debug, PartialEq, Serialize, Deserialize)]
pub struct Information {
    /// Type of message
    #[serde(default = "default_message_type")]
    pub message_type: String,
    /// Version of the message format
    #[serde(default = "default_version")]
    pub version: String,
    /// Unique identifier of the source
    pub source_uuid: String,
    /// Timestamp in milliseconds since UNIX epoch
    pub timestamp: u64,
    /// Instance identifier
    pub instance_id: String,
    /// Type of instance (Central, Edge, Local)
    pub instance_type: InstanceType,
    /// Identifier of the central instance if applicable
    pub central_instance_id: Option<String>,
    /// Running status
    #[serde(default = "default_false")]
    pub running: bool,
    /// Validity duration in seconds, when 0 the instance is terminated
    pub validity_duration: u32,
    /// Public IP addresses of the instance
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub public_ip_address: Vec<String>,
    /// MQTT endpoints without TLS
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub mqtt_ip: Vec<String>,
    /// MQTT endpoints with TLS
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub mqtt_tls_ip: Vec<String>,
    /// HTTP endpoints without TLS
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub http_ip: Vec<String>,
    /// HTTP endpoints with TLS
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub http_tls_ip: Vec<String>,
    /// HTTP proxy endpoints
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub http_proxy: Vec<String>,
    /// NTP server endpoints
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub ntp_servers: Vec<String>,
    /// DNS server endpoints
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub domain_name_servers: Vec<String>,
    /// Service area definition
    pub service_area: Option<ServiceArea>,
    /// List of cell identifiers
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub cells_id: Vec<u32>,
}

/// Returns the default message type.
fn default_message_type() -> String {
    "information".to_string()
}

/// Returns the default version.
fn default_version() -> String {
    "2.1.0".to_string()
}

/// Returns the default value for a boolean as `false`.
fn default_false() -> bool {
    false
}

impl Default for Information {
    /// Provides a default implementation for the `Information` struct.
    /// Sets default values for fields where applicable.
    fn default() -> Self {
        Self {
            // Initialize using the default value for the message type
            message_type: default_message_type(),
            version: default_version(),
            source_uuid: String::new(),
            timestamp: 0,
            instance_id: String::new(),
            instance_type: InstanceType::default(),
            central_instance_id: None,
            running: default_false(),
            validity_duration: 0,
            public_ip_address: Vec::new(),
            mqtt_ip: Vec::new(),
            mqtt_tls_ip: Vec::new(),
            http_ip: Vec::new(),
            http_tls_ip: Vec::new(),
            http_proxy: Vec::new(),
            ntp_servers: Vec::new(),
            domain_name_servers: Vec::new(),
            service_area: None,
            cells_id: Vec::new(),
        }
    }
}

/// Represents the type of instance.
///
/// Possible values:
/// - `Local`: A local instance.
/// - `Edge`: An edge instance.
/// - `Central`: A central instance (default).
#[derive(Clone, Debug, Default, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "lowercase")]
pub enum InstanceType {
    /// A local instance.
    Local,
    /// An edge instance.
    Edge,
    /// A central instance (default).
    #[default]
    Central,
}

/// Represents the service area of an instance.
///
/// Service area can be one of the following:
/// - `Point`: A specific point with coordinates and an optional radius.
/// - `Polygon`: A polygon defined by a list of vertices.
/// - `Tiles`: A set of tiles identified by quadkeys.
#[derive(Clone, Debug, Serialize, Deserialize, PartialEq)]
#[serde(tag = "type")]
pub enum ServiceArea {
    /// A point with coordinates and an optional radius.
    #[serde(rename = "point")]
    Point {
        coordinates: [f32; 2],
        radius: Option<u32>,
    },
    /// A polygon defined by a list of vertices.
    #[serde(rename = "polygon")]
    Polygon { vertices: Vec<[f32; 2]> },
    /// A set of tiles identified by quadkeys.
    #[serde(rename = "tiles")]
    Tiles { quadkeys: Vec<String> },
}

impl Default for ServiceArea {
    /// Provides a default implementation for the `ServiceArea` enum.
    /// Defaults to the `Tiles` variant with an empty list of quadkeys.
    fn default() -> Self {
        ServiceArea::Tiles {
            quadkeys: Vec::new(),
        }
    }
}

/// Represents a vertex with coordinates.
///
/// This struct is used to define vertices for polygons or other geometric shapes.
#[serde_with::skip_serializing_none]
#[derive(Clone, Debug, Default, PartialEq, Serialize, Deserialize)]
pub struct Vertex {
    /// Coordinates of the vertex.
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    coordinates: Vec<f32>,
}

impl Information {
    /// A constant representing the type of the information message.
    pub const TYPE: &'static str = "info";

    /// Replaces the current `Information` instance with a new one.
    ///
    /// # Arguments
    ///
    /// * `new_info` - New `Information` instance to replace the current one.
    pub fn replace(&mut self, new_info: Information) {
        *self = new_info;
    }
}

impl Mortal for Information {
    /// Calculates the timeout for the `Information` instance.
    ///
    /// Timeout is calculated as the sum of the timestamp and the validity duration (in milliseconds).
    fn timeout(&self) -> u64 {
        self.timestamp + u64::from(self.validity_duration) * 1000_u64
    }

    /// Terminates the `Information` instance by setting its validity duration to 0.
    fn terminate(&mut self) {
        self.validity_duration = 0
    }

    /// Checks if the `Information` instance has been terminated.
    ///
    /// Returns `true` if the instance has expired, otherwise `false`.
    fn terminated(&self) -> bool {
        self.expired()
    }
}

impl Payload for Information {}

/// A boxed type for the `Information` struct.
///
/// This is used to avoid increasing the size of enums that include `Information` as a variant.
pub type BoxedInformation = Box<Information>;

#[cfg(test)]
mod tests {
    use crate::exchange::message::information::{Information, InstanceType, ServiceArea};

    fn generate_central_information() -> Information {
        Information {
            instance_id: "corp_role_32".to_string(),
            service_area: Some(ServiceArea::Tiles {
                quadkeys: vec!["12020".to_string()],
            }),
            ..Default::default()
        }
    }

    fn generate_edge_information() -> Information {
        Information {
            instance_id: "corp_role_32".to_string(),
            service_area: Some(ServiceArea::Tiles {
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
            }),
            ..Default::default()
        }
    }

    fn generate_default_information() -> Information {
        Information {
            message_type: "information".to_string(),
            source_uuid: "default_uuid".to_string(),
            timestamp: 0,
            version: "2.1.0".to_string(),
            instance_id: "default_instance".to_string(),
            instance_type: InstanceType::Central,
            central_instance_id: None,
            running: false,
            validity_duration: 0,
            public_ip_address: vec![],
            mqtt_ip: vec![],
            mqtt_tls_ip: vec![],
            http_ip: vec![],
            http_tls_ip: vec![],
            http_proxy: vec![],
            ntp_servers: vec![],
            domain_name_servers: vec![],
            service_area: None,
            cells_id: vec![],
        }
    }

    fn generate_point_service_area() -> ServiceArea {
        ServiceArea::Point {
            coordinates: [1.0, 2.0],
            radius: Some(10),
        }
    }

    fn generate_polygon_service_area() -> ServiceArea {
        ServiceArea::Polygon {
            vertices: vec![[1.0, 2.0], [3.0, 4.0], [5.0, 6.0]],
        }
    }

    fn generate_tiles_service_area() -> ServiceArea {
        ServiceArea::Tiles {
            quadkeys: vec!["12020".to_string(), "12021".to_string()],
        }
    }

    #[test]
    fn information_has_correct_defaults() {
        let info = Information::default();
        assert_eq!(info.message_type, "information");
        assert_eq!(info.version, "2.1.0");
        assert!(!info.running);
        assert!(info.service_area.is_none());
    }

    #[test]
    fn default_information_has_correct_defaults() {
        let info = generate_default_information();
        assert_eq!(info.message_type, "information");
        assert_eq!(info.version, "2.1.0");
        assert!(!info.running);
        assert!(info.service_area.is_none());
    }

    #[test]
    fn central_information_has_correct_defaults() {
        let info = generate_central_information();
        assert_eq!(info.instance_id, "corp_role_32");
        assert_eq!(info.instance_type, InstanceType::Central);
        assert_eq!(info.message_type, "information");
        assert_eq!(info.version, "2.1.0");
        if let Some(ServiceArea::Tiles { quadkeys }) = info.service_area {
            assert_eq!(quadkeys, vec!["12020"]);
        } else {
            panic!("Expected ServiceArea::Tiles variant");
        }
    }

    #[test]
    fn edge_information_has_correct_defaults() {
        let info = generate_edge_information();
        assert_eq!(info.instance_id, "corp_role_32");
        assert_eq!(info.instance_type, InstanceType::Central);
        assert_eq!(info.message_type, "information");
        assert!(info.service_area.is_some());
        if let Some(ServiceArea::Tiles { quadkeys }) = info.service_area {
            assert!(!quadkeys.is_empty());
            assert_eq!(quadkeys[0], "1202032231330103");
        } else {
            panic!("Expected ServiceArea::Tiles variant");
        }
    }

    #[test]
    fn service_area_point_is_serialized_correctly() {
        let service_area = generate_point_service_area();
        if let ServiceArea::Point {
            coordinates,
            radius,
        } = service_area
        {
            assert_eq!(coordinates, [1.0, 2.0]);
            assert_eq!(radius, Some(10));
        } else {
            panic!("Expected ServiceArea::Point variant");
        }
    }

    #[test]
    fn service_area_polygon_is_serialized_correctly() {
        let service_area = generate_polygon_service_area();
        if let ServiceArea::Polygon { vertices } = service_area {
            assert_eq!(vertices.len(), 3);
            assert_eq!(vertices[0], [1.0, 2.0]);
        } else {
            panic!("Expected ServiceArea::Polygon variant");
        }
    }

    #[test]
    fn service_area_tiles_is_serialized_correctly() {
        let service_area = generate_tiles_service_area();
        if let ServiceArea::Tiles { quadkeys } = service_area {
            assert_eq!(quadkeys.len(), 2);
            assert_eq!(quadkeys[0], "12020");
        } else {
            panic!("Expected ServiceArea::Tiles variant");
        }
    }

    #[test]
    fn information_replace_updates_all_fields() {
        let mut info = generate_default_information();
        let new_info = Information {
            message_type: "new_type".to_string(),
            source_uuid: "new_uuid".to_string(),
            timestamp: 12345,
            version: "2.1.0".to_string(),
            instance_id: "new_instance".to_string(),
            instance_type: InstanceType::Edge,
            central_instance_id: Some("central_id".to_string()),
            running: true,
            validity_duration: 3600,
            public_ip_address: vec!["192.168.1.1".to_string()],
            mqtt_ip: vec!["mqtt.example.com".to_string()],
            mqtt_tls_ip: vec!["mqtts.example.com".to_string()],
            http_ip: vec!["http.example.com".to_string()],
            http_tls_ip: vec!["https.example.com".to_string()],
            http_proxy: vec!["proxy.example.com".to_string()],
            ntp_servers: vec!["ntp.example.com".to_string()],
            domain_name_servers: vec!["dns.example.com".to_string()],
            service_area: Some(generate_tiles_service_area()),
            cells_id: vec![1, 2, 3],
        };
        info.replace(new_info.clone());
        assert_eq!(info, new_info);
    }

    #[test]
    fn test_default_information() {
        let info = Information::default();
        assert_eq!(info.message_type, "information");
        assert_eq!(info.version, "2.1.0");
        assert!(!info.running);
        assert!(info.service_area.is_none());
    }

    #[test]
    fn test_serialize_information() {
        let info = Information {
            message_type: "information".to_string(),
            version: "2.1.0".to_string(),
            source_uuid: "test-uuid".to_string(),
            timestamp: 1234567890,
            instance_id: "test-instance".to_string(),
            instance_type: InstanceType::Central,
            central_instance_id: None,
            running: false,
            validity_duration: 3600,
            public_ip_address: vec![],
            mqtt_ip: vec![],
            mqtt_tls_ip: vec![],
            http_ip: vec![],
            http_tls_ip: vec![],
            http_proxy: vec![],
            ntp_servers: vec![],
            domain_name_servers: vec![],
            service_area: None,
            cells_id: vec![],
        };

        let json = serde_json::to_value(&info).unwrap();
        assert_eq!(json["message_type"], "information");
        assert_eq!(json["version"], "2.1.0");
        assert!(!json.as_object().unwrap().contains_key("mqtt_ip"));
    }
}
