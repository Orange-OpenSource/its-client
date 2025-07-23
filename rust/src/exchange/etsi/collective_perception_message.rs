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

use crate::exchange::etsi::angle::Angle;
pub(crate) use crate::exchange::etsi::heading_from_etsi;
use crate::exchange::etsi::map_extended_message::{Intersection, RoadSegment};
use crate::exchange::etsi::perceived_object::PerceivedObject;
use crate::exchange::etsi::reference_position::ReferencePosition;
use crate::exchange::etsi::shape::Shape;
use crate::exchange::etsi::timestamp_to_etsi;
use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::message::content_error::ContentError::{NotAMortal, NotOriginatingVehicle};
use crate::exchange::mortal::Mortal;
use crate::mobility::mobile::Mobile;
use crate::mobility::mobile_perceived_object::MobilePerceivedObject;
use crate::mobility::position::Position;
use serde::{Deserialize, Serialize};
use std::any::type_name;

/// Represents a Collective Perception Message (CPM) according to an ETSI standard.
///
/// This message is used to describe information around itself.
/// It implements the schema defined in the [CPM version 2.1.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cpm/cpm_schema_2-1-0.json
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CollectivePerceptionMessage {
    /// Protocol version (mandatory).
    pub protocol_version: u8,
    /// Unique identifier for the station (mandatory).
    pub station_id: u32,
    /// Container with management information about the station (mandatory).
    pub management_container: ManagementContainer,

    /// Container with originating vehicle specifications (optional).
    pub originating_vehicle_container: Option<OriginatingVehicleContainer>,
    /// List of originating rsu specifications (optional).
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub originating_rsu_container: Vec<MapReference>,
    /// List of sensor specifications (optional).
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub sensor_information_container: Vec<SensorInformation>,
    /// List of perception region information (optional).
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub perception_region_container: Vec<PerceptionRegion>,
    /// List of detected objects (optional).
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub perceived_object_container: Vec<PerceivedObject>,
}

/// Represents the management container of a CPM.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct ManagementContainer {
    /// Reference time of the station (mandatory).
    pub reference_time: u64,
    /// Reference position of the station (mandatory).
    pub reference_position: ReferencePosition,

    /// Information regarding the message segmentation on the facility layer (optional).
    pub segmentation_info: Option<SegmentationInfo>,
    /// The planned or expected range of the CPM generation rate (optional).
    pub message_rate_range: Option<MessageRateRange>,
}

