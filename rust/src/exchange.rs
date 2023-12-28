// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

pub(crate) mod cause;
pub mod etsi;
pub mod message;
pub mod mortal;
pub mod sequence_number;

use crate::exchange::message::Message;
use crate::mobility::position::Position;

use crate::transport::payload::Payload;
use serde::{Deserialize, Serialize};

#[serde_with::skip_serializing_none]
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Exchange {
    #[serde(rename = "type")]
    pub type_field: String,
    pub origin: String,
    pub version: String,
    pub source_uuid: String,
    pub timestamp: u64,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub path: Vec<PathElement>,
    pub message: Message,
}

#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PathElement {
    pub position: Position,
    pub message_type: String,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PositionConfidence {
    pub position_confidence_ellipse: Option<PositionConfidenceEllipse>,
    pub altitude: Option<u8>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PositionConfidenceEllipse {
    pub semi_major_confidence: Option<u16>,
    pub semi_minor_confidence: Option<u16>,
    pub semi_major_orientation: Option<u16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PathHistory {
    pub path_position: PathPosition,
    pub path_delta_time: Option<u16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PathPosition {
    pub delta_latitude: Option<i32>,
    pub delta_longitude: Option<i32>,
    pub delta_altitude: Option<i32>,
}

impl Exchange {
    pub fn new(
        component: String,
        timestamp: u64,
        path: Vec<PathElement>,
        message: Message,
    ) -> Box<Exchange> {
        Box::from(Exchange {
            // FIXME `Message` no longer provides get_type() method
            //        will be brought through Content trait
            type_field: "".to_string(), //message.get_type(),
            origin: "mec_application".to_string(),
            version: "1.1.1".to_string(),
            source_uuid: component,
            path,
            timestamp,
            message,
        })
    }

    // FIXME the following code is commented because it requires Configuration which has not been brought back
    // into the refactoring branch, it will be implemented in further commit
    //
    // TODO find a better way to appropriate
    // pub fn appropriate(&mut self, configuration: &Configuration, timestamp: u128) {
    //     self.origin = "mec_application".to_string();
    //     let _number = self.message.appropriate(configuration, timestamp);
    //     self.source_uuid = configuration.component_name(None);
    //     self.timestamp = timestamp;
    // }
    // ----- ENDFIXME
}

impl Payload for Exchange {}

impl PartialEq for Exchange {
    fn eq(&self, other: &Self) -> bool {
        self.message == other.message
    }
}

impl Eq for Exchange {}

// FIXME the following code is commented because it requires structs or functions which will be added later in the
// refactoring branch; this code will be either uncommented and fixed or deleted following following refactoring choices
//
// impl Mortal for Exchange {
//     fn timeout(&self) -> u128 {
//         self.message.timeout()
//     }
//
//     fn terminate(&mut self) {
//         self.message.terminate();
//     }
//
//     fn terminated(&self) -> bool {
//         self.message.terminated()
//     }
//
//     fn remaining_time(&self) -> u128 {
//         (self.timeout() - now()) / 1000
//     }
// }
//
// impl Reception for Exchange {}
//
// impl hash::Hash for Exchange {
//     fn hash<H: hash::Hasher>(&self, state: &mut H) {
//         self.message.hash(state);
//     }
// }

// #[cfg(test)]
// mod tests {
//     fn basic_cam() -> &'static str {
//         r#"
// {
//   "type": "cam",
//   "origin": "self",
//   "version": "1.0.0",
//   "source_uuid": "uuid14",
//   "timestamp": 1574778515424,
//   "message": {
//     "protocol_version": 1,
//     "station_id": 42,
//     "generation_delta_time": 3,
//     "basic_container": {
//       "reference_position": {
//         "latitude": 486263556,
//         "longitude": 22492123,
//         "altitude": 20000
//       }
//     },
//     "high_frequency_container": {}
//   }
// }
// "#
//     }
//
//     fn standard_cam() -> &'static str {
//         r#"
// {
//   "type": "cam",
//   "origin": "self",
//   "version": "1.0.0",
//   "source_uuid": "uuid14",
//   "timestamp": 1574778515424,
//   "message": {
//     "protocol_version": 1,
//     "station_id": 42,
//     "generation_delta_time": 3,
//     "basic_container": {
//       "station_type": 5,
//       "reference_position": {
//         "latitude": 486263556,
//         "longitude": 22492123,
//         "altitude": 20000
//       },
//       "confidence": {
//         "position_confidence_ellipse": {
//           "semi_major_confidence": 100,
//           "semi_minor_confidence": 50,
//           "semi_major_orientation": 180
//         },
//         "altitude": 3
//       }
//     },
//     "high_frequency_container": {
//       "heading": 180,
//       "speed": 365,
//       "drive_direction": 0,
//       "vehicle_length": 40,
//       "vehicle_width": 20,
//       "confidence": {
//         "heading": 2,
//         "speed": 3,
//         "vehicle_length": 0
//       }
//     }
//   }
// }
// "#
//     }
//
//     fn full_cam() -> &'static str {
//         r#"
// {
//   "type": "cam",
//   "origin": "self",
//   "version": "1.0.0",
//   "source_uuid": "uuid14",
//   "timestamp": 1574778515424,
//   "message": {
//     "protocol_version": 1,
//     "station_id": 42,
//     "generation_delta_time": 3,
//     "basic_container": {
//       "station_type": 5,
//       "reference_position": {
//         "latitude": 486263556,
//         "longitude": 22492123,
//         "altitude": 20000
//       },
//       "confidence": {
//         "position_confidence_ellipse": {
//           "semi_major_confidence": 100,
//           "semi_minor_confidence": 50,
//           "semi_major_orientation": 180
//         },
//         "altitude": 3
//       }
//     },
//     "high_frequency_container": {
//       "heading": 180,
//       "speed": 253,
//       "drive_direction": 0,
//       "vehicle_length": 40,
//       "vehicle_width": 20,
//       "curvature": 0,
//       "curvature_calculation_mode": 1,
//       "longitudinal_acceleration": 2,
//       "yaw_rate": 0,
//       "acceleration_control": "0000010",
//       "lane_position": 1,
//       "lateral_acceleration": 7,
//       "vertical_acceleration": 2,
//       "confidence": {
//         "heading": 2,
//         "speed": 3,
//         "vehicle_length": 0,
//         "yaw_rate": 0,
//         "longitudinal_acceleration": 1,
//         "curvature": 1,
//         "lateral_acceleration": 2,
//         "vertical_acceleration": 1
//       }
//     },
//     "low_frequency_container": {
//       "vehicle_role": 0,
//       "exterior_lights": "00000011",
//       "path_history": [
//         {
//           "path_position": {
//             "delta_latitude": 102,
//             "delta_longitude": 58,
//             "delta_altitude": -10
//           },
//           "path_delta_time": 19
//         },
//         {
//           "path_position": {
//             "delta_latitude": 96,
//             "delta_longitude": 42,
//             "delta_altitude": -6
//           },
//           "path_delta_time": 21
//         }
//       ]
//     }
//   }
// }
// "#
//     }
//
//     fn basic_denm() -> &'static str {
//         r#"
// {
//   "type": "denm",
//   "origin": "self",
//   "version": "1.0.0",
//   "source_uuid": "uuid14",
//   "timestamp": 1574778515425,
//   "message": {
//     "protocol_version": 1,
//     "station_id": 42,
//     "management_container": {
//       "action_id": {
//         "originating_station_id": 41,
//         "sequence_number": 1
//       },
//       "detection_time": 503253331000,
//       "reference_time": 503253331050,
//       "event_position": {
//         "latitude": 486263556,
//         "longitude": 224921234,
//         "altitude": 20000
//       }
//     }
//   }
// }
//     "#
//     }
//
//     fn standard_denm() -> &'static str {
//         r#"
// {
//   "type": "denm",
//   "origin": "self",
//   "version": "1.0.0",
//   "source_uuid": "uuid14",
//   "timestamp": 1574778515425,
//   "message": {
//     "protocol_version": 1,
//     "station_id": 42,
//     "management_container": {
//       "action_id": {
//         "originating_station_id": 41,
//         "sequence_number": 1
//       },
//       "detection_time": 503253332000,
//       "reference_time": 503253330000,
//       "event_position": {
//         "latitude": 486263556,
//         "longitude": 224921234,
//         "altitude": 20000
//       },
//       "station_type": 5,
//       "confidence": {
//         "position_confidence_ellipse": {
//           "semi_major_confidence": 100,
//           "semi_minor_confidence": 50,
//           "semi_major_orientation": 180
//         },
//         "altitude": 3
//       }
//     },
//     "situation_container": {
//       "event_type": {
//         "cause": 97,
//         "subcause": 0
//       }
//     },
//     "location_container": {
//       "event_speed": 289,
//       "event_position_heading": 1806,
//       "traces": [
//         {
//           "path_history": []
//         }
//       ],
//       "confidence": {
//         "speed": 3,
//         "heading": 2
//       }
//     }
//   }
// }
//     "#
//     }
//
//     fn full_denm() -> &'static str {
//         r#"
// {
//   "type": "denm",
//   "origin": "self",
//   "version": "1.0.0",
//   "source_uuid": "uuid14",
//   "timestamp": 1574778515425,
//   "message": {
//     "protocol_version": 1,
//     "station_id": 42,
//     "management_container": {
//       "action_id": {
//         "originating_station_id": 41,
//         "sequence_number": 1
//       },
//       "detection_time": 503253332000,
//       "reference_time": 503253330000,
//       "termination": 1,
//       "event_position": {
//         "latitude": 486263556,
//         "longitude": 224921234,
//         "altitude": 20000
//       },
//       "relevance_distance": 3,
//       "relevance_traffic_direction": 2,
//       "validity_duration": 600,
//       "transmission_interval": 500,
//       "station_type": 5,
//       "confidence": {
//         "position_confidence_ellipse": {
//           "semi_major_confidence": 100,
//           "semi_minor_confidence": 50,
//           "semi_major_orientation": 180
//         },
//         "altitude": 3
//       }
//     },
//     "situation_container": {
//       "information_quality": 1,
//       "event_type": {
//         "cause": 97,
//         "subcause": 0
//       },
//       "linked_cause": {
//         "cause": 1,
//         "subcause": 1
//       }
//     },
//     "location_container": {
//       "event_speed": 289,
//       "event_position_heading": 1806,
//       "traces": [
//         {
//           "path_history": [
//             {
//               "path_position": {
//                 "delta_latitude": 102,
//                 "delta_longitude": 58,
//                 "delta_altitude": -10
//               },
//               "path_delta_time": 19
//             },
//             {
//               "path_position": {
//                 "delta_latitude": 96,
//                 "delta_longitude": 42,
//                 "delta_altitude": -6
//               },
//               "path_delta_time": 21
//             }
//           ]
//         },
//         {
//           "path_history": [
//             {
//               "path_position": {
//                 "delta_latitude": 75,
//                 "delta_longitude": 12,
//                 "delta_altitude": 3
//               },
//               "path_delta_time": 20
//             },
//             {
//               "path_position": {
//                 "delta_latitude": 74,
//                 "delta_longitude": 11,
//                 "delta_altitude": 2
//               },
//               "path_delta_time": 20
//             },
//             {
//               "path_position": {
//                 "delta_latitude": 73,
//                 "delta_longitude": 10,
//                 "delta_altitude": 6
//               },
//               "path_delta_time": 20
//             }
//           ]
//         }
//       ],
//       "road_type": 0,
//       "confidence": {
//         "speed": 3,
//         "heading": 2
//       }
//     },
//     "alacarte_container": {
//       "lane_position": -1,
//       "positioning_solution": 2
//     }
//   }
// }
//     "#
//     }
//
//     fn basic_cpm() -> &'static str {
//         r#"{
//           "type": "cpm",
//           "origin": "self",
//           "version": "1.1.3",
//           "source_uuid": "uuid1",
//           "timestamp": 1574778515425,
//           "message": {
//             "protocol_version": 1,
//             "station_id": 12345,
//             "generation_delta_time": 65535,
//             "management_container": {
//               "station_type": 5,
//               "reference_position": {
//                 "latitude": 426263556,
//                 "longitude": -82492123,
//                 "altitude": 800001
//               },
//               "confidence": {
//                 "position_confidence_ellipse": {
//                   "semi_major_confidence": 4095,
//                   "semi_minor_confidence": 4095,
//                   "semi_major_orientation": 3601
//                 },
//                 "altitude": 15
//               }
//             }
//           }
//         }"#
//     }
//
//     fn standard_cpm() -> &'static str {
//         r#"{
//             "type": "cpm",
//             "origin": "self",
//             "version": "1.1.3",
//             "source_uuid": "uuid1",
//             "timestamp": 1574778515425,
//             "message": {
//                 "protocol_version": 1,
//                 "station_id": 12345,
//                 "generation_delta_time": 65535,
//                 "management_container": {
//                     "station_type": 5,
//                     "reference_position": {
//                         "latitude": 426263556,
//                         "longitude": -82492123,
//                         "altitude": 800001
//                     },
//                     "confidence": {
//                         "position_confidence_ellipse": {
//                             "semi_major_confidence": 4095,
//                             "semi_minor_confidence": 4095,
//                             "semi_major_orientation": 3601
//                         },
//                         "altitude": 15
//                     }
//                 },
//                 "station_data_container": {
//                     "originating_vehicle_container": {
//                         "heading": 180,
//                         "speed": 1600,
//                         "confidence": {
//                             "heading": 127,
//                             "speed": 127
//                         }
//                     }
//                 },
//                 "sensor_information_container": [{
//                     "sensor_id": 1,
//                     "type": 3,
//                     "detection_area": {
//                         "vehicle_sensor": {
//                             "ref_point_id": 0,
//                             "x_sensor_offset": -20,
//                             "y_sensor_offset": 20,
//                             "vehicle_sensor_property_list": [{
//                                 "range": 5000,
//                                 "horizontal_opening_angle_start": 600,
//                                 "horizontal_opening_angle_end": 600
//                             }]
//                         }
//                     }
//                 }],
//                 "perceived_object_container": [{
//                     "object_id": 0,
//                     "time_of_measurement": 50,
//                     "confidence": {
//                         "x_distance": 102,
//                         "y_distance": 102,
//                         "x_speed": 127,
//                         "y_speed": 127,
//                         "object": 10
//                     },
//                     "x_distance": 400,
//                     "y_distance": 100,
//                     "x_speed": 1400,
//                     "y_speed": 500,
//                     "object_age": 1500
//                 }]
//             }
//         }"#
//     }
//
//     fn full_cpm() -> &'static str {
//         r#"{
//             "type": "cpm",
//             "origin": "self",
//             "version": "1.1.3",
//             "source_uuid": "uuid1",
//             "timestamp": 1574778515425,
//             "message": {
//                 "protocol_version": 255,
//                 "station_id": 4294967295,
//                 "generation_delta_time": 65535,
//                 "management_container": {
//                     "station_type": 254,
//                     "reference_position": {
//                         "latitude": 426263556,
//                         "longitude": -82492123,
//                         "altitude": 800001
//                     },
//                     "confidence": {
//                         "position_confidence_ellipse": {
//                             "semi_major_confidence": 4095,
//                             "semi_minor_confidence": 4095,
//                             "semi_major_orientation": 3601
//                         },
//                         "altitude": 15
//                     }
//                 },
//                 "station_data_container": {
//                     "originating_vehicle_container": {
//                         "heading": 180,
//                         "speed": 1600,
//                         "drive_direction": 0,
//                         "vehicle_length": 31,
//                         "vehicle_width": 18,
//                         "longitudinal_acceleration": -160,
//                         "yaw_rate": -32766,
//                         "lateral_acceleration": -2,
//                         "vertical_acceleration": -1,
//                         "confidence": {
//                             "heading": 127,
//                             "speed": 127,
//                             "vehicle_length": 3,
//                             "yaw_rate": 2,
//                             "longitudinal_acceleration": 12,
//                             "lateral_acceleration": 13,
//                             "vertical_acceleration": 14
//                         }
//                     }
//                 },
//                 "sensor_information_container": [{
//                     "sensor_id": 1,
//                     "type": 3,
//                     "detection_area": {
//                         "vehicle_sensor": {
//                             "ref_point_id": 255,
//                             "x_sensor_offset": -3094,
//                             "y_sensor_offset": -1000,
//                             "z_sensor_offset": 1000,
//                             "vehicle_sensor_property_list": [{
//                                 "range": 10000,
//                                 "horizontal_opening_angle_start": 3601,
//                                 "horizontal_opening_angle_end": 3601,
//                                 "vertical_opening_angle_start": 3601,
//                                 "vertical_opening_angle_end": 3601
//                             }]
//                         }
//                     }
//                 }],
//                 "perceived_object_container": [{
//                     "object_id": 0,
//                     "time_of_measurement": 50,
//                     "confidence": {
//                         "x_distance": 102,
//                         "y_distance": 102,
//                         "x_speed": 7,
//                         "y_speed": 7,
//                         "object": 10
//                     },
//                     "x_distance": 400,
//                     "y_distance": 100,
//                     "z_distance": 50,
//                     "x_speed": 1400,
//                     "y_speed": 500,
//                     "z_speed": 0,
//                     "object_age": 1500,
//                     "object_ref_point": 8,
//                     "x_acceleration": -160,
//                     "y_acceleration": 0,
//                     "z_acceleration": 161,
//                     "roll_angle": 0,
//                     "pitch_angle": 3600,
//                     "yaw_angle": 3601,
//                     "roll_rate": -32766,
//                     "pitch_rate": 0,
//                     "yaw_rate": 32767,
//                     "roll_acceleration": -32766,
//                     "pitch_acceleration": 0,
//                     "yaw_acceleration": 32767,
//                     "lower_triangular_correlation_matrix_columns": [
//                         [-100, -99, -98],
//                         [0, 1, 2],
//                         [98, 99, 100]
//                     ],
//                     "planar_object_dimension_1": 1023,
//                     "planar_object_dimension_2": 1023,
//                     "vertical_object_dimension": 1023,
//                     "sensor_id_list": [1, 2, 10, 100, 255],
//                     "dynamic_status": 2,
//                     "classification": [{
//                         "object_class": {
//                             "vehicle": 10
//                         },
//                         "confidence": 101
//                     }, {
//                         "object_class": {
//                             "single_vru": {
//                                 "pedestrian": 2
//                             }
//                         },
//                         "confidence": 25
//                     }, {
//                         "object_class": {
//                             "vru_group": {
//                                 "group_size": 12,
//                                 "group_type": {
//                                     "pedestrian": true,
//                                     "bicyclist": false,
//                                     "motorcyclist": false,
//                                     "animal": true
//                                 },
//                                 "cluster_id": 255
//                             }
//                         },
//                         "confidence": 64
//                     }, {
//                         "object_class": {
//                             "other": 1
//                         },
//                         "confidence": 0
//                     }],
//                     "matched_position": {
//                         "lane_id": 255,
//                         "longitudinal_lane_position": 32767
//                     }
//                 }]
//             }
//         }"#
//     }
//
//     fn bad_cam_without_timestamp() -> &'static str {
//         r#"
// {
//   "type": "cam",
//   "origin": "self",
//   "version": "1.0.0",
//   "source_uuid": "uuid14",
//   "message": {
//     "protocol_version": 1,
//     "station_id": 42,
//     "generation_delta_time": 3,
//     "basic_container": {
//       "reference_position": {
//         "latitude": 486263556,
//         "longitude": 22492123,
//         "altitude": 20000
//       }
//     },
//     "high_frequency_container": {}
//   }
// }
// "#
//     }
//
//     fn bad_denm_with_string_timestamp() -> &'static str {
//         r#"
// {
//   "type": "denm",
//   "origin": "self",
//   "version": "1.0.0",
//   "source_uuid": "uuid14",
//   "timestamp": "1574778515425",
//   "message": {
//     "protocol_version": 1,
//     "station_id": 42,
//     "management_container": {
//       "action_id": {
//         "originating_station_id": 41,
//         "sequence_number": 1
//       },
//       "detection_time": 503253332000,
//       "reference_time": 503253330000,
//       "event_position": {
//         "latitude": 486263556,
//         "longitude": 224921234,
//         "altitude": 20000
//       }
//     }
//   }
// }
//     "#
//     }
//
//     fn bad_denm_with_protocol_version_u32() -> &'static str {
//         r#"
// {
//   "type": "denm",
//   "origin": "self",
//   "version": "1.0.0",
//   "source_uuid": "uuid14",
//   "timestamp": 1574778515425,
//   "message": {
//     "protocol_version": 4242424242,
//     "station_id": 42,
//     "management_container": {
//       "action_id": {
//         "originating_station_id": 41,
//         "sequence_number": 1
//       },
//       "detection_time": 503253332000,
//       "reference_time": 503253330000,
//       "event_position": {
//         "latitude": 486263556,
//         "longitude": 224921234,
//         "altitude": 20000
//       }
//     }
//   }
// }
//     "#
//     }
//
//     fn bad_cpm_with_negative_timestamp() -> &'static str {
//         r#"
// {
//   "type": "cpm",
//   "origin": "self",
//   "version": "1.0.0",
//   "source_uuid": "uuid1",
//   "timestamp": -1,
//   "message": {
//     "protocol_version": 1,
//     "station_id": 12345,
//     "message_id": 14,
//     "generation_delta_time": 65535,
//     "management_container": {
//       "station_type": 5,
//       "reference_position": {
//         "latitude": 426263556,
//         "longitude": -82492123,
//         "altitude": 800001
//       },
//       "confidence": {
//         "position_confidence_ellipse": {
//           "semi_major_confidence": 4095,
//           "semi_minor_confidence": 4095,
//           "semi_major_orientation": 3601
//         },
//         "altitude": 15
//       }
//     },
//     "numberOfPerceivedObjects": 1
//   }
// }"#
//     }
//
//     fn remove_whitespace(s: &str) -> String {
//         s.split_whitespace().collect()
//     }
//
//     #[test]
//     fn it_can_deserialize_then_serialize_a_basic_cam() {
//         let json = basic_cam();
//         let cam: Exchange = serde_json::from_str(json).unwrap();
//         assert_eq!(cam.timestamp, 1574778515424);
//         assert_eq!(
//             serde_json::to_string(&cam).unwrap(),
//             remove_whitespace(json)
//         );
//     }
//
//     #[test]
//     fn it_can_deserialize_then_serialize_a_standard_cam() {
//         let json = standard_cam();
//         let cam: Exchange = serde_json::from_str(json).unwrap();
//         assert_eq!(cam.timestamp, 1574778515424);
//         if let Message::CAM(message) = &cam.message {
//             assert_eq!(message.high_frequency_container.speed.unwrap(), 365);
//             assert_eq!(
//                 serde_json::to_string(&cam).unwrap(),
//                 remove_whitespace(json)
//             );
//         } else {
//             panic!("no cam deserialized");
//         };
//     }
//
//     #[test]
//     fn it_can_deserialize_then_serialize_a_full_cam() {
//         let json = full_cam();
//         let cam: Exchange = serde_json::from_str(json).unwrap();
//         assert_eq!(cam.timestamp, 1574778515424);
//         if let Message::CAM(message) = &cam.message {
//             assert_eq!(message.high_frequency_container.speed.unwrap(), 253);
//             assert_eq!(
//                 message
//                     .high_frequency_container
//                     .curvature_calculation_mode
//                     .unwrap(),
//                 1
//             );
//             assert_eq!(
//                 serde_json::to_string(&cam).unwrap(),
//                 remove_whitespace(json)
//             );
//         } else {
//             panic!("no cam deserialized");
//         };
//     }
//
//     #[test]
//     fn it_can_deserialize_then_serialize_a_basic_denm() {
//         let json = basic_denm();
//         let denm: Exchange = serde_json::from_str(json).unwrap();
//         assert_eq!(denm.timestamp, 1574778515425);
//         if let Message::DENM(message) = &denm.message {
//             assert_eq!(
//                 message
//                     .management_container
//                     .action_id
//                     .originating_station_id,
//                 41
//             );
//             assert_eq!(
//                 serde_json::to_string(&denm).unwrap(),
//                 remove_whitespace(json)
//             );
//         } else {
//             panic!("no denm deserialized");
//         };
//     }
//
//     #[test]
//     fn it_can_deserialize_then_serialize_a_standard_denm() {
//         let json = standard_denm();
//         let denm: Exchange = serde_json::from_str(json).unwrap();
//         assert_eq!(denm.timestamp, 1574778515425);
//         if let Message::DENM(message) = &denm.message {
//             assert_eq!(
//                 message
//                     .management_container
//                     .action_id
//                     .originating_station_id,
//                 41
//             );
//             match &message.situation_container {
//                 Some(s) => assert_eq!(s.event_type.cause, 97),
//                 None => panic!("Situation container is undefined"),
//             };
//             assert_eq!(
//                 serde_json::to_string(&denm).unwrap(),
//                 remove_whitespace(json)
//             );
//         } else {
//             panic!("no denm deserialized");
//         };
//     }
//
//     #[test]
//     fn it_can_deserialize_then_serialize_a_full_denm() {
//         let json = full_denm();
//         let denm: Exchange = serde_json::from_str(json).unwrap();
//         assert_eq!(denm.timestamp, 1574778515425);
//         if let Message::DENM(message) = &denm.message {
//             assert_eq!(
//                 message
//                     .management_container
//                     .action_id
//                     .originating_station_id,
//                 41
//             );
//             match &message.situation_container {
//                 Some(s) => {
//                     assert_eq!(s.event_type.cause, 97);
//                     assert_eq!(s.information_quality.unwrap(), 1);
//                     match &s.linked_cause {
//                         Some(lc) => assert_eq!(lc.cause, 1),
//                         None => panic!("Linked cause is undefined"),
//                     };
//                 }
//                 None => panic!("Situation container is undefined"),
//             };
//             assert_eq!(
//                 serde_json::to_string(&denm).unwrap(),
//                 remove_whitespace(json)
//             );
//         } else {
//             panic!("no denm deserialized");
//         };
//     }
//
//     #[test]
//     fn it_can_deserialize_then_serialize_a_basic_cpm() {
//         let json = basic_cpm();
//         let cpm: Exchange = serde_json::from_str(json).unwrap();
//         assert_eq!(cpm.timestamp, 1574778515425);
//         assert_eq!(
//             serde_json::to_string(&cpm).unwrap(),
//             remove_whitespace(json)
//         );
//     }
//
//     #[test]
//     fn it_can_deserialize_then_serialize_a_standard_cpm() {
//         let json = standard_cpm();
//         let cpm: Exchange = serde_json::from_str(json).unwrap();
//         assert_eq!(cpm.timestamp, 1574778515425);
//         if let Message::CPM(message) = &cpm.message {
//             assert_eq!(
//                 message
//                     .station_data_container
//                     .as_ref()
//                     .unwrap()
//                     .originating_vehicle_container
//                     .as_ref()
//                     .unwrap()
//                     .speed,
//                 1600
//             );
//             assert_eq!(
//                 serde_json::to_string(&cpm).unwrap(),
//                 remove_whitespace(json)
//             );
//         } else {
//             panic!("no cpm deserialized");
//         };
//     }
//
//     #[test]
//     fn it_can_deserialize_then_serialize_a_full_cpm() {
//         let json = full_cpm();
//         let cpm: Exchange = serde_json::from_str(json).unwrap();
//         assert_eq!(cpm.timestamp, 1574778515425);
//         if let Message::CPM(message) = &cpm.message {
//             assert_eq!(
//                 message
//                     .station_data_container
//                     .as_ref()
//                     .unwrap()
//                     .originating_vehicle_container
//                     .as_ref()
//                     .unwrap()
//                     .speed,
//                 1600
//             );
//             assert_eq!(
//                 message.sensor_information_container[0]
//                     .detection_area
//                     .vehicle_sensor
//                     .as_ref()
//                     .unwrap()
//                     .x_sensor_offset,
//                 -3094
//             );
//             assert_eq!(
//                 serde_json::to_string(&cpm).unwrap(),
//                 remove_whitespace(json)
//             );
//         } else {
//             panic!("no cpm deserialized");
//         };
//     }
//
//     #[test]
//     #[should_panic]
//     fn it_should_panic_when_deserializing_a_cam_without_timestamp() {
//         let json = bad_cam_without_timestamp();
//         let _: Exchange = serde_json::from_str(json).unwrap();
//     }
//
//     #[test]
//     #[should_panic]
//     fn it_should_panic_when_deserializing_a_denm_with_timestamp_as_string() {
//         let json = bad_denm_with_string_timestamp();
//         let _: Exchange = serde_json::from_str(json).unwrap();
//     }
//
//     #[test]
//     #[should_panic]
//     fn it_should_panic_when_deserializing_a_cpm_without_timestamp() {
//         let json = bad_cpm_with_negative_timestamp();
//         let _: Exchange = serde_json::from_str(json).unwrap();
//     }
//
//     #[test]
//     #[should_panic]
//     fn it_should_panic_when_deserializing_a_denm_with_protocol_version_bigger_than_u8() {
//         let json = bad_denm_with_protocol_version_u32();
//         let _: Exchange = serde_json::from_str(json).unwrap();
//     }
//
//     #[test]
//     fn spat_exchange_deserialization() {
//         let spat_str = r#"{
//             "origin": "remoteSender",
//             "source_uuid": "uuid_3101",
//             "type": "spat",
//             "message": {
//                 "sendingStationId": 2327711328,
//                 "protocolVersion": 1,
//                 "id": 1654,
//                 "region": 751,
//                 "timestamp": 1665994085248,
//                 "revision": 1,
//                 "states": [{
//                     "ttc": 17352,
//                     "toc": 1665994102644,
//                     "nextChange": 1665994102644,
//                     "id": 1,
//                     "state": 6,
//                     "nextChanges": [{
//                         "ttc": 17352,
//                         "toc": 1665994102644,
//                         "nextChange": 1665994102644,
//                         "state": 6
//                     }]
//                 }, {
//                     "ttc": 21352,
//                     "toc": 1665994106644,
//                     "nextChange": 1665994106644,
//                     "id": 2,
//                     "state": 3,
//                     "nextChanges": [{
//                         "ttc": 21352,
//                         "toc": 1665994106644,
//                         "nextChange": 1665994106644,
//                         "state": 3
//                     }]
//                 }, {
//                     "ttc": 10452,
//                     "toc": 1665994095744,
//                     "nextChange": 1665994095744,
//                     "id": 3,
//                     "state": 6,
//                     "nextChanges": [{
//                         "ttc": 10452,
//                         "toc": 1665994095744,
//                         "nextChange": 1665994095744,
//                         "state": 6
//                     }]
//                 }, {
//                     "ttc": 16352,
//                     "toc": 1665994101644,
//                     "nextChange": 1665994101644,
//                     "id": 4,
//                     "state": 3,
//                     "nextChanges": [{
//                         "ttc": 16352,
//                         "toc": 1665994101644,
//                         "nextChange": 1665994101644,
//                         "state": 3
//                     }]
//                 }, {
//                     "ttc": 16352,
//                     "toc": 1665994101644,
//                     "nextChange": 1665994101644,
//                     "id": 5,
//                     "state": 3,
//                     "nextChanges": [{
//                         "ttc": 16352,
//                         "toc": 1665994101644,
//                         "nextChange": 1665994101644,
//                         "state": 3
//                     }]
//                 }, {
//                     "ttc": 23352,
//                     "toc": 1665994108644,
//                     "nextChange": 1665994108644,
//                     "id": 6,
//                     "state": 3,
//                     "nextChanges": [{
//                         "ttc": 23352,
//                         "toc": 1665994108644,
//                         "nextChange": 1665994108644,
//                         "state": 3
//                     }]
//                 }, {
//                     "ttc": 11352,
//                     "toc": 1665994096644,
//                     "nextChange": 1665994096644,
//                     "id": 7,
//                     "state": 6,
//                     "nextChanges": [{
//                         "ttc": 11352,
//                         "toc": 1665994096644,
//                         "nextChange": 1665994096644,
//                         "state": 6
//                     }]
//                 }, {
//                     "ttc": 17352,
//                     "toc": 1665994102644,
//                     "nextChange": 1665994102644,
//                     "id": 8,
//                     "state": 3,
//                     "nextChanges": [{
//                         "ttc": 17352,
//                         "toc": 1665994102644,
//                         "nextChange": 1665994102644,
//                         "state": 3
//                     }]
//                 }, {
//                     "ttc": 17352,
//                     "toc": 1665994102644,
//                     "nextChange": 1665994102644,
//                     "id": 9,
//                     "state": 6,
//                     "nextChanges": [{
//                         "ttc": 17352,
//                         "toc": 1665994102644,
//                         "nextChange": 1665994102644,
//                         "state": 6
//                     }]
//                 }, {
//                     "ttc": 15352,
//                     "toc": 1665994100644,
//                     "nextChange": 1665994100644,
//                     "id": 10,
//                     "state": 6,
//                     "nextChanges": [{
//                         "ttc": 15352,
//                         "toc": 1665994100644,
//                         "nextChange": 1665994100644,
//                         "state": 6
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 11,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 12,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 13,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 14,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 15,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 16,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 17,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 18,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 19,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 20,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 21,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 22,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 23,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 24,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 25,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 26,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 27,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 28,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 29,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 30,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 31,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 32,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 33,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 34,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 35,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }, {
//                     "toc": 0,
//                     "nextChange": 0,
//                     "id": 36,
//                     "state": 8,
//                     "nextChanges": [{
//                         "toc": 0,
//                         "nextChange": 0,
//                         "state": 8
//                     }]
//                 }]
//             },
//             "version": "1.0.0",
//             "timestamp": 1665994085292
//         }"#;
//
//         match serde_json::from_str::<Exchange>(spat_str) {
//             Ok(exchange) => {
//                 assert_eq!(exchange.type_field, "spat");
//             }
//             Err(e) => {
//                 assert_eq!(e.to_string(), "");
//             }
//         }
//     }
