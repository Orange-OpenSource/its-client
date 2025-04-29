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

use crate::exchange::etsi::mobile_perceived_object::MobilePerceivedObject;
use crate::exchange::etsi::perceived_object::PerceivedObject;
use crate::exchange::etsi::reference_position::ReferencePosition;
pub(crate) use crate::exchange::etsi::{
    Heading, Speed, acceleration_from_etsi, heading_from_etsi, speed_from_etsi,
    timestamp_to_generation_delta_time,
};
use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::message::content_error::ContentError::{
    MissingStationDataContainer, NotAMortal, RsuOriginatingMessage,
};
use crate::exchange::mortal::Mortal;
use crate::mobility::mobile::Mobile;
use crate::mobility::position::Position;
use serde::{Deserialize, Serialize};
use std::any::type_name;

/// Represents a Collective Perception Message (CPM) as defined by ETSI TS 103 324 v2.1.0.
/// This structure contains information about the station, detected objects, and sensor data.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CollectivePerceptionMessage {
    /// Protocol version of the CPM (mandatory).
    pub protocol_version: u8,
    /// Unique identifier for the station (mandatory).
    pub station_id: u32,
    /// Time difference in milliseconds since the last generation of the CPM (mandatory).
    pub generation_delta_time: u16,
    /// Container with management information about the station (mandatory).
    pub management_container: ManagementContainer,

    /// Container with data about the originating station (optional).
    pub station_data_container: Option<StationDataContainer>,
    /// List of sensor specifications (optional).
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub sensor_information_container: Vec<SensorInformation>,
    /// List of detected objects (optional).
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub perceived_object_container: Vec<PerceivedObject>,
    /// List of detected free spaces (optional).
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub free_space_addendum_container: Vec<FreeSpaceAddendum>,
}

/// Represents the management container of a CPM.
/// Contains information about the station type and its reference position.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct ManagementContainer {
    /// Type of the station (mandatory).
    pub station_type: u8,
    /// Reference position of the station (mandatory).
    pub reference_position: ReferencePosition,
}

/// Represents the container for data originating from a station.
/// Includes information about the originating vehicle or RSU (Road-Side Unit).
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct StationDataContainer {
    /// Data about the originating vehicle (optional).
    pub originating_vehicle_container: Option<OriginatingVehicleContainer>,
    /// Data about the originating RSU (optional).
    pub originating_rsu_container: Option<OriginatingRSUContainer>,
}

/// Represents the container for data originating from a vehicle.
/// Includes information about the vehicle's heading, speed, dimensions, and acceleration.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct OriginatingVehicleContainer {
    /// Heading of the vehicle in centidegrees (mandatory).
    pub heading: Heading,
    /// Speed of the vehicle in decimeters per second (mandatory).
    pub speed: Speed,

    /// Direction of the vehicle's movement (optional).
    /// Values: 0 (forward), 1 (backward), 2 (stationary).
    pub drive_direction: Option<u8>,
    /// Length of the vehicle in decimeters (optional).
    pub vehicle_length: Option<VehicleLength>,
    /// Width of the vehicle in decimeters (optional).
    pub vehicle_width: Option<u8>,
    /// Longitudinal acceleration of the vehicle in centimeters per second squared (optional).
    pub longitudinal_acceleration: Option<Acceleration>,
    /// Lateral acceleration of the vehicle in centimeters per second squared (optional).
    pub lateral_acceleration: Option<Acceleration>,
    /// Vertical acceleration of the vehicle in centimeters per second squared (optional).
    pub vertical_acceleration: Option<Acceleration>,
}

/// Represents the acceleration of a vehicle.
/// Includes the acceleration value and its confidence level.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Acceleration {
    /// Acceleration value in centimeters per second squared (mandatory).
    pub value: i16,
    /// Confidence level for the acceleration (optional).
    pub confidence: Option<u8>,
}

