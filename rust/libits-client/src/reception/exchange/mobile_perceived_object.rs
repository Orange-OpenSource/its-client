// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
use core::cmp;
use std::f32::consts::PI;

use crate::reception::exchange::mobile;
use crate::reception::exchange::mobile::{speed_from_yaw_angle, Mobile};
use crate::reception::exchange::perceived_object::PerceivedObject;
use crate::reception::exchange::reference_position::ReferencePosition;
use crate::reception::typed::Typed;
use log::warn;
use serde::{Deserialize, Serialize};

#[derive(Clone, Debug, Hash, Serialize, Deserialize)]
pub struct MobilePerceivedObject {
    pub perceived_object: PerceivedObject,
    pub mobile_id: u32,
    pub reference_position: ReferencePosition,
    pub speed: u16,
    pub heading: u16,
}

impl MobilePerceivedObject {
    pub(crate) fn new(
        perceived_object: PerceivedObject,
        cpm_station_type: u8,
        cpm_station_id: u32,
        cpm_position: &ReferencePosition,
        cpm_heading: Option<u16>,
    ) -> Self {
        let compute_mobile_id = compute_id(perceived_object.object_id, cpm_station_id);
        let computed_reference_position = match cpm_station_type {
            15 => cpm_position.get_offset_destination(
                perceived_object.x_distance.into(),
                perceived_object.y_distance.into(),
            ),
            _ => compute_position_from_mobile(
                perceived_object.x_distance,
                perceived_object.y_distance,
                cpm_position,
                cpm_heading.unwrap_or_default(),
            ),
        };
        let computed_speed = compute_speed(perceived_object.x_speed, perceived_object.y_speed);

        let computed_heading = match cpm_station_type {
            15 => compute_heading_from_rsu(&perceived_object),
            _ => compute_heading_from_mobile(&perceived_object, cpm_heading.unwrap_or_default()),
        };

        Self {
            perceived_object,
            mobile_id: compute_mobile_id,
            reference_position: computed_reference_position,
            speed: computed_speed,
            heading: computed_heading,
        }
    }
}

impl Mobile for MobilePerceivedObject {
    fn mobile_id(&self) -> u32 {
        self.mobile_id
    }

    fn position(&self) -> &ReferencePosition {
        &self.reference_position
    }

    fn speed(&self) -> Option<u16> {
        Some(self.speed)
    }

    fn heading(&self) -> Option<u16> {
        Some(self.heading)
    }
}

impl cmp::PartialEq for MobilePerceivedObject {
    fn eq(&self, other: &Self) -> bool {
        self.perceived_object == other.perceived_object
            && self.mobile_id == other.mobile_id
            && self.reference_position == other.reference_position
            && self.heading == other.heading
            && self.speed == other.speed
    }
}

impl Typed for MobilePerceivedObject {
    fn get_type() -> String {
        "po".to_string()
    }
}

