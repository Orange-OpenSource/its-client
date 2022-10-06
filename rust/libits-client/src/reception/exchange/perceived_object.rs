// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
use serde::{Deserialize, Serialize};

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct PerceivedObject {
    pub object_id: u8,
    pub time_of_measurement: i16,
    pub confidence: ObjectConfidence,
    pub x_distance: i32,
    pub y_distance: i32,
    pub z_distance: Option<i32>,
    pub x_speed: i16,
    pub y_speed: i16,
    pub z_speed: Option<i16>,
    pub object_age: u16,
    pub object_ref_point: Option<u8>,
    pub x_acceleration: Option<i16>,
    pub y_acceleration: Option<i16>,
    pub z_acceleration: Option<i16>,
    pub roll_angle: Option<u16>,
    pub pitch_angle: Option<u16>,
    pub yaw_angle: Option<u16>,
    pub roll_rate: Option<i16>,
    pub pitch_rate: Option<i16>,
    pub yaw_rate: Option<i16>,
    pub roll_acceleration: Option<i16>,
    pub pitch_acceleration: Option<i16>,
    pub yaw_acceleration: Option<i16>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub lower_triangular_correlation_matrix_columns: Vec<Vec<i8>>,
    pub planar_object_dimension_1: Option<u16>,
    pub planar_object_dimension_2: Option<u16>,
    pub vertical_object_dimension: Option<u16>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub sensor_id_list: Vec<u8>,
    pub dynamic_status: Option<u8>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub classification: Vec<ObjectClassification>,
    pub matched_position: Option<MatchedPosition>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct ObjectConfidence {
    pub x_distance: u16,
    pub y_distance: u16,
    pub x_speed: u8,
    pub y_speed: u8,
    pub object: Option<u8>,
}

#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct ObjectClassification {
    pub object_class: ObjectClass,
    pub confidence: u8,
}

#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum ObjectClass {
    Vehicle(u8),
    SingleVru(SingleVruClass),
    VruGroup(VruGroupClass),
    Other(u8),
}

#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub enum SingleVruClass {
    Pedestrian(u8),
    Bicyclist(u8),
    Motorcyclist(u8),
    Animal(u8),
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct VruGroupClass {
    pub group_size: u8,
    pub group_type: VruGroupType,
    pub cluster_id: Option<u8>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct VruGroupType {
    pub pedestrian: bool,
    pub bicyclist: bool,
    pub motorcyclist: bool,
    pub animal: bool,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct MatchedPosition {
    pub lane_id: u8,
    pub longitudinal_lane_position: u16,
}

#[cfg(test)]
mod test {
    use crate::reception::exchange::perceived_object::PerceivedObject;

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