/// Represents the length of a vehicle.
/// Includes the length value and its confidence level.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct VehicleLength {
    /// Length of the vehicle in decimeters (mandatory).
    pub value: u16,
    /// Confidence level for the vehicle length (optional).
    pub confidence: Option<TrailerPresence>,
}

/// Enum representing the presence of a trailer.
/// Includes various states such as no trailer, trailer with known/unknown length, or unavailable.
#[derive(Debug, Clone, Copy, Hash, PartialEq, Eq, Serialize, Deserialize)]
#[serde(from = "u8", into = "u8")]
pub enum TrailerPresence {
    /// No trailer is present.
    NoTrailerPresent = 0,
    /// Trailer is present with a known length.
    TrailerPresentWithKnownLength = 1,
    /// Trailer is present with an unknown length.
    TrailerPresentWithUnknownLength = 2,
    /// Trailer presence is unknown.
    TrailerPresenceIsUnknown = 3,
    /// Trailer presence information is unavailable.
    Unavailable = 4,
}

impl TrailerPresence {
    /// Attempts to convert a `u8` value into a `TrailerPresence` enum.
    /// Returns an error if the value is invalid.
    fn try_from(value: u8) -> Result<TrailerPresence, String> {
        match value {
            0 => Ok(TrailerPresence::NoTrailerPresent),
            1 => Ok(TrailerPresence::TrailerPresentWithKnownLength),
            2 => Ok(TrailerPresence::TrailerPresentWithUnknownLength),
            3 => Ok(TrailerPresence::TrailerPresenceIsUnknown),
            4 => Ok(TrailerPresence::Unavailable),
            _ => Err(format!("Invalid trailer presence value: {}", value)),
        }
    }
}

impl From<u8> for TrailerPresence {
    /// Converts a `u8` value into a `TrailerPresence` enum.
    /// Defaults to `TrailerPresence::Unavailable` if the value is invalid.
    fn from(value: u8) -> Self {
        Self::try_from(value).unwrap_or(TrailerPresence::Unavailable)
    }
}

impl From<TrailerPresence> for u8 {
    fn from(trailer_presence: TrailerPresence) -> Self {
        trailer_presence as u8
    }
}

/// Represents the container for data originating from a Road-Side Unit (RSU).
/// This includes information about the intersection and road segment references.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct OriginatingRSUContainer {
    /// Road regulator id (optional).
    pub region: Option<u16>,

    /// Identifier for the intersection reference (optional).
    pub intersection_reference_id: Option<u16>,
    /// Identifier for the road segment reference (optional).
    pub road_segment_reference_id: Option<u16>,
}

/// Represents information about a sensor, including its identifier, type, and detection area.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct SensorInformation {
    /// Sensor identifier (mandatory).
    pub sensor_id: u8,
    /// Type of the sensor (mandatory).
    #[serde(rename = "type")]
    pub sensor_type: SensorType,
    /// Detection area of the sensor (mandatory).
    pub detection_area: DetectionArea,
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
    Fusion = 8,
    InductionLoop = 9,
    SphericalCamera = 10,
    ItsAggregation = 11,
    UnKnown12 = 12,
    UnKnown13 = 13,
    UnKnown14 = 14,
    UnKnown15 = 15,
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
            8 => Ok(SensorType::Fusion),
            9 => Ok(SensorType::InductionLoop),
            10 => Ok(SensorType::SphericalCamera),
            11 => Ok(SensorType::ItsAggregation),
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

