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

use crate::exchange::etsi::acceleration::{Acceleration, AccelerationMagnitude};
use crate::exchange::etsi::angle::Angle;
use crate::exchange::etsi::collective_perception_message::MapReference;
use crate::exchange::etsi::coordinate::CartesianCoordinate;
use crate::exchange::etsi::longitudinal_lane_position::LongitudinalLanePosition;
use crate::exchange::etsi::object_dimension::ObjectDimension;
use crate::exchange::etsi::shape::Shape;
use crate::exchange::etsi::speed::Speed;
use crate::exchange::etsi::velocity::Velocity;
use serde::{Deserialize, Serialize};

/// Represents a Perceived Object (PO) according to an ETSI standard.
///
/// This message is used to describe information on a unique element around itself.
/// It implements the schema defined in the [CPM version 2.1.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cpm/cpm_schema_2-1-0.json#L315
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PerceivedObject {
    /// Time difference since the last generation of the message.
    pub measurement_delta_time: i16,
    /// Position of the perceived object in 3D Cartesian coordinates with confidence.
    pub position: CartesianPosition3DWithConfidence,

    // optional fields
    /// Unique identifier for the perceived object.
    pub object_id: Option<u16>,
    /// Velocity of the perceived object in 3D with confidence.
    pub velocity: Option<Velocity3dWithConfidence>,
    /// Acceleration of the perceived object in 3D with confidence.
    pub acceleration: Option<Acceleration3dWithConfidence>,
    /// Angles of the perceived object in Euler angle with confidence.
    pub angles: Option<EulerAnglesWithConfidence>,
    /// Angular velocity of the perceived object around the Z-axis with confidence.
    pub z_angular_velocity: Option<CartesianAngularVelocityComponent>,
    /// List of lower triangular positive semidefinite matrices representing correlations.
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub lower_triangular_correlation_matrices: Vec<LowerTriangularPositiveSemidefiniteMatrix>,
    /// Dimensions of the perceived object in 3D with confidence.
    pub object_dimension_z: Option<ObjectDimension>,
    /// Dimensions of the perceived object in 2D with confidence.
    pub object_dimension_y: Option<ObjectDimension>,
    /// Dimensions of the perceived object in 1D with confidence.
    pub object_dimension_x: Option<ObjectDimension>,
    /// Age of the perceived object in milliseconds.
    pub object_age: Option<i16>,
    /// Quality of the object perception, ranging from 0 to 255.
    pub object_perception_quality: Option<u8>,
    /// List of sensor IDs that detected the perceived object.
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub sensor_id_list: Vec<u8>,
    /// List of classifications for the perceived object.
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub classification: Vec<ObjectClassification>,
    /// Position of the perceived object on a map, if available.
    pub map_position: Option<MapPosition>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct MapPosition {
    // optional fields
    /// Reference to the map segment where the perceived object is located.
    pub map_reference: Option<MapReference>,
    /// Identifier for the lane where the perceived object is located.
    pub lane_id: Option<u8>,
    /// Identifier for the connection associated with the perceived object.
    pub connection_id: Option<u8>,
    /// Longitudinal lane position of the perceived object, if available.
    pub longitudinal_lane_position: Option<LongitudinalLanePosition>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct LowerTriangularPositiveSemidefiniteMatrix {
    /// Indicates which components are included in the matrix.
    pub components_included_in_the_matrix: ComponentIncludedInTheMatrix,

    // optional fields
    /// The matrix itself, represented as a vector of vectors of i8.
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub matrix: Vec<Vec<i8>>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Copy, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct ComponentIncludedInTheMatrix {
    /// Indicates whether the X position is included in the matrix.
    x_position: bool,
    /// Indicates whether the Y position is included in the matrix.
    y_position: bool,
    /// Indicates whether the Z position is included in the matrix.
    z_position: bool,
    /// Indicates whether the X velocity or velocity magnitude is included in the matrix.
    x_velocity_or_velocity_magnitude: bool,
    /// Indicates whether the X velocity or velocity direction is included in the matrix.
    x_velocity_or_velocity_direction: bool,
    /// Indicates whether the Z speed is included in the matrix.
    z_speed: bool,
    /// Indicates whether the X acceleration or acceleration magnitude is included in the matrix.
    x_accel_or_accel_magnitude: bool,
    /// Indicates whether the Y acceleration or acceleration direction is included in the matrix.
    y_accel_or_accel_direction: bool,
    /// Indicates whether the Z acceleration is included in the matrix.
    z_acceleration: bool,
    /// Indicates whether the Z angle is included in the matrix.
    z_angle: bool,
    /// Indicates whether the Y angle is included in the matrix.
    y_angle: bool,
    /// Indicates whether the X angle is included in the matrix.
    x_angle: bool,
    /// Indicates whether the Z angular velocity is included in the matrix.
    z_angular_velocity: bool,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct EulerAnglesWithConfidence {
    /// Z angle in centidegrees with confidence.
    pub z_angle: Angle,

    // optional fields
    /// Y angle in centidegrees with confidence.
    pub y_angle: Option<Angle>,
    /// X angle in centidegrees with confidence.
    pub x_angle: Option<Angle>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Acceleration3dWithConfidence {
    // optional fields
    /// Polar acceleration of the perceived object with confidence.
    pub polar_acceleration: Option<PolarAcceleration>,
    /// Cartesian acceleration of the perceived object with confidence.
    pub cartesian_acceleration: Option<CartesianAcceleration>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PolarAcceleration {
    /// Acceleration size of the perceived object.
    pub acceleration_magnitude: AccelerationMagnitude,
    /// Acceleration direction of the perceived object in centidegrees.
    pub acceleration_direction: Angle,

    // optional fields
    /// Z acceleration of the perceived object with confidence.
    pub z_acceleration: Option<Acceleration>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CartesianAcceleration {
    /// X acceleration of the perceived object with confidence.
    pub x_acceleration: Acceleration,
    /// Y acceleration of the perceived object with confidence.
    pub y_acceleration: Acceleration,

    // optional fields
    /// Z acceleration of the perceived object with confidence.
    pub z_acceleration: Option<Acceleration>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CartesianAngularVelocityComponent {
    /// Angular velocity value in centidegrees per second.
    pub value: i16,
    /// Confidence level for the angular velocity.
    pub confidence: u8,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Velocity3dWithConfidence {
    // optional fields
    /// Polar velocity of the perceived object with confidence.
    pub polar_velocity: Option<PolarVelocity>,
    /// Cartesian velocity of the perceived object with confidence.
    pub cartesian_velocity: Option<CartesianVelocity>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PolarVelocity {
    /// Velocity magnitude of the perceived object.
    pub velocity_magnitude: Speed,
    /// Velocity direction of the perceived object in centidegrees.
    pub velocity_direction: Angle,

    // optional fields
    /// Z velocity of the perceived object with confidence.
    pub z_velocity: Option<Velocity>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CartesianVelocity {
    /// X velocity of the perceived object with confidence.
    pub x_velocity: Velocity,
    /// Y velocity of the perceived object with confidence.
    pub y_velocity: Velocity,

    // optional fields
    /// Z velocity of the perceived object with confidence.
    pub z_velocity: Option<Velocity>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Copy, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CartesianPosition3DWithConfidence {
    /// X coordinate of the perceived object in Cartesian coordinates with confidence.
    pub x_coordinate: CartesianCoordinate,
    /// Y coordinate of the perceived object in Cartesian coordinates with confidence.
    pub y_coordinate: CartesianCoordinate,

    // optional fields
    /// Z coordinate of the perceived object in Cartesian coordinates with confidence.
    pub z_coordinate: Option<CartesianCoordinate>,
}

/// Represents the classification of a detected object.
/// This includes the object's class and the confidence level of the classification.
#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct ObjectClassification {
    /// Class of the object, such as vehicle, pedestrian, or other types.
    pub object_class: ObjectClass,
    /// Confidence level of the classification ranging from 0 to 255.
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

/// Represents a group.
/// This struct includes information about the group size, type, and an optional cluster identifier.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Group {
    /// The shape of the bounding box that encloses the cluster.
    pub cluster_bounding_box_shape: Shape,
    /// The size of the cluster, indicating the number in the group.
    pub cluster_cardinality_size: u8,

    // optional fields
    /// An identifier for the cluster.
    pub cluster_id: Option<u8>,
    /// Profiles of the cluster, indicating the presence of different types.
    pub cluster_profiles: Option<ClusterProfiles>,
}

/// Represents the type of group.
/// This struct specifies the presence of different categories within the group.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct ClusterProfiles {
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
            "measurement_delta_time": 2047,
            "position": {
                "x_coordinate": {
                    "value": 131071,
                    "confidence": 4096
                },
                "y_coordinate": {
                    "value": -131072,
                    "confidence": 1
                }
            }
        }"#
    }

    fn standard_po() -> &'static str {
        r#"{
            "measurement_delta_time": 2047,
            "position": {
                "x_coordinate": {
                    "value": 131071,
                    "confidence": 4096
                },
                "y_coordinate": {
                    "value": -131072,
                    "confidence": 1
                }
            },
            "object_id": 65535,
            "velocity": {
                "cartesian_velocity": {
                    "x_velocity": {
                        "value": 16383,
                        "confidence": 127
                    },
                    "y_velocity": {
                        "value": -16383,
                        "confidence": 1
                    }
                }
            },
            "acceleration": {
                "cartesian_acceleration": {
                    "x_acceleration": {
                        "value": 161,
                        "confidence": 102
                    },
                    "y_acceleration": {
                        "value": -160,
                        "confidence": 0
                    }
                }
            },
            "object_dimension_z": {
                "value": 256,
                "confidence": 32
            },
            "object_dimension_y": {
                "value": 1,
                "confidence": 1
            },
            "object_dimension_x": {
                "value": 128,
                "confidence": 17
            },
            "object_age": 2047,
            "object_perception_quality": 15,
            "classification": [
                {
                    "object_class": {
                        "vehicle": 255
                    },
                    "confidence": 101
                }
            ]
        }"#
    }

    fn full_po() -> &'static str {
        r#"{
            "measurement_delta_time": 2047,
            "position": {
                "x_coordinate": {
                    "value": 131071,
                    "confidence": 4096
                },
                "y_coordinate": {
                    "value": -131072,
                    "confidence": 1
                },
                "z_coordinate": {
                    "value": 0,
                    "confidence": 2048
                }
            },
            "object_id": 65535,
            "velocity": {
                "cartesian_velocity": {
                    "x_velocity": {
                        "value": 16383,
                        "confidence": 127
                    },
                    "y_velocity": {
                        "value": -16383,
                        "confidence": 1
                    },
                    "z_velocity": {
                        "value": 0,
                        "confidence": 64
                    }
                }
            },
            "acceleration": {
                "cartesian_acceleration": {
                    "x_acceleration": {
                        "value": 161,
                        "confidence": 102
                    },
                    "y_acceleration": {
                        "value": -160,
                        "confidence": 0
                    },
                    "z_acceleration": {
                        "value": 0,
                        "confidence": 61
                    }
                }
            },
            "angles": {
                "z_angle": {
                    "value": 3601,
                    "confidence": 127
                },
                "y_angle": {
                    "value": 0,
                    "confidence": 1
                },
                "x_angle": {
                    "value": 1800,
                    "confidence": 64
                }
            },
            "z_angular_velocity": {
                "value": 256,
                "confidence": 7
            },
            "lower_triangular_correlation_matrices": [
                {
                    "components_included_in_the_matrix": {
                        "x_position": true,
                        "y_position": true,
                        "z_position": true,
                        "x_velocity_or_velocity_magnitude": true,
                        "x_velocity_or_velocity_direction": true,
                        "z_speed": true,
                        "x_accel_or_accel_magnitude": true,
                        "y_accel_or_accel_direction": true,
                        "z_acceleration": true,
                        "z_angle": true,
                        "y_angle": true,
                        "x_angle": true,
                        "z_angular_velocity": true
                    },
                    "matrix": [
                        [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
                        [0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
                        [0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
                        [0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0],
                        [0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0],
                        [0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
                        [0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0],
                        [0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0],
                        [0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0],
                        [0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0],
                        [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0],
                        [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0],
                        [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1]
                    ]
                },
                {
                    "components_included_in_the_matrix": {
                        "x_position": false,
                        "y_position": false,
                        "z_position": false,
                        "x_velocity_or_velocity_magnitude": false,
                        "x_velocity_or_velocity_direction": false,
                        "z_speed": false,
                        "x_accel_or_accel_magnitude": false,
                        "y_accel_or_accel_direction": false,
                        "z_acceleration": false,
                        "z_angle": false,
                        "y_angle": false,
                        "x_angle": false,
                        "z_angular_velocity": false
                    },
                    "matrix": [
                        [],
                        [],
                        [],
                        [],
                        [],
                        [],
                        [],
                        [],
                        [],
                        [],
                        [],
                        [],
                        []
                    ]
                }
            ],
            "object_dimension_z": {
                "value": 256,
                "confidence": 32
            },
            "object_dimension_y": {
                "value": 1,
                "confidence": 1
            },
            "object_dimension_x": {
                "value": 128,
                "confidence": 17
            },
            "object_age": 2047,
            "object_perception_quality": 15,
            "sensor_id_list": [1, 2, 3],
            "classification": [
                {
                    "object_class": {
                        "vehicle": 255
                    },
                    "confidence": 101
                },
                {
                    "object_class": {
                        "vru": {
                            "pedestrian": 15
                        }
                    },
                    "confidence": 1
                },
                {
                    "object_class": {
                        "group": {
                            "cluster_bounding_box_shape": {
                                "rectangle": {
                                        "center_point": {
                                            "x_coordinate": 32767,
                                            "y_coordinate": -32768,
                                            "z_coordinate": 0
                                        },
                                        "semi_length": 102,
                                        "semi_breadth": 0,
                                        "orientation": 3601,
                                        "height": 4095
                                    }
                            },
                            "cluster_cardinality_size": 255,
                            "cluster_id": 255,
                            "cluster_profiles": {
                                "pedestrian": true,
                                "bicyclist": true,
                                "motorcyclist": true,
                                "animal": true
                            }
                        }
                    },
                    "confidence": 50
                },
                {
                    "object_class": {
                        "other": 255
                    },
                    "confidence": 25
                }
            ],
            "map_position": {
                "map_reference": {
                    "road_segment": {
                        "id": 65535,
                        "region": 0
                    }
                },
                "lane_id": 255,
                "connection_id": 255,
                "longitudinal_lane_position": {
                    "value": 32767,
                    "confidence": 1023
                }
            }
        }"#
    }

    fn parse_and_verify_po(data: &str, expected_id: Option<u16>) {
        match serde_json::from_str::<PerceivedObject>(data) {
            Ok(po) => {
                assert_eq!(po.object_id, expected_id);
            }
            Err(e) => {
                panic!("Failed to deserialize PO: '{e}'");
            }
        }
    }

    #[test]
    fn test_deserialize_minimal_po() {
        parse_and_verify_po(minimal_po(), None);
    }

    #[test]
    fn test_deserialize_standard_po() {
        parse_and_verify_po(standard_po(), Some(65535));
    }

    #[test]
    fn test_deserialize_full_po() {
        parse_and_verify_po(full_po(), Some(65535));
    }
}
