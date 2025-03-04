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

use std::f64::consts::PI;
use std::hash::{Hash, Hasher};

use self::integer_sqrt::IntegerSquareRoot;
use crate::exchange::etsi::collective_perception_message::CollectivePerceptionMessage;
use crate::exchange::etsi::perceived_object::PerceivedObject;
use crate::exchange::etsi::speed_from_etsi;
use crate::mobility::mobile::Mobile;
use crate::mobility::position::{Position, enu_destination, haversine_destination};
use log::trace;

const PI2: f64 = 2. * PI;

#[derive(Clone, Debug)]
pub struct MobilePerceivedObject {
    pub perceived_object: PerceivedObject,
    pub mobile_id: u32,
    pub position: Position,
    pub speed: f64,
    pub heading: f64,
    pub acceleration: f64,
}

impl MobilePerceivedObject {
    // TODO FGA: check how to keep it private and manage end to end CPMs with natively
    //           perceived object mobility
    pub(crate) fn new(
        perceived_object: PerceivedObject,
        cpm: &CollectivePerceptionMessage,
    ) -> Self {
        let mobile_id = compute_id(perceived_object.object_id, cpm.station_id);
        let speed = speed_from_yaw_angle(perceived_object.x_speed, perceived_object.y_speed);
        let (position, heading) = match cpm.management_container.station_type {
            15 => {
                let position = enu_destination(
                    &cpm.position(),
                    perceived_object.x_distance as f64 / 100.,
                    perceived_object.y_distance as f64 / 100.,
                    perceived_object.z_distance.unwrap_or_default() as f64 / 100.,
                );
                let heading = compute_heading_from_rsu(&perceived_object);

                (position, heading)
            }
            _ => {
                let position = compute_position_from_mobile(
                    perceived_object.x_distance,
                    perceived_object.y_distance,
                    &cpm.position(),
                    cpm.heading().unwrap_or_default(),
                );
                let heading = compute_heading_from_mobile(
                    &perceived_object,
                    cpm.heading().unwrap_or_default(),
                );

                (position, heading)
            }
        };

        Self {
            perceived_object,
            mobile_id,
            position,
            speed,
            heading,
            // TODO
            acceleration: 0.0,
        }
    }
}

impl Mobile for MobilePerceivedObject {
    fn id(&self) -> u32 {
        self.mobile_id
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

/// FIXME this function does not create a unique id (issue [99](https://github.com/Orange-OpenSource/its-client/issues/99))
fn compute_id(object_id: u8, cpm_station_id: u32) -> u32 {
    let string_id = format!("{}{}", cpm_station_id, object_id);
    match string_id.parse() {
        Ok(id) => id,
        Err(_err) => {
            trace!(
                "unable to generate a mobile id with {}, we create a short one",
                string_id
            );
            cpm_station_id + object_id as u32
        }
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

fn compute_heading_from_mobile(perceived_object: &PerceivedObject, cpm_heading: f64) -> f64 {
    (cpm_heading + compute_heading_from_rsu(perceived_object)) % PI2
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
fn compute_heading_from_rsu(perceived_object: &PerceivedObject) -> f64 {
    let y_speed = f64::from(perceived_object.y_speed);
    let x_speed = f64::from(perceived_object.x_speed);

    PI + -x_speed.atan2(-y_speed)
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::collective_perception_message::{
        CollectivePerceptionMessage, ManagementContainer, OriginatingVehicleContainer,
        StationDataContainer,
    };
    use crate::exchange::etsi::mobile_perceived_object::{
        MobilePerceivedObject, compute_heading_from_mobile, compute_heading_from_rsu, compute_id,
        compute_position_from_mobile,
    };
    use crate::exchange::etsi::perceived_object::PerceivedObject;
    use crate::exchange::etsi::reference_position::{
        ReferencePosition, altitude_from_etsi, coordinate_from_etsi,
    };
    use crate::exchange::etsi::{heading_from_etsi, speed_from_etsi};
    use crate::mobility::position::Position;
    use std::f64::consts::PI;

    macro_rules! po {
        ($x_speed:expr_2021, $y_speed:expr_2021) => {
            PerceivedObject {
                x_speed: $x_speed,
                y_speed: $y_speed,
                ..Default::default()
            }
        };
    }

    macro_rules! test_compute_position_from_mobile {
        ($test_name:ident, $x:expr_2021, $y:expr_2021, $expected:expr_2021) => {
            #[test]
            fn $test_name() {
                let position = compute_position_from_mobile(
                    $x,
                    $y,
                    &ReferencePosition {
                        latitude: 486251958,
                        longitude: 22415093,
                        altitude: 900,
                    }
                    .as_position(),
                    heading_from_etsi(900),
                );

                println!(
                    "Anchor: {}",
                    ReferencePosition {
                        latitude: 486251958,
                        longitude: 22415093,
                        altitude: 900,
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
    fn it_can_compute_an_id() {
        //not too large, we concatenate
        assert_eq!(compute_id(1, 100), 1001);
        assert_eq!(compute_id(1, 400000000), 4000000001);
        //too large, we add
        assert_eq!(compute_id(1, 500000000), 500000001);
    }

    #[test]
    fn constructor_from_mobile() {
        let perceived_object = PerceivedObject {
            object_id: 1,
            x_distance: 50000,
            y_distance: 10000,
            ..Default::default()
        };
        let expected_mobile_perceived_object = MobilePerceivedObject {
            perceived_object: perceived_object.clone(),
            mobile_id: 101,
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
            perceived_object,
            &CollectivePerceptionMessage {
                station_id: 10,
                management_container: ManagementContainer {
                    station_type: 5,
                    reference_position: ReferencePosition {
                        latitude: 434667520,
                        longitude: 1205862,
                        altitude: 220000,
                    },
                    ..Default::default()
                },
                station_data_container: Some(StationDataContainer {
                    originating_vehicle_container: Some(OriginatingVehicleContainer {
                        heading: 1800,
                        ..Default::default()
                    }),
                    originating_rsu_container: None,
                }),
                ..Default::default()
            },
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
            object_id: 1,
            x_distance: 114,
            y_distance: -2757,
            x_speed: 480,
            y_speed: -345,
            ..Default::default()
        };
        let expected_mobile_perceived_object = MobilePerceivedObject {
            perceived_object: perceived_object.clone(),
            mobile_id: 101,
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
            perceived_object,
            &CollectivePerceptionMessage {
                station_id: 10,
                management_container: ManagementContainer {
                    station_type: 15,
                    reference_position: ReferencePosition {
                        latitude: 488417860,
                        longitude: 23678940,
                        altitude: 900,
                    },
                    ..Default::default()
                },
                ..Default::default()
            },
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
        ($test_name:ident, $po:expr_2021, $mob_heading:expr_2021, $expected:expr_2021) => {
            #[test]
            fn $test_name() {
                let _epsilon = 1e-11;

                let heading = compute_heading_from_mobile(&$po, $mob_heading);
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
        ($test_name:ident, $po:expr_2021, $expected:expr_2021) => {
            #[test]
            fn $test_name() {
                let _epsilon = 1e-11;

                let heading = compute_heading_from_rsu(&$po);
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