/// Represents the detection area of a sensor.
/// This includes various possible shapes such as polygons, radial areas,
/// circular areas, elliptical areas, and rectangular areas.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct DetectionArea {
    /// Vehicle sensor information (optional).
    pub vehicle_sensor: Option<VehicleSensor>,
    /// Radial representation of a stationary sensor's detection area (optional).
    pub stationary_sensor_radial: Option<StationarySensorRadial>,
    /// Polygon representation of a stationary sensor's detection area (optional).
    pub stationary_sensor_polygon: Option<Vec<Offset>>,
    /// Circular representation of a stationary sensor's detection area (optional).
    pub stationary_sensor_circular: Option<CircularArea>,
    /// Elliptical representation of a stationary sensor's detection area (optional).
    pub stationary_sensor_ellipse: Option<EllipticalArea>,
    /// Rectangular representation of a stationary sensor's detection area (optional).
    pub stationary_sensor_rectangle: Option<RectangularArea>,
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

/// Represents a stationary sensor with a radial detection area.
/// This includes the range, horizontal and vertical opening angles,
/// and an optional offset for the sensor's position.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct StationarySensorRadial {
    /// The maximum range of the sensor in millimeters (mandatory).
    pub range: u16,
    /// The starting angle of the horizontal opening in centidegrees (mandatory).
    pub horizontal_opening_angle_start: u16,
    /// The ending angle of the horizontal opening in centidegrees (mandatory).
    pub horizontal_opening_angle_end: u16,

    /// The starting angle of the vertical opening in centidegrees (optional).
    pub vertical_opening_angle_start: Option<u16>,
    /// The ending angle of the vertical opening in centidegrees (optional).
    pub vertical_opening_angle_end: Option<u16>,
    /// The offset of the sensor's position (optional).
    pub sensor_position_offset: Option<Offset>,
}

/// Represents a circular area.
/// This includes the center point of the circle and its radius.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CircularArea {
    /// The radius of the circular area in millimeters (mandatory).
    pub radius: u16,

    /// The center point of the circular area (optional).
    pub node_center_point: Option<Offset>,
}

/// Represents an elliptical area.
/// This includes the semi-major and semi-minor range lengths,
/// the orientation of the semi-major range, and an optional center point and height.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct EllipticalArea {
    /// The length of the semi-major axis in millimeters (mandatory).
    pub semi_major_range_length: u16,
    /// The length of the semi-minor axis in millimeters (mandatory).
    pub semi_minor_range_length: u16,
    /// The orientation of the semi-major axis in centidegrees (mandatory).
    pub semi_major_range_orientation: u16,

    /// The center point of the elliptical area (optional).
    pub node_center_point: Option<Offset>,
    /// The height of the elliptical area in millimeters (optional).
    pub semi_height: Option<u16>,
}

/// Represents a rectangular area.
/// This includes the semi-major and semi-minor range lengths,
/// the orientation of the semi-major range, and an optional center point and height.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct RectangularArea {
    /// The length of the semi-major axis in millimeters (mandatory).
    pub semi_major_range_length: u16,
    /// The length of the semi-minor axis in millimeters (mandatory).
    pub semi_minor_range_length: u16,
    /// The orientation of the semi-major axis in centidegrees (mandatory).
    pub semi_major_range_orientation: u16,

    /// The center point of the rectangular area (optional).
    pub node_center_point: Option<Offset>,
    /// The height of the rectangular area in millimeters (optional).
    pub semi_height: Option<u16>,
}

/// Represents an offset in a 3D coordinate system.
/// This includes the x and y coordinates, and an optional z coordinate.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Offset {
    /// The x-coordinate of the offset in millimeters (mandatory).
    pub x: i16,
    /// The y-coordinate of the offset in millimeters (mandatory).
    pub y: i16,

    /// The z-coordinate of the offset in millimeters (optional).
    pub z: Option<i16>,
}

/// Represents a free space addendum.
/// This includes the free space area, confidence level, contributing sensor IDs,
/// and an optional shadowing indicator.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct FreeSpaceAddendum {
    /// Free space area (mandatory).
    pub free_space_area: FreeSpaceArea,
    /// Confidence level for the free space (mandatory).
    pub free_space_confidence: u8,

    /// List of sensor IDs contributing to the free space detection (optional).
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub sensor_id_list: Vec<u8>,
    /// Indicates whether shadowing applies (optional).
    pub shadowing_applies: Option<bool>,
}

