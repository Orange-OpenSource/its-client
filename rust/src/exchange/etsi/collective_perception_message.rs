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
use crate::exchange::etsi::mobile_perceived_object::MobilePerceivedObject;
use crate::exchange::etsi::perceived_object::PerceivedObject;
use crate::exchange::etsi::reference_position::ReferencePosition;
use crate::exchange::etsi::shape::Shape;
use crate::exchange::etsi::timestamp_to_etsi;
use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::message::content_error::ContentError::{NotAMortal, NotOriginatingVehicle};
use crate::exchange::mortal::Mortal;
use crate::mobility::mobile::Mobile;
use crate::mobility::position::Position;
use serde::{Deserialize, Serialize};
use std::any::type_name;

/// Represents a Collective Perception Message (CPM) according to an ETSI standard.
///
/// This message is used to describe information around itself.
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
pub struct CollectivePerceptionMessage {
    /// Protocol version of the CPM (mandatory).
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

/// Represents a vehicle sensor and its properties.
/// This includes the sensor's reference point, offsets, and a list of properties.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct VehicleSensor {
    /// X-axis offset of the sensor in millimeters (mandatory).
    pub x_sensor_offset: i16,
    /// Y-axis offset of the sensor in millimeters (mandatory).
    pub y_sensor_offset: i16,
    /// List of properties associated with the vehicle sensor (mandatory).
    pub vehicle_sensor_property_list: Vec<VehicleSensorProperty>,

    /// Z-axis offset of the sensor in millimeters (optional).
    pub z_sensor_offset: Option<u16>,
    /// Identifier for the reference point of the sensor (optional).
    pub ref_point_id: Option<u8>,
}

