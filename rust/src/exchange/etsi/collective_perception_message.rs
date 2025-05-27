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
use crate::exchange::etsi::{
    acceleration_from_etsi, heading_from_etsi, speed_from_etsi, timestamp_to_generation_delta_time,
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

/// Collective Perception Message (CPM) according to ETSI TS 103 324 v2.1.0
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CollectivePerceptionMessage {
    /// Protocol version (mandatory)
    pub protocol_version: u8,
    /// Station identifier (mandatory)
    pub station_id: u32,
    /// Generation delta time in milliseconds (mandatory)
    pub generation_delta_time: u16,
    /// Management container with station information (mandatory)
    pub management_container: ManagementContainer,
    /// Station data container with vehicle state information (optional)
    pub station_data_container: Option<StationDataContainer>,
    /// List of sensor specifications (optional)
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub sensor_information_container: Vec<SensorInformation>,
    /// List of detected objects (optional)
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub perceived_object_container: Vec<PerceivedObject>,
    /// List of detected free spaces (optional)
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub free_space_addendum_container: Vec<FreeSpaceAddendum>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct ManagementContainer {
    /// Station type (mandatory)
    pub station_type: u8,
    /// Reference position (mandatory)
    pub reference_position: ReferencePosition,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct StationDataContainer {
    /// Container for originating vehicle data (optional)
    pub originating_vehicle_container: Option<OriginatingVehicleContainer>,
    /// Container for originating RSU data (optional)
    pub originating_rsu_container: Option<OriginatingRSUContainer>,
}

/// Represents the container for data originating from a vehicle.
/// This includes information about the vehicle's heading, speed, dimensions,
/// acceleration, and confidence levels for these measurements.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct OriginatingVehicleContainer {
    /// Heading of the vehicle in centidegrees.
    /// Range: 0 to 35999
    pub heading: u16,
    /// Speed of the vehicle in 0.01 m/s.
    /// Range: 0 to 16383
    pub speed: u16,
    /// Direction of the vehicle's movement (optional).
    /// Values: 0 (forward), 1 (backward), 2 (stationary)
    pub drive_direction: Option<u8>,
    /// Length of the vehicle in decimeters (optional).
    /// Range: 0 to 1023
    pub vehicle_length: Option<u16>,
    /// Width of the vehicle in decimeters (optional).
    /// Range: 0 to 255
    pub vehicle_width: Option<u8>,
    /// Longitudinal acceleration of the vehicle in 0.01 m/s² (optional).
    /// Range: -1600 to 1600
    pub longitudinal_acceleration: Option<i16>,
    /// Yaw rate of the vehicle in 0.01 degrees/s (optional).
    /// Range: -32766 to 32767
    pub yaw_rate: Option<i16>,
    /// Lateral acceleration of the vehicle in 0.01 m/s² (optional).
    /// Range: -1600 to 1600
    pub lateral_acceleration: Option<i16>,
    /// Vertical acceleration of the vehicle in 0.01 m/s² (optional).
    /// Range: -1600 to 1600
    pub vertical_acceleration: Option<i16>,
    /// Confidence levels for the vehicle's measurements.
    pub confidence: OriginatingVehicleContainerConfidence,
}

/// Represents the container for data originating from a Road-Side Unit (RSU).
/// This includes information about the intersection and road segment references.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct OriginatingRSUContainer {
    /// Identifier for the intersection reference (optional).
    pub intersection_reference_id: Option<IntersectionReferenceId>,
    /// Identifier for the road segment reference (optional).
    pub road_segment_reference_id: Option<u32>,
}

/// Represents the identifier for an intersection reference.
/// This includes the road regulator ID and the intersection ID.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct IntersectionReferenceId {
    /// Identifier for the road regulator (optional).
    pub road_regulator_id: Option<u32>,
    /// Identifier for the intersection (mandatory).
    pub intersection_id: u32,
}

/// Represents the confidence levels for various attributes of a vehicle's state.
/// Each field indicates the confidence in the corresponding measurement or property.
/// Confidence values are typically represented as percentages or scaled values.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct OriginatingVehicleContainerConfidence {
    /// Confidence in the heading measurement.
    /// Range: 0 to 255
    pub heading: u8,
    /// Confidence in the speed measurement.
    /// Range: 0 to 255
    pub speed: u8,
    /// Confidence in the vehicle length measurement (optional).
    /// Range: 0 to 255
    pub vehicle_length: Option<u8>,
    /// Confidence in the yaw rate measurement (optional).
    /// Range: 0 to 255
    pub yaw_rate: Option<u8>,
    /// Confidence in the longitudinal acceleration measurement (optional).
    /// Range: 0 to 255
    pub longitudinal_acceleration: Option<u8>,
    /// Confidence in the lateral acceleration measurement (optional).
    /// Range: 0 to 255
    pub lateral_acceleration: Option<u8>,
    /// Confidence in the vertical acceleration measurement (optional).
    /// Range: 0 to 255
    pub vertical_acceleration: Option<u8>,
}

