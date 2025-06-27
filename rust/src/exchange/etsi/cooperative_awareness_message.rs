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

use crate::exchange::etsi::reference_position::{PathPoint, ReferencePosition};
use crate::exchange::etsi::{
    acceleration_from_etsi, heading_from_etsi, heading_to_etsi, speed_from_etsi, speed_to_etsi,
    timestamp_to_generation_delta_time,
};
use crate::mobility::mobile::Mobile;
use std::any::type_name;

use crate::exchange::etsi::acceleration::Acceleration;
use crate::exchange::etsi::cause_code::CauseCode;
use crate::exchange::etsi::curvature::Curvature;
use crate::exchange::etsi::heading::Heading;
use crate::exchange::etsi::speed::Speed;
use crate::exchange::etsi::steering_wheel_angle::SteeringWheelAngle;
use crate::exchange::etsi::vehicle_length::VehicleLength;
use crate::exchange::etsi::yaw_rate::YawRate;
use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::message::content_error::ContentError::NotAMortal;
use crate::exchange::mortal::Mortal;
use crate::mobility::position::Position;
use serde::{Deserialize, Serialize};

/// Represents a Cooperative Awareness Message (CAM) according to an ETSI standard.
///
/// This message is used to describe the information about itself.
/// It implements the schema defined in the [CAM version 2.2.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cam/cam_schema_2-2-0.json
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CooperativeAwarenessMessage {
    /// Protocol version (mandatory).
    pub protocol_version: u8,
    /// Unique identifier for the station (mandatory).
    pub station_id: u32,
    /// Time difference since the last generation of the message (mandatory).
    pub generation_delta_time: u16,
    /// Basic container with basic information about the station (mandatory).
    pub basic_container: BasicContainer,
    /// High-frequency container with detailed vehicle information (mandatory).
    pub high_frequency_container: HighFrequencyContainer,

    /// Low-frequency container with less frequent data about the vehicle (optional).
    pub low_frequency_container: Option<LowFrequencyContainer>,
    /// Special vehicle container for additional vehicle information (optional).
    pub special_vehicle_container: Option<SpecialVehicleContainer>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct BasicContainer {
    // TODO: implement a StationType enum
    pub station_type: u8,
    pub reference_position: ReferencePosition,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct HighFrequencyContainer {
    pub basic_vehicle_container_high_frequency: Option<BasicVehicleContainerHighFrequency>,
    pub rsu_container_high_frequency: Option<RSUContainerHighFrequency>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct BasicVehicleContainerHighFrequency {
    pub heading: Heading,
    pub speed: Speed,
    //TODO: implement a DriveDirection enum
    pub drive_direction: u8,
    pub vehicle_length: VehicleLength,
    pub vehicle_width: u16,
    pub longitudinal_acceleration: Acceleration,
    pub curvature: Curvature,
    //TODO: implement a CurvatureCalculationMode enum
    pub curvature_calculation_mode: u8,
    pub yaw_rate: YawRate,

    pub acceleration_control: Option<AccelerationControl>,
    pub lane_position: Option<i8>,
    pub steering_wheel_angle: Option<SteeringWheelAngle>,
    pub lateral_acceleration: Option<Acceleration>,
    pub vertical_acceleration: Option<Acceleration>,
    pub performance_class: Option<PerformanceClass>,
    pub cen_dsrc_tolling_zone: Option<CenDSRCTollingZone>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct RSUContainerHighFrequency {
    // TODO: implement the container
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct LowFrequencyContainer {
    pub basic_vehicle_container_low_frequency: Option<BasicVehicleContainerLowFrequency>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct BasicVehicleContainerLowFrequency {
    // TODO: implement a VehicleRole enum
    pub vehicle_role: u8,
    pub exterior_lights: ExteriorLights,
    pub path_history: Vec<PathPoint>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct SpecialVehicleContainer {
    pub public_transport_container: Option<PublicTransportContainer>,
    pub special_transport_container: Option<SpecialTransportContainer>,
    pub dangerous_goods_container: Option<DangerousGoodsContainer>,
    pub road_works_container_basic: Option<RoadWorksContainerBasic>,
    pub rescue_container: Option<RescueContainer>,
    pub emergency_container: Option<EmergencyContainer>,
    pub safety_car_container: Option<SafetyCarContainer>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct SafetyCarContainer {
    pub light_bar_siren_in_use: LightBarSirenInUse,

    pub incident_indication: Option<IncidentIndication>,
    pub traffic_rule: Option<u8>,
    pub speed_limit: Option<u8>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct EmergencyContainer {
    pub light_bar_siren_in_use: LightBarSirenInUse,

    pub incident_indication: Option<IncidentIndication>,
    pub emergency_priority: Option<EmergencyPriority>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct EmergencyPriority {
    pub request_for_right_of_way: bool,
    pub request_for_free_crossing_at_a_traffic_light: bool,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct IncidentIndication {
    pub cc_and_scc: CauseCode,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct RescueContainer {
    pub light_bar_siren_in_use: LightBarSirenInUse,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct RoadWorksContainerBasic {
    pub light_bar_siren_in_use: LightBarSirenInUse,

    pub road_works_sub_cause_code: Option<u8>,
    pub closed_lanes: Option<ClosedLanes>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct ClosedLanes {
    pub inner_hard_shoulder_status: Option<u8>,
    pub outer_hard_shoulder_status: Option<u8>,
    pub driving_lane_status: Option<DrivingLaneStatus>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct DrivingLaneStatus {
    pub lane_1_closed: bool,
    pub lane_2_closed: bool,
    pub lane_3_closed: bool,
    pub lane_4_closed: bool,
    pub lane_5_closed: bool,
    pub lane_6_closed: bool,
    pub lane_7_closed: bool,
    pub lane_8_closed: bool,
    pub lane_9_closed: bool,
    pub lane_10_closed: bool,
    pub lane_11_closed: bool,
    pub lane_12_closed: bool,
    pub lane_13_closed: bool,
}
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct DangerousGoodsContainer {
    pub dangerous_goods_basic: u8,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct SpecialTransportContainer {
    pub special_transport_type: SpecialTransportType,
    pub light_bar_siren_in_use: LightBarSirenInUse,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct SpecialTransportType {
    pub heavy_load: bool,
    pub excess_width: bool,
    pub excess_length: bool,
    pub excess_height: bool,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct LightBarSirenInUse {
    pub light_bar_activated: bool,
    pub siren_activated: bool,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PublicTransportContainer {
    pub embarkation_status: bool,

    pub pt_activation: Option<PTActivation>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PTActivation {
    pub pt_activation_type: u8,
    pub pt_activation_data: String,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct AccelerationControl {
    pub brake_pedal_engaged: bool,
    pub gas_pedal_engaged: bool,
    pub emergency_brake_engaged: bool,
    pub collision_warning_engaged: bool,
    pub acc_engaged: bool,
    pub cruise_control_engaged: bool,
    pub speed_limiter_engaged: bool,
}

#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
#[serde(from = "u8", into = "u8")]
pub enum PerformanceClass {
    #[default]
    Unavailable = 0,
    A = 1,
    B = 2,
}

impl PerformanceClass {
    /// Attempts to convert a `u8` value into a `PerformanceClass` enum.
    /// Returns an error if the value is invalid.
    fn try_from(value: u8) -> Result<PerformanceClass, String> {
        match value {
            0 => Ok(PerformanceClass::Unavailable),
            1 => Ok(PerformanceClass::A),
            2 => Ok(PerformanceClass::B),
            _ => Err(format!("Invalid performance class value: {}", value)),
        }
    }
}

impl From<u8> for PerformanceClass {
    /// Converts a `u8` value into a `PerformanceClass` enum.
    /// Defaults to `PerformanceClass::Unavailable` if the value is invalid.
    fn from(value: u8) -> Self {
        Self::try_from(value).unwrap_or(PerformanceClass::Unavailable)
    }
}

impl From<PerformanceClass> for u8 {
    fn from(performance_class: PerformanceClass) -> Self {
        performance_class as u8
    }
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CenDSRCTollingZone {
    pub protected_zone_latitude: i32,
    pub protected_zone_longitude: i32,
    pub cen_dsrc_tolling_zone_id: Option<u32>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct ExteriorLights {
    pub low_beam_headlights_on: bool,
    pub high_beam_headlights_on: bool,
    pub left_turn_signal_on: bool,
    pub right_turn_signal_on: bool,
    pub daytime_running_lights_on: bool,
    pub reverse_light_on: bool,
    pub fog_light_on: bool,
    pub parking_lights_on: bool,
}

impl CooperativeAwarenessMessage {
    /// Creates a message from minimal required information
    ///
    /// This function is a helper to quickly create messages, the remaining information is defaulted
    /// If you want to fill more information, you can still edit the returned struct, or you can
    /// initialize the struct directly with more information
    ///
    /// **Note: All mobility arguments have to be using SI units**
    pub fn new(
        station_id: u32,
        station_type: u8,
        position: Position,
        speed: f64,
        heading: f64,
    ) -> Self {
        CooperativeAwarenessMessage {
            station_id,
            basic_container: BasicContainer {
                station_type,
                reference_position: ReferencePosition::from(position),
            },
            high_frequency_container: HighFrequencyContainer {
                basic_vehicle_container_high_frequency: Some(BasicVehicleContainerHighFrequency {
                    heading: Heading {
                        value: heading_to_etsi(heading),
                        ..Default::default()
                    },
                    speed: Speed {
                        value: speed_to_etsi(speed),
                        ..Default::default()
                    },
                    ..Default::default()
                }),
                ..Default::default()
            },
            ..Default::default()
        }
    }
}

impl Mobile for CooperativeAwarenessMessage {
    fn id(&self) -> u32 {
        self.station_id
    }

    fn position(&self) -> Position {
        self.basic_container.reference_position.as_position()
    }

    fn speed(&self) -> Option<f64> {
        self.high_frequency_container
            .basic_vehicle_container_high_frequency
            .as_ref()
            .map(|vehicle| speed_from_etsi(vehicle.speed.value))
    }

    fn heading(&self) -> Option<f64> {
        self.high_frequency_container
            .basic_vehicle_container_high_frequency
            .as_ref()
            .map(|vehicle| heading_from_etsi(vehicle.heading.value))
    }

    fn acceleration(&self) -> Option<f64> {
        self.high_frequency_container
            .basic_vehicle_container_high_frequency
            .as_ref()
            .map(|vehicle| acceleration_from_etsi(vehicle.longitudinal_acceleration.value))
    }
}

impl Content for CooperativeAwarenessMessage {
    fn get_type(&self) -> &str {
        "cam"
    }

    fn appropriate(&mut self, timestamp: u64, new_station_id: u32) {
        self.station_id = new_station_id;
        self.generation_delta_time = timestamp_to_generation_delta_time(timestamp);
    }

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError> {
        Ok(self)
    }

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError> {
        Err(NotAMortal(type_name::<CooperativeAwarenessMessage>()))
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::acceleration::Acceleration;
    use crate::exchange::etsi::cooperative_awareness_message::{
        BasicContainer, BasicVehicleContainerHighFrequency, CooperativeAwarenessMessage,
        HighFrequencyContainer, PerformanceClass,
    };
    use crate::exchange::etsi::curvature::{Curvature, CurvatureConfidence};
    use crate::exchange::etsi::heading::Heading;
    use crate::exchange::etsi::reference_position::{Altitude, ReferencePosition};
    use crate::exchange::etsi::speed::Speed;
    use crate::exchange::etsi::vehicle_length::{TrailerPresence, VehicleLength};
    use crate::exchange::etsi::yaw_rate::YawRate;
    use crate::mobility::mobile::Mobile;

    fn minimal_cam() -> &'static str {
        r#"{
            "protocol_version": 255,
            "station_id": 4294967295,
            "generation_delta_time": 65535,
            "basic_container": {
                "station_type": 255,
                "reference_position": {
                    "latitude": 900000001,
                    "longitude": 1800000001,
                     "altitude": {
                        "value": 800001,
                        "confidence": 15
                    },
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 0,
                        "semi_major_orientation": 3601
                    }                                        
                }
            },
            "high_frequency_container": {
                "basic_vehicle_container_high_frequency": {
                    "heading": {
                        "value": 3601,
                        "confidence": 127
                    },
                    "speed": {
                        "value": 16383,
                        "confidence": 127
                    },
                    "drive_direction": 2,
                    "vehicle_length": {
                        "value": 1023,
                        "confidence": 4
                    },
                    "vehicle_width": 62,
                    "longitudinal_acceleration": {
                        "value": 161,
                        "confidence": 102
                    },
                    "curvature": {
                        "value": 1023,
                        "confidence": 7
                    },
                    "curvature_calculation_mode": 2,
                    "yaw_rate": {
                        "value": 32767,
                        "confidence": 8
                    }                    
                }
           }
        }"#
    }

    fn standard_cam() -> &'static str {
        r#"{
            "protocol_version": 255,
            "station_id": 4294967295,
            "generation_delta_time": 65535,
            "basic_container": {
                "station_type": 255,
                "reference_position": {
                    "latitude": 900000001,
                    "longitude": 1800000001,
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
                    },
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 0,
                        "semi_major_orientation": 3601
                    }                                           
                }
            },
            "high_frequency_container": {
                "basic_vehicle_container_high_frequency": {
                    "heading": {
                        "value": 3601,
                        "confidence": 127
                    },
                    "speed": {
                        "value": 16383,
                        "confidence": 127
                    },
                    "drive_direction": 2,
                    "vehicle_length": {
                        "value": 1023,
                        "confidence": 4
                    },
                    "vehicle_width": 62,
                    "longitudinal_acceleration": {
                        "value": 161,
                        "confidence": 102
                    },
                    "curvature": {
                        "value": 1023,
                        "confidence": 7
                    },
                    "curvature_calculation_mode": 2,
                    "yaw_rate": {
                        "value": 32767,
                        "confidence": 8
                    },
                    "acceleration_control": {             
                        "brake_pedal_engaged": true,
                        "gas_pedal_engaged": true,
                        "emergency_brake_engaged": true,
                        "collision_warning_engaged": true,
                        "acc_engaged": true,
                        "cruise_control_engaged": true,
                        "speed_limiter_engaged": true
                    },
                    "lateral_acceleration": {
                        "value": -160,
                        "confidence": 0
                    }
                }
           },
            "low_frequency_container": {
                "basic_vehicle_container_low_frequency": {
                    "vehicle_role": 15,
                    "exterior_lights": {
                        "low_beam_headlights_on": true,
                        "high_beam_headlights_on": false,
                        "left_turn_signal_on": true,
                        "right_turn_signal_on": false,
                        "daytime_running_lights_on": true,
                        "reverse_light_on": false,
                        "fog_light_on": false,
                        "parking_lights_on": true
                    },
                    "path_history": []
                }
            }
        }"#
    }

    fn full_cam() -> &'static str {
        r#"{
            "protocol_version": 255,
            "station_id": 4294967295,
            "generation_delta_time": 65535,
            "basic_container": {
                "station_type": 255,
                "reference_position": {
                    "latitude": 900000001,
                    "longitude": 1800000001,
                    "altitude": {
                        "value": 800001,
                        "confidence": 15
                    },
                    "position_confidence_ellipse": {
                        "semi_major": 4095,
                        "semi_minor": 0,
                        "semi_major_orientation": 3601
                    }                                           
                }
            },
            "high_frequency_container": {
                "basic_vehicle_container_high_frequency": {
                    "heading": {
                        "value": 3601,
                        "confidence": 127
                    },
                    "speed": {
                        "value": 16383,
                        "confidence": 127
                    },
                    "drive_direction": 2,
                    "vehicle_length": {
                        "value": 1023,
                        "confidence": 4
                    },
                    "vehicle_width": 62,
                    "longitudinal_acceleration": {
                        "value": 161,
                        "confidence": 102
                    },
                    "curvature": {
                        "value": 1023,
                        "confidence": 7
                    },
                    "curvature_calculation_mode": 2,
                    "yaw_rate": {
                        "value": 32767,
                        "confidence": 8
                    },
                    "acceleration_control": {             
                        "brake_pedal_engaged": true,
                        "gas_pedal_engaged": true,
                        "emergency_brake_engaged": true,
                        "collision_warning_engaged": true,
                        "acc_engaged": true,
                        "cruise_control_engaged": true,
                        "speed_limiter_engaged": true
                    },
                    "lane_position": 14,
                    "steering_wheel_angle": {
                        "value": 512,
                        "confidence": 127
                    },
                    "lateral_acceleration": {
                        "value": -160,
                        "confidence": 0
                    },
                    "vertical_acceleration": {
                        "value": 0,
                        "confidence": 63
                    },
                    "performance_class": 2,
                    "cen_dsrc_tolling_zone": {
                        "protected_zone_latitude": 900000001,
                        "protected_zone_longitude": 1800000001,
                        "cen_dsrc_tolling_zone_id": 134217727
                    }
                }
           },
            "low_frequency_container": {
                "basic_vehicle_container_low_frequency": {
                    "vehicle_role": 15,
                    "exterior_lights": {
                        "low_beam_headlights_on": true,
                        "high_beam_headlights_on": false,
                        "left_turn_signal_on": true,
                        "right_turn_signal_on": false,
                        "daytime_running_lights_on": true,
                        "reverse_light_on": false,
                        "fog_light_on": false,
                        "parking_lights_on": true
                    },
                    "path_history": [
                        {
                            "path_position": {
                                "delta_latitude": 131072,
                                "delta_longitude": -131071,
                                "delta_altitude": 12800
                            },
                            "path_delta_time": 65535
                        },
                        {
                            "path_position": {
                                "delta_latitude": -131071,
                                "delta_longitude": 131072,
                                "delta_altitude": -12700
                            },
                            "path_delta_time": 1
                        }
                    ]
                }
            },
            "special_vehicle_container": {
                "public_transport_container": {
                    "embarkation_status": true,
                    "pt_activation": {
                        "pt_activation_type": 255,
                        "pt_activation_data": "abcdefghijklmnopqrst"
                    }
                }
            }
        }"#
    }

    #[test]
    fn test_deserialize_minimal_cam() {
        let data = minimal_cam();

        let cam = serde_json::from_str::<CooperativeAwarenessMessage>(data).unwrap();
        assert_eq!(cam.protocol_version, 255);
        assert_eq!(cam.station_id, 4294967295);
        assert_eq!(cam.generation_delta_time, 65535);
        assert_eq!(cam.basic_container.station_type, 255);
        assert_eq!(cam.basic_container.reference_position.latitude, 900000001);
        assert_eq!(cam.basic_container.reference_position.longitude, 1800000001);
        assert_eq!(
            cam.basic_container.reference_position.altitude.value,
            800001
        );
        assert_eq!(
            cam.basic_container.reference_position.altitude.confidence,
            15
        );
        let basic_vehicle_container_high_frequency = cam
            .high_frequency_container
            .basic_vehicle_container_high_frequency
            .as_ref()
            .unwrap();
        assert_eq!(basic_vehicle_container_high_frequency.heading.value, 3601);
        assert_eq!(
            basic_vehicle_container_high_frequency.heading.confidence,
            127
        );
        assert_eq!(basic_vehicle_container_high_frequency.speed.value, 16383);
        assert_eq!(basic_vehicle_container_high_frequency.speed.confidence, 127);
        assert_eq!(basic_vehicle_container_high_frequency.drive_direction, 2);
        assert_eq!(
            basic_vehicle_container_high_frequency.vehicle_length.value,
            1023
        );
        assert_eq!(
            basic_vehicle_container_high_frequency
                .vehicle_length
                .confidence,
            TrailerPresence::Unavailable
        );
        assert_eq!(basic_vehicle_container_high_frequency.vehicle_width, 62);
        assert_eq!(
            basic_vehicle_container_high_frequency
                .longitudinal_acceleration
                .value,
            161
        );
        assert_eq!(
            basic_vehicle_container_high_frequency
                .longitudinal_acceleration
                .confidence,
            102
        );
        assert_eq!(basic_vehicle_container_high_frequency.curvature.value, 1023);
        assert_eq!(
            basic_vehicle_container_high_frequency.curvature.confidence,
            CurvatureConfidence::Unavailable
        );
        assert_eq!(
            basic_vehicle_container_high_frequency.curvature_calculation_mode,
            2
        );
        assert_eq!(basic_vehicle_container_high_frequency.yaw_rate.value, 32767);
        assert_eq!(
            basic_vehicle_container_high_frequency.yaw_rate.confidence,
            8
        );
    }

    #[test]
    fn test_deserialize_standard_cam() {
        let data = standard_cam();

        let cam = serde_json::from_str::<CooperativeAwarenessMessage>(data).unwrap();
        assert_eq!(cam.protocol_version, 255);
        assert_eq!(cam.station_id, 4294967295);
        assert_eq!(cam.generation_delta_time, 65535);
        assert_eq!(cam.basic_container.station_type, 255);
        assert_eq!(cam.basic_container.reference_position.latitude, 900000001);
        assert_eq!(cam.basic_container.reference_position.longitude, 1800000001);
        assert_eq!(
            cam.basic_container.reference_position.altitude.value,
            800001
        );
        assert_eq!(
            cam.basic_container.reference_position.altitude.confidence,
            15
        );
        let basic_vehicle_container_high_frequency = cam
            .high_frequency_container
            .basic_vehicle_container_high_frequency
            .as_ref()
            .unwrap();
        assert_eq!(basic_vehicle_container_high_frequency.heading.value, 3601);
        assert_eq!(
            basic_vehicle_container_high_frequency.heading.confidence,
            127
        );
        assert_eq!(basic_vehicle_container_high_frequency.speed.value, 16383);
        assert_eq!(basic_vehicle_container_high_frequency.speed.confidence, 127);
        assert_eq!(basic_vehicle_container_high_frequency.drive_direction, 2);
        assert_eq!(
            basic_vehicle_container_high_frequency.vehicle_length.value,
            1023
        );
        assert_eq!(
            basic_vehicle_container_high_frequency
                .vehicle_length
                .confidence,
            TrailerPresence::Unavailable
        );
        assert_eq!(basic_vehicle_container_high_frequency.vehicle_width, 62);
        assert_eq!(
            basic_vehicle_container_high_frequency
                .longitudinal_acceleration
                .value,
            161
        );
        assert_eq!(
            basic_vehicle_container_high_frequency
                .longitudinal_acceleration
                .confidence,
            102
        );
        assert_eq!(basic_vehicle_container_high_frequency.curvature.value, 1023);
        assert_eq!(
            basic_vehicle_container_high_frequency.curvature.confidence,
            CurvatureConfidence::Unavailable
        );
        assert_eq!(
            basic_vehicle_container_high_frequency.curvature_calculation_mode,
            2
        );
        assert_eq!(basic_vehicle_container_high_frequency.yaw_rate.value, 32767);
        assert_eq!(
            basic_vehicle_container_high_frequency.yaw_rate.confidence,
            8
        );
        assert!(
            basic_vehicle_container_high_frequency
                .acceleration_control
                .is_some()
        );
        let acceleration_control = basic_vehicle_container_high_frequency
            .acceleration_control
            .as_ref()
            .unwrap();
        assert!(acceleration_control.brake_pedal_engaged);
        assert!(acceleration_control.gas_pedal_engaged);
        assert!(acceleration_control.emergency_brake_engaged);
        assert!(acceleration_control.collision_warning_engaged);
        assert!(acceleration_control.acc_engaged);
        assert!(acceleration_control.cruise_control_engaged);
        assert!(acceleration_control.speed_limiter_engaged);
        assert!(
            basic_vehicle_container_high_frequency
                .lateral_acceleration
                .is_some()
        );
        let lateral_acceleration = basic_vehicle_container_high_frequency
            .lateral_acceleration
            .as_ref()
            .unwrap();
        assert_eq!(lateral_acceleration.value, -160);
        assert_eq!(lateral_acceleration.confidence, 0);
        assert!(cam.low_frequency_container.is_some());
        let basic_vehicle_container_low_frequency = cam
            .low_frequency_container
            .as_ref()
            .unwrap()
            .basic_vehicle_container_low_frequency
            .as_ref()
            .unwrap();
        assert_eq!(basic_vehicle_container_low_frequency.vehicle_role, 15);
        assert!(
            basic_vehicle_container_low_frequency
                .exterior_lights
                .low_beam_headlights_on
        );
        assert!(
            !basic_vehicle_container_low_frequency
                .exterior_lights
                .high_beam_headlights_on
        );
        assert!(
            basic_vehicle_container_low_frequency
                .exterior_lights
                .left_turn_signal_on
        );
        assert!(
            !basic_vehicle_container_low_frequency
                .exterior_lights
                .right_turn_signal_on
        );
        assert!(
            basic_vehicle_container_low_frequency
                .exterior_lights
                .daytime_running_lights_on
        );
        assert!(
            !basic_vehicle_container_low_frequency
                .exterior_lights
                .reverse_light_on
        );
        assert!(
            !basic_vehicle_container_low_frequency
                .exterior_lights
                .fog_light_on
        );
        assert!(
            basic_vehicle_container_low_frequency
                .exterior_lights
                .parking_lights_on
        );
    }

    #[test]
    fn test_deserialize_full_cam() {
        let data = full_cam();

        let cam = serde_json::from_str::<CooperativeAwarenessMessage>(data).unwrap();
        assert_eq!(cam.protocol_version, 255);
        assert_eq!(cam.station_id, 4294967295);
        assert_eq!(cam.generation_delta_time, 65535);
        assert_eq!(cam.basic_container.station_type, 255);
        assert_eq!(cam.basic_container.reference_position.latitude, 900000001);
        assert_eq!(cam.basic_container.reference_position.longitude, 1800000001);
        assert_eq!(
            cam.basic_container.reference_position.altitude.value,
            800001
        );
        assert_eq!(
            cam.basic_container.reference_position.altitude.confidence,
            15
        );
        let basic_vehicle_container_high_frequency = cam
            .high_frequency_container
            .basic_vehicle_container_high_frequency
            .as_ref()
            .unwrap();
        assert_eq!(basic_vehicle_container_high_frequency.heading.value, 3601);
        assert_eq!(
            basic_vehicle_container_high_frequency.heading.confidence,
            127
        );
        assert_eq!(basic_vehicle_container_high_frequency.speed.value, 16383);
        assert_eq!(basic_vehicle_container_high_frequency.speed.confidence, 127);
        assert_eq!(basic_vehicle_container_high_frequency.drive_direction, 2);
        assert_eq!(
            basic_vehicle_container_high_frequency.vehicle_length.value,
            1023
        );
        assert_eq!(
            basic_vehicle_container_high_frequency
                .vehicle_length
                .confidence,
            TrailerPresence::Unavailable
        );
        assert_eq!(basic_vehicle_container_high_frequency.vehicle_width, 62);
        assert_eq!(
            basic_vehicle_container_high_frequency
                .longitudinal_acceleration
                .value,
            161
        );
        assert_eq!(
            basic_vehicle_container_high_frequency
                .longitudinal_acceleration
                .confidence,
            102
        );
        assert_eq!(basic_vehicle_container_high_frequency.curvature.value, 1023);
        assert_eq!(
            basic_vehicle_container_high_frequency.curvature.confidence,
            CurvatureConfidence::Unavailable
        );
        assert_eq!(
            basic_vehicle_container_high_frequency.curvature_calculation_mode,
            2
        );
        assert_eq!(basic_vehicle_container_high_frequency.yaw_rate.value, 32767);
        assert_eq!(
            basic_vehicle_container_high_frequency.yaw_rate.confidence,
            8
        );
        assert!(
            basic_vehicle_container_high_frequency
                .acceleration_control
                .is_some()
        );
        let acceleration_control = basic_vehicle_container_high_frequency
            .acceleration_control
            .as_ref()
            .unwrap();
        assert!(acceleration_control.brake_pedal_engaged);
        assert!(acceleration_control.gas_pedal_engaged);
        assert!(acceleration_control.emergency_brake_engaged);
        assert!(acceleration_control.collision_warning_engaged);
        assert!(acceleration_control.acc_engaged);
        assert!(acceleration_control.cruise_control_engaged);
        assert!(acceleration_control.speed_limiter_engaged);
        assert!(
            basic_vehicle_container_high_frequency
                .lane_position
                .is_some()
        );
        let lane_position = basic_vehicle_container_high_frequency
            .lane_position
            .unwrap();
        assert_eq!(lane_position, 14);
        assert!(
            basic_vehicle_container_high_frequency
                .steering_wheel_angle
                .is_some()
        );
        let steering_wheel_angle = basic_vehicle_container_high_frequency
            .steering_wheel_angle
            .as_ref()
            .unwrap();
        assert_eq!(steering_wheel_angle.value, 512);
        assert_eq!(steering_wheel_angle.confidence, 127);
        assert!(
            basic_vehicle_container_high_frequency
                .lateral_acceleration
                .is_some()
        );
        let lateral_acceleration = basic_vehicle_container_high_frequency
            .lateral_acceleration
            .as_ref()
            .unwrap();
        assert_eq!(lateral_acceleration.value, -160);
        assert_eq!(lateral_acceleration.confidence, 0);
        assert!(
            basic_vehicle_container_high_frequency
                .vertical_acceleration
                .is_some()
        );
        let vertical_acceleration = basic_vehicle_container_high_frequency
            .vertical_acceleration
            .as_ref()
            .unwrap();
        assert_eq!(vertical_acceleration.value, 0);
        assert_eq!(vertical_acceleration.confidence, 63);
        assert!(
            basic_vehicle_container_high_frequency
                .performance_class
                .is_some()
        );
        let performance_class = basic_vehicle_container_high_frequency
            .performance_class
            .as_ref()
            .unwrap();
        assert_eq!(*performance_class, PerformanceClass::B);
        assert!(
            basic_vehicle_container_high_frequency
                .cen_dsrc_tolling_zone
                .is_some()
        );
        let cen_dsrc_tolling_zone = basic_vehicle_container_high_frequency
            .cen_dsrc_tolling_zone
            .as_ref()
            .unwrap();
        assert_eq!(cen_dsrc_tolling_zone.protected_zone_latitude, 900000001);
        assert_eq!(cen_dsrc_tolling_zone.protected_zone_longitude, 1800000001);
        assert_eq!(
            cen_dsrc_tolling_zone.cen_dsrc_tolling_zone_id,
            Some(134217727)
        );
        assert!(cam.low_frequency_container.is_some());
        let basic_vehicle_container_low_frequency = cam
            .low_frequency_container
            .as_ref()
            .unwrap()
            .basic_vehicle_container_low_frequency
            .as_ref()
            .unwrap();
        assert_eq!(basic_vehicle_container_low_frequency.vehicle_role, 15);
        assert!(
            basic_vehicle_container_low_frequency
                .exterior_lights
                .low_beam_headlights_on
        );
        assert!(
            !basic_vehicle_container_low_frequency
                .exterior_lights
                .high_beam_headlights_on
        );
        assert!(
            basic_vehicle_container_low_frequency
                .exterior_lights
                .left_turn_signal_on
        );
        assert!(
            !basic_vehicle_container_low_frequency
                .exterior_lights
                .right_turn_signal_on
        );
        assert!(
            basic_vehicle_container_low_frequency
                .exterior_lights
                .daytime_running_lights_on
        );
        assert!(
            !basic_vehicle_container_low_frequency
                .exterior_lights
                .reverse_light_on
        );
        assert!(
            !basic_vehicle_container_low_frequency
                .exterior_lights
                .fog_light_on
        );
        assert!(
            basic_vehicle_container_low_frequency
                .exterior_lights
                .parking_lights_on
        );
        assert_eq!(basic_vehicle_container_low_frequency.path_history.len(), 2);
        let path_point_1 = &basic_vehicle_container_low_frequency.path_history[0];
        assert_eq!(path_point_1.path_position.delta_latitude, 131072);
        assert_eq!(path_point_1.path_position.delta_longitude, -131071);
        assert_eq!(path_point_1.path_position.delta_altitude, 12800);
        assert!(path_point_1.path_delta_time.is_some());
        assert_eq!(path_point_1.path_delta_time.unwrap(), 65535);
        let path_point_2 = &basic_vehicle_container_low_frequency.path_history[1];
        assert_eq!(path_point_2.path_position.delta_latitude, -131071);
        assert_eq!(path_point_2.path_position.delta_longitude, 131072);
        assert_eq!(path_point_2.path_position.delta_altitude, -12700);
        assert!(path_point_2.path_delta_time.is_some());
        assert_eq!(path_point_2.path_delta_time.unwrap(), 1);
        assert!(cam.special_vehicle_container.is_some());
        let special_vehicle_container = cam.special_vehicle_container.as_ref().unwrap();
        assert!(
            special_vehicle_container
                .public_transport_container
                .is_some()
        );
        let public_transport_container = special_vehicle_container
            .public_transport_container
            .as_ref()
            .unwrap();
        assert!(public_transport_container.embarkation_status);
        assert!(public_transport_container.pt_activation.is_some());
        let pt_activation = public_transport_container.pt_activation.as_ref().unwrap();
        assert_eq!(pt_activation.pt_activation_type, 255);
        assert_eq!(pt_activation.pt_activation_data, "abcdefghijklmnopqrst");
    }

    #[test]
    fn test_reserialize_minimal_cam() {
        let data = minimal_cam();

        let cam = serde_json::from_str::<CooperativeAwarenessMessage>(data).unwrap();
        let serialized = serde_json::to_string(&cam).unwrap();
        assert_eq!(
            serialized,
            data.replace("\n", "").replace(" ", "").replace("\t", "")
        );
    }

    #[test]
    fn test_reserialize_standard_cam() {
        let data = standard_cam();

        let cam = serde_json::from_str::<CooperativeAwarenessMessage>(data).unwrap();
        let serialized = serde_json::to_string(&cam).unwrap();
        assert_eq!(
            serialized,
            data.replace("\n", "").replace(" ", "").replace("\t", "")
        );
    }

    #[test]
    fn test_reserialize_full_cam() {
        let data = full_cam();

        let cam = serde_json::from_str::<CooperativeAwarenessMessage>(data).unwrap();
        let serialized = serde_json::to_string(&cam).unwrap();
        assert_eq!(
            serialized,
            data.replace("\n", "").replace(" ", "").replace("\t", "")
        );
    }

    #[test]
    fn test_cooperative_awareness_message_as_position() {
        let reference_position = ReferencePosition {
            latitude: 123456789,
            longitude: 987654321,
            altitude: Altitude {
                value: 1000,
                confidence: Default::default(),
            },
            position_confidence_ellipse: Default::default(),
        };
        let cam = CooperativeAwarenessMessage {
            protocol_version: 1,
            station_id: 12345,
            generation_delta_time: 100,
            basic_container: BasicContainer {
                station_type: 1,
                reference_position,
            },
            high_frequency_container: HighFrequencyContainer {
                basic_vehicle_container_high_frequency: Some(BasicVehicleContainerHighFrequency {
                    heading: Heading {
                        value: 1800,
                        confidence: Default::default(),
                    },
                    speed: Speed {
                        value: 500,
                        confidence: Default::default(),
                    },
                    drive_direction: 0,
                    vehicle_length: VehicleLength {
                        value: 500,
                        confidence: Default::default(),
                    },
                    vehicle_width: 200,
                    longitudinal_acceleration: Acceleration {
                        value: 100,
                        confidence: Default::default(),
                    },
                    curvature: Curvature {
                        value: 50,
                        confidence: Default::default(),
                    },
                    curvature_calculation_mode: 0,
                    yaw_rate: YawRate {
                        value: 10,
                        confidence: Default::default(),
                    },
                    acceleration_control: None,
                    lane_position: None,
                    steering_wheel_angle: None,
                    lateral_acceleration: None,
                    vertical_acceleration: None,
                    performance_class: None,
                    cen_dsrc_tolling_zone: None,
                }),
                rsu_container_high_frequency: None,
            },
            low_frequency_container: None,
            special_vehicle_container: None,
        };

        let position = cam.position();
        assert_eq!(position.latitude, 12.3456789_f64.to_radians());
        assert_eq!(position.longitude, 98.7654321_f64.to_radians());
        assert_eq!(position.altitude, 10.00);
    }

    #[test]
    fn test_cam_defaults() {
        let cam = CooperativeAwarenessMessage::default();
        assert_eq!(cam.protocol_version, 0);
        assert_eq!(cam.station_id, 0);
        assert_eq!(cam.generation_delta_time, 0);
        assert!(cam.low_frequency_container.is_none());
        assert!(cam.special_vehicle_container.is_none());
    }
}