/// Represents the container for data originating from a vehicle.
/// Includes information about the vehicle's heading, speed, dimensions, and acceleration.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct OriginatingVehicleContainer {
    pub orientation_angle: Angle,

    pub pitch_angle: Option<Angle>,
    pub roll_angle: Option<Angle>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub trailer_data_set: Vec<Trailer>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PerceptionRegion {
    pub measurement_delta_time: i16,
    pub perception_region_confidence: u8,
    pub perception_region_shape: Shape,
    pub shadowing_applies: bool,

    pub sensor_id_list: Vec<u8>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub perceived_object_ids: Vec<u16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct MapReference {
    pub road_segment: Option<RoadSegment>,
    pub intersection: Option<Intersection>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Trailer {
    pub ref_point_id: u8,
    pub hitch_point_offset: u8,
    pub hitch_angle: Angle,

    pub front_overhang: Option<u8>,
    pub rear_overhang: Option<u8>,
    pub trailer_width: Option<u8>,
}

/// Represents information about a sensor, including its identifier, type, and detection area.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct SensorInformation {
    /// Sensor identifier (mandatory).
    pub sensor_id: u8,
    /// Type of the sensor (mandatory).
    pub sensor_type: SensorType,
    /// Indicates if the standard shadowing approach applies to the described perception region (mandatory).
    pub shadowing_applies: bool,

    /// The perception region of the sensor (optional).
    pub perception_region_shape: Option<Shape>,
    ///The homogeneous perception region confidence that can be assumed for the entire perception region shape of this sensor (optional).
    pub perception_region_confidence: Option<u8>,
}

#[derive(Default, Debug, Clone, Copy, Hash, PartialEq, Eq, Serialize, Deserialize)]
#[serde(from = "u8", into = "u8")]
pub enum SensorType {
    #[default]
    Undefined = 0,
    Radar = 1,
    Lidar = 2,
    MonoVideo = 3,
    StereoVision = 4,
    NightVision = 5,
    Ultrasonic = 6,
    Pmd = 7,
    InductionLoop = 8,
    SphericalCamera = 9,
    Uwb = 10,
    Acoustic = 11,
    LocalAggregation = 12,
    ItsAggregation = 13,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct SegmentationInfo {
    pub total_msg_no: u8,
    pub this_msg_no: u8,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct MessageRateRange {
    pub message_rate_min: MessageRateHz,
    pub message_rate_max: MessageRateHz,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct MessageRateHz {
    pub mantissa: u8,
    pub exponent: i8,
}

impl CollectivePerceptionMessage {
    pub fn mobile_perceived_object_list(&self) -> Vec<MobilePerceivedObject> {
        self.perceived_object_container
            .iter()
            .map(|perceived_object| {
                MobilePerceivedObject::new(
                    // assumed clone : we store a copy into the MobilePerceivedObject container
                    // TODO use a lifetime to propage the lifecycle between PerceivedObject and MobilePerceivedObject instead of clone
                    perceived_object.clone(),
                    self,
                )
            })
            .collect()
    }
}

impl Mobile for CollectivePerceptionMessage {
    fn id(&self) -> u32 {
        self.station_id
    }

    fn position(&self) -> Position {
        self.management_container.reference_position.as_position()
    }

    fn speed(&self) -> Option<f64> {
        // No speed is provided
        None
    }

    fn heading(&self) -> Option<f64> {
        if let Some(originating_vehicle_container) = &self.originating_vehicle_container {
            return Some(heading_from_etsi(
                originating_vehicle_container.orientation_angle.value,
            ));
        }
        None
    }

    fn acceleration(&self) -> Option<f64> {
        // No acceleration is provided
        None
    }
}

impl Content for CollectivePerceptionMessage {
    fn get_type(&self) -> &str {
        "cpm"
    }

    fn appropriate(&mut self, timestamp: u64, new_station_id: u32) {
        self.station_id = new_station_id;
        self.management_container.reference_time = timestamp_to_etsi(timestamp);
    }

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError> {
        match self.originating_vehicle_container {
            Some(_) => Ok(self),
            None => Err(NotOriginatingVehicle(type_name::<
                CollectivePerceptionMessage,
            >())),
        }
    }

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError> {
        Err(NotAMortal(type_name::<CollectivePerceptionMessage>()))
    }
}

impl SensorType {
    fn try_from(value: u8) -> Result<SensorType, String> {
        match value {
            0 => Ok(SensorType::Undefined),
            1 => Ok(SensorType::Radar),
            2 => Ok(SensorType::Lidar),
            3 => Ok(SensorType::MonoVideo),
            4 => Ok(SensorType::StereoVision),
            5 => Ok(SensorType::NightVision),
            6 => Ok(SensorType::Ultrasonic),
            7 => Ok(SensorType::Pmd),
            8 => Ok(SensorType::InductionLoop),
            9 => Ok(SensorType::SphericalCamera),
            10 => Ok(SensorType::Uwb),
            11 => Ok(SensorType::Acoustic),
            12 => Ok(SensorType::LocalAggregation),
            13 => Ok(SensorType::ItsAggregation),
            _ => Err(format!("Invalid sensor type value: {value}")),
        }
    }
}

impl From<u8> for SensorType {
    fn from(value: u8) -> Self {
        Self::try_from(value).unwrap_or(SensorType::Undefined)
    }
}

impl From<SensorType> for u8 {
    fn from(sensor_type: SensorType) -> Self {
        sensor_type as u8
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::collective_perception_message::{
        CollectivePerceptionMessage, MapReference, SensorType,
    };

    fn minimal_cpm() -> &'static str {
        r#"{
            "protocol_version": 255,
            "station_id": 4294967295,
            "management_container": {
                "reference_time": 4398046511103,
                "reference_position": {
                    "latitude": 900000001,
                    "longitude": -1800000001,
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
                    },                    
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 4095,
                        "semi_major_orientation": 3601
                    }
                }
            }
        }"#
    }

    fn standard_cpm() -> &'static str {
        r#"{
            "protocol_version": 255,
            "station_id": 4294967295,
            "management_container": {
                "reference_time": 4398046511103,
                "reference_position": {
                    "latitude": 900000001,
                    "longitude": -1800000001,
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
                    },
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 4095,
                        "semi_major_orientation": 3601
                    }                    
                }
            },
            "sensor_information_container": [
                {
                    "sensor_id": 255,
                    "sensor_type": 13,
                    "shadowing_applies": true
                }
            ],
            "perception_region_container": [
                {
                    "measurement_delta_time": 2047,
                    "perception_region_confidence": 101,
                    "perception_region_shape": {
                        "polygonal": {
                            "polygon": [
                                {
                                    "x_coordinate": -32768,
                                    "y_coordinate": -32768,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": 32767,
                                    "y_coordinate": -32768,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": 32767,
                                    "y_coordinate": 32767,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": -32768,
                                    "y_coordinate": 32767,
                                    "z_coordinate": -32768
                                }
                            ]
                        }
                    },
                    "shadowing_applies": true,
                    "sensor_id_list": [ 1 ]
                }
            ]
        }"#
    }

    fn full_rsu_cpm() -> &'static str {
        r#"{
            "protocol_version": 255,
            "station_id": 4294967295,
            "management_container": {
                "reference_time": 4398046511103,
                "reference_position": {
                    "latitude": 900000001,
                    "longitude": -1800000001,
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
                    },
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 4095,
                        "semi_major_orientation": 3601
                    }                    
                },
                "segmentation_info": {
                    "total_msg_no": 8,
                    "this_msg_no": 8
                },
                "message_rate_range": {
                    "message_rate_min": {
                        "mantissa": 100,
                        "exponent": 2
                    },
                    "message_rate_max": {
                        "mantissa": 1,
                        "exponent": -5
                    }
                }
            },
            "originating_rsu_container": [
                {
                    "road_segment": {
                        "id": 65535,
                        "region": 65535
                    }
                },
                {
                    "intersection": {
                        "id": 65535,
                        "region": 65535
                    }
                }
            ],
            "sensor_information_container": [
                {
                    "sensor_id": 255,
                    "sensor_type": 13,
                    "shadowing_applies": true,                    
                    "perception_region_shape": {
                         "polygonal": {
                            "polygon": [
                                {
                                    "x_coordinate": -32768,
                                    "y_coordinate": -32768,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": 32767,
                                    "y_coordinate": -32768,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": 32767,
                                    "y_coordinate": 32767,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": -32768,
                                    "y_coordinate": 32767,
                                    "z_coordinate": -32768
                                }
                            ],
                            "shape_reference_point": {
                                "x_coordinate": 32767,
                                "y_coordinate": -32768,
                                "z_coordinate": 0
                            },
                            "height": 2045
                        }
                    },
                    "perception_region_confidence": 101
                },
                {
                    "sensor_id": 2,
                    "sensor_type": 1,
                    "shadowing_applies": true,                    
                    "perception_region_shape": {
                        "circular": {
                            "radius": 4095,
                            "shape_reference_point": {
                                "x_coordinate": 32767,
                                "y_coordinate": -32768,
                                "z_coordinate": 0
                            },
                            "height": 0
                        }
                    },
                    "perception_region_confidence": 101
                },
                {
                    "sensor_id": 3,
                    "sensor_type": 2,
                    "shadowing_applies": true,
                    "perception_region_shape": {
                        "rectangular": {
                            "semi_length": 102,
                            "semi_breadth": 0,
                            "center_point": {
                                "x_coordinate": 32767,
                                "y_coordinate": -32768,
                                "z_coordinate": 0
                            },
                            "orientation": 3601,
                            "height": 4095
                        }
                    },
                    "perception_region_confidence": 101
                },
                {
                    "sensor_id": 4,
                    "sensor_type": 3,
                    "shadowing_applies": true,
                    "perception_region_shape": {
                        "elliptical": {
                            "semi_major_axis_length": 4095,
                            "semi_minor_axis_length": 0,
                            "shape_reference_point": {
                                "x_coordinate": 32767,
                                "y_coordinate": -32768,
                                "z_coordinate": 0
                            },
                            "orientation": 1800,
                            "height": 2047
                        }
                    },
                    "perception_region_confidence": 101
                },
                {
                    "sensor_id": 5,
                    "sensor_type": 4,
                    "shadowing_applies": true,
                    "perception_region_shape": {
                        "radial": {
                            "range": 4095,
                            "stationary_horizontal_opening_angle_start": 900,
                            "stationary_horizontal_opening_angle_end": 2700,
                            "shape_reference_point": {
                                "x_coordinate": 32767,
                                "y_coordinate": -32768,
                                "z_coordinate": 0
                            },
                            "vertical_opening_angle_start": 0,
                            "vertical_opening_angle_end": 3601
                        }
                    },
                    "perception_region_confidence": 101
                },
                {
                    "sensor_id": 6,
                    "sensor_type": 5,
                    "shadowing_applies": true,
                    "perception_region_shape": {
                        "radial_shapes": {
                            "ref_point_id": 255,
                            "x_coordinate": 1001,
                            "y_coordinate": -3094,
                            "radial_shapes_list": [
                                {
                                    "range": 4095,
                                    "stationary_horizontal_opening_angle_start": 900,
                                    "stationary_horizontal_opening_angle_end": 2700,
                                    "shape_reference_point": {
                                        "x_coordinate": 32767,
                                        "y_coordinate": -32768,
                                        "z_coordinate": 0
                                    },
                                    "vertical_opening_angle_start": 0,
                                    "vertical_opening_angle_end": 3601
                                },
                                {
                                    "range": 2047,
                                    "stationary_horizontal_opening_angle_start": 0,
                                    "stationary_horizontal_opening_angle_end": 3601,
                                    "shape_reference_point": {
                                        "x_coordinate": 32767,
                                        "y_coordinate": -32768,
                                        "z_coordinate": 0
                                    },
                                    "vertical_opening_angle_start": 0,
                                    "vertical_opening_angle_end": 3601
                                }
                            ],
                            "z_coordinate": 1047
                        }
                    },
                    "perception_region_confidence": 101
                }
            ],
            "perception_region_container": [
                {
                    "measurement_delta_time": 2047,
                    "perception_region_confidence": 101,
                    "perception_region_shape": {
                        "polygonal": {
                            "polygon": [
                                {
                                    "x_coordinate": -32768,
                                    "y_coordinate": -32768,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": 32767,
                                    "y_coordinate": -32768,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": 32767,
                                    "y_coordinate": 32767,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": -32768,
                                    "y_coordinate": 32767,
                                    "z_coordinate": -32768
                                }
                            ]
                        }
                    },
                    "shadowing_applies": true,
                    "sensor_id_list": [ 1 ]
                },
                {
                    "measurement_delta_time": -2048,
                    "perception_region_confidence": 1,
                    "perception_region_shape": {
                        "elliptical": {
                            "semi_major_axis_length": 4095,
                            "semi_minor_axis_length": 0,
                            "shape_reference_point": {
                                "x_coordinate": 32767,
                                "y_coordinate": -32768,
                                "z_coordinate": 0
                            },
                            "orientation": 3601,
                            "height": 4095
                        }
                    },
                    "shadowing_applies": false,
                    "sensor_id_list": [ 2, 3, 4, 5, 6 ]
                }
            ]
        }"#
    }

    fn full_vehicle_cpm() -> &'static str {
        r#"{
            "protocol_version": 255,
            "station_id": 4294967295,
            "management_container": {
                "reference_time": 4398046511103,
                "reference_position": {
                    "latitude": 900000001,
                    "longitude": -1800000001,
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
                    },
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 4095,
                        "semi_major_orientation": 3601
                    }                    
                },
                "segmentation_info": {
                    "total_msg_no": 8,
                    "this_msg_no": 8
                },
                "message_rate_range": {
                    "message_rate_min": {
                        "mantissa": 100,
                        "exponent": 2
                    },
                    "message_rate_max": {
                        "mantissa": 1,
                        "exponent": -5
                    }
                }
            },
            "originating_vehicle_container": {
                "orientation_angle": {
                    "value": 3601,
                    "confidence": 127
                },
                "pitch_angle": {
                    "value": 0,
                    "confidence": 1
                },
                "roll_angle": {
                    "value": 1800,
                    "confidence": 64
                },
                "trailer_data_set": [
                    {
                        "ref_point_id": 255,
                        "hitch_point_offset": 255,
                        "hitch_angle": {
                            "value": 3601,
                            "confidence": 127
                        },
                        "front_overhang": 255,
                        "rear_overhang": 255,
                        "trailer_width": 62
                    },
                    {
                        "ref_point_id": 0,
                        "hitch_point_offset": 0,
                        "hitch_angle": {
                            "value": 0,
                            "confidence": 1
                        },
                        "front_overhang": 0,
                        "rear_overhang": 0,
                        "trailer_width": 1
                    }
                ]
            },
            "sensor_information_container": [
                {
                    "sensor_id": 255,
                    "sensor_type": 13,
                    "shadowing_applies": true,                    
                    "perception_region_shape": {
                         "polygonal": {
                            "polygon": [
                                {
                                    "x_coordinate": -32768,
                                    "y_coordinate": -32768,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": 32767,
                                    "y_coordinate": -32768,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": 32767,
                                    "y_coordinate": 32767,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": -32768,
                                    "y_coordinate": 32767,
                                    "z_coordinate": -32768
                                }
                            ],
                            "shape_reference_point": {
                                "x_coordinate": 32767,
                                "y_coordinate": -32768,
                                "z_coordinate": 0
                            },
                            "height": 2045
                        }
                    },
                    "perception_region_confidence": 101
                },
                {
                    "sensor_id": 2,
                    "sensor_type": 1,
                    "shadowing_applies": true,
                    "perception_region_shape": {
                        "circular": {
                            "radius": 4095,
                            "shape_reference_point": {
                                "x_coordinate": 32767,
                                "y_coordinate": -32768,
                                "z_coordinate": 0
                            },
                            "height": 0
                        }
                    },
                    "perception_region_confidence": 101
                },
                {
                    "sensor_id": 3,
                    "sensor_type": 2,
                    "shadowing_applies": true,
                    "perception_region_shape": {
                        "rectangular": {
                            "semi_length": 102,
                            "semi_breadth": 0,
                            "center_point": {
                                "x_coordinate": 32767,
                                "y_coordinate": -32768,
                                "z_coordinate": 0
                            },
                            "orientation": 3601,
                            "height": 4095
                        }
                    },
                    "perception_region_confidence": 101
                },
                {
                    "sensor_id": 4,
                    "sensor_type": 3,
                    "shadowing_applies": true,
                    "perception_region_shape": {
                        "elliptical": {
                            "semi_major_axis_length": 4095,
                            "semi_minor_axis_length": 0,
                            "shape_reference_point": {
                                "x_coordinate": 32767,
                                "y_coordinate": -32768,
                                "z_coordinate": 0
                            },
                            "orientation": 1800,
                            "height": 2047
                        }
                    },
                    "perception_region_confidence": 101
                },
                {
                    "sensor_id": 5,
                    "sensor_type": 4,
                    "shadowing_applies": true,
                    "perception_region_shape": {
                        "radial": {
                            "range": 4095,
                            "stationary_horizontal_opening_angle_start": 900,
                            "stationary_horizontal_opening_angle_end": 2700,
                            "shape_reference_point": {
                                "x_coordinate": 32767,
                                "y_coordinate": -32768,
                                "z_coordinate": 0
                            },
                            "vertical_opening_angle_start": 0,
                            "vertical_opening_angle_end": 3601
                        }
                    },
                    "perception_region_confidence": 101
                },
                {
                    "sensor_id": 6,
                    "sensor_type": 5,
                    "shadowing_applies": true,
                    "perception_region_shape": {
                        "radial_shapes": {
                            "ref_point_id": 255,
                            "x_coordinate": 1001,
                            "y_coordinate": -3094,
                            "radial_shapes_list": [
                                {
                                    "range": 4095,
                                    "stationary_horizontal_opening_angle_start": 900,
                                    "stationary_horizontal_opening_angle_end": 2700,
                                    "shape_reference_point": {
                                        "x_coordinate": 32767,
                                        "y_coordinate": -32768,
                                        "z_coordinate": 0
                                    },
                                    "vertical_opening_angle_start": 0,
                                    "vertical_opening_angle_end": 3601
                                },
                                {
                                    "range": 2047,
                                    "stationary_horizontal_opening_angle_start": 0,
                                    "stationary_horizontal_opening_angle_end": 3601,
                                    "shape_reference_point": {
                                        "x_coordinate": 32767,
                                        "y_coordinate": -32768,
                                        "z_coordinate": 0
                                    },
                                    "vertical_opening_angle_start": 0,
                                    "vertical_opening_angle_end": 3601
                                }
                            ],
                            "z_coordinate": 1047
                        }
                    },
                    "perception_region_confidence": 101
                }
            ],
            "perception_region_container": [
                {
                    "measurement_delta_time": 2047,
                    "perception_region_confidence": 101,
                    "perception_region_shape": {
                        "polygonal": {
                            "polygon": [
                                {
                                    "x_coordinate": -32768,
                                    "y_coordinate": -32768,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": 32767,
                                    "y_coordinate": -32768,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": 32767,
                                    "y_coordinate": 32767,
                                    "z_coordinate": -32768
                                },
                                {
                                    "x_coordinate": -32768,
                                    "y_coordinate": 32767,
                                    "z_coordinate": -32768
                                }
                            ]
                        }
                    },
                    "shadowing_applies": true,
                    "sensor_id_list": [ 1 ]
                },
                {
                    "measurement_delta_time": -2048,
                    "perception_region_confidence": 1,
                    "perception_region_shape": {
                        "elliptical": {
                            "semi_major_axis_length": 4095,
                            "semi_minor_axis_length": 0,
                            "shape_reference_point": {
                                "x_coordinate": 32767,
                                "y_coordinate": -32768,
                                "z_coordinate": 0
                            },
                            "orientation": 3601,
                            "height": 4095
                        }
                    },
                    "shadowing_applies": false,
                    "sensor_id_list": [ 2, 3, 4, 5, 6 ]
                }
            ]
        }"#
    }

    #[test]
    fn test_deserialize_minimal_cpm() {
        let data = minimal_cpm();

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();

        // minimal
        assert_eq!(cpm.protocol_version, 255);
        assert_eq!(cpm.station_id, 4294967295);
        assert_eq!(cpm.management_container.reference_time, 4398046511103);
        assert_eq!(
            cpm.management_container.reference_position.latitude,
            900000001
        );
        assert_eq!(
            cpm.management_container.reference_position.longitude,
            -1800000001
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .position_confidence_ellipse
                .semi_major,
            4095
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .position_confidence_ellipse
                .semi_minor,
            4095
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .position_confidence_ellipse
                .semi_major_orientation,
            3601
        );
        assert_eq!(
            cpm.management_container.reference_position.altitude.value,
            800001
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .altitude
                .confidence,
            15
        );

        // the other containers should be empty
        assert!(cpm.originating_vehicle_container.is_none());
        assert!(cpm.originating_rsu_container.is_empty());
        assert!(cpm.sensor_information_container.is_empty());
        assert!(cpm.perception_region_container.is_empty());
        assert!(cpm.perceived_object_container.is_empty());
    }

    #[test]
    fn test_deserialize_standard_cpm() {
        let data = standard_cpm();

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();

        // minimal
        assert_eq!(cpm.protocol_version, 255);
        assert_eq!(cpm.station_id, 4294967295);
        assert_eq!(cpm.management_container.reference_time, 4398046511103);
        assert_eq!(
            cpm.management_container.reference_position.latitude,
            900000001
        );
        assert_eq!(
            cpm.management_container.reference_position.longitude,
            -1800000001
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .position_confidence_ellipse
                .semi_major,
            4095
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .position_confidence_ellipse
                .semi_minor,
            4095
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .position_confidence_ellipse
                .semi_major_orientation,
            3601
        );
        assert_eq!(
            cpm.management_container.reference_position.altitude.value,
            800001
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .altitude
                .confidence,
            15
        );

        //standard
        assert_eq!(cpm.sensor_information_container[0].sensor_id, 255);
        assert_eq!(
            cpm.sensor_information_container[0].sensor_type,
            SensorType::ItsAggregation
        );
        assert!(cpm.sensor_information_container[0].shadowing_applies);

        assert_eq!(
            cpm.perception_region_container[0].measurement_delta_time,
            2047
        );
        assert_eq!(
            cpm.perception_region_container[0].perception_region_confidence,
            101
        );
        let polygon = &cpm.perception_region_container[0]
            .perception_region_shape
            .polygonal
            .as_ref()
            .unwrap()
            .polygon;
        assert_eq!(polygon.len(), 4);
        assert_eq!(polygon[0].x_coordinate, -32768);
        assert_eq!(polygon[0].y_coordinate, -32768);
        assert_eq!(polygon[0].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[1].x_coordinate, 32767);
        assert_eq!(polygon[1].y_coordinate, -32768);
        assert_eq!(polygon[1].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[2].x_coordinate, 32767);
        assert_eq!(polygon[2].y_coordinate, 32767);
        assert_eq!(polygon[2].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[3].x_coordinate, -32768);
        assert_eq!(polygon[3].y_coordinate, 32767);
        assert_eq!(polygon[3].z_coordinate.unwrap(), -32768);
        assert!(cpm.perception_region_container[0].shadowing_applies);
        assert_eq!(cpm.perception_region_container[0].sensor_id_list, vec![1]);

        // the other containers should be empty
        assert!(cpm.originating_vehicle_container.is_none());
        assert!(cpm.originating_rsu_container.is_empty());
        assert!(cpm.perceived_object_container.is_empty());
    }

    #[test]
    fn test_deserialize_full_vehicle_cpm() {
        let data = full_vehicle_cpm();

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();

        // minimal
        assert_eq!(cpm.protocol_version, 255);
        assert_eq!(cpm.station_id, 4294967295);
        assert_eq!(cpm.management_container.reference_time, 4398046511103);
        assert_eq!(
            cpm.management_container.reference_position.latitude,
            900000001
        );
        assert_eq!(
            cpm.management_container.reference_position.longitude,
            -1800000001
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .position_confidence_ellipse
                .semi_major,
            4095
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .position_confidence_ellipse
                .semi_minor,
            4095
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .position_confidence_ellipse
                .semi_major_orientation,
            3601
        );
        assert_eq!(
            cpm.management_container.reference_position.altitude.value,
            800001
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .altitude
                .confidence,
            15
        );

        // standard
        assert_eq!(cpm.sensor_information_container[0].sensor_id, 255);
        assert_eq!(
            cpm.sensor_information_container[0].sensor_type,
            SensorType::ItsAggregation
        );
        assert!(cpm.sensor_information_container[0].shadowing_applies);

        assert_eq!(
            cpm.perception_region_container[0].measurement_delta_time,
            2047
        );
        assert_eq!(
            cpm.perception_region_container[0].perception_region_confidence,
            101
        );
        let polygon = &cpm.perception_region_container[0]
            .perception_region_shape
            .polygonal
            .as_ref()
            .unwrap()
            .polygon;
        assert_eq!(polygon.len(), 4);
        assert_eq!(polygon[0].x_coordinate, -32768);
        assert_eq!(polygon[0].y_coordinate, -32768);
        assert_eq!(polygon[0].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[1].x_coordinate, 32767);
        assert_eq!(polygon[1].y_coordinate, -32768);
        assert_eq!(polygon[1].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[2].x_coordinate, 32767);
        assert_eq!(polygon[2].y_coordinate, 32767);
        assert_eq!(polygon[2].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[3].x_coordinate, -32768);
        assert_eq!(polygon[3].y_coordinate, 32767);
        assert_eq!(polygon[3].z_coordinate.unwrap(), -32768);
        assert!(cpm.perception_region_container[0].shadowing_applies);
        assert_eq!(cpm.perception_region_container[0].sensor_id_list, vec![1]);

        // full vehicle
        assert!(cpm.management_container.segmentation_info.is_some());
        let segmentation_info = cpm.management_container.segmentation_info.unwrap();
        assert_eq!(segmentation_info.total_msg_no, 8);
        assert_eq!(segmentation_info.this_msg_no, 8);
        assert!(cpm.management_container.message_rate_range.is_some());
        let message_rate_range = cpm.management_container.message_rate_range.unwrap();
        assert_eq!(message_rate_range.message_rate_min.mantissa, 100);
        assert_eq!(message_rate_range.message_rate_min.exponent, 2);
        assert_eq!(message_rate_range.message_rate_max.mantissa, 1);
        assert_eq!(message_rate_range.message_rate_max.exponent, -5);

        assert!(cpm.originating_vehicle_container.is_some());
        let originating_vehicle = cpm.originating_vehicle_container.as_ref().unwrap();
        assert_eq!(originating_vehicle.orientation_angle.value, 3601);
        assert_eq!(originating_vehicle.orientation_angle.confidence, 127);
        assert!(originating_vehicle.pitch_angle.is_some());
        let pitch_angle = originating_vehicle.pitch_angle.as_ref().unwrap();
        assert_eq!(pitch_angle.value, 0);
        assert_eq!(pitch_angle.confidence, 1);
        assert!(originating_vehicle.roll_angle.is_some());
        let roll_angle = originating_vehicle.roll_angle.as_ref().unwrap();
        assert_eq!(roll_angle.value, 1800);
        assert_eq!(roll_angle.confidence, 64);
        assert_eq!(originating_vehicle.trailer_data_set.len(), 2);
        assert_eq!(originating_vehicle.trailer_data_set[0].ref_point_id, 255);
        assert_eq!(
            originating_vehicle.trailer_data_set[0].hitch_point_offset,
            255
        );
        assert_eq!(
            originating_vehicle.trailer_data_set[0].hitch_angle.value,
            3601
        );
        assert_eq!(
            originating_vehicle.trailer_data_set[0]
                .hitch_angle
                .confidence,
            127
        );
        assert_eq!(
            originating_vehicle.trailer_data_set[0]
                .front_overhang
                .unwrap(),
            255
        );
        assert_eq!(
            originating_vehicle.trailer_data_set[0]
                .rear_overhang
                .unwrap(),
            255
        );
        assert_eq!(
            originating_vehicle.trailer_data_set[0]
                .trailer_width
                .unwrap(),
            62
        );
        assert_eq!(originating_vehicle.trailer_data_set[1].ref_point_id, 0);
        assert_eq!(
            originating_vehicle.trailer_data_set[1].hitch_point_offset,
            0
        );
        assert_eq!(originating_vehicle.trailer_data_set[1].hitch_angle.value, 0);
        assert_eq!(
            originating_vehicle.trailer_data_set[1]
                .hitch_angle
                .confidence,
            1
        );
        assert_eq!(
            originating_vehicle.trailer_data_set[1]
                .front_overhang
                .unwrap(),
            0
        );
        assert_eq!(
            originating_vehicle.trailer_data_set[1]
                .rear_overhang
                .unwrap(),
            0
        );
        assert_eq!(
            originating_vehicle.trailer_data_set[1]
                .trailer_width
                .unwrap(),
            1
        );

        assert_eq!(cpm.sensor_information_container.len(), 6);
        let perception_region_shape = cpm.sensor_information_container[0]
            .perception_region_shape
            .as_ref()
            .unwrap();
        assert!(perception_region_shape.polygonal.is_some());
        let polygonal = perception_region_shape.polygonal.as_ref().unwrap();
        let polygon = &polygonal.polygon;
        assert_eq!(polygon.len(), 4);
        assert_eq!(polygon[0].x_coordinate, -32768);
        assert_eq!(polygon[0].y_coordinate, -32768);
        assert_eq!(polygon[0].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[1].x_coordinate, 32767);
        assert_eq!(polygon[1].y_coordinate, -32768);
        assert_eq!(polygon[1].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[2].x_coordinate, 32767);
        assert_eq!(polygon[2].y_coordinate, 32767);
        assert_eq!(polygon[2].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[3].x_coordinate, -32768);
        assert_eq!(polygon[3].y_coordinate, 32767);
        assert_eq!(polygon[3].z_coordinate.unwrap(), -32768);
        assert!(polygonal.shape_reference_point.is_some());
        let shape_reference_point = polygonal.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(polygonal.height.unwrap(), 2045);
        assert_eq!(
            cpm.sensor_information_container[0]
                .perception_region_confidence
                .unwrap(),
            101
        );

        assert_eq!(cpm.sensor_information_container[1].sensor_id, 2);
        assert_eq!(
            cpm.sensor_information_container[1].sensor_type,
            SensorType::Radar
        );
        assert!(cpm.sensor_information_container[1].shadowing_applies);
        let perception_region_shape = cpm.sensor_information_container[1]
            .perception_region_shape
            .as_ref()
            .unwrap();
        assert!(perception_region_shape.circular.is_some());
        let circular = perception_region_shape.circular.as_ref().unwrap();
        assert_eq!(circular.radius, 4095);
        assert!(circular.shape_reference_point.is_some());
        let shape_reference_point = circular.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(circular.height.unwrap(), 0);
        assert_eq!(
            cpm.sensor_information_container[1]
                .perception_region_confidence
                .unwrap(),
            101
        );

        assert_eq!(cpm.sensor_information_container[2].sensor_id, 3);
        assert_eq!(
            cpm.sensor_information_container[2].sensor_type,
            SensorType::Lidar
        );
        assert!(cpm.sensor_information_container[2].shadowing_applies);
        let perception_region_shape = cpm.sensor_information_container[2]
            .perception_region_shape
            .as_ref()
            .unwrap();
        assert!(perception_region_shape.rectangular.is_some());
        let rectangular = perception_region_shape.rectangular.as_ref().unwrap();
        assert!(rectangular.center_point.is_some());
        let center_point = rectangular.center_point.as_ref().unwrap();
        assert_eq!(center_point.x_coordinate, 32767);
        assert_eq!(center_point.y_coordinate, -32768);
        assert_eq!(center_point.z_coordinate.unwrap(), 0);
        assert_eq!(rectangular.semi_length, 102);
        assert_eq!(rectangular.semi_breadth, 0);
        assert_eq!(rectangular.orientation.unwrap(), 3601);
        assert_eq!(rectangular.height.unwrap(), 4095);

        assert_eq!(cpm.sensor_information_container[3].sensor_id, 4);
        assert_eq!(
            cpm.sensor_information_container[3].sensor_type,
            SensorType::MonoVideo
        );
        assert!(cpm.sensor_information_container[3].shadowing_applies);
        let perception_region_shape = cpm.sensor_information_container[3]
            .perception_region_shape
            .as_ref()
            .unwrap();
        assert!(perception_region_shape.elliptical.is_some());
        let elliptical = perception_region_shape.elliptical.as_ref().unwrap();
        assert_eq!(elliptical.semi_major_axis_length, 4095);
        assert_eq!(elliptical.semi_minor_axis_length, 0);
        assert!(elliptical.shape_reference_point.is_some());
        let shape_reference_point = elliptical.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(elliptical.orientation.unwrap(), 1800);
        assert_eq!(elliptical.height.unwrap(), 2047);
        assert_eq!(
            cpm.sensor_information_container[3]
                .perception_region_confidence
                .unwrap(),
            101
        );

        assert_eq!(cpm.sensor_information_container[4].sensor_id, 5);
        assert_eq!(
            cpm.sensor_information_container[4].sensor_type,
            SensorType::StereoVision
        );
        assert!(cpm.sensor_information_container[4].shadowing_applies);
        assert!(
            cpm.sensor_information_container[4]
                .perception_region_shape
                .is_some()
        );
        let perception_region_shape = cpm.sensor_information_container[4]
            .perception_region_shape
            .as_ref()
            .unwrap();
        assert!(perception_region_shape.radial.is_some());
        let radial = perception_region_shape.radial.as_ref().unwrap();
        assert_eq!(radial.range, 4095);
        assert_eq!(radial.stationary_horizontal_opening_angle_start, 900);
        assert_eq!(radial.stationary_horizontal_opening_angle_end, 2700);
        assert!(radial.shape_reference_point.is_some());
        let shape_reference_point = radial.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(radial.vertical_opening_angle_start.unwrap(), 0);
        assert_eq!(radial.vertical_opening_angle_end.unwrap(), 3601);
        assert_eq!(
            cpm.sensor_information_container[4]
                .perception_region_confidence
                .unwrap(),
            101
        );

        assert_eq!(cpm.sensor_information_container[5].sensor_id, 6);
        assert_eq!(
            cpm.sensor_information_container[5].sensor_type,
            SensorType::NightVision
        );
        assert!(cpm.sensor_information_container[5].shadowing_applies);
        let perception_region_shape = cpm.sensor_information_container[5]
            .perception_region_shape
            .as_ref()
            .unwrap();
        assert!(perception_region_shape.radial_shapes.is_some());
        let radial_shapes = perception_region_shape.radial_shapes.as_ref().unwrap();
        assert_eq!(radial_shapes.ref_point_id, 255);
        assert_eq!(radial_shapes.x_coordinate, 1001);
        assert_eq!(radial_shapes.y_coordinate, -3094);
        assert_eq!(radial_shapes.z_coordinate.unwrap(), 1047);
        assert_eq!(radial_shapes.radial_shapes_list.len(), 2);
        let radial_shape = &radial_shapes.radial_shapes_list[0];
        assert_eq!(radial_shape.range, 4095);
        assert_eq!(radial_shape.stationary_horizontal_opening_angle_start, 900);
        assert_eq!(radial_shape.stationary_horizontal_opening_angle_end, 2700);
        assert!(radial_shape.shape_reference_point.is_some());
        let shape_reference_point = radial_shape.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(radial_shape.vertical_opening_angle_start.unwrap(), 0);
        assert_eq!(radial_shape.vertical_opening_angle_end.unwrap(), 3601);
        let radial_shape = &radial_shapes.radial_shapes_list[1];
        assert_eq!(radial_shape.range, 2047);
        assert_eq!(radial_shape.stationary_horizontal_opening_angle_start, 0);
        assert_eq!(radial_shape.stationary_horizontal_opening_angle_end, 3601);
        assert!(radial_shape.shape_reference_point.is_some());
        let shape_reference_point = radial_shape.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(radial_shape.vertical_opening_angle_start.unwrap(), 0);
        assert_eq!(radial_shape.vertical_opening_angle_end.unwrap(), 3601);
        assert_eq!(
            cpm.sensor_information_container[5]
                .perception_region_confidence
                .unwrap(),
            101
        );

        assert_eq!(
            cpm.perception_region_container[1].measurement_delta_time,
            -2048
        );
        assert_eq!(
            cpm.perception_region_container[1].perception_region_confidence,
            1
        );
        let perception_region_shape = &cpm.perception_region_container[1].perception_region_shape;
        assert!(perception_region_shape.elliptical.is_some());
        let elliptical = perception_region_shape.elliptical.as_ref().unwrap();
        assert_eq!(elliptical.semi_major_axis_length, 4095);
        assert_eq!(elliptical.semi_minor_axis_length, 0);
        assert!(elliptical.shape_reference_point.is_some());
        let shape_reference_point = elliptical.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(elliptical.orientation.unwrap(), 3601);
        assert_eq!(elliptical.height.unwrap(), 4095);
        assert!(!cpm.perception_region_container[1].shadowing_applies);
        assert_eq!(
            cpm.perception_region_container[1].sensor_id_list,
            vec![2, 3, 4, 5, 6]
        );
        assert!(
            cpm.perception_region_container[1]
                .perceived_object_ids
                .is_empty()
        );

        // the other containers should be empty
        assert!(cpm.originating_rsu_container.is_empty());
        assert!(cpm.perceived_object_container.is_empty());
    }

    #[test]
    fn test_deserialize_full_rsu_cpm() {
        let data = full_rsu_cpm();

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();

        // minimal
        assert_eq!(cpm.protocol_version, 255);
        assert_eq!(cpm.station_id, 4294967295);
        assert_eq!(cpm.management_container.reference_time, 4398046511103);
        assert_eq!(
            cpm.management_container.reference_position.latitude,
            900000001
        );
        assert_eq!(
            cpm.management_container.reference_position.longitude,
            -1800000001
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .position_confidence_ellipse
                .semi_major,
            4095
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .position_confidence_ellipse
                .semi_minor,
            4095
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .position_confidence_ellipse
                .semi_major_orientation,
            3601
        );
        assert_eq!(
            cpm.management_container.reference_position.altitude.value,
            800001
        );
        assert_eq!(
            cpm.management_container
                .reference_position
                .altitude
                .confidence,
            15
        );

        // standard
        assert_eq!(cpm.sensor_information_container[0].sensor_id, 255);
        assert_eq!(
            cpm.sensor_information_container[0].sensor_type,
            SensorType::ItsAggregation
        );
        assert!(cpm.sensor_information_container[0].shadowing_applies);

        assert_eq!(
            cpm.perception_region_container[0].measurement_delta_time,
            2047
        );
        assert_eq!(
            cpm.perception_region_container[0].perception_region_confidence,
            101
        );
        let polygon = &cpm.perception_region_container[0]
            .perception_region_shape
            .polygonal
            .as_ref()
            .unwrap()
            .polygon;
        assert_eq!(polygon.len(), 4);
        assert_eq!(polygon[0].x_coordinate, -32768);
        assert_eq!(polygon[0].y_coordinate, -32768);
        assert_eq!(polygon[0].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[1].x_coordinate, 32767);
        assert_eq!(polygon[1].y_coordinate, -32768);
        assert_eq!(polygon[1].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[2].x_coordinate, 32767);
        assert_eq!(polygon[2].y_coordinate, 32767);
        assert_eq!(polygon[2].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[3].x_coordinate, -32768);
        assert_eq!(polygon[3].y_coordinate, 32767);
        assert_eq!(polygon[3].z_coordinate.unwrap(), -32768);
        assert!(cpm.perception_region_container[0].shadowing_applies);
        assert_eq!(cpm.perception_region_container[0].sensor_id_list, vec![1]);

        // full rsu
        assert!(cpm.management_container.segmentation_info.is_some());
        let segmentation_info = cpm.management_container.segmentation_info.unwrap();
        assert_eq!(segmentation_info.total_msg_no, 8);
        assert_eq!(segmentation_info.this_msg_no, 8);
        assert!(cpm.management_container.message_rate_range.is_some());
        let message_rate_range = cpm.management_container.message_rate_range.unwrap();
        assert_eq!(message_rate_range.message_rate_min.mantissa, 100);
        assert_eq!(message_rate_range.message_rate_min.exponent, 2);
        assert_eq!(message_rate_range.message_rate_max.mantissa, 1);
        assert_eq!(message_rate_range.message_rate_max.exponent, -5);

        assert_eq!(cpm.originating_rsu_container.len(), 2);
        assert!(cpm.originating_rsu_container[0].road_segment.is_some());
        let road_segment = cpm.originating_rsu_container[0]
            .road_segment
            .as_ref()
            .unwrap();
        assert_eq!(road_segment.id, 65535);
        assert_eq!(road_segment.region.unwrap(), 65535);
        assert!(cpm.originating_rsu_container[1].intersection.is_some());
        let intersection = cpm.originating_rsu_container[1]
            .intersection
            .as_ref()
            .unwrap();
        assert_eq!(intersection.id, 65535);
        assert_eq!(intersection.region.unwrap(), 65535);

        assert_eq!(cpm.sensor_information_container.len(), 6);
        let perception_region_shape = cpm.sensor_information_container[0]
            .perception_region_shape
            .as_ref()
            .unwrap();
        assert!(perception_region_shape.polygonal.is_some());
        let polygonal = perception_region_shape.polygonal.as_ref().unwrap();
        let polygon = &polygonal.polygon;
        assert_eq!(polygon.len(), 4);
        assert_eq!(polygon[0].x_coordinate, -32768);
        assert_eq!(polygon[0].y_coordinate, -32768);
        assert_eq!(polygon[0].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[1].x_coordinate, 32767);
        assert_eq!(polygon[1].y_coordinate, -32768);
        assert_eq!(polygon[1].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[2].x_coordinate, 32767);
        assert_eq!(polygon[2].y_coordinate, 32767);
        assert_eq!(polygon[2].z_coordinate.unwrap(), -32768);
        assert_eq!(polygon[3].x_coordinate, -32768);
        assert_eq!(polygon[3].y_coordinate, 32767);
        assert_eq!(polygon[3].z_coordinate.unwrap(), -32768);
        assert!(polygonal.shape_reference_point.is_some());
        let shape_reference_point = polygonal.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(polygonal.height.unwrap(), 2045);
        assert_eq!(
            cpm.sensor_information_container[0]
                .perception_region_confidence
                .unwrap(),
            101
        );

        assert_eq!(cpm.sensor_information_container[1].sensor_id, 2);
        assert_eq!(
            cpm.sensor_information_container[1].sensor_type,
            SensorType::Radar
        );
        assert!(cpm.sensor_information_container[1].shadowing_applies);
        let perception_region_shape = cpm.sensor_information_container[1]
            .perception_region_shape
            .as_ref()
            .unwrap();
        assert!(perception_region_shape.circular.is_some());
        let circular = perception_region_shape.circular.as_ref().unwrap();
        assert_eq!(circular.radius, 4095);
        assert!(circular.shape_reference_point.is_some());
        let shape_reference_point = circular.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(circular.height.unwrap(), 0);
        assert_eq!(
            cpm.sensor_information_container[1]
                .perception_region_confidence
                .unwrap(),
            101
        );

        assert_eq!(cpm.sensor_information_container[2].sensor_id, 3);
        assert_eq!(
            cpm.sensor_information_container[2].sensor_type,
            SensorType::Lidar
        );
        assert!(cpm.sensor_information_container[2].shadowing_applies);
        let perception_region_shape = cpm.sensor_information_container[2]
            .perception_region_shape
            .as_ref()
            .unwrap();
        assert!(perception_region_shape.rectangular.is_some());
        let rectangular = perception_region_shape.rectangular.as_ref().unwrap();
        assert!(rectangular.center_point.is_some());
        let center_point = rectangular.center_point.as_ref().unwrap();
        assert_eq!(center_point.x_coordinate, 32767);
        assert_eq!(center_point.y_coordinate, -32768);
        assert_eq!(center_point.z_coordinate.unwrap(), 0);
        assert_eq!(rectangular.semi_length, 102);
        assert_eq!(rectangular.semi_breadth, 0);
        assert_eq!(rectangular.orientation.unwrap(), 3601);
        assert_eq!(rectangular.height.unwrap(), 4095);

        assert_eq!(cpm.sensor_information_container[3].sensor_id, 4);
        assert_eq!(
            cpm.sensor_information_container[3].sensor_type,
            SensorType::MonoVideo
        );
        assert!(cpm.sensor_information_container[3].shadowing_applies);
        let perception_region_shape = cpm.sensor_information_container[3]
            .perception_region_shape
            .as_ref()
            .unwrap();
        assert!(perception_region_shape.elliptical.is_some());
        let elliptical = perception_region_shape.elliptical.as_ref().unwrap();
        assert_eq!(elliptical.semi_major_axis_length, 4095);
        assert_eq!(elliptical.semi_minor_axis_length, 0);
        assert!(elliptical.shape_reference_point.is_some());
        let shape_reference_point = elliptical.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(elliptical.orientation.unwrap(), 1800);
        assert_eq!(elliptical.height.unwrap(), 2047);
        assert_eq!(
            cpm.sensor_information_container[3]
                .perception_region_confidence
                .unwrap(),
            101
        );

        assert_eq!(cpm.sensor_information_container[4].sensor_id, 5);
        assert_eq!(
            cpm.sensor_information_container[4].sensor_type,
            SensorType::StereoVision
        );
        assert!(cpm.sensor_information_container[4].shadowing_applies);
        assert!(
            cpm.sensor_information_container[4]
                .perception_region_shape
                .is_some()
        );
        let perception_region_shape = cpm.sensor_information_container[4]
            .perception_region_shape
            .as_ref()
            .unwrap();
        assert!(perception_region_shape.radial.is_some());
        let radial = perception_region_shape.radial.as_ref().unwrap();
        assert_eq!(radial.range, 4095);
        assert_eq!(radial.stationary_horizontal_opening_angle_start, 900);
        assert_eq!(radial.stationary_horizontal_opening_angle_end, 2700);
        assert!(radial.shape_reference_point.is_some());
        let shape_reference_point = radial.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(radial.vertical_opening_angle_start.unwrap(), 0);
        assert_eq!(radial.vertical_opening_angle_end.unwrap(), 3601);
        assert_eq!(
            cpm.sensor_information_container[4]
                .perception_region_confidence
                .unwrap(),
            101
        );

        assert_eq!(cpm.sensor_information_container[5].sensor_id, 6);
        assert_eq!(
            cpm.sensor_information_container[5].sensor_type,
            SensorType::NightVision
        );
        assert!(cpm.sensor_information_container[5].shadowing_applies);
        let perception_region_shape = cpm.sensor_information_container[5]
            .perception_region_shape
            .as_ref()
            .unwrap();
        assert!(perception_region_shape.radial_shapes.is_some());
        let radial_shapes = perception_region_shape.radial_shapes.as_ref().unwrap();
        assert_eq!(radial_shapes.ref_point_id, 255);
        assert_eq!(radial_shapes.x_coordinate, 1001);
        assert_eq!(radial_shapes.y_coordinate, -3094);
        assert_eq!(radial_shapes.z_coordinate.unwrap(), 1047);
        assert_eq!(radial_shapes.radial_shapes_list.len(), 2);
        let radial_shape = &radial_shapes.radial_shapes_list[0];
        assert_eq!(radial_shape.range, 4095);
        assert_eq!(radial_shape.stationary_horizontal_opening_angle_start, 900);
        assert_eq!(radial_shape.stationary_horizontal_opening_angle_end, 2700);
        assert!(radial_shape.shape_reference_point.is_some());
        let shape_reference_point = radial_shape.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(radial_shape.vertical_opening_angle_start.unwrap(), 0);
        assert_eq!(radial_shape.vertical_opening_angle_end.unwrap(), 3601);
        let radial_shape = &radial_shapes.radial_shapes_list[1];
        assert_eq!(radial_shape.range, 2047);
        assert_eq!(radial_shape.stationary_horizontal_opening_angle_start, 0);
        assert_eq!(radial_shape.stationary_horizontal_opening_angle_end, 3601);
        assert!(radial_shape.shape_reference_point.is_some());
        let shape_reference_point = radial_shape.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(radial_shape.vertical_opening_angle_start.unwrap(), 0);
        assert_eq!(radial_shape.vertical_opening_angle_end.unwrap(), 3601);
        assert_eq!(
            cpm.sensor_information_container[5]
                .perception_region_confidence
                .unwrap(),
            101
        );

        assert_eq!(
            cpm.perception_region_container[1].measurement_delta_time,
            -2048
        );
        assert_eq!(
            cpm.perception_region_container[1].perception_region_confidence,
            1
        );
        let perception_region_shape = &cpm.perception_region_container[1].perception_region_shape;
        assert!(perception_region_shape.elliptical.is_some());
        let elliptical = perception_region_shape.elliptical.as_ref().unwrap();
        assert_eq!(elliptical.semi_major_axis_length, 4095);
        assert_eq!(elliptical.semi_minor_axis_length, 0);
        assert!(elliptical.shape_reference_point.is_some());
        let shape_reference_point = elliptical.shape_reference_point.as_ref().unwrap();
        assert_eq!(shape_reference_point.x_coordinate, 32767);
        assert_eq!(shape_reference_point.y_coordinate, -32768);
        assert_eq!(shape_reference_point.z_coordinate.unwrap(), 0);
        assert_eq!(elliptical.orientation.unwrap(), 3601);
        assert_eq!(elliptical.height.unwrap(), 4095);
        assert!(!cpm.perception_region_container[1].shadowing_applies);
        assert_eq!(
            cpm.perception_region_container[1].sensor_id_list,
            vec![2, 3, 4, 5, 6]
        );
        assert!(
            cpm.perception_region_container[1]
                .perceived_object_ids
                .is_empty()
        );

        // the other containers should be empty
        assert!(cpm.originating_vehicle_container.is_none());
        assert!(cpm.perceived_object_container.is_empty());
    }

    #[test]
    fn test_reserialize_minimal_cpm() {
        let data = minimal_cpm();

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();
        let serialized = serde_json::to_string(&cpm).unwrap();
        assert_eq!(
            serialized,
            data.replace("\n", "").replace(" ", "").replace("\t", "")
        );
    }

    #[test]
    fn test_reserialize_standard_cpm() {
        let data = standard_cpm();

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();
        let serialized = serde_json::to_string(&cpm).unwrap();
        assert_eq!(
            serialized,
            data.replace("\n", "").replace(" ", "").replace("\t", "")
        );
    }

    #[test]
    fn test_reserialize_full_vehicle_cpm() {
        let data = full_vehicle_cpm();

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();
        let serialized = serde_json::to_string(&cpm).unwrap();
        assert_eq!(
            serialized,
            data.replace("\n", "").replace(" ", "").replace("\t", "")
        );
    }

    #[test]
    fn test_reserialize_full_rsu_cpm() {
        let data = full_rsu_cpm();

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();
        let serialized = serde_json::to_string(&cpm).unwrap();
        assert_eq!(
            serialized,
            data.replace("\n", "").replace(" ", "").replace("\t", "")
        );
    }

    #[test]
    fn test_deserialize_road_segment_map_reference() {
        let data = r#"{
            "road_segment": {
                "id": 65535,
                "region": 65535
            }
        }"#;

        match serde_json::from_str::<MapReference>(data) {
            Ok(object) => {
                let road_segment = object.road_segment.unwrap();
                assert_eq!(road_segment.id, 65535);
                assert_eq!(road_segment.region, Some(65535));
            }
            Err(e) => panic!("Failed to deserialize a road segment MapReference: '{e}'"),
        }
    }

    #[test]
    fn test_deserialize_intersection_map_reference() {
        let data = r#"{
            "intersection": {
                "id": 65535,
                "region": 65535
            }
        }"#;

        match serde_json::from_str::<MapReference>(data) {
            Ok(object) => {
                let intersection = object.intersection.unwrap();
                assert_eq!(intersection.id, 65535);
                assert_eq!(intersection.region, Some(65535));
            }
            Err(e) => panic!("Failed to deserialize an intersection MapReference: '{e}'"),
        }
    }

    #[test]
    fn test_cpm_defaults() {
        let cpm = CollectivePerceptionMessage::default();
        assert_eq!(cpm.protocol_version, 0);
        assert_eq!(cpm.station_id, 0);
        assert!(cpm.originating_vehicle_container.is_none());
        assert!(cpm.originating_rsu_container.is_empty());
        assert!(cpm.sensor_information_container.is_empty());
        assert!(cpm.perception_region_container.is_empty());
        assert!(cpm.perceived_object_container.is_empty());
    }

    #[test]
    fn test_deserialize_collective_perception_message_with_empty_containers() {
        let data = r#"{
                "protocol_version": 3,
                "station_id": 54321,
                "management_container": {
                    "reference_time": 4398046511103,
                    "reference_position": {
                        "latitude": 111111111,
                        "longitude": 222222222,
                        "altitude": {
                            "value": 800001,
                            "confidence": 15
                        },
                        "position_confidence_ellipse": {
                            "semi_major": 4095,
                            "semi_minor": 4095,
                            "semi_major_orientation": 3601
                        }                        
                    }
                },
                "sensor_information_container": [],
                "perception_region_container": [],
                "perceived_object_container": []
            }"#;

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();
        assert_eq!(cpm.protocol_version, 3);
        assert_eq!(cpm.station_id, 54321);
        assert_eq!(
            cpm.management_container.reference_position.latitude,
            111111111
        );
        assert_eq!(
            cpm.management_container.reference_position.longitude,
            222222222
        );
        assert!(cpm.originating_vehicle_container.is_none());
        assert!(cpm.originating_rsu_container.is_empty());
        assert!(cpm.sensor_information_container.is_empty());
        assert!(cpm.perception_region_container.is_empty());
        assert!(cpm.perceived_object_container.is_empty());
    }
}