/// Represents information about a sensor, including its identifier, type, and detection area.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct SensorInformation {
    /// Sensor identifier (mandatory).
    pub sensor_id: u8,
    /// Type of the sensor (mandatory).
    #[serde(rename = "type")]
    pub sensor_type: u8,
    /// Detection area covered by the sensor (mandatory).
    pub detection_area: DetectionArea,
}

/// Represents the detection area of a sensor.
/// This includes various possible shapes such as polygons, radial areas,
/// circular areas, elliptical areas, and rectangular areas.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct DetectionArea {
    /// Vehicle sensor information (optional).
    pub vehicle_sensor: Option<VehicleSensor>,
    /// Polygon representation of a stationary sensor's detection area (optional).
    pub stationary_sensor_polygon: Option<Vec<Offset>>,
    /// Radial representation of a stationary sensor's detection area (optional).
    pub stationary_sensor_radial: Option<StationarySensorRadial>,
    /// Circular representation of a stationary sensor's detection area (optional).
    pub stationary_sensor_circular: Option<CircularArea>,
    /// Elliptical representation of a stationary sensor's detection area (optional).
    pub stationary_sensor_ellipse: Option<EllipticArea>,
    /// Rectangular representation of a stationary sensor's detection area (optional).
    pub stationary_sensor_rectangle: Option<RectangleArea>,
}

/// Represents a vehicle sensor and its properties.
/// This includes the sensor's reference point, offsets, and a list of properties.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct VehicleSensor {
    /// Identifier for the reference point of the sensor (mandatory).
    pub ref_point_id: u8,
    /// X-axis offset of the sensor in millimeters (mandatory).
    pub x_sensor_offset: i16,
    /// Y-axis offset of the sensor in millimeters (mandatory).
    pub y_sensor_offset: i16,
    /// Z-axis offset of the sensor in millimeters (optional).
    pub z_sensor_offset: Option<u16>,
    /// List of properties associated with the vehicle sensor (optional).
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub vehicle_sensor_property_list: Vec<VehicleSensorProperty>,
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
    /// The center point of the circular area (optional).
    pub node_center_point: Option<Offset>,
    /// The radius of the circular area in millimeters (mandatory).
    pub radius: u16,
}