/// Represents the properties of a vehicle sensor.
/// This includes the range and opening angles for both horizontal and vertical directions.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct VehicleSensorProperty {
    /// The range of the sensor in millimeters (mandatory).
    pub range: u16,
    /// The starting angle of the horizontal opening in centidegrees (mandatory).
    pub horizontal_opening_angle_start: u16,
    /// The ending angle of the horizontal opening in centidegrees (mandatory).
    pub horizontal_opening_angle_end: u16,

    /// The starting angle of the vertical opening in centidegrees (optional).
    pub vertical_opening_angle_start: Option<u16>,
    /// The ending angle of the vertical opening in centidegrees (optional).
    pub vertical_opening_angle_end: Option<u16>,
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
    pub min_message_rate: MessageRateHz,
    pub max_message_rate: MessageRateHz,
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
            _ => Err(format!("Invalid sensor type value: {}", value)),
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
        CollectivePerceptionMessage, MapReference,
    };

    macro_rules! assert_float_eq {
        ($a:expr, $b:expr, $e:expr) => {
            let delta = (($a) - ($b)).abs();
            assert!(delta <= ($e), "Actual:   {}\nExpected: {}", $a, $b)
        };
    }

    fn minimal_cpm() -> &'static str {
        r#"{
            "protocol_version": 255,
            "station_id": 4294967295,
            "management_container": {
                "reference_time": 4398046511103,
                "reference_position": {
                    "latitude": 900000001,
                    "longitude": -1800000001
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 4095,
                        "semi_major_orientation": 3601
                    },
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
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
                    "longitude": -1800000001
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 4095,
                        "semi_major_orientation": 3601
                    },
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
                    }
                }
            },
            "sensor_information_container": [
                {
                    "sensor_id": 1,
                    "sensor_type": 3,
                    "shadowing_applies": true,
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
                                    "z": -32768
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
            ],
            "perceived_object_container": []
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
                    "longitude": -1800000001
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 4095,
                        "semi_major_orientation": 3601
                    },
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
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
                        "id": 0,
                        "region": 0
                    }
                }
            ],
            "sensor_information_container": [
                {
                    "sensor_id": 1,
                    "sensor_type": 31,
                    "perception_region_shape": {
                        "rectangular": {
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
                    "perception_region_confidence": 101,
                    "shadowing_applies": true,
                },
                {
                    "sensor_id": 2,
                    "sensor_type": 0,
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
                    "perception_region_confidence": 101,
                    "shadowing_applies": true,
                },
                {
                    "sensor_id": 3,
                    "sensor_type": 15,
                    "perception_region_shape": {
                        "polygonal": {
                            "polygon": [
                                {
                                    "x_coordinate": -32768,
                                    "y_coordinate": -32768,
                                    "z": -32768
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
                    "perception_region_confidence": 101,
                    "shadowing_applies": true,
                },
                {
                    "sensor_id": 4,
                    "sensor_type": 3,
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
                    "perception_region_confidence": 101,
                    "shadowing_applies": true,
                },
                {
                    "sensor_id": 5,
                    "sensor_type": 7,
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
                    "perception_region_confidence": 101,
                    "shadowing_applies": true,
                },
                {
                    "sensor_id": 6,
                    "sensor_type": 3,
                    "perception_region_shape": {
                        "radial_shapes": {
                            "ref_point_id": 255,
                            "x_coordinate": 1001,
                            "y_coordinate": -3094,
                            "z_coordinate": 1047,
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
                            ]
                        }
                    },
                    "perception_region_confidence": 101,
                    "shadowing_applies": true,
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
                                    "z": -32768
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
                    "sensor_id_list": [ 1, 2, 3, 4, 5, 6 ],
                    "perceived_object_ids": []
                }
            ],
            "perceived_object_container": []
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
                    "longitude": -1800000001
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 4095,
                        "semi_major_orientation": 3601
                    },
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
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
                "orientation_angle": 3601,
                "pitch_angle": 0,
                "roll_angle": 1800,
                "trailer_data_set": [
                    {
                        "ref_point_id": 255,
                        "hitch_point_offset": 255,
                        "hitch_angle": 3601,
                        "front_overhang": 255,
                        "rear_overhang": 255,
                        "trailer_width": 62
                    },
                    {
                        "ref_point_id": 0,
                        "hitch_point_offset": 0,
                        "hitch_angle": 0,
                        "front_overhang": 0,
                        "rear_overhang": 0,
                        "trailer_width": 1
                    }
                ]
            },
            "sensor_information_container": [
                {
                    "sensor_id": 1,
                    "sensor_type": 31,
                    "perception_region_shape": {
                        "rectangular": {
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
                    "perception_region_confidence": 101,
                    "shadowing_applies": true,
                },
                {
                    "sensor_id": 2,
                    "sensor_type": 0,
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
                    "perception_region_confidence": 101,
                    "shadowing_applies": true,
                },
                {
                    "sensor_id": 3,
                    "sensor_type": 15,
                    "perception_region_shape": {
                        "polygonal": {
                            "polygon": [
                                {
                                    "x_coordinate": -32768,
                                    "y_coordinate": -32768,
                                    "z": -32768
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
                    "perception_region_confidence": 101,
                    "shadowing_applies": true,
                },
                {
                    "sensor_id": 4,
                    "sensor_type": 3,
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
                    "perception_region_confidence": 101,
                    "shadowing_applies": true,
                },
                {
                    "sensor_id": 5,
                    "sensor_type": 7,
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
                    "perception_region_confidence": 101,
                    "shadowing_applies": true,
                },
                {
                    "sensor_id": 6,
                    "sensor_type": 3,
                    "perception_region_shape": {
                        "radial_shapes": {
                            "ref_point_id": 255,
                            "x_coordinate": 1001,
                            "y_coordinate": -3094,
                            "z_coordinate": 1047,
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
                            ]
                        }
                    },
                    "perception_region_confidence": 101,
                    "shadowing_applies": true,
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
                                    "z": -32768
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
                    "sensor_id_list": [ 1, 2, 3, 4, 5, 6 ],
                    "perceived_object_ids": []
                }
            ],
            "perceived_object_container": []
        }"#
    }

    #[test]
    fn test_deserialize_minimal_cpm() {
        let data = minimal_cpm();

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();
        assert_eq!(cpm.protocol_version, 2);
        assert_eq!(cpm.station_id, 42);
        assert!(cpm.originating_vehicle_container.is_none());
        assert!(cpm.originating_rsu_container.is_empty());
        assert!(cpm.sensor_information_container.is_empty());
        assert!(cpm.perception_region_container.is_empty());
        assert!(cpm.perceived_object_container.is_empty());
    }

    #[test]
    fn test_deserialize_standard_cpm() {
        let data = standard_cpm();

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
    fn test_deserialize_full_vehicle_cpm() {
        let data = full_vehicle_cpm();

        match serde_json::from_str::<CollectivePerceptionMessage>(data) {
            Ok(cpm) => {
                assert_eq!(cpm.station_id, 4294967295);
            }
            Err(e) => {
                panic!("Failed to deserialize vehicle CPM: '{}'", e);
            }
        }
    }

    #[test]
    fn test_deserialize_full_rsu_cpm() {
        let data = full_rsu_cpm();

        match serde_json::from_str::<CollectivePerceptionMessage>(data) {
            Ok(cpm) => {
                assert_eq!(cpm.station_id, 4294967295);
            }
            Err(e) => {
                panic!("Failed to deserialize RSU CPM: '{}'", e);
            }
        }
    }

    #[test]
    fn test_deserialize_road_segment_map_reference() {
        let data = r#"{
            "road_segment": {
                "id": 65535,
                "region": 65535
            },
        }"#;

        match serde_json::from_str::<MapReference>(data) {
            Ok(object) => {
                let road_segment = object.intersection.unwrap();
                assert_eq!(road_segment.id, 65535);
                assert_eq!(road_segment.region, Some(65535));
            }
            Err(e) => panic!("Failed to deserialize a road segment MapReference: '{}'", e),
        }
    }

    #[test]
    fn test_deserialize_intersection_map_reference() {
        let data = r#"{
            "intersection": {
                "id": 65535,
                "region": 65535
            },
        }"#;

        match serde_json::from_str::<MapReference>(data) {
            Ok(object) => {
                let intersection = object.intersection.unwrap();
                assert_eq!(intersection.id, 65535);
                assert_eq!(intersection.region, Some(65535));
            }
            Err(e) => panic!(
                "Failed to deserialize an intersection MapReference: '{}'",
                e
            ),
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
                        "longitude": 222222222
                        "position_confidence_ellipse": {
                            "semi_major": 4095,
                            "semi_minor": 4095,
                            "semi_major_orientation": 3601
                        },
                        "altitude": {
                            "value": 800001,
                            "confidence": 15
                        }
                    }
                },
                "sensor_information_container": [],
                "perception_region_container": []
                "perceived_object_container": [],
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
