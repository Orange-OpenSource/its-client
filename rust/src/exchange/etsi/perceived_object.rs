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

use crate::exchange::etsi::coordinate::CartesianCoordinate;
use crate::exchange::etsi::shape::Shape;
use serde::{Deserialize, Serialize};

/// Represents a Perceived Object (PO) according to an ETSI standard.
///
/// This message is used to describe information on a unique element around itself.
/// It implements the schema defined in the CPM file version 2.1.0.
///
/// # Fields
///
/// * `protocol_version` - The version of the protocol used
/// * `station_id` - Unique identifier for the ITS station
/// * `management_container` - Contains basic information about the DENM
/// * `originating_vehicle_container` - Contains data about the originating vehicle
/// * `originating_rsu_container` - Contains data about the originating RSU
/// * `sensor_information_container` - List of sensor specifications
/// * `perception_region_container` - Contains information about the perception region
/// * `perceived_object_container` - List of detected objects
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PerceivedObject {
    pub measurement_delta_time: i16,
    pub position: CartesianPosition3DWithConfidence,

    pub object_id: Option<u16>,
    pub velocity: Option<Velocity3dWithConfidence>,
    pub acceleration: Option<Acceleration3dWithConfidence>,
    pub angles: Option<EulerAnglesWithConfidence>,
    pub z_angular_velocity: Option<CartesianAngularVelocityComponent>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub lower_triangular_correlation_matrices: Vec<LowerTriangularPositiveSemidefiniteMatrix>,
    pub object_dimension_z: Option<ObjectDimension>,
    pub object_dimension_y: Option<ObjectDimension>,
    pub object_dimension_x: Option<ObjectDimension>,
    pub object_age: Option<i16>,
    pub object_perception_quality: Option<u8>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub sensor_id_list: Vec<u8>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub classification: Vec<ObjectClassification>,
    pub map_position: Option<MapPosition>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CartesianPosition3DWithConfidence {
    pub x_coordinate: CartesianCoordinate,
    pub y_coordinate: CartesianCoordinate,

    pub z_coordinate: Option<CartesianCoordinate>,
}

/// Represents the classification of a detected object.
/// This includes the object's class and the confidence level of the classification.
#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct ObjectClassification {
    /// The class of the object, such as vehicle, pedestrian, or other types.
    pub object_class: ObjectClass,
    /// The confidence level of the classification ranging from 0 to 255.
    pub confidence: u8,
}

/// Represents the classification of an object.
/// This enum defines various object classes, including vehicles, single vulnerable road users (VRUs),
/// VRU groups, and other types. Each variant may include additional data specific to the class.
#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum ObjectClass {
    /// Represents a vehicle with an associated identifier.
    Vehicle(u8),
    /// Represents a single vulnerable road user (VRU), such as a pedestrian or bicyclist.
    Vru(Vru),
    /// Represents a group of vulnerable road users (VRUs), including group size and type.
    Group(Group),
    /// Represents an object of another type with an associated identifier.
    Other(u8),
}

/// Represents the classification of a single vulnerable road user (VRU).
/// This enum defines various types of single VRUs, such as pedestrians, bicyclists, motorcyclists, and animals.
/// Each variant includes an associated identifier.
#[serde_with::skip_serializing_none]
#[derive(Debug, Copy, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum Vru {
    /// Represents a pedestrian with an associated identifier.
    Pedestrian(u8),
    /// Represents a bicyclist with an associated identifier.
    BicyclistAndLightVruVehicle(u8),
    /// Represents a motorcyclist with an associated identifier.
    Motorcyclist(u8),
    /// Represents an animal with an associated identifier.
    Animal(u8),
}

/// Represents a group of vulnerable road users (VRUs).
/// This struct includes information about the group size, type, and an optional cluster identifier.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Group {
    pub cluster_bounding_box_shape: Shape,
    pub cluster_cardinality_size: u8,

    pub cluster_id: Option<u8>,
    pub cluster_profiles: Option<CluserProfiles>,
}

/// Represents the type of group of vulnerable road users (VRUs).
/// This struct specifies the presence of different categories of VRUs within the group.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CluserProfiles {
    /// Indicates whether the group contains pedestrians.
    pub pedestrian: bool,
    /// Indicates whether the group contains bicyclists.
    pub bicyclist: bool,
    /// Indicates whether the group contains motorcyclists.
    pub motorcyclist: bool,
    /// Indicates whether the group contains animals.
    pub animal: bool,
}

impl PerceivedObject {
    pub fn is_pedestrian(&self) -> bool {
        self.classification.iter().any(|object_classification| {
            matches!(
                object_classification.object_class,
                ObjectClass::Vru(Vru::Pedestrian(_))
            )
        })
    }

    pub fn is_vehicle(&self) -> bool {
        self.classification.iter().any(|object_classification| {
            matches!(object_classification.object_class, ObjectClass::Vehicle(_))
        })
    }
}