/// Represents a free space area.
/// This includes various possible shapes such as polygons, circular areas,
/// elliptical areas, and rectangular areas.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct FreeSpaceArea {
    /// Polygon representation of the free space (optional).
    #[serde(skip_serializing_if = "Option::is_none")]
    pub free_space_polygon: Option<Vec<Offset>>,
    /// Circular representation of the free space (optional).
    #[serde(skip_serializing_if = "Option::is_none")]
    pub free_space_circular: Option<CircularArea>,
    /// Elliptical representation of the free space (optional).
    #[serde(skip_serializing_if = "Option::is_none")]
    pub free_space_ellipse: Option<EllipticalArea>,
    /// Rectangular representation of the free space (optional).
    #[serde(skip_serializing_if = "Option::is_none")]
    pub free_space_rectangle: Option<RectangularArea>,
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
        if let Some(station_data_container) = &self.station_data_container {
            if let Some(originating_vehicle_container) =
                &station_data_container.originating_vehicle_container
            {
                return Some(speed_from_etsi(originating_vehicle_container.speed.value));
            }
        }
        None
    }

    fn heading(&self) -> Option<f64> {
        if let Some(station_data_container) = &self.station_data_container {
            if let Some(originating_vehicle_container) =
                &station_data_container.originating_vehicle_container
            {
                return Some(heading_from_etsi(
                    originating_vehicle_container.heading.value,
                ));
            }
        }
        None
    }

    fn acceleration(&self) -> Option<f64> {
        if let Some(station_data_container) = &self.station_data_container {
            if let Some(originating_vehicle_container) =
                &station_data_container.originating_vehicle_container
            {
                if let Some(longitudinal_acceleration) =
                    &originating_vehicle_container.longitudinal_acceleration
                {
                    return Some(acceleration_from_etsi(longitudinal_acceleration.value));
                }
            }
        }
        None
    }
}

impl Content for CollectivePerceptionMessage {
    fn get_type(&self) -> &str {
        "cpm"
    }

