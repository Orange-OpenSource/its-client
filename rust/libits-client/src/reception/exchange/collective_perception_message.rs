// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
use serde::{Deserialize, Serialize};

use crate::reception::exchange::mobile::Mobile;
use crate::reception::exchange::mobile_perceived_object::MobilePerceivedObject;
use crate::reception::exchange::perceived_object::PerceivedObject;
use crate::reception::exchange::{PositionConfidence, ReferencePosition};
use crate::reception::typed::Typed;

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct CollectivePerceptionMessage {
    pub protocol_version: u8,
    pub station_id: u32,
    // pub message_id: u8,
    pub generation_delta_time: u16,
    pub management_container: ManagementContainer,
    pub station_data_container: Option<StationDataContainer>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub sensor_information_container: Vec<SensorInformation>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub perceived_object_container: Vec<PerceivedObject>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub free_space_addendum_container: Vec<FreeSpaceAddendum>,
}

impl CollectivePerceptionMessage {
    pub fn mobile_perceived_object_list(&self) -> Vec<MobilePerceivedObject> {
        let cpm_heading = match &self.station_data_container {
            Some(station_data_container) => {
                if let Some(originating_vehicle_container) =
                    &station_data_container.originating_vehicle_container
                {
                    Some(originating_vehicle_container.heading)
                } else {
                    None
                }
            }
            None => None,
        };

        if !self.perceived_object_container.is_empty() {
            return self
                .perceived_object_container
                .iter()
                .map(|perceived_object| {
                    MobilePerceivedObject::new(
                        //assumed clone : we store a copy into the MobilePerceivedObject container
                        // TODO use a lifetime to propage the lifecycle betwwen PerceivedObject and MobilePerceivedObject instead of clone
                        perceived_object.clone(),
                        self.management_container.station_type,
                        self.station_id,
                        &self.management_container.reference_position,
                        cpm_heading,
                    )
                })
                .collect();
        }
        Vec::new()
    }
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct ManagementContainer {
    pub station_type: u8,
    pub reference_position: ReferencePosition,
    pub confidence: PositionConfidence,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct StationDataContainer {
    pub originating_vehicle_container: Option<OriginatingVehicleContainer>,
    pub originating_rsu_container: Option<OriginatingRSUContainer>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct OriginatingVehicleContainer {
    pub heading: u16,
    pub speed: u16,
    pub drive_direction: Option<u8>,
    pub vehicle_length: Option<u16>,
    pub vehicle_width: Option<u8>,
    pub longitudinal_acceleration: Option<i16>,
    pub yaw_rate: Option<i16>,
    pub lateral_acceleration: Option<i16>,
    pub vertical_acceleration: Option<i16>,
    pub confidence: OriginatingVehicleContainerConfidence,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct OriginatingRSUContainer {
    pub intersection_reference_id: Option<IntersectionReferenceId>,
    pub road_segment_reference_id: Option<u32>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct IntersectionReferenceId {
    pub road_regulator_id: Option<u32>,
    pub intersection_id: u32,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct OriginatingVehicleContainerConfidence {
    pub heading: u8,
    pub speed: u8,
    pub vehicle_length: Option<u8>,
    pub yaw_rate: Option<u8>,
    pub longitudinal_acceleration: Option<u8>,
    pub lateral_acceleration: Option<u8>,
    pub vertical_acceleration: Option<u8>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct SensorInformation {
    pub sensor_id: u8,
    #[serde(rename = "type")]
    pub sensor_type: u8,
    pub detection_area: DetectionArea,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct DetectionArea {
    pub vehicle_sensor: Option<VehicleSensor>,
    pub stationary_sensor_polygon: Option<Vec<Offset>>,
    pub stationary_sensor_radial: Option<StationarySensorRadial>,
    pub stationary_sensor_circular: Option<CircularArea>,
    pub stationary_sensor_ellipse: Option<EllipticArea>,
    pub stationary_sensor_rectangle: Option<RectangleArea>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct VehicleSensor {
    pub ref_point_id: u8,
    pub x_sensor_offset: i16,
    pub y_sensor_offset: i16,
    pub z_sensor_offset: Option<u16>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub vehicle_sensor_property_list: Vec<VehicleSensorProperty>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct VehicleSensorProperty {
    pub range: u16,
    pub horizontal_opening_angle_start: u16,
    pub horizontal_opening_angle_end: u16,
    pub vertical_opening_angle_start: Option<u16>,
    pub vertical_opening_angle_end: Option<u16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct StationarySensorRadial {
    pub range: u16,
    pub horizontal_opening_angle_start: u16,
    pub horizontal_opening_angle_end: u16,
    pub vertical_opening_angle_start: Option<u16>,
    pub vertical_opening_angle_end: Option<u16>,
    pub sensor_position_offset: Option<Offset>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CircularArea {
    pub node_center_point: Option<Offset>,
    pub radius: u16,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct EllipticArea {
    pub semi_major_range_length: u16,
    pub semi_minor_range_length: u16,
    pub semi_major_range_orientation: u16,
    pub node_center_point: Option<Offset>,
    pub semi_height: Option<u16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct RectangleArea {
    pub semi_major_range_length: u16,
    pub semi_minor_range_length: u16,
    pub semi_major_range_orientation: u16,
    pub node_center_point: Option<Offset>,
    pub semi_height: Option<u16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Offset {
    pub x: i32,
    pub y: i32,
    pub z: Option<i32>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct FreeSpaceAddendum {
    pub free_space_area: FreeSpaceArea,
    pub free_space_confidence: u8,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub sensor_id_list: Vec<u8>,
    pub shadowing_applies: Option<bool>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct FreeSpaceArea {
    pub free_space_polygon: Option<Vec<Offset>>,
    pub free_space_circular: Option<CircularArea>,
    pub free_space_ellipse: Option<EllipticArea>,
    pub free_space_rectangle: Option<RectangleArea>,
}

impl Mobile for CollectivePerceptionMessage {
    fn mobile_id(&self) -> u32 {
        self.station_id
    }

    fn position(&self) -> &ReferencePosition {
        &self.management_container.reference_position
    }

    fn speed(&self) -> Option<u16> {
        if let Some(station_data_container) = &self.station_data_container {
            if let Some(originating_vehicle_container) =
                &station_data_container.originating_vehicle_container
            {
                return Some(originating_vehicle_container.speed);
            }
        }
        None
    }

    fn heading(&self) -> Option<u16> {
        if let Some(station_data_container) = &self.station_data_container {
            if let Some(originating_vehicle_container) =
                &station_data_container.originating_vehicle_container
            {
                return Some(originating_vehicle_container.heading);
            }
        }
        None
    }
}

impl Typed for CollectivePerceptionMessage {
    fn get_type() -> String {
        "cpm".to_string()
    }
}

#[cfg(test)]
mod tests {
    use crate::reception::exchange::collective_perception_message::{
        CircularArea, CollectivePerceptionMessage, DetectionArea, EllipticArea, FreeSpaceAddendum,
        FreeSpaceArea, ManagementContainer, Offset, OriginatingVehicleContainer, RectangleArea,
        SensorInformation, StationDataContainer, StationarySensorRadial, VehicleSensor,
        VehicleSensorProperty,
    };
    use crate::reception::exchange::mobile_perceived_object::MobilePerceivedObject;
    use crate::reception::exchange::perceived_object::{ObjectConfidence, PerceivedObject};
    use crate::reception::exchange::reference_position::ReferencePosition;

    fn create_perceived_object_in_front() -> PerceivedObject {
        PerceivedObject {
            object_id: 45,
            time_of_measurement: 50,
            confidence: ObjectConfidence {
                x_distance: 102,
                y_distance: 102,
                x_speed: 127,
                y_speed: 127,
                object: Some(10),
            },
            x_distance: 40,
            y_distance: 100,
            x_speed: 1400,
            y_speed: 500,
            object_age: Default::default(),
            object_ref_point: Some(0),
            z_distance: None,
            z_speed: None,
            x_acceleration: None,
            y_acceleration: None,
            z_acceleration: None,
            roll_angle: None,
            pitch_angle: None,
            yaw_angle: None,
            roll_rate: None,
            pitch_rate: None,
            yaw_rate: None,
            roll_acceleration: None,
            pitch_acceleration: None,
            yaw_acceleration: None,
            lower_triangular_correlation_matrix_columns: vec![],
            planar_object_dimension_1: None,
            planar_object_dimension_2: None,
            vertical_object_dimension: None,
            sensor_id_list: vec![],
            dynamic_status: None,
            classification: vec![],
            matched_position: None,
        }
    }

    fn create_perceived_object_behind() -> PerceivedObject {
        PerceivedObject {
            object_id: 48,
            time_of_measurement: 51,
            confidence: ObjectConfidence {
                x_distance: 102,
                y_distance: 102,
                x_speed: 127,
                y_speed: 127,
                object: Some(10),
            },
            x_distance: -40,
            y_distance: 100,
            x_speed: 1200,
            y_speed: 400,
            object_age: Default::default(),
            object_ref_point: Some(0),
            z_distance: None,
            z_speed: None,
            x_acceleration: None,
            y_acceleration: None,
            z_acceleration: None,
            roll_angle: None,
            pitch_angle: None,
            yaw_angle: None,
            roll_rate: None,
            pitch_rate: None,
            yaw_rate: None,
            roll_acceleration: None,
            pitch_acceleration: None,
            yaw_acceleration: None,
            lower_triangular_correlation_matrix_columns: vec![],
            planar_object_dimension_1: None,
            planar_object_dimension_2: None,
            vertical_object_dimension: None,
            sensor_id_list: vec![],
            dynamic_status: None,
            classification: vec![],
            matched_position: None,
        }
    }

    fn create_cpm_with_two_perceived_object() -> CollectivePerceptionMessage {
        CollectivePerceptionMessage {
            protocol_version: 1,
            station_id: 31470,
            // message_id: 12,
            generation_delta_time: 65535,
            management_container: ManagementContainer {
                station_type: 5,
                reference_position: ReferencePosition {
                    latitude: 426263556,
                    longitude: -82492123,
                    altitude: 800001,
                },
                confidence: Default::default(),
            },
            station_data_container: Some(StationDataContainer {
                originating_vehicle_container: Some(OriginatingVehicleContainer {
                    heading: 900,
                    speed: 1600,
                    ..Default::default()
                }),
                ..Default::default()
            }),
            sensor_information_container: vec![SensorInformation {
                sensor_id: 3,
                sensor_type: 3,
                detection_area: DetectionArea {
                    vehicle_sensor: Some(VehicleSensor {
                        ref_point_id: 0,
                        x_sensor_offset: -20,
                        y_sensor_offset: 20,
                        z_sensor_offset: Some(0),
                        vehicle_sensor_property_list: vec![VehicleSensorProperty {
                            range: 5000,
                            horizontal_opening_angle_start: 600,
                            horizontal_opening_angle_end: 600,
                            vertical_opening_angle_start: None,
                            vertical_opening_angle_end: None,
                        }],
                    }),
                    stationary_sensor_polygon: Some(vec![
                        Offset {
                            x: 1,
                            y: 2,
                            z: Some(0),
                        },
                        Offset {
                            x: 11,
                            y: 22,
                            z: Some(1),
                        },
                        Offset {
                            x: 111,
                            y: 222,
                            z: Some(2),
                        },
                    ]),
                    stationary_sensor_radial: Some(StationarySensorRadial {
                        range: 10000,
                        horizontal_opening_angle_start: 900,
                        horizontal_opening_angle_end: 2700,
                        vertical_opening_angle_start: None,
                        vertical_opening_angle_end: None,
                        sensor_position_offset: Some(Offset {
                            x: 1,
                            y: 2,
                            z: None,
                        }),
                    }),
                    stationary_sensor_circular: Some(CircularArea {
                        radius: 10000,
                        node_center_point: Some(Offset {
                            x: 1,
                            y: 2,
                            z: None,
                        }),
                    }),
                    stationary_sensor_ellipse: Some(EllipticArea {
                        semi_major_range_length: 1,
                        semi_minor_range_length: 2,
                        semi_major_range_orientation: 3,
                        semi_height: Some(4),
                        node_center_point: Some(Offset {
                            x: 5,
                            y: 6,
                            z: None,
                        }),
                    }),
                    stationary_sensor_rectangle: Some(RectangleArea {
                        semi_major_range_length: 1,
                        semi_minor_range_length: 2,
                        semi_major_range_orientation: 3,
                        semi_height: Some(4),
                        node_center_point: Some(Offset {
                            x: 5,
                            y: 6,
                            z: None,
                        }),
                    }),
                },
            }],
            perceived_object_container: vec![
                create_perceived_object_in_front(),
                create_perceived_object_behind(),
            ],
            free_space_addendum_container: vec![FreeSpaceAddendum {
                free_space_area: FreeSpaceArea {
                    free_space_polygon: Some(vec![
                        Offset {
                            x: 1,
                            y: 2,
                            z: None,
                        },
                        Offset {
                            x: 11,
                            y: 22,
                            z: None,
                        },
                        Offset {
                            x: 111,
                            y: 222,
                            z: None,
                        },
                        Offset {
                            x: 1111,
                            y: 2222,
                            z: None,
                        },
                    ]),
                    free_space_circular: None,
                    free_space_ellipse: None,
                    free_space_rectangle: None,
                },
                free_space_confidence: 50,
                sensor_id_list: vec![1, 2, 3, 4, 5],
                shadowing_applies: Some(false),
            }],
        }
    }

    #[test]
    fn it_can_provide_the_mobile_perceived_object_list() {
        assert_eq!(
            create_cpm_with_two_perceived_object().mobile_perceived_object_list(),
            vec![
                MobilePerceivedObject {
                    perceived_object: create_perceived_object_in_front(),
                    mobile_id: 3147045,
                    reference_position: ReferencePosition {
                        latitude: 426263645,
                        longitude: -82492074,
                        altitude: 800001,
                    },
                    speed: 1486,
                    heading: 900
                },
                MobilePerceivedObject {
                    perceived_object: create_perceived_object_behind(),
                    mobile_id: 3147048,
                    reference_position: ReferencePosition {
                        latitude: 426263645,
                        longitude: -82492170,
                        altitude: 800001,
                    },
                    speed: 1264,
                    heading: 900
                },
            ]
        );
    }

    #[test]
    fn test_deserialize() {
        let data = r#"{
            "protocol_version": 0,
            "station_id": 0,
            "generation_delta_time": 0,
            "management_container": {
              "station_type": 15,
              "reference_position": {
                "latitude": 488640493,
                "longitude": 23310526,
                "altitude": 900
              },
              "confidence": {
                "position_confidence_ellipse": {
                  "semi_major_confidence": 1,
                  "semi_minor_confidence": 1,
                  "semi_major_orientation": 0
                },
                "altitude": 0
              }
            },
            "perceived_object_container": [
              {
                "object_id": 5,
                "time_of_measurement": 2,
                "x_distance": 804,
                "y_distance": 400,
                "x_speed": 401,
                "y_speed": 401,
                "object_age": 1500,
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
              },
              {
                "object_id": 7,
                "time_of_measurement": 2,
                "x_distance": -1594,
                "y_distance": 540,
                "x_speed": 652,
                "y_speed": 652,
                "object_age": 1500,
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
              },
              {
                "object_id": 9,
                "time_of_measurement": 3,
                "x_distance": 1009,
                "y_distance": 581,
                "x_speed": 283,
                "y_speed": 283,
                "object_age": 1500,
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
              },
              {
                "object_id": 11,
                "time_of_measurement": 3,
                "x_distance": -224,
                "y_distance": 3077,
                "x_speed": 343,
                "y_speed": 343,
                "object_age": 1500,
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
              },
              {
                "object_id": 12,
                "time_of_measurement": 4,
                "x_distance": 3329,
                "y_distance": -813,
                "x_speed": 735,
                "y_speed": 735,
                "object_age": 1500,
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
              },
              {
                "object_id": 88,
                "time_of_measurement": 7,
                "x_distance": 1056,
                "y_distance": 979,
                "y_speed": 7,
                "x_speed": 7,
                "object_age": 1500,
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
              },
              {
                "object_id": 195,
                "time_of_measurement": 7,
                "x_distance": -365,
                "y_distance": 2896,
                "x_speed": 514,
                "y_speed": 514,
                "object_age": 1500,
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
              },
              {
                "object_id": 200,
                "time_of_measurement": 7,
                "x_distance": 42,
                "y_distance": 1523,
                "x_speed": 948,
                "y_speed": 948,
                "object_age": 1500,
                "dynamic_status": 0,
                "classification": [
                  {
                    "object_class": {
                      "single_vru": {
                        "bicyclist": 1
                      }
                    },
                    "confidence": 36
                  }
                ],
                "confidence": {
                  "x_distance": 4095,
                  "y_distance": 4095,
                  "x_speed": 0,
                  "y_speed": 0,
                  "object": 10
                }
              },
              {
                "object_id": 206,
                "time_of_measurement": 7,
                "x_distance": -857,
                "y_distance": 117,
                "x_speed": 241,
                "y_speed": 241,
                "object_age": 1500,
                "dynamic_status": 0,
                "classification": [
                  {
                    "object_class": {
                      "single_vru": {
                        "bicyclist": 1
                      }
                    },
                    "confidence": 36
                  }
                ],
                "confidence": {
                  "x_distance": 4095,
                  "y_distance": 4095,
                  "x_speed": 0,
                  "y_speed": 0,
                  "object": 10
                }
              },
              {
                "object_id": 214,
                "time_of_measurement": 8,
                "x_distance": 776,
                "y_distance": 498,
                "x_speed": 223,
                "y_speed": 223,
                "object_age": 1500,
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
              },
              {
                "object_id": 225,
                "time_of_measurement": 9,
                "x_distance": -103,
                "y_distance": 511,
                "x_speed": 556,
                "y_speed": 556,
                "object_age": 1500,
                "dynamic_status": 0,
                "classification": [
                  {
                    "object_class": {
                      "single_vru": {
                        "bicyclist": 1
                      }
                    },
                    "confidence": 39
                  }
                ],
                "confidence": {
                  "x_distance": 4095,
                  "y_distance": 4095,
                  "x_speed": 0,
                  "y_speed": 0,
                  "object": 10
                }
              },
              {
                "object_id": 229,
                "time_of_measurement": 9,
                "x_distance": 1603,
                "y_distance": 517,
                "x_speed": 154,
                "y_speed": 154,
                "object_age": 1500,
                "dynamic_status": 0,
                "classification": [
                  {
                    "object_class": {
                      "single_vru": {
                        "pedestrian": 1
                      }
                    },
                    "confidence": 46
                  }
                ],
                "confidence": {
                  "x_distance": 4095,
                  "y_distance": 4095,
                  "x_speed": 0,
                  "y_speed": 0,
                  "object": 10
                }
              },
              {
                "object_id": 241,
                "time_of_measurement": 10,
                "x_distance": 1872,
                "y_distance": -164,
                "x_speed": 134,
                "y_speed": 134,
                "object_age": 1500,
                "dynamic_status": 0,
                "classification": [
                  {
                    "object_class": {
                      "single_vru": {
                        "pedestrian": 1
                      }
                    },
                    "confidence": 44
                  }
                ],
                "confidence": {
                  "x_distance": 4095,
                  "y_distance": 4095,
                  "x_speed": 0,
                  "y_speed": 0,
                  "object": 10
                }
              },
              {
                "object_id": 244,
                "time_of_measurement": 10,
                "x_distance": 611,
                "y_distance": 339,
                "x_speed": 283,
                "y_speed": 283,
                "object_age": 1500,
                "dynamic_status": 0,
                "classification": [
                  {
                    "object_class": {
                      "single_vru": {
                        "pedestrian": 1
                      }
                    },
                    "confidence": 44
                  }
                ],
                "confidence": {
                  "x_distance": 4095,
                  "y_distance": 4095,
                  "x_speed": 0,
                  "y_speed": 0,
                  "object": 10
                }
              },
              {
                "object_id": 247,
                "time_of_measurement": 10,
                "x_distance": -645,
                "y_distance": 12,
                "x_speed": 735,
                "y_speed": 735,
                "object_age": 1500,
                "dynamic_status": 0,
                "classification": [
                  {
                    "object_class": {
                      "single_vru": {
                        "pedestrian": 1
                      }
                    },
                    "confidence": 44
                  }
                ],
                "confidence": {
                  "x_distance": 4095,
                  "y_distance": 4095,
                  "x_speed": 0,
                  "y_speed": 0,
                  "object": 10
                }
              },
              {
                "object_id": 249,
                "time_of_measurement": 10,
                "x_distance": 4360,
                "y_distance": -262,
                "x_speed": 513,
                "y_speed": 513,
                "object_age": 1500,
                "dynamic_status": 0,
                "classification": [
                  {
                    "object_class": {
                      "single_vru": {
                        "pedestrian": 1
                      }
                    },
                    "confidence": 44
                  }
                ],
                "confidence": {
                  "x_distance": 4095,
                  "y_distance": 4095,
                  "x_speed": 0,
                  "y_speed": 0,
                  "object": 10
                }
              }
            ]
        }"#;

        match serde_json::from_str::<CollectivePerceptionMessage>(data) {
            Ok(cpm) => {
                assert_eq!(cpm.station_id, 0);
            }
            Err(e) => {
                panic!("Failed to deserialize CPM: '{}'", e);
            }
        }
    }

    #[test]
    fn test_deserialize_full_cpm() {
        let data = r#"{
		"protocol_version": 255,
		"station_id": 4294967295,
		"generation_delta_time": 65535,
		"management_container": {
			"station_type": 254,
			"reference_position": {
				"latitude": 426263556,
				"longitude": -82492123,
				"altitude": 800001
			},
			"confidence": {
				"position_confidence_ellipse": {
					"semi_major_confidence": 4095,
					"semi_minor_confidence": 4095,
					"semi_major_orientation": 3601
				},
				"altitude": 15
			}
		},
		"station_data_container": {
			"originating_vehicle_container": {
				"heading": 180,
				"speed": 1600,
				"drive_direction": 0,
				"vehicle_length": 31,
				"vehicle_width": 18,
				"longitudinal_acceleration": -160,
				"yaw_rate": -32766,
				"lateral_acceleration": -2,
				"vertical_acceleration": -1,
				"confidence": {
					"heading": 127,
					"speed": 127,
					"vehicle_length": 3,
					"yaw_rate": 2,
					"longitudinal_acceleration": 12,
					"lateral_acceleration": 13,
					"vertical_acceleration": 14
				}
			}
		},
		"sensor_information_container": [{
			"sensor_id": 1,
			"type": 3,
			"detection_area": {
				"vehicle_sensor": {
					"ref_point_id": 255,
					"x_sensor_offset": -3094,
					"y_sensor_offset": -1000,
					"z_sensor_offset": 1000,
					"vehicle_sensor_property_list": [{
						"range": 10000,
						"horizontal_opening_angle_start": 3601,
						"horizontal_opening_angle_end": 3601,
						"vertical_opening_angle_start": 3601,
						"vertical_opening_angle_end": 3601
					}]
				},
				"stationary_sensor_radial": {
					"range": 10000,
					"horizontal_opening_angle_start": 3601,
					"horizontal_opening_angle_end": 3601,
					"vertical_opening_angle_start": 3601,
					"vertical_opening_angle_end": 3601,
					"sensor_position_offset": {
						"x": 32767,
						"y": -32768,
						"z": 0
					}
				},
				"stationary_sensor_polygon": [{
						"x": -32768,
						"y": 32767,
						"z": 0
					},
					{
						"x": 10,
						"y": 20,
						"z": 30
					},
					{
						"x": 32767,
						"y": -32768,
						"z": 0
					}
				],
				"stationary_sensor_circular": {
					"radius": 10000,
					"node_center_point": {
						"x": 32767,
						"y": -32768,
						"z": 0
					}
				},
				"stationary_sensor_ellipse": {
					"node_center_point": {
						"x": 32767,
						"y": -32768,
						"z": 0
					},
					"semi_major_range_length": 10000,
					"semi_minor_range_length": 10000,
					"semi_major_range_orientation": 3601,
					"semi_height": 10000
				},
				"stationary_sensor_rectangle": {
					"node_center_point": {
						"x": 32767,
						"y": -32768,
						"z": 0
					},
					"semi_major_range_length": 10000,
					"semi_minor_range_length": 10000,
					"semi_major_range_orientation": 3601,
					"semi_height": 10000
				}
			}
		}],
		"perceived_object_container": [{
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
		}],
		"free_space_addendum_container": [{
				"free_space_area": {
					"free_space_polygon": [{
							"x": -32768,
							"y": 32767,
							"z": 0
						},
						{
							"x": 10,
							"y": 20,
							"z": 30
						},
						{
							"x": 32767,
							"y": -32768,
							"z": 0
						}
					]
				},
				"free_space_confidence": 0,
				"sensor_id_list": [0, 1, 2],
				"shadowing_applies": false
			},
			{
				"free_space_area": {
					"free_space_circular": {
						"radius": 10000,
						"node_center_point": {
							"x": 32767,
							"y": -32768,
							"z": 0
						}
					}
				},
				"free_space_confidence": 101,
				"sensor_id_list": [10, 20, 30],
				"shadowing_applies": true
			},
			{
				"free_space_area": {
					"free_space_ellipse": {
						"node_center_point": {
							"x": 32767,
							"y": -32768,
							"z": 0
						},
						"semi_major_range_length": 10000,
						"semi_minor_range_length": 10000,
						"semi_major_range_orientation": 3601,
						"semi_height": 10000
					}
				},
				"free_space_confidence": 0,
				"sensor_id_list": [100, 102, 103],
				"shadowing_applies": false
			},
			{
				"free_space_area": {
					"free_space_rectangle": {
						"node_center_point": {
							"x": 32767,
							"y": -32768,
							"z": 0
						},
						"semi_major_range_length": 10000,
						"semi_minor_range_length": 10000,
						"semi_major_range_orientation": 3601,
						"semi_height": 10000
					}
				},
				"free_space_confidence": 50,
				"sensor_id_list": [200, 250, 255],
				"shadowing_applies": true
			}
		]
	}"#;

        match serde_json::from_str::<CollectivePerceptionMessage>(data) {
            Ok(cpm) => {
                assert_eq!(cpm.station_id, 4294967295);
            }
            Err(e) => {
                panic!("Failed to deserialize CPM: '{}'", e);
            }
        }
    }

    #[test]
    fn test_deserialize_minimal_offset() {
        let data = r#"{
            "x": 123456,
            "y": 654321
        }"#;

        match serde_json::from_str::<Offset>(data) {
            Ok(offset) => {
                assert_eq!(offset.x, 123456);
                assert_eq!(offset.y, 654321);
                assert!(offset.z.is_none());
            }
            Err(e) => panic!("Failed to deserialize minimal Offset: '{}'", e),
        }
    }

    #[test]
    fn test_deserialize_full_offset() {
        let data = r#"{
            "x": 123456,
            "y": 654321,
            "z": 456789
        }"#;

        match serde_json::from_str::<Offset>(data) {
            Ok(offset) => {
                assert_eq!(offset.x, 123456);
                assert_eq!(offset.y, 654321);
                assert_eq!(offset.z, Some(456789));
            }
            Err(e) => panic!("Failed to deserialize minimal Offset: '{}'", e),
        }
    }

    #[test]
    fn test_deserialize_minimal_stationary_sensor_radial() {
        let data = r#"{
            "range": 1,
            "horizontal_opening_angle_start": 2,
            "horizontal_opening_angle_end": 3
        }"#;

        match serde_json::from_str::<StationarySensorRadial>(data) {
            Ok(stationary_sensor_radial) => {
                assert_eq!(stationary_sensor_radial.range, 1);
                assert_eq!(stationary_sensor_radial.horizontal_opening_angle_start, 2);
                assert_eq!(stationary_sensor_radial.horizontal_opening_angle_end, 3);
                assert!(stationary_sensor_radial.sensor_position_offset.is_none());
                assert!(stationary_sensor_radial
                    .vertical_opening_angle_start
                    .is_none());
                assert!(stationary_sensor_radial
                    .vertical_opening_angle_end
                    .is_none());
            }
            Err(e) => panic!(
                "Failed to deserialize minimal StationarySensorRadial: '{}'",
                e
            ),
        }
    }

    #[test]
    fn test_deserialize_full_stationary_sensor_radial() {
        let data = r#"{
            "range": 1,
            "horizontal_opening_angle_start": 2,
            "horizontal_opening_angle_end": 3,
            "sensor_position_offset": {
                "x": 123456,
                "y": 654321
            },
            "vertical_opening_angle_start": 123,
            "vertical_opening_angle_end": 456
        }"#;

        match serde_json::from_str::<StationarySensorRadial>(data) {
            Ok(stationary_sensor_radial) => {
                assert_eq!(stationary_sensor_radial.range, 1);
                assert_eq!(stationary_sensor_radial.horizontal_opening_angle_start, 2);
                assert_eq!(stationary_sensor_radial.horizontal_opening_angle_end, 3);
                assert_eq!(
                    stationary_sensor_radial.sensor_position_offset,
                    Some(Offset {
                        x: 123456,
                        y: 654321,
                        z: None
                    })
                );
                assert_eq!(
                    stationary_sensor_radial.vertical_opening_angle_start,
                    Some(123)
                );
                assert_eq!(
                    stationary_sensor_radial.vertical_opening_angle_end,
                    Some(456)
                );
            }
            Err(e) => panic!(
                "Failed to deserialize minimal StationarySensorRadial: '{}'",
                e
            ),
        }
    }

    #[test]
    fn deserialize_minimal_circular_area() {
        let data = r#"{
            "radius": 999
        }"#;

        match serde_json::from_str::<CircularArea>(data) {
            Ok(circular_area) => {
                assert_eq!(circular_area.radius, 999);
                assert!(circular_area.node_center_point.is_none());
            }
            Err(e) => panic!("Failed to deserialize: '{}'", e),
        }
    }

    #[test]
    fn deserialize_full_circular_area() {
        let data = r#"{
            "radius": 999,
            "node_center_point": {
                "x": 1,
                "y": 2
            }
        }"#;

        match serde_json::from_str::<CircularArea>(data) {
            Ok(circular_area) => {
                assert_eq!(circular_area.radius, 999);
                assert!(circular_area.node_center_point.is_some());
            }
            Err(e) => panic!("Failed to deserialize: '{}'", e),
        }
    }

    #[test]
    fn deserialize_minimal_elliptic_area() {
        let data = r#"{
            "semi_major_range_length": 1,
            "semi_minor_range_length": 2,
            "semi_major_range_orientation": 3
        }"#;

        match serde_json::from_str::<EllipticArea>(data) {
            Ok(elliptic_area) => {
                assert_eq!(elliptic_area.semi_major_range_length, 1);
                assert_eq!(elliptic_area.semi_minor_range_length, 2);
                assert_eq!(elliptic_area.semi_major_range_orientation, 3);
                assert!(elliptic_area.node_center_point.is_none());
                assert!(elliptic_area.semi_height.is_none());
            }
            Err(e) => panic!("Failed to deserialize EllipticArea: '{}'", e),
        }
    }

    #[test]
    fn deserialize_full_elliptic_area() {
        let data = r#"{
            "semi_major_range_length": 1,
            "semi_minor_range_length": 2,
            "semi_major_range_orientation": 3,
            "semi_height": 4,
            "node_center_point": {
                "x": 5,
                "y": 6
            }
        }"#;

        match serde_json::from_str::<EllipticArea>(data) {
            Ok(elliptic_area) => {
                assert_eq!(elliptic_area.semi_major_range_length, 1);
                assert_eq!(elliptic_area.semi_minor_range_length, 2);
                assert_eq!(elliptic_area.semi_major_range_orientation, 3);
                assert!(elliptic_area.node_center_point.is_some());
                assert!(elliptic_area.semi_height.is_some());
            }
            Err(e) => panic!("Failed to deserialize EllipticArea: '{}'", e),
        }
    }

    #[test]
    fn deserialize_minimal_rectangle_area() {
        let data = r#"{
            "semi_major_range_length": 1,
            "semi_minor_range_length": 2,
            "semi_major_range_orientation": 3
        }"#;

        match serde_json::from_str::<RectangleArea>(data) {
            Ok(elliptic_area) => {
                assert_eq!(elliptic_area.semi_major_range_length, 1);
                assert_eq!(elliptic_area.semi_minor_range_length, 2);
                assert_eq!(elliptic_area.semi_major_range_orientation, 3);
                assert!(elliptic_area.node_center_point.is_none());
                assert!(elliptic_area.semi_height.is_none());
            }
            Err(e) => panic!("Failed to deserialize RectangleArea: '{}'", e),
        }
    }

    #[test]
    fn deserialize_full_rectangle_area() {
        let data = r#"{
            "semi_major_range_length": 1,
            "semi_minor_range_length": 2,
            "semi_major_range_orientation": 3,
            "semi_height": 4,
            "node_center_point": {
                "x": 5,
                "y": 6
            }
        }"#;

        match serde_json::from_str::<RectangleArea>(data) {
            Ok(elliptic_area) => {
                assert_eq!(elliptic_area.semi_major_range_length, 1);
                assert_eq!(elliptic_area.semi_minor_range_length, 2);
                assert_eq!(elliptic_area.semi_major_range_orientation, 3);
                assert!(elliptic_area.node_center_point.is_some());
                assert!(elliptic_area.semi_height.is_some());
            }
            Err(e) => panic!("Failed to deserialize RectangleArea: '{}'", e),
        }
    }

    #[test]
    fn deserialize_minimal_frees_space_area() {
        let data = r#"{
            "free_space_area": {
                "free_space_polygon": [{
                        "x": -32768,
                        "y": 32767,
                        "z": 0
                    },
                    {
                        "x": 10,
                        "y": 20,
                        "z": 30
                    },
                    {
                        "x": 32767,
                        "y": -32768,
                        "z": 0
                    }
                ]
            },
            "free_space_confidence": 12
        }"#;

        match serde_json::from_str::<FreeSpaceAddendum>(data) {
            Ok(free_space_addendum) => {
                assert!(free_space_addendum
                    .free_space_area
                    .free_space_polygon
                    .is_some());
                assert!(free_space_addendum
                    .free_space_area
                    .free_space_circular
                    .is_none());
                assert!(free_space_addendum
                    .free_space_area
                    .free_space_rectangle
                    .is_none());
                assert!(free_space_addendum
                    .free_space_area
                    .free_space_ellipse
                    .is_none());
                assert_eq!(free_space_addendum.free_space_confidence, 12);
                assert!(free_space_addendum.sensor_id_list.is_empty());
                assert!(free_space_addendum.shadowing_applies.is_none());
            }
            Err(e) => panic!("Failed to deserialize FreeSpaceAddendum: '{}'", e),
        }
    }

    #[test]
    fn d() {
        let data = r#"{
            "free_space_area": {
                "free_space_circular": {
                    "radius": 10000,
                    "node_center_point": {
                        "x": 32767,
                        "y": -32768,
                        "z": 0
                    }
                }
            },
            "free_space_confidence": 101,
            "sensor_id_list": [10, 20, 30],
            "shadowing_applies": true
        }"#;

        match serde_json::from_str::<FreeSpaceAddendum>(data) {
            Ok(free_space_addendum) => {
                assert!(free_space_addendum
                    .free_space_area
                    .free_space_polygon
                    .is_none());
                assert!(free_space_addendum
                    .free_space_area
                    .free_space_circular
                    .is_some());
                assert!(free_space_addendum
                    .free_space_area
                    .free_space_rectangle
                    .is_none());
                assert!(free_space_addendum
                    .free_space_area
                    .free_space_ellipse
                    .is_none());
                assert_eq!(free_space_addendum.free_space_confidence, 101);
                assert_eq!(free_space_addendum.sensor_id_list.len(), 3);
                assert_eq!(free_space_addendum.shadowing_applies, Some(true))
            }
            Err(e) => panic!("Failed to deserialize FreeSpaceAddendum: '{}'", e),
        }
    }
}