#[cfg(test)]
mod test {
    use crate::exchange::etsi::perceived_object::PerceivedObject;

    fn minimal_po() -> &'static str {
        r#"{
            "object_id": 255,
            "time_of_measurement": 1500,
            "x_distance": 132767,
            "y_distance": -132768,
            "x_speed": 16383,
            "y_speed": -16383,
            "object_age": 1500,
            "confidence": {
                "x_distance": 4095,
                "y_distance": 0,
                "x_speed": 7,
                "y_speed": 0,
                "object": 15
            }
        }"#
    }

    fn standard_po() -> &'static str {
        r#"{
            "object_id": 5,
            "time_of_measurement": 2,
            "x_distance": 804,
            "y_distance": 400,
            "z_distance": 132767,
            "x_speed": 401,
            "y_speed": 401,
            "z_speed": 0,
            "x_acceleration": 161,
            "y_acceleration": -160,
            "z_acceleration": 0,
            "object_age": 365,
            "object_ref_point": 8,
            "dynamic_status": 0,
            "classification": [
                {
                    "object_class": {
                      "single_vru": {
                        "pedestrian": 1
                      }
                    },
                    "confidence": 89
                }
            ],
            "confidence": {
                "x_distance": 4095,
                "y_distance": 0,
                "z_distance": 2047,
                "x_speed": 7,
                "y_speed": 0,
                "z_speed": 3,
                "x_acceleration": 102,
                "y_acceleration": 0,
                "z_acceleration": 51,
                "object": 10
            }
        }"#
    }

    fn full_po() -> &'static str {
        r#"{
            "object_id": 0,
            "time_of_measurement": 50,
            "x_distance": 400,
            "y_distance": 100,
            "z_distance": 50,
            "x_speed": 1400,
            "y_speed": 500,
            "z_speed": 0,
            "x_acceleration": -160,
            "y_acceleration": 0,
            "z_acceleration": 161,
            "roll_angle": 0,
            "pitch_angle": 3600,
            "yaw_angle": 3601,
            "roll_rate": -32766,
            "pitch_rate": 0,
            "yaw_rate": 32767,
            "roll_acceleration": -32766,
            "pitch_acceleration": 0,
            "yaw_acceleration": 32767,
            "lower_triangular_correlation_matrix_columns": [
                [-100, -99, -98],
                [0, 1, 2],
                [98, 99, 100]
            ],
            "planar_object_dimension_1": 1023,
            "planar_object_dimension_2": 1023,
            "vertical_object_dimension": 1023,
            "object_ref_point": 8,
            "object_age": 1500,
            "sensor_id_list": [1, 2, 10, 100, 255],
            "dynamic_status": 2,
            "classification": [
                {
                    "object_class": {
                        "vehicle": 10
                    },
                    "confidence": 101
                },
                {
                    "object_class": {
                        "single_vru": {
                            "pedestrian": 2
                        }
                    },
                    "confidence": 25
                },
                {
                    "object_class": {
                        "vru_group": {
                            "group_type": {
                                "pedestrian": true,
                                "bicyclist": false,
                                "motorcyclist": false,
                                "animal": true
                            },
                            "group_size": 12,
                            "cluster_id": 255
                        }
                    },
                    "confidence": 64
                },
                {
                    "object_class": {
                        "other": 1
                    },
                    "confidence": 0
                }
            ],
            "matched_position": {
                "lane_id": 255,
                "longitudinal_lane_position": 32767
            },
            "confidence": {
                "x_distance": 102,
                "y_distance": 102,
                "z_distance": 102,
                "x_speed": 7,
                "y_speed": 7,
                "z_speed": 7,
                "x_acceleration": 102,
                "y_acceleration": 102,
                "z_acceleration": 102,
                "roll_angle": 127,
                "pitch_angle": 127,
                "yaw_angle": 127,
                "roll_rate": 8,
                "pitch_rate": 8,
                "yaw_rate": 8,
                "roll_acceleration": 8,
                "pitch_acceleration": 8,
                "yaw_acceleration": 8,
                "planar_object_dimension_1": 102,
                "planar_object_dimension_2": 102,
                "vertical_object_dimension": 102,
                "longitudinal_lane_position": 102,
                "object": 10
            }
        }"#
    }

    fn parse_and_verify_po(data: &str, expected_id: u16) {
        match serde_json::from_str::<PerceivedObject>(data) {
            Ok(po) => {
                assert_eq!(po.object_id, Some(expected_id));
            }
            Err(e) => {
                panic!("Failed to deserialize PO: '{}'", e);
            }
        }
    }

    #[test]
    fn test_deserialize_minimal_po() {
        parse_and_verify_po(minimal_po(), 255);
    }

    #[test]
    fn test_deserialize_standard_po() {
        parse_and_verify_po(standard_po(), 5);
    }

    #[test]
    fn test_deserialize_full_po() {
        parse_and_verify_po(full_po(), 0);
    }
}