    fn appropriate(&mut self, timestamp: u64, new_station_id: u32) {
        self.station_id = new_station_id;
        self.generation_delta_time = timestamp_to_generation_delta_time(timestamp);
    }

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError> {
        match &self.station_data_container {
            Some(container) => match container.originating_vehicle_container {
                Some(_) => Ok(self),
                None => Err(RsuOriginatingMessage(type_name::<
                    CollectivePerceptionMessage,
                >())),
            },
            None => Err(MissingStationDataContainer(type_name::<
                CollectivePerceptionMessage,
            >())),
        }
    }

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError> {
        Err(NotAMortal(type_name::<CollectivePerceptionMessage>()))
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::collective_perception_message::{
        CircularArea, CollectivePerceptionMessage, EllipticalArea, FreeSpaceAddendum,
        ManagementContainer, Offset, RectangularArea, StationarySensorRadial,
    };

    use crate::exchange::etsi::perceived_object::PerceivedObject;
    use crate::exchange::etsi::reference_position::{
        Altitude, ReferencePosition, altitude_from_etsi, coordinate_from_etsi,
    };
    use crate::exchange::etsi::speed_from_etsi;

    macro_rules! assert_float_eq {
        ($a:expr, $b:expr, $e:expr) => {
            let delta = (($a) - ($b)).abs();
            assert!(delta <= ($e), "Actual:   {}\nExpected: {}", $a, $b)
        };
    }

    fn minimal_cpm() -> &'static str {
        r#"{
            "protocol_version": 2,
            "station_id": 42,
            "generation_delta_time": 1000,
            "management_container": {
                "station_type": 5,
                "reference_position": {
                    "latitude": 426263556,
                    "longitude": -82492123
                }
            }
        }"#
    }

    fn standard_cpm() -> &'static str {
        r#"{
            "protocol_version": 0,
            "station_id": 0,
            "generation_delta_time": 0,
            "management_container": {
                "station_type": 15,
                "reference_position": {
                    "latitude": 488640493,
                    "longitude": 23310526,
                    "altitude": {
                        "value": 900,
                        "confidence": 2
                    },
                    "position_confidence_ellipse": {
                        "semi_major_confidence": 1,
                        "semi_minor_confidence": 1,
                        "semi_major_orientation": 0
                    }
                }
            },
            "sensor_information_container": [
                {
                    "sensor_id": 1,
                    "type": 3,
                    "detection_area": {
                        "stationary_sensor_polygon": [
                                {
                                    "x": -32768,
                                    "y": -32768,
                                    "z": -32768
                                },
                                {
                                    "x": 32767,
                                    "y": -32768,
                                    "z": -32768
                                },
                                {
                                    "x": 32767,
                                    "y": 32767,
                                    "z": -32768
                                },
                                {
                                    "x": -32768,
                                    "y": 32767,
                                    "z": -32768
                                }
                            ]
                    }
                }
            ],
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
                }
            ]
        }"#
    }

    fn full_cpm() -> &'static str {
        r#"{
            "protocol_version": 255,
            "station_id": 4294967295,
            "generation_delta_time": 65535,
            "management_container": {
                "station_type": 254,
                "reference_position": {
                    "latitude": 426263556,
                    "longitude": -82492123,
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
                    }
                },
                "position_confidence_ellipse": {
                    "semi_major_confidence": 4095,
                    "semi_minor_confidence": 4095,
                    "semi_major_orientation": 3601
                }
            },
            "station_data_container": {
                "originating_vehicle_container": {
                    "heading": {
                        "value": 180,
                        "confidence": 127
                    },
                    "speed": {
                        "value": 1600,
                        "confidence": 127
                    },
                    "drive_direction": 0,
                    "vehicle_length": {
                        "value": 31,
                        "confidence": 127
                    },
                    "vehicle_width": 18,
                    "longitudinal_acceleration": {
                        "value": -160,
                        "confidence": 127
                    },
                    "lateral_acceleration": {
                        "value": -2,
                        "confidence": 127
                    },
                    "vertical_acceleration": {
                        "value": -1,
                        "confidence": 127
                    }
                }
            },
            "sensor_information_container": [
                {
                    "sensor_id": 1,
                    "type": 3,
                    "detection_area": {
                        "vehicle_sensor": {
                            "ref_point_id": 255,
                            "x_sensor_offset": -3094,
                            "y_sensor_offset": -1000,
                            "z_sensor_offset": 1000,
                            "vehicle_sensor_property_list": [
                                {
                                    "range": 10000,
                                    "horizontal_opening_angle_start": 3601,
                                    "horizontal_opening_angle_end": 3601,
                                    "vertical_opening_angle_start": 3601,
                                    "vertical_opening_angle_end": 3601
                                }
                            ]
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
                        "stationary_sensor_polygon": [
                            {
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
                }
            ],
            "perceived_object_container": [
                {
                    "object_id": 0,
                    "time_of_measurement": 50,
                    "x_distance": 400,
                    "y_distance": 100,
                    "x_speed": 1400,
                    "y_speed": 500,
                    "object_age": 1500,
                    "confidence": {
                        "x_distance": 4095,
                        "y_distance": 0,
                        "x_speed": 7,
                        "y_speed": 0,
                        "object": 15
                    }
                },
                {
                    "object_id": 1,
                    "time_of_measurement": 50,
                    "x_distance": 400,
                    "y_distance": 100,
                    "x_speed": 1400,
                    "y_speed": 500,
                    "object_age": 1500,
                    "confidence": {
                        "x_distance": 4095,
                        "y_distance": 0,
                        "x_speed": 7,
                        "y_speed": 0,
                        "object": 15
                    }
                }
            ],
            "free_space_addendum_container": [
                {
                    "free_space_area": {
                        "free_space_polygon": [
                            {
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
        }"#
    }

    #[test]
    fn it_can_provide_the_mobile_perceived_object_list() {
        let cpm = CollectivePerceptionMessage {
            station_id: 12,
            management_container: ManagementContainer {
                station_type: 15,
                reference_position: ReferencePosition {
                    latitude: 488417860,
                    longitude: 23678940,
                    altitude: Some(Altitude {
                        value: 900,
                        ..Default::default()
                    }),
                    ..Default::default()
                },
            },
            perceived_object_container: vec![
                PerceivedObject {
                    object_id: 1,
                    x_distance: 1398,
                    y_distance: -1138,
                    z_distance: None,
                    x_speed: 389,
                    y_speed: 25,
                    z_speed: None,
                    object_age: 1500,
                    ..Default::default()
                },
                PerceivedObject {
                    object_id: 4,
                    x_distance: 102,
                    y_distance: -942,
                    z_distance: None,
                    x_speed: 9,
                    y_speed: 16,
                    z_speed: None,
                    object_age: 533,
                    ..Default::default()
                },
            ],
            ..Default::default()
        };

        let perceived_object = cpm.mobile_perceived_object_list();
        let first = perceived_object
            .first()
            .expect("Perceived object list must contain two elements");
        let second = perceived_object
            .get(1)
            .expect("Perceived object list must contain two elements");

        assert_eq!(first.mobile_id, 121);
        assert_float_eq!(
            first.position.latitude,
            coordinate_from_etsi(488416836),
            1e-6
        );
        assert_float_eq!(
            first.position.longitude,
            coordinate_from_etsi(23680844),
            1e-6
        );
        assert_float_eq!(first.position.altitude, altitude_from_etsi(900), 1e-3);
        assert_float_eq!(first.speed, speed_from_etsi(389), 1e-5);
        assert_float_eq!(first.heading.to_degrees(), 86.3, 1e-1);

        assert_eq!(second.mobile_id, 124);
        assert_float_eq!(
            second.position.latitude,
            coordinate_from_etsi(488417013),
            1e-6
        );
        assert_float_eq!(
            second.position.longitude,
            coordinate_from_etsi(23679078),
            1e-6
        );
        assert_float_eq!(second.position.altitude, altitude_from_etsi(900), 1e-3);
        assert_float_eq!(second.speed, speed_from_etsi(18), 1e-5);
        assert_float_eq!(second.heading.to_degrees(), 29.3, 1e-1);
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
    fn test_deserialize_full_cpm() {
        let data = full_cpm();

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
            "x": 12345,
            "y": 5432
        }"#;

        match serde_json::from_str::<Offset>(data) {
            Ok(offset) => {
                assert_eq!(offset.x, 12345);
                assert_eq!(offset.y, 5432);
                assert!(offset.z.is_none());
            }
            Err(e) => panic!("Failed to deserialize minimal Offset: '{}'", e),
        }
    }

    #[test]
    fn test_deserialize_full_offset() {
        let data = r#"{
            "x": 23456,
            "y": 4321,
            "z": 6789
        }"#;

        match serde_json::from_str::<Offset>(data) {
            Ok(offset) => {
                assert_eq!(offset.x, 23456);
                assert_eq!(offset.y, 4321);
                assert_eq!(offset.z, Some(6789));
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
                assert!(
                    stationary_sensor_radial
                        .vertical_opening_angle_start
                        .is_none()
                );
                assert!(
                    stationary_sensor_radial
                        .vertical_opening_angle_end
                        .is_none()
                );
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
                "x": -3456,
                "y": 4321
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
                        x: -3456,
                        y: 4321,
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

        match serde_json::from_str::<EllipticalArea>(data) {
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

        match serde_json::from_str::<EllipticalArea>(data) {
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

        match serde_json::from_str::<RectangularArea>(data) {
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

        match serde_json::from_str::<RectangularArea>(data) {
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
    fn deserialize_minimal_free_space_area_polygon() {
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
                assert!(
                    free_space_addendum
                        .free_space_area
                        .free_space_polygon
                        .is_some()
                );
                assert!(
                    free_space_addendum
                        .free_space_area
                        .free_space_circular
                        .is_none()
                );
                assert!(
                    free_space_addendum
                        .free_space_area
                        .free_space_rectangle
                        .is_none()
                );
                assert!(
                    free_space_addendum
                        .free_space_area
                        .free_space_ellipse
                        .is_none()
                );
                assert_eq!(free_space_addendum.free_space_confidence, 12);
                assert!(free_space_addendum.sensor_id_list.is_empty());
                assert!(free_space_addendum.shadowing_applies.is_none());
            }
            Err(e) => panic!("Failed to deserialize FreeSpaceAddendum: '{}'", e),
        }
    }

    #[test]
    fn deserialize_minimal_free_space_area_circular() {
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
                assert!(
                    free_space_addendum
                        .free_space_area
                        .free_space_polygon
                        .is_none()
                );
                assert!(
                    free_space_addendum
                        .free_space_area
                        .free_space_circular
                        .is_some()
                );
                assert!(
                    free_space_addendum
                        .free_space_area
                        .free_space_rectangle
                        .is_none()
                );
                assert!(
                    free_space_addendum
                        .free_space_area
                        .free_space_ellipse
                        .is_none()
                );
                assert_eq!(free_space_addendum.free_space_confidence, 101);
                assert_eq!(free_space_addendum.sensor_id_list.len(), 3);
                assert_eq!(free_space_addendum.shadowing_applies, Some(true))
            }
            Err(e) => panic!("Failed to deserialize FreeSpaceAddendum: '{}'", e),
        }
    }

    #[test]
    fn test_deserialize_minimal_cpm() {
        let data = minimal_cpm();

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();
        assert_eq!(cpm.protocol_version, 2);
        assert_eq!(cpm.station_id, 42);
        assert_eq!(cpm.generation_delta_time, 1000);
        assert!(cpm.station_data_container.is_none());
        assert!(cpm.sensor_information_container.is_empty());
        assert!(cpm.perceived_object_container.is_empty());
        assert!(cpm.free_space_addendum_container.is_empty());
    }

    #[test]
    fn test_cpm_defaults() {
        let cpm = CollectivePerceptionMessage::default();
        assert_eq!(cpm.protocol_version, 0);
        assert_eq!(cpm.station_id, 0);
        assert_eq!(cpm.generation_delta_time, 0);
        assert!(cpm.sensor_information_container.is_empty());
        assert!(cpm.perceived_object_container.is_empty());
        assert!(cpm.free_space_addendum_container.is_empty());
    }

    #[test]
    fn deserialize_collective_perception_message_with_empty_containers() {
        let data = r#"{
                "protocol_version": 3,
                "station_id": 54321,
                "generation_delta_time": 2000,
                "management_container": {
                    "station_type": 5,
                    "reference_position": {
                        "latitude": 111111111,
                        "longitude": 222222222
                    }
                },
                "sensor_information_container": [],
                "perceived_object_container": [],
                "free_space_addendum_container": []
            }"#;

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();
        assert_eq!(cpm.protocol_version, 3);
        assert_eq!(cpm.station_id, 54321);
        assert_eq!(cpm.generation_delta_time, 2000);
        assert_eq!(cpm.management_container.station_type, 5);
        assert_eq!(
            cpm.management_container.reference_position.latitude,
            111111111
        );
        assert_eq!(
            cpm.management_container.reference_position.longitude,
            222222222
        );
        assert!(cpm.station_data_container.is_none());
        assert!(cpm.sensor_information_container.is_empty());
        assert!(cpm.perceived_object_container.is_empty());
        assert!(cpm.free_space_addendum_container.is_empty());
    }
}
