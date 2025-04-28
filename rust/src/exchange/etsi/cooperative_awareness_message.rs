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
/// It implements the schema defined in the CAM file version 2.2.0.
///
/// # Fields
///
/// - `protocol_version`: The version of the protocol used
/// - `station_id`: Unique identifier for the station sending the message
/// - `generation_delta_time`: The time difference since the last generation of the message
/// - `basic_container`: Contains basic information about the station
/// - `high_frequency_container`: Contains high frequency data such as speed, heading, and acceleration
/// - `low_frequency_container`: Container for low frequency data such as vehicle role and path history
/// - `special_vehicle_container`: Container for special vehicle data
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CooperativeAwarenessMessage {
    pub protocol_version: u8,
    pub station_id: u32,
    pub generation_delta_time: u16,
    pub basic_container: BasicContainer,
    pub high_frequency_container: HighFrequencyContainer,

    pub low_frequency_container: Option<LowFrequencyContainer>,
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
    // TODO: implement the container
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
    /// Creates a [CAM][1] message from minimal required information
    ///
    /// This function is a helper to quickly create messages, the remaining information is defaulted
    /// If you want to fill more information, you can still edit the returned struct, or you can
    /// initialize the struct directly with more information
    ///
    /// **Note: All mobility arguments have to be using SI units**
    ///
    /// [1]: CooperativeAwarenessMessage
    fn new(
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
                ..Default::default()
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
        if let Some(vehicle) = &self
            .high_frequency_container
            .basic_vehicle_container_high_frequency
        {
            Some(speed_from_etsi(vehicle.speed.value))
        } else {
            None
        }
    }

    fn heading(&self) -> Option<f64> {
        if let Some(vehicle) = &self
            .high_frequency_container
            .basic_vehicle_container_high_frequency
        {
            Some(heading_from_etsi(vehicle.heading.value))
        } else {
            None
        }
    }

    fn acceleration(&self) -> Option<f64> {
        if let Some(vehicle) = &self
            .high_frequency_container
            .basic_vehicle_container_high_frequency
        {
            Some(acceleration_from_etsi(
                vehicle.longitudinal_acceleration.value,
            ))
        } else {
            None
        }
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
        HighFrequencyContainer,
    };
    use crate::exchange::etsi::curvature::Curvature;
    use crate::exchange::etsi::heading::Heading;
    use crate::exchange::etsi::reference_position::{Altitude, ReferencePosition};
    use crate::exchange::etsi::speed::Speed;
    use crate::exchange::etsi::vehicle_length::VehicleLength;
    use crate::exchange::etsi::yaw_rate::YawRate;
    use crate::mobility::mobile::Mobile;

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
        assert_eq!(position.latitude, 1.23456789);
        assert_eq!(position.longitude, 9.87654321);
        assert_eq!(position.altitude, 100.0);
    }
}
