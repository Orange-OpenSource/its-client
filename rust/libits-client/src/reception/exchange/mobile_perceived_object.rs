use core::cmp;

use crate::reception::exchange::mobile;
use crate::reception::exchange::mobile::Mobile;
use crate::reception::exchange::perceived_object::PerceivedObject;
use crate::reception::exchange::reference_position::ReferencePosition;

#[derive(Debug)]
pub struct MobilePerceivedObject {
    pub perceived_object: PerceivedObject,
    pub reference_position: ReferencePosition,
}

impl MobilePerceivedObject {
    pub(crate) fn new(
        perceived_object: PerceivedObject,
        cpm_position: &ReferencePosition,
        cpm_heading: u16,
    ) -> Self {
        let computed_reference_position = compute_position(
            perceived_object.distance.x_distance,
            perceived_object.distance.y_distance,
            cpm_position,
            cpm_heading,
        );
        Self {
            perceived_object,
            reference_position: computed_reference_position,
        }
    }
}

impl Mobile for MobilePerceivedObject {
    fn mobile_id(&self) -> u32 {
        self.perceived_object.object_id as u32
    }

    fn position(&self) -> &ReferencePosition {
        &self.reference_position
    }

    fn speed(&self) -> Option<u16> {
        // TODO compute it from Speed
        None
    }

    fn heading(&self) -> Option<u16> {
        // not known from a sensor
        // TODO compute it using historical position if needed
        None
    }
}

impl cmp::PartialEq for MobilePerceivedObject {
    fn eq(&self, other: &Self) -> bool {
        self.perceived_object == other.perceived_object
            && self.reference_position == other.reference_position
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

#[cfg(test)]
mod tests {
    use crate::reception::exchange::mobile_perceived_object::{
        compute_position, MobilePerceivedObject,
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
                reference_position: ReferencePosition {
                    latitude: 434622516,
                    longitude: 1218218,
                    altitude: 220000,
                },
            }
        );
    }
}