/// Represents an elliptical area.
/// This includes the semi-major and semi-minor range lengths,
/// the orientation of the semi-major range, and an optional center point and height.
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct EllipticArea {
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
pub struct RectangleArea {
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
    /// The x-coordinate of the offset in millimeters.
    pub x: i32,
    /// The y-coordinate of the offset in millimeters.
    pub y: i32,
    /// The z-coordinate of the offset in millimeters (optional).
    pub z: Option<i32>,
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
    pub free_space_ellipse: Option<EllipticArea>,
    /// Rectangular representation of the free space (optional).
    #[serde(skip_serializing_if = "Option::is_none")]
    pub free_space_rectangle: Option<RectangleArea>,
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
                return Some(speed_from_etsi(originating_vehicle_container.speed));
            }
        }
        None
    }

    fn heading(&self) -> Option<f64> {
        if let Some(station_data_container) = &self.station_data_container {
            if let Some(originating_vehicle_container) =
                &station_data_container.originating_vehicle_container
            {
                return Some(heading_from_etsi(originating_vehicle_container.heading));
            }
        }
        None
    }

    fn acceleration(&self) -> Option<f64> {
        if let Some(station_data_container) = &self.station_data_container {
            if let Some(originating_vehicle_container) =
                &station_data_container.originating_vehicle_container
            {
                return originating_vehicle_container
                    .longitudinal_acceleration
                    .map(acceleration_from_etsi);
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
        CircularArea, CollectivePerceptionMessage, EllipticArea, FreeSpaceAddendum,
        ManagementContainer, Offset, RectangleArea, StationarySensorRadial,
    };

    use crate::exchange::etsi::perceived_object::PerceivedObject;
    use crate::exchange::etsi::reference_position::{
        Altitude, ReferencePosition, altitude_from_etsi, coordinate_from_etsi,
    };
    use crate::exchange::etsi::speed_from_etsi;

    macro_rules! assert_float_eq {
        ($a:expr, $b:expr, $e:expr) => {
            let delta = (($a) - ($b)).abs();
            assert!(delta <= $e, "Actual:   {}\nExpected: {}", $a, $b)
        };
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
                "altitude": {
                    "value": 900,
                    "confidence": 2
                }              },
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
                "altitude": {
                    "value": 800001,
                    "confidence": 1
                }
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
        let data = r#"{
            "protocol_version": 2,
            "station_id": 42,
            "generation_delta_time": 1000,
            "management_container": {
                "station_type": 5,
                "reference_position": {
                    "latitude": 426263556,
                    "longitude": -82492123
                },
                "confidence": {
                    "position_confidence_ellipse": {
                        "semi_major_confidence": 100,
                        "semi_minor_confidence": 50,
                        "semi_major_orientation": 180
                    },
                    "altitude": 3
                }
            }
        }"#;

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
    fn deserialize_minimal_collective_perception_message() {
        let data = r#"{
                "protocol_version": 1,
                "station_id": 12345,
                "generation_delta_time": 500,
                "management_container": {
                    "station_type": 3,
                    "reference_position": {
                        "latitude": 123456789,
                        "longitude": 987654321
                    }
                }
            }"#;

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();
        assert_eq!(cpm.protocol_version, 1);
        assert_eq!(cpm.station_id, 12345);
        assert_eq!(cpm.generation_delta_time, 500);
        assert_eq!(cpm.management_container.station_type, 3);
        assert_eq!(
            cpm.management_container.reference_position.latitude,
            123456789
        );
        assert_eq!(
            cpm.management_container.reference_position.longitude,
            987654321
        );
        assert_eq!(cpm.management_container.reference_position.altitude, None);
        assert!(cpm.station_data_container.is_none());
        assert!(cpm.sensor_information_container.is_empty());
        assert!(cpm.perceived_object_container.is_empty());
        assert!(cpm.free_space_addendum_container.is_empty());
    }

    #[test]
    fn deserialize_collective_perception_message_with_full_data() {
        let data = r#"{
                "protocol_version": 2,
                "station_id": 67890,
                "generation_delta_time": 1000,
                "management_container": {
                    "station_type": 4,
                    "reference_position": {
                        "latitude": 987654321,
                        "longitude": 123456789,
                        "altitude": {
                            "value": 3000,
                            "confidence": 3
                        }                    
                    }
                },
                "station_data_container": {
                    "originating_vehicle_container": {
                        "heading": 180,
                        "speed": 1500,
                        "drive_direction": 0,
                        "vehicle_length": 50,
                        "vehicle_width": 20,
                        "longitudinal_acceleration": 100,
                        "yaw_rate": 50,
                        "confidence": {
                            "heading": 100,
                            "speed": 100
                        }
                    }
                },
                "sensor_information_container": [
                    {
                        "sensor_id": 1,
                        "type": 2,
                        "detection_area": {
                            "vehicle_sensor": {
                                "ref_point_id": 1,
                                "x_sensor_offset": 100,
                                "y_sensor_offset": 200
                            }
                        }
                    }
                ],
                "perceived_object_container": [
                    {
                        "object_id": 1,
                        "time_of_measurement": 10,
                        "x_distance": 100,
                        "y_distance": 200,
                        "object_age": 1000,
                        "x_speed": 50,
                        "y_speed": 10,
                        "confidence": {
                            "x_distance": 100,
                            "y_distance": 100,
                            "x_speed": 10,
                            "y_speed": 5,
                            "object": 10
                        }
                    }
                ],
                "free_space_addendum_container": [
                    {
                        "free_space_area": {
                            "free_space_polygon": [
                                {"x": 0, "y": 0},
                                {"x": 100, "y": 100}
                            ]
                        },
                        "free_space_confidence": 90
                    }
                ]
            }"#;

        let cpm = serde_json::from_str::<CollectivePerceptionMessage>(data).unwrap();
        assert_eq!(cpm.protocol_version, 2);
        assert_eq!(cpm.station_id, 67890);
        assert_eq!(cpm.generation_delta_time, 1000);
        assert_eq!(cpm.management_container.station_type, 4);
        assert_eq!(
            cpm.management_container.reference_position.latitude,
            987654321
        );
        assert_eq!(
            cpm.management_container.reference_position.longitude,
            123456789
        );
        assert_eq!(
            cpm.management_container.reference_position.altitude,
            Some(Altitude {
                value: 3000,
                confidence: Some(3),
            })
        );
        assert!(cpm.station_data_container.is_some());
        assert_eq!(cpm.sensor_information_container.len(), 1);
        assert_eq!(cpm.perceived_object_container.len(), 1);
        assert_eq!(cpm.free_space_addendum_container.len(), 1);
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
                "station_data_container": null,
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
