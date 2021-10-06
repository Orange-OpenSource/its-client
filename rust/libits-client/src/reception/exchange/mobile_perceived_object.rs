use core::cmp;

use crate::reception::exchange::mobile;
use crate::reception::exchange::mobile::{speed_from_yaw_angle, Mobile};
use crate::reception::exchange::perceived_object::PerceivedObject;
use crate::reception::exchange::reference_position::ReferencePosition;
use crate::reception::typed::Typed;
use log::warn;
use serde::{Deserialize, Serialize};

#[derive(Clone, Debug, Hash, Serialize, Deserialize)]
pub struct MobilePerceivedObject {
    pub(crate) perceived_object: PerceivedObject,
    pub(crate) mobile_id: u32,
    pub(crate) reference_position: ReferencePosition,
    pub(crate) speed: u16,
    pub(crate) heading: u16,
}

impl MobilePerceivedObject {
    pub(crate) fn new(
        perceived_object: PerceivedObject,
        cpm_station_id: u32,
        cpm_position: &ReferencePosition,
        cpm_heading: u16,
    ) -> Self {
        let compute_mobile_id = compute_id(perceived_object.object_id, cpm_station_id);
        let computed_reference_position = compute_position(
            perceived_object.distance.x_distance,
            perceived_object.distance.y_distance,
            cpm_position,
            cpm_heading,
        );
        let computed_speed = compute_speed(
            perceived_object.speed.x_speed,
            perceived_object.speed.y_speed,
        );
        let computed_heading = compute_heading(perceived_object.distance.y_distance, cpm_heading);

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

fn compute_position(
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

fn compute_heading(y_distance: i32, cpm_heading: u16) -> u16 {
    match y_distance {
        y if y < 0 => (cpm_heading + 1800) % 3600,
        _ => cpm_heading,
    }
}

#[cfg(test)]
mod tests {
    use crate::reception::exchange::mobile_perceived_object::{
        compute_heading, compute_id, compute_position, MobilePerceivedObject,
    };
    use crate::reception::exchange::perceived_object::{Distance, PerceivedObject};
    use crate::reception::exchange::reference_position::ReferencePosition;

    #[test]
    fn it_can_compute_a_position() {
        //south with x
        assert_eq!(
            compute_position(
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
            compute_position(
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
        //east
        assert_eq!(compute_heading(50000, 900), 900);
        assert_eq!(compute_heading(-50000, 2700), 900);
        //south
        assert_eq!(compute_heading(50000, 1800), 1800);
        assert_eq!(compute_heading(-50000, 1800), 0);
        //west
        assert_eq!(compute_heading(50000, 2700), 2700);
        assert_eq!(compute_heading(-50000, 2700), 900);
    }

    #[test]
    fn create_a_new() {
        //south east with x and y
        assert_eq!(
            MobilePerceivedObject::new(
                PerceivedObject {
                    object_id: 1,
                    distance: Distance {
                        x_distance: 50000,
                        y_distance: 10000,
                    },
                    ..Default::default()
                },
                10,
                &ReferencePosition {
                    latitude: 434667520,
                    longitude: 1205862,
                    altitude: 220000,
                },
                1800,
            ),
            MobilePerceivedObject {
                perceived_object: PerceivedObject {
                    object_id: 1,
                    distance: Distance {
                        x_distance: 50000,
                        y_distance: 10000,
                    },
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
}
