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

use serde::{Deserialize, Serialize};

/// Represents a perceived object detected by sensors, including its position, speed,
/// classification, and other attributes.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PerceivedObject {
    /// Unique identifier for the detected object (mandatory).
    pub object_id: u8,
    /// Time of measurement in milliseconds relative to the message's generation time (mandatory).
    pub time_of_measurement: i16,
    /// X distance from the reference point in decimeters (mandatory).
    pub x_distance: i32,
    /// Y distance from the reference point in decimeters (mandatory).
    pub y_distance: i32,
    /// Z distance from the reference point in decimeters (optional).
    pub z_distance: Option<i32>,
    /// X speed in decimeters per second (mandatory).
    pub x_speed: i16,
    /// Y speed in decimeters per second (mandatory).
    pub y_speed: i16,
    /// Z speed in decimeters per second (optional).
    pub z_speed: Option<i16>,
    /// Age of the object in milliseconds, indicating how long it has been observed (mandatory).
    pub object_age: u16,
    /// Confidence levels for various attributes of the object (mandatory).
    pub confidence: ObjectConfidence,

    /// Reference point of the object (optional).
    pub object_ref_point: Option<u8>,
    /// X acceleration in decimeters per second squared (optional).
    pub x_acceleration: Option<i16>,
    /// Y acceleration in decimeters per second squared (optional).
    pub y_acceleration: Option<i16>,
    /// Z acceleration in decimeters per second squared (optional).
    pub z_acceleration: Option<i16>,
    /// Roll angle of the object in centidegrees (optional).
    pub roll_angle: Option<u16>,
    /// Pitch angle of the object in centidegrees (optional).
    pub pitch_angle: Option<u16>,
    /// Yaw angle of the object in centidegrees (optional).
    pub yaw_angle: Option<u16>,
    /// Roll rate of the object in centidegrees per second (optional).
    pub roll_rate: Option<i16>,
    /// Pitch rate of the object in centidegrees per second (optional).
    pub pitch_rate: Option<i16>,
    /// Yaw rate of the object in centidegrees per second (optional).
    pub yaw_rate: Option<i16>,
    /// Roll acceleration of the object in centidegrees per second squared (optional).
    pub roll_acceleration: Option<i16>,
    /// Pitch acceleration of the object in centidegrees per second squared (optional).
    pub pitch_acceleration: Option<i16>,
    /// Yaw acceleration of the object in centidegrees per second squared (optional).
    pub yaw_acceleration: Option<i16>,
    /// Lower triangular correlation matrix columns for the object (optional).
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub lower_triangular_correlation_matrix_columns: Vec<Vec<i8>>,
    /// First planar dimension of the object in decimeters (optional).
    pub planar_object_dimension_1: Option<u16>,
    /// Second planar dimension of the object in decimeters (optional).
    pub planar_object_dimension_2: Option<u16>,
    /// Vertical dimension of the object in decimeters (optional).
    pub vertical_object_dimension: Option<u16>,
    /// List of sensor IDs that detected the object (optional).
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub sensor_id_list: Vec<u8>,
    /// Dynamic status of the object (optional).
    pub dynamic_status: Option<u8>,
    /// Classification of the object, including its type and confidence (optional).
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub classification: Vec<ObjectClassification>,
    /// Matched position of the object, such as lane ID and longitudinal position (optional).
    pub matched_position: Option<MatchedPosition>,
}

/// Represents the confidence levels for various attributes of a detected object.
/// Each field indicates the confidence in the corresponding measurement or property.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct ObjectConfidence {
    /// Confidence in the x-distance measurement.
    /// Range: 0 to 65535
    pub x_distance: u16,
    /// Confidence in the y-distance measurement.
    /// Range: 0 to 65535
    pub y_distance: u16,
    /// Confidence in the x-speed measurement.
    /// Range: 0 to 255
    pub x_speed: u8,
    /// Confidence in the y-speed measurement.
    /// Range: 0 to 255
    pub y_speed: u8,
    /// Confidence in the overall object detection (optional).
    /// Range: 0 to 255
    pub object: Option<u8>,
}

