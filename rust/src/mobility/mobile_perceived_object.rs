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

extern crate integer_sqrt;

use self::integer_sqrt::IntegerSquareRoot;
use crate::exchange::etsi::collective_perception_message::CollectivePerceptionMessage;
use crate::exchange::etsi::perceived_object::{CartesianVelocity, PerceivedObject};
use crate::exchange::etsi::speed_from_etsi;
use crate::mobility::mobile::Mobile;
use crate::mobility::position::{Position, enu_destination, haversine_destination};
use rand::Rng;
use std::f64::consts::PI;
use std::fmt::{Debug, Display, Formatter};
use std::hash::{Hash, Hasher};

const PI2: f64 = 2. * PI;

#[derive(Clone, Debug)]
pub struct MobilePerceivedObject {
    pub perceived_object: PerceivedObject,
    pub mobile_id: MobilePerceivedObjectId,
    pub position: Position,
    pub speed: f64,
    pub heading: f64,
    pub acceleration: f64,
}

#[derive(Clone, Debug)]
pub struct MobilePerceivedObjectId {
    pub station_id: u32,
    pub object_id: u16,
    pub object_id_rotation_count: u8,
}

impl MobilePerceivedObject {
    // TODO check how to keep it private and manage end to end CPMs with natively perceived object mobility
    pub(crate) fn new(
        cpm: &CollectivePerceptionMessage,
        perceived_object: PerceivedObject,
        object_id_rotation_count: Option<u8>,
    ) -> Self {
        let object_id = perceived_object
            .object_id
            .unwrap_or_else(|| rand::rng().random_range(0..=u16::MAX));
        let cartesian_velocity = &perceived_object
            .velocity
            .as_ref()
            .and_then(|v| v.cartesian_velocity.as_ref())
            .map_or(CartesianVelocity::default(), |v| v.clone());
        let speed = speed_from_yaw_angle(
            cartesian_velocity.x_velocity.value,
            cartesian_velocity.y_velocity.value,
        );
        let (position, heading) = match &cpm.originating_vehicle_container {
            Some(_) => {
                let cpm_heading = cpm.heading().unwrap_or_default();
                // If the CPM has an originating vehicle container, we compute the heading
                // from the perceived object and the vehicle's heading.
                (
                    compute_position_from_mobile(
                        perceived_object.position.x_coordinate.value,
                        perceived_object.position.y_coordinate.value,
                        &cpm.position(),
                        cpm_heading,
                    ),
                    compute_heading_from_mobile(
                        cartesian_velocity.x_velocity.value,
                        cartesian_velocity.y_velocity.value,
                        cpm_heading,
                    ),
                )
            }
            None => {
                // If there is no originating vehicle container, we compute from the RSU
                (
                    enu_destination(
                        &cpm.position(),
                        perceived_object.position.x_coordinate.value as f64 / 100.,
                        perceived_object.position.y_coordinate.value as f64 / 100.,
                        perceived_object
                            .position
                            .z_coordinate
                            .unwrap_or_default()
                            .value as f64
                            / 100.,
                    ),
                    compute_heading_from_rsu(
                        cartesian_velocity.x_velocity.value,
                        cartesian_velocity.y_velocity.value,
                    ),
                )
            }
        };
        // FIXME compute acceleration from perceived object velocity. See https://github.com/Orange-OpenSource/its-client/issues/416
        let acceleration = 0.0;

        Self {
            perceived_object,
            mobile_id: MobilePerceivedObjectId {
                station_id: cpm.station_id,
                object_id,
                object_id_rotation_count: object_id_rotation_count.unwrap_or(0),
            },
            position,
            speed,
            heading,
            acceleration,
        }
    }
}

impl Mobile for MobilePerceivedObject {
    fn id(&self) -> u32 {
        self.mobile_id.station_id
    }

    fn position(&self) -> Position {
        self.position
    }