fn compute_id(object_id: u8, cpm_station_id: u32) -> u32 {
    let string_id = format!("{}{}", cpm_station_id, object_id);
    match string_id.parse() {
        Ok(id) => id,
        Err(_err) => {
            warn!(
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
    cpm_position: &ReferencePosition,
    cpm_heading: u16,
) -> ReferencePosition {
    let x_offset_meters = x_distance as f64 / 100.0;
    let y_offset_meters = y_distance as f64 / 100.0;
    let heading_in_degrees = mobile::heading_in_degrees(cpm_heading);
    return cpm_position
        .get_destination(x_offset_meters, heading_in_degrees)
        .get_destination(y_offset_meters, (heading_in_degrees - 90.0 + 360.0) % 360.0);
}

fn compute_speed(x_speed: i16, y_speed: i16) -> u16 {
    speed_from_yaw_angle(x_speed, y_speed)
}

fn compute_heading_from_mobile(perceived_object: &PerceivedObject, cpm_heading: u16) -> u16 {
    // FIXME this does not compute the real PO's heading
    match perceived_object.y_distance {
        y if y < 0 => (cpm_heading - 1800) % 3600,
        _ => cpm_heading,
    }
}

/// Computes the heading of the perceived object based on its `x` and `y` speed
/// where `x` is West <-> East axis (negative `x` is West, positive `x` is East)
/// and `y` is North <-> South axis (negative `y` is South, positive y is North)
///
/// References:
/// - https://www.omnicalculator.com/math/vector-direction
/// - https://support.nortekgroup.com/hc/en-us/articles/360012774640-How-do-I-calculate-current-speed-and-direction-from-three-beam-ADCP-velocity-components-
///
fn compute_heading_from_rsu(perceived_object: &PerceivedObject) -> u16 {
    let y_speed = f32::from(perceived_object.y_speed);
    let x_speed = f32::from(perceived_object.x_speed);

    let radians = -x_speed.atan2(-y_speed);
    let degrees = radians * 180. / PI;
    let heading = 180. + degrees;
    (heading * 10.) as u16
}

#[cfg(test)]
mod tests {
    use crate::reception::exchange::mobile_perceived_object::{
        compute_heading_from_mobile, compute_heading_from_rsu, compute_id,
        compute_position_from_mobile, MobilePerceivedObject,
    };
    use crate::reception::exchange::perceived_object::PerceivedObject;
    use crate::reception::exchange::reference_position::ReferencePosition;

    macro_rules! po {
        ($x_speed:expr, $y_speed:expr) => {
            PerceivedObject {
                object_id: 0,
                time_of_measurement: 0,
                confidence: Default::default(),
                x_distance: 0,
                y_distance: 0,
                z_distance: None,
                x_speed: $x_speed,
                y_speed: $y_speed,
                z_speed: None,
                object_age: 0,
                object_ref_point: None,
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
        };
    }

    #[test]
    fn it_can_compute_a_position() {
        //south with x
        assert_eq!(
            compute_position_from_mobile(
                50000,
                0,
                &ReferencePosition {
                    latitude: 434667520,
                    longitude: 1205862,
                    altitude: 220000,
                },
                1800,
            ),
            ReferencePosition {
                latitude: 434622516,
                longitude: 1205862,
                altitude: 220000,
            }
        );
        // east with y
        assert_eq!(
            compute_position_from_mobile(
                0,
                10000,
                &ReferencePosition {
                    latitude: 434667520,
                    longitude: 1205862,
                    altitude: 220000,
                },
                1800,
            ),
            ReferencePosition {
                latitude: 434667520,
                longitude: 1218219,
                altitude: 220000,
            }
        );
    }

    #[test]
    fn it_can_compute_an_id() {
        //not too large, we concatenate
        assert_eq!(compute_id(1, 100), 1001);
        assert_eq!(compute_id(1, 400000000), 4000000001);
        //too large, we add
        assert_eq!(compute_id(1, 500000000), 500000001);
    }

    #[test]
    fn it_can_compute_a_heading() {
        let pos_y_distance_po = PerceivedObject {
            object_id: 1,
            x_distance: 50000,
            y_distance: 10000,
            ..Default::default()
        };
        let neg_y_distance_po = PerceivedObject {
            object_id: 1,
            x_distance: 50000,
            y_distance: -10000,
            ..Default::default()
        };
        //east
        assert_eq!(compute_heading_from_mobile(&pos_y_distance_po, 900), 900);
        assert_eq!(compute_heading_from_mobile(&neg_y_distance_po, 2700), 900);
        //south
        assert_eq!(compute_heading_from_mobile(&pos_y_distance_po, 1800), 1800);
        assert_eq!(compute_heading_from_mobile(&neg_y_distance_po, 1800), 0);
        //west
        assert_eq!(compute_heading_from_mobile(&pos_y_distance_po, 2700), 2700);
        assert_eq!(compute_heading_from_mobile(&neg_y_distance_po, 2700), 900);
    }

    #[test]
    fn create_a_new() {
        //south east with x and y
        assert_eq!(
            MobilePerceivedObject::new(
                PerceivedObject {
                    object_id: 1,
                    x_distance: 50000,
                    y_distance: 10000,
                    ..Default::default()
                },
                5,
                10,
                &ReferencePosition {
                    latitude: 434667520,
                    longitude: 1205862,
                    altitude: 220000,
                },
                Some(1800),
            ),
            MobilePerceivedObject {
                perceived_object: PerceivedObject {
                    object_id: 1,
                    x_distance: 50000,
                    y_distance: 10000,
                    ..Default::default()
                },
                mobile_id: 101,
                reference_position: ReferencePosition {
                    latitude: 434622516,
                    longitude: 1218218,
                    altitude: 220000,
                },
                speed: 0,
                heading: 1800,
            }
        );
    }

    #[test]
    fn east_heading_rsu_computation() {
        let perceived_object = po! {90, 0};

        let heading = compute_heading_from_rsu(&perceived_object);

        assert_eq!(900, heading);
    }

    #[test]
    fn west_heading_rsu_computation() {
        let perceived_object = po! {-270, 0};

        let heading = compute_heading_from_rsu(&perceived_object);

        assert_eq!(2700, heading);
    }

    #[test]
    fn north_heading_rsu_computation() {
        let perceived_object = po! {0, 360};

        let heading = compute_heading_from_rsu(&perceived_object);

        assert_eq!(0, heading);
    }

    #[test]
    fn south_heading_rsu_computation() {
        let perceived_object = po! {0,  -180};

        let heading = compute_heading_from_rsu(&perceived_object);

        assert_eq!(1800, heading);
    }

    #[test]
    fn north_east_heading_rsu_computation() {
        let perceived_object = po! {45, 45};

        let heading = compute_heading_from_rsu(&perceived_object);

        assert_eq!(450, heading);
    }

    #[test]
    fn south_east_heading_rsu_computation() {
        let perceived_object = po! {135, -135};

        let heading = compute_heading_from_rsu(&perceived_object);

        assert_eq!(1350, heading);
    }

    #[test]
    fn south_west_heading_rsu_computation() {
        let perceived_object = po! {-225, -225};

        let heading = compute_heading_from_rsu(&perceived_object);

        assert_eq!(2250, heading);
    }

    #[test]
    fn north_west_heading_rsu_computation() {
        let perceived_object = po! {-315, 315};

        let heading = compute_heading_from_rsu(&perceived_object);

        assert_eq!(3150, heading);
    }
}