/// Represents the classification of a detected object.
/// This includes the object's class and the confidence level of the classification.
#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct ObjectClassification {
    /// The class of the object, such as vehicle, pedestrian, or other types.
    pub object_class: ObjectClass,
    /// The confidence level of the classification, ranging from 0 to 255.
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
    SingleVru(SingleVruClass),
    /// Represents a group of vulnerable road users (VRUs), including group size and type.
    VruGroup(VruGroupClass),
    /// Represents an object of another type with an associated identifier.
    Other(u8),
}

/// Represents the classification of a single vulnerable road user (VRU).
/// This enum defines various types of single VRUs, such as pedestrians, bicyclists, motorcyclists, and animals.
/// Each variant includes an associated identifier.
#[serde_with::skip_serializing_none]
#[derive(Debug, Copy, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum SingleVruClass {
    /// Represents a pedestrian with an associated identifier.
    Pedestrian(u8),
    /// Represents a bicyclist with an associated identifier.
    Bicyclist(u8),
    /// Represents a motorcyclist with an associated identifier.
    Motorcyclist(u8),
    /// Represents an animal with an associated identifier.
    Animal(u8),
}

/// Represents a group of vulnerable road users (VRUs).
/// This struct includes information about the group size, type, and an optional cluster identifier.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct VruGroupClass {
    /// The size of the group, indicating the number of VRUs in the group.
    pub group_size: u8,
    /// The type of the group, specifying the categories of VRUs present (e.g., pedestrians, bicyclists).
    pub group_type: VruGroupType,
    /// An optional identifier for the cluster to which the group belongs.
    pub cluster_id: Option<u8>,
}

/// Represents the type of group of vulnerable road users (VRUs).
/// This struct specifies the presence of different categories of VRUs within the group.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct VruGroupType {
    /// Indicates whether the group contains pedestrians.
    pub pedestrian: bool,
    /// Indicates whether the group contains bicyclists.
    pub bicyclist: bool,
    /// Indicates whether the group contains motorcyclists.
    pub motorcyclist: bool,
    /// Indicates whether the group contains animals.
    pub animal: bool,
}

/// Represents the matched position of an object within a specific context,
/// such as a lane on a road. This includes the lane ID and the longitudinal
/// position within the lane.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct MatchedPosition {
    /// The identifier of the lane where the object is located.
    pub lane_id: u8,
    /// The longitudinal position of the object within the lane, measured in decimeters.
    pub longitudinal_lane_position: u16,
}

impl PerceivedObject {
    pub fn is_pedestrian(&self) -> bool {
        self.classification.iter().any(|object_classification| {
            matches!(
                object_classification.object_class,
                ObjectClass::SingleVru(SingleVruClass::Pedestrian(_))
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

    #[test]
    fn test_deserialize() {
        let data = r#"{
                "object_id": 5,
                "time_of_measurement": 2,
                "x_distance": 804,
                "y_distance": 400,
                "x_speed": 401,
                "y_speed": 401,
                "object_age": 1500,
                "object_ref_point": 0,
                "dynamic_status": 0,
                "classification": [
                  {
                    "object_class": {
                      "single_vru": {
                        "pedestrian": 1
                      }
                    },
                    "confidence": 40
                  }
                ],
                "confidence": {
                  "x_distance": 4095,
                  "y_distance": 4095,
                  "x_speed": 0,
                  "y_speed": 0,
                  "object": 10
                }
              }"#;

        match serde_json::from_str::<PerceivedObject>(data) {
            Ok(po) => {
                assert_eq!(5, po.object_id);
            }
            Err(e) => {
                panic!("Failed to deserialize PO: '{}'", e);
            }
        }
    }

    #[test]
    fn test_deserialize_full_po() {
        let data = r#"{
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
            },
            "object_age": 1500,
            "sensor_id_list": [1, 2, 10, 100, 255],
            "dynamic_status": 2,
            "classification": [{
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
            }
        }"#;

        match serde_json::from_str::<PerceivedObject>(data) {
            Ok(po) => {
                assert_eq!(0, po.object_id);
            }
            Err(e) => {
                panic!("Failed to deserialize PO: '{}'", e);
            }
        }
    }
}
mod tests {}