    fn speed(&self) -> Option<f64> {
        Some(self.speed)
    }

    fn heading(&self) -> Option<f64> {
        Some(self.heading)
    }

    fn acceleration(&self) -> Option<f64> {
        Some(self.acceleration)
    }
}

impl PartialEq for MobilePerceivedObject {
    fn eq(&self, other: &Self) -> bool {
        self.perceived_object == other.perceived_object
            && self.mobile_id == other.mobile_id
            && self.position == other.position
            && self.heading == other.heading
            && self.speed == other.speed
    }
}

impl Hash for MobilePerceivedObject {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.perceived_object.hash(state);
        self.mobile_id.hash(state);
        self.position.hash(state);
        ((self.heading * 100.).round() as i32).hash(state);
        ((self.speed * 100.).round() as i32).hash(state);
    }
}

impl Display for MobilePerceivedObjectId {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{}{:05}{:03}",
            self.station_id, self.object_id, self.object_id_rotation_count
        )
    }
}

impl From<MobilePerceivedObjectId> for u64 {
    fn from(mobile_perceived_object_id: MobilePerceivedObjectId) -> Self {
        format!(
            "{}{:05}{:03}",
            mobile_perceived_object_id.station_id,
            mobile_perceived_object_id.object_id,
            mobile_perceived_object_id.object_id_rotation_count
        )
        .parse()
        .unwrap_or(0)
    }
}

impl From<u64> for MobilePerceivedObjectId {
    fn from(id: u64) -> Self {
        let station_id = (id / 100000000) as u32;
        let object_id = ((id % 100000000) / 1000) as u16;
        let object_id_rotation_count = (id % 1000) as u8;

        MobilePerceivedObjectId {
            station_id,
            object_id,
            object_id_rotation_count,
        }
    }
}

impl PartialEq for MobilePerceivedObjectId {
    fn eq(&self, other: &Self) -> bool {
        self.station_id == other.station_id
            && self.object_id == other.object_id
            && self.object_id_rotation_count == other.object_id_rotation_count
    }
}

impl Hash for MobilePerceivedObjectId {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.station_id.hash(state);
        self.object_id.hash(state);
        self.object_id_rotation_count.hash(state);
    }
}

fn compute_position_from_mobile(
    x_distance: i32,
    y_distance: i32,
    position: &Position,
    heading: f64,
) -> Position {
    let x_offset_meters = x_distance as f64 / 100.0;
    let y_offset_meters = y_distance as f64 / 100.0;

    let intermediate = haversine_destination(position, heading, x_offset_meters);
    haversine_destination(
        &intermediate,
        (heading - PI / 2. + 2. * PI) % (2. * PI),
        y_offset_meters,
    )
}

fn compute_heading_from_mobile(
    x_velocity_value: i16,
    y_velocity_value: i16,
    cpm_heading: f64,
) -> f64 {
    (cpm_heading + compute_heading_from_rsu(x_velocity_value, y_velocity_value)) % PI2
}

pub fn speed_from_yaw_angle(x_speed: i16, y_speed: i16) -> f64 {
    let _s = (x_speed.unsigned_abs() as u32).pow(2) + (y_speed.unsigned_abs() as u32).pow(2);
    speed_from_etsi(
        ((x_speed.unsigned_abs() as u32).pow(2) + (y_speed.unsigned_abs() as u32).pow(2))
            .integer_sqrt() as u16,
    )
}

/// Computes the heading of the perceived object based on its `x` and `y` speed
/// where `x` is West <-> East axis (negative `x` is West, positive `x` is East)
/// and `y` is North <-> South axis (negative `y` is South, positive y is North)
///
/// References:
/// - https://www.omnicalculator.com/math/vector-direction
/// - https://support.nortekgroup.com/hc/en-us/articles/360012774640-How-do-I-calculate-current-speed-and-direction-from-three-beam-ADCP-velocity-components-
///
fn compute_heading_from_rsu(x_velocity_value: i16, y_velocity_value: i16) -> f64 {
    PI + -(x_velocity_value as f64).atan2(-(y_velocity_value as f64))
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::angle::Angle;
    use crate::exchange::etsi::collective_perception_message::{
        CollectivePerceptionMessage, ManagementContainer, OriginatingVehicleContainer,
    };
    use crate::exchange::etsi::coordinate::CartesianCoordinate;
    use crate::exchange::etsi::perceived_object::{
        CartesianPosition3DWithConfidence, CartesianVelocity, PerceivedObject,
        Velocity3dWithConfidence,
    };
    use crate::exchange::etsi::reference_position::{
        Altitude, ReferencePosition, altitude_from_etsi, coordinate_from_etsi,
    };
    use crate::exchange::etsi::velocity::Velocity;
    use crate::exchange::etsi::{heading_from_etsi, speed_from_etsi};
    use crate::mobility::mobile_perceived_object::{
        MobilePerceivedObject, compute_heading_from_rsu, compute_position_from_mobile,
    };
    use crate::mobility::mobile_perceived_object::{
        MobilePerceivedObjectId, compute_heading_from_mobile,
    };
    use crate::mobility::position::Position;
    use std::f64::consts::PI;

    macro_rules! po {
        ($x_velocity:expr, $y_velocity:expr) => {
            PerceivedObject {
                measurement_delta_time: 2047,
                position: CartesianPosition3DWithConfidence {
                    x_coordinate: CartesianCoordinate {
                        value: 131071,
                        confidence: 4096,
                    },
                    y_coordinate: CartesianCoordinate {
                        value: -131072,
                        confidence: 1,
                    },
                    ..Default::default()
                },
                velocity: Some(Velocity3dWithConfidence {
                    cartesian_velocity: Some(CartesianVelocity {
                        x_velocity: Velocity {
                            value: $x_velocity,
                            confidence: 127,
                        },
                        y_velocity: Velocity {
                            value: $y_velocity,
                            confidence: 1,
                        },
                        ..Default::default()
                    }),
                    ..Default::default()
                }),
                ..Default::default()
            }
        };
    }

    macro_rules! test_compute_position_from_mobile {
        ($test_name:ident, $x:expr, $y:expr, $expected:expr) => {
            #[test]
            fn $test_name() {
                let position = compute_position_from_mobile(
                    $x,
                    $y,
                    &ReferencePosition {
                        latitude: 486251958,
                        longitude: 22415093,
                        altitude: Altitude {
                            value: 900,
                            ..Default::default()
                        },
                        ..Default::default()
                    }
                    .as_position(),
                    heading_from_etsi(900),
                );

                println!(
                    "Anchor: {}",
                    ReferencePosition {
                        latitude: 486251958,
                        longitude: 22415093,
                        altitude: Altitude {
                            value: 900,
                            ..Default::default()
                        },
                        ..Default::default()
                    }
                    .as_position()
                );
                println!("PO position: {}", position);

                assert!(
                    (position.latitude - $expected.latitude).abs() <= 1e-6,
                    "Latitude: {} (expected: {})",
                    position.latitude.to_degrees(),
                    $expected.latitude.to_degrees()
                );
                assert!(
                    (position.longitude - $expected.longitude).abs() <= 1e-6,
                    "Longitude: {} (expected: {})",
                    position.longitude.to_degrees(),
                    $expected.longitude.to_degrees()
                );
                assert!(
                    (position.altitude - $expected.altitude).abs() <= 1e-4,
                    "Altitude: {} (expected: {})",
                    position.altitude,
                    $expected.altitude
                );
            }
        };
    }

    test_compute_position_from_mobile!(
        x_distance_only_position_from_mobile,
        1800,
        0,
        Position {
            latitude: coordinate_from_etsi(486251958),
            longitude: coordinate_from_etsi(22417534),
            altitude: altitude_from_etsi(900),
        }
    );

    test_compute_position_from_mobile!(
        y_distance_only_position_from_mobile,
        0,
        700,
        Position {
            latitude: coordinate_from_etsi(486252587),
            longitude: coordinate_from_etsi(22415093),
            altitude: altitude_from_etsi(900),
        }
    );

    test_compute_position_from_mobile!(
        x_and_y_distance_position_from_mobile,
        1800,
        700,
        Position {
            latitude: coordinate_from_etsi(486252587),
            longitude: coordinate_from_etsi(22417535),
            altitude: altitude_from_etsi(900),
        }
    );

    #[test]
    fn it_can_display_correctly_an_id() {
        assert_eq!(
            MobilePerceivedObjectId {
                station_id: 0,
                object_id: 0,
                object_id_rotation_count: 0,
            }
            .to_string(),
            "000000000"
        );
        assert_eq!(
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 0,
                object_id_rotation_count: 0,
            }
            .to_string(),
            "100000000"
        );
        assert_eq!(
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 1,
                object_id_rotation_count: 0,
            }
            .to_string(),
            "100001000"
        );
        assert_eq!(
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 1,
                object_id_rotation_count: 1,
            }
            .to_string(),
            "100001001"
        );
        assert_eq!(
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 0,
                object_id_rotation_count: 1,
            }
            .to_string(),
            "100000001"
        );

        assert_eq!(
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 10000,
                object_id_rotation_count: 0,
            }
            .to_string(),
            "110000000"
        );
        assert_eq!(
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 10000,
                object_id_rotation_count: 100,
            }
            .to_string(),
            "110000100"
        );
        assert_eq!(
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 0,
                object_id_rotation_count: 100,
            }
            .to_string(),
            "100000100"
        );
        assert_eq!(
            MobilePerceivedObjectId {
                station_id: 4294967295,
                object_id: 65535,
                object_id_rotation_count: 255,
            }
            .to_string(),
            "429496729565535255"
        );
    }

    #[test]
    fn it_can_convert_correctly_an_id_from_u64() {
        assert_eq!(
            MobilePerceivedObjectId::from(0u64),
            MobilePerceivedObjectId {
                station_id: 0,
                object_id: 0,
                object_id_rotation_count: 0,
            }
        );
        assert_eq!(
            MobilePerceivedObjectId::from(100000000u64),
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 0,
                object_id_rotation_count: 0,
            }
        );
        assert_eq!(
            MobilePerceivedObjectId::from(100001000u64),
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 1,
                object_id_rotation_count: 0,
            }
        );
        assert_eq!(
            MobilePerceivedObjectId::from(100001001u64),
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 1,
                object_id_rotation_count: 1,
            }
        );
        assert_eq!(
            MobilePerceivedObjectId::from(100000001u64),
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 0,
                object_id_rotation_count: 1,
            }
        );
        assert_eq!(
            MobilePerceivedObjectId::from(110000000u64),
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 10000,
                object_id_rotation_count: 0,
            }
        );
        assert_eq!(
            MobilePerceivedObjectId::from(110000100u64),
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 10000,
                object_id_rotation_count: 100,
            }
        );
        assert_eq!(
            MobilePerceivedObjectId::from(100000100u64),
            MobilePerceivedObjectId {
                station_id: 1,
                object_id: 0,
                object_id_rotation_count: 100,
            }
        );
        assert_eq!(
            MobilePerceivedObjectId::from(429496729565535255u64),
            MobilePerceivedObjectId {
                station_id: 4294967295,
                object_id: 65535,
                object_id_rotation_count: 255,
            }
        );
    }

    #[test]
    fn it_can_convert_correctly_an_id_into_u64() {
        assert_eq!(
            u64::from(MobilePerceivedObjectId {
                station_id: 0,
                object_id: 0,
                object_id_rotation_count: 0,
            }),
            0u64
        );
        assert_eq!(
            u64::from(MobilePerceivedObjectId {
                station_id: 1,
                object_id: 0,
                object_id_rotation_count: 0,
            }),
            100000000u64
        );
        assert_eq!(
            u64::from(MobilePerceivedObjectId {
                station_id: 1,
                object_id: 1,
                object_id_rotation_count: 0,
            }),
            100001000u64
        );
        assert_eq!(
            u64::from(MobilePerceivedObjectId {
                station_id: 1,
                object_id: 1,
                object_id_rotation_count: 1,
            }),
            100001001u64
        );
        assert_eq!(
            u64::from(MobilePerceivedObjectId {
                station_id: 1,
                object_id: 0,
                object_id_rotation_count: 1,
            }),
            100000001u64
        );
        assert_eq!(
            u64::from(MobilePerceivedObjectId {
                station_id: 1,
                object_id: 10000,
                object_id_rotation_count: 0,
            }),
            110000000u64
        );
        assert_eq!(
            u64::from(MobilePerceivedObjectId {
                station_id: 1,
                object_id: 10000,
                object_id_rotation_count: 100,
            }),
            110000100u64
        );
        assert_eq!(
            u64::from(MobilePerceivedObjectId {
                station_id: 1,
                object_id: 0,
                object_id_rotation_count: 100,
            }),
            100000100u64
        );
        assert_eq!(
            u64::from(MobilePerceivedObjectId {
                station_id: 4294967295,
                object_id: 65535,
                object_id_rotation_count: 255,
            }),
            429496729565535255u64
        );
    }

    #[test]
    fn constructor_from_mobile() {
        let perceived_object = PerceivedObject {
            object_id: Some(101),
            measurement_delta_time: 2047,
            position: CartesianPosition3DWithConfidence {
                x_coordinate: CartesianCoordinate {
                    value: 50000,
                    confidence: 4096,
                },
                y_coordinate: CartesianCoordinate {
                    value: 10000,
                    confidence: 1,
                },
                ..Default::default()
            },
            ..Default::default()
        };
        let expected_mobile_perceived_object = MobilePerceivedObject {
            perceived_object: perceived_object.clone(),
            mobile_id: MobilePerceivedObjectId {
                station_id: 10,
                object_id: 101,
                object_id_rotation_count: 0,
            },
            position: Position {
                latitude: coordinate_from_etsi(434622516),
                longitude: coordinate_from_etsi(1218218),
                altitude: altitude_from_etsi(220000),
            },
            speed: 0.,
            heading: PI,
            acceleration: 0.,
        };

        let mobile_perceived_object = MobilePerceivedObject::new(
            &CollectivePerceptionMessage {
                station_id: 10,
                management_container: ManagementContainer {
                    reference_time: 4398046511103,
                    reference_position: ReferencePosition {
                        latitude: 434667520,
                        longitude: 1205862,
                        altitude: Altitude {
                            value: 220000,
                            ..Default::default()
                        },
                        ..Default::default()
                    },
                    ..Default::default()
                },
                originating_vehicle_container: Some(OriginatingVehicleContainer {
                    orientation_angle: Angle {
                        value: 1800,
                        ..Default::default()
                    },
                    ..Default::default()
                }),
                ..Default::default()
            },
            perceived_object,
            None,
        );

        assert_eq!(
            mobile_perceived_object.mobile_id,
            expected_mobile_perceived_object.mobile_id
        );
        assert_eq!(
            mobile_perceived_object.perceived_object,
            expected_mobile_perceived_object.perceived_object
        );
        assert!(
            (mobile_perceived_object.speed - expected_mobile_perceived_object.speed).abs() <= 1e-8
        );
        assert!(
            (mobile_perceived_object.heading - expected_mobile_perceived_object.heading).abs()
                <= 1e-8,
            "Actual: {} (expected: {})",
            mobile_perceived_object.heading,
            expected_mobile_perceived_object.heading
        );
        assert!(
            (mobile_perceived_object.acceleration - expected_mobile_perceived_object.acceleration)
                .abs()
                <= 1e-8
        );
    }

    #[test]
    fn constructor_from_rsu() {
        let perceived_object = PerceivedObject {
            object_id: Some(101),
            measurement_delta_time: 2047,
            position: CartesianPosition3DWithConfidence {
                x_coordinate: CartesianCoordinate {
                    value: 114,
                    confidence: 4096,
                },
                y_coordinate: CartesianCoordinate {
                    value: -2757,
                    confidence: 1,
                },
                ..Default::default()
            },
            velocity: Some(Velocity3dWithConfidence {
                cartesian_velocity: Some(CartesianVelocity {
                    x_velocity: Velocity {
                        value: 480,
                        confidence: 127,
                    },
                    y_velocity: Velocity {
                        value: -345,
                        confidence: 1,
                    },
                    z_velocity: Default::default(),
                }),
                ..Default::default()
            }),
            ..Default::default()
        };
        let expected_mobile_perceived_object = MobilePerceivedObject {
            perceived_object: perceived_object.clone(),
            mobile_id: MobilePerceivedObjectId {
                station_id: 10,
                object_id: 101,
                object_id_rotation_count: 0,
            },
            position: Position {
                latitude: coordinate_from_etsi(488415432),
                longitude: coordinate_from_etsi(23679076),
                altitude: altitude_from_etsi(900),
            },
            speed: speed_from_etsi(591),
            heading: heading_from_etsi(1257),
            acceleration: 0.0,
        };

        let mobile_perceived_object = MobilePerceivedObject::new(
            &CollectivePerceptionMessage {
                station_id: 10,
                management_container: ManagementContainer {
                    reference_time: 4398046511103,
                    reference_position: ReferencePosition {
                        latitude: 488417860,
                        longitude: 23678940,
                        altitude: Altitude {
                            value: 900,
                            ..Default::default()
                        },
                        ..Default::default()
                    },
                    ..Default::default()
                },
                ..Default::default()
            },
            perceived_object,
            None,
        );

        assert_eq!(
            mobile_perceived_object.mobile_id,
            expected_mobile_perceived_object.mobile_id
        );
        assert_eq!(
            mobile_perceived_object.perceived_object,
            expected_mobile_perceived_object.perceived_object
        );
        assert!(
            (mobile_perceived_object.position.latitude
                - expected_mobile_perceived_object.position.latitude)
                .abs()
                <= 1e-6,
            "Actual: {} (expected: {})",
            mobile_perceived_object.position.latitude,
            expected_mobile_perceived_object.position.latitude
        );
        assert!(
            (mobile_perceived_object.position.longitude
                - expected_mobile_perceived_object.position.longitude)
                .abs()
                <= 1e-6,
            "Actual: {} (expected: {})",
            mobile_perceived_object.position.longitude,
            expected_mobile_perceived_object.position.longitude
        );
        assert!(
            (mobile_perceived_object.position.altitude
                - expected_mobile_perceived_object.position.altitude)
                .abs()
                <= 1e-4,
            "Actual: {} (expected: {})",
            mobile_perceived_object.position.altitude,
            expected_mobile_perceived_object.position.altitude
        );
        assert!(
            (mobile_perceived_object.speed - expected_mobile_perceived_object.speed).abs() <= 1e-8,
            "Actual: {} (expected: {})",
            mobile_perceived_object.speed,
            expected_mobile_perceived_object.speed
        );
        assert!(
            (mobile_perceived_object.heading - expected_mobile_perceived_object.heading).abs()
                <= 1e-3,
            "Actual: {} (expected: {})",
            mobile_perceived_object.heading,
            expected_mobile_perceived_object.heading
        );
        assert!(
            (mobile_perceived_object.acceleration - expected_mobile_perceived_object.acceleration)
                .abs()
                <= 1e-8,
            "Actual: {} (expected: {})",
            mobile_perceived_object.acceleration,
            expected_mobile_perceived_object.acceleration
        );
    }

    macro_rules! test_mobile_heading_computation {
        ($test_name:ident, $po:expr, $mob_heading:expr, $expected:expr) => {
            #[test]
            fn $test_name() {
                let _epsilon = 1e-11;

                let cartesian_velocity = $po
                    .velocity
                    .as_ref()
                    .and_then(|v| v.cartesian_velocity.as_ref())
                    .map_or(CartesianVelocity::default(), |v| v.clone());

                let heading = compute_heading_from_mobile(
                    cartesian_velocity.x_velocity.value,
                    cartesian_velocity.y_velocity.value,
                    $mob_heading,
                );
                let delta = (heading - $expected).abs();

                assert!(
                    delta <= 1e-11,
                    "Actual: {} (expected: {})",
                    heading.to_degrees(),
                    $expected.to_degrees()
                );
            }
        };
    }

    test_mobile_heading_computation!(
        north_east_heading_mobile_mobile_heading_north,
        po! {360, 360},
        0f64.to_radians(),
        45f64.to_radians()
    );

    test_mobile_heading_computation!(
        north_west_heading_mobile_mobile_heading_north,
        po! {-360, 360},
        0f64.to_radians(),
        315f64.to_radians()
    );

    test_mobile_heading_computation!(
        south_east_heading_mobile_heading_east,
        po! {360, 360},
        90f64.to_radians(),
        135f64.to_radians()
    );

    test_mobile_heading_computation!(
        south_west_heading_mobile_heading_west,
        po! {-360, 360},
        270f64.to_radians(),
        225f64.to_radians()
    );

    test_mobile_heading_computation!(
        north_east_heading_mobile_heading_east,
        po! {-360, 360},
        90f64.to_radians(),
        45f64.to_radians()
    );

    macro_rules! test_rsu_heading_computation {
        ($test_name:ident, $po:expr, $expected:expr) => {
            #[test]
            fn $test_name() {
                let _epsilon = 1e-11;

                let cartesian_velocity = $po
                    .velocity
                    .as_ref()
                    .and_then(|v| v.cartesian_velocity.as_ref())
                    .map_or(CartesianVelocity::default(), |v| v.clone());

                let heading = compute_heading_from_rsu(
                    cartesian_velocity.x_velocity.value,
                    cartesian_velocity.y_velocity.value,
                );
                let delta = (heading - $expected).abs();

                assert!(
                    delta <= 1e-11,
                    "Actual: {} (expected: {})",
                    heading.to_degrees(),
                    $expected.to_degrees()
                );
            }
        };
    }

    test_rsu_heading_computation!(
        east_heading_rsu_computation,
        po! {90, 0},
        90f64.to_radians()
    );
    test_rsu_heading_computation!(
        west_heading_rsu_computation,
        po! {-270, 0},
        270f64.to_radians()
    );

    test_rsu_heading_computation!(
        north_heading_rsu_computation,
        po! {0, 360},
        0f64.to_radians()
    );

    test_rsu_heading_computation!(
        south_heading_rsu_computation,
        po! {0,  -180},
        180f64.to_radians()
    );

    test_rsu_heading_computation!(
        north_east_heading_rsu_computation,
        po! {45, 45},
        45f64.to_radians()
    );
    test_rsu_heading_computation!(
        south_east_heading_rsu_computation,
        po! {135, -135},
        135f64.to_radians()
    );

    test_rsu_heading_computation!(
        south_west_heading_rsu_computation,
        po! {-225, -225},
        225f64.to_radians()
    );

    test_rsu_heading_computation!(
        north_west_heading_rsu_computation,
        po! {-315, 315},
        315f64.to_radians()
    );
}
