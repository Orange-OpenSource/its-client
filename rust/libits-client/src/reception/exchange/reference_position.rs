use core::fmt;
use std::f64::consts;

use cheap_ruler::{CheapRuler, DistanceUnit};
use navigation::Location;
use serde::{Deserialize, Serialize};

const EARTH_RADIUS: u32 = 6371000;
// in meters
const LG_MOD: u8 = 180;
// Max longitude on WGS 84
const COORDINATE_SIGNIFICANT_DIGIT: u8 = 7;

const ALTITUDE_SIGNIFICANT_DIGIT: u8 = 3;

#[derive(Clone, Default, Debug, Eq, Hash, PartialEq, Serialize, Deserialize)]
pub struct ReferencePosition {
    pub latitude: i32,
    pub longitude: i32,
    pub altitude: i32,
}

impl ReferencePosition {
    pub fn get_distance(&self, other: &ReferencePosition) -> u32 {
        let longitude = get_coordinate(self.longitude) * consts::PI / LG_MOD as f64;
        let latitude = get_coordinate(self.latitude) * consts::PI / LG_MOD as f64;
        let other_longitude = get_coordinate(other.longitude) * consts::PI / LG_MOD as f64;
        let other_latitude = get_coordinate(other.latitude) * consts::PI / LG_MOD as f64;

        let longitude_distance = other_longitude - longitude;
        let latitude_distance = other_latitude - latitude;

        // Haversine formula
        let a = (latitude_distance / 2.0).sin() * (latitude_distance / 2.0).sin()
            + latitude.cos()
                * other_latitude.cos()
                * (longitude_distance / 2.0).sin()
                * (longitude_distance / 2.0).sin();

        let c = 2.0 * a.sqrt().atan2((1.0 - a).sqrt());

        let distance = EARTH_RADIUS as f64 * c;
        distance.round() as u32
    }

    pub fn get_heading(&self, other: &ReferencePosition) -> u16 {
        // FIXME use cheap_ruler instead of navigation crates
        let self_location = Location::new(
            get_coordinate(self.latitude),
            get_coordinate(self.longitude),
        );
        let other_location = Location::new(
            get_coordinate(other.latitude),
            get_coordinate(other.longitude),
        );

        let bearing = self_location.calc_bearing_to(&other_location);
        (bearing * 10_f64).round() as u16
    }

    pub fn get_destination(&self, distance: f64, bearing: f64) -> Self {
        let longitude_coordinate = get_coordinate(self.longitude);
        let latitude_coordinate = get_coordinate(self.latitude);
        let cr = CheapRuler::new(latitude_coordinate, DistanceUnit::Meters);
        let p1 = (longitude_coordinate, latitude_coordinate).into();
        let destination = cr.destination(&p1, distance, bearing);
        ReferencePosition {
            longitude: get_etsi_coordinate(destination.lng()),
            latitude: get_etsi_coordinate(destination.lat()),
            altitude: self.altitude,
        }
    }
}

impl fmt::Display for ReferencePosition {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "lat {}/long {}/alt {}",
            get_coordinate(self.latitude),
            get_coordinate(self.longitude),
            get_altitude(self.altitude),
        )
    }
}

fn get_coordinate(etsi_coordinate: i32) -> f64 {
    let base: i32 = 10;
    etsi_coordinate as f64 / base.pow(COORDINATE_SIGNIFICANT_DIGIT as u32) as f64
}

fn get_etsi_coordinate(coordinate: f64) -> i32 {
    let base: i32 = 10;
    (coordinate * base.pow(COORDINATE_SIGNIFICANT_DIGIT as u32) as f64) as i32
}

fn get_altitude(etsi_altitude: i32) -> f64 {
    let base: i32 = 10;
    etsi_altitude as f64 / base.pow(ALTITUDE_SIGNIFICANT_DIGIT as u32) as f64
}

#[cfg(test)]
mod tests {
    use navigation::Location;

    use crate::reception::exchange::ReferencePosition;

    fn teqmo_lane_merge_reference_postion() -> ReferencePosition {
        // center is at TEQMO lane merge position
        ReferencePosition {
            latitude: 486244870,
            longitude: 22436370,
            ..Default::default() // no altitude
        }
    }

    fn teqmo_city_reference_postion() -> ReferencePosition {
        // center is at TEQMO city
        ReferencePosition {
            latitude: 486249990,
            longitude: 22412116,
            ..Default::default() // no altitude
        }
    }

    #[test]
    fn compute_100_meters_distance() {
        let position = teqmo_lane_merge_reference_postion();
        // I take a point at 100 meters
        let other_position = ReferencePosition {
            latitude: 486237420,
            longitude: 22428750,
            ..Default::default() // no altitude
        };
        assert_eq!(position.get_distance(&other_position), 100);
    }

    #[test]
    fn compute_31_meters_distance() {
        // center is at TEQMO city
        let position = teqmo_city_reference_postion();
        // I take a point at 31 meters
        let other_position = ReferencePosition {
            latitude: 486252239,
            longitude: 22409705,
            ..Default::default() // no altitude
        };
        assert_eq!(position.get_distance(&other_position), 31);
    }

    #[test]
    fn calc_bearing_montlhery1_to_montlhery2() {
        let montlhery_1 = Location::new(48.6250323, 2.2412096);
        let montlhery_2 = Location::new(48.6249755, 2.2412662);

        assert_eq!(
            "146.63",
            format!("{:.2}", montlhery_1.calc_bearing_to(&montlhery_2))
        );
        assert_eq!(
            "142.57",
            format!(
                "{:.2}",
                montlhery_1.estimate_bearing_to(&montlhery_2, 69.0, 53.0)
            )
        );
    }

    #[test]
    fn calc_bearing_montlhery3_to_montlhery4() {
        let montlhery_3 = Location::new(48.6234734, 2.2397949);
        let montlhery_4 = Location::new(48.6234641, 2.2398374);

        assert_eq!(
            "108.32",
            format!("{:.2}", montlhery_3.calc_bearing_to(&montlhery_4))
        );
        assert_eq!(
            "105.90",
            format!(
                "{:.2}",
                montlhery_3.estimate_bearing_to(&montlhery_4, 69.0, 53.0)
            )
        );
    }

    #[test]
    fn get_heading_montlhery3_to_montlhery4() {
        let montlhery_3 = ReferencePosition {
            latitude: 486234734,
            longitude: 22397949,
            altitude: 15710,
        };
        let montlhery_4 = ReferencePosition {
            latitude: 486234641,
            longitude: 22398374,
            altitude: 15773,
        };
        assert_eq!(1083, montlhery_3.get_heading(&montlhery_4));
    }

    #[test]
    fn calc_bearing_boulder_to_dia() {
        // 39.8617° N, 104.6731° W
        let dia = Location::new(39.8617, -104.6731);

        // 40.0274° N, 105.2519° W
        let boulder = Location::new(40.0274, -105.2519);

        assert_eq!("110.48", format!("{:.*}", 2, boulder.calc_bearing_to(&dia)));
        assert_eq!(
            "110.44",
            format!("{:.*}", 2, boulder.estimate_bearing_to(&dia, 69.0, 53.0))
        );
    }

    #[test]
    fn it_can_get_south_destination() {
        let position = teqmo_lane_merge_reference_postion();
        // I take a point at 100 meters on south
        let other_position = ReferencePosition {
            latitude: 486235877,
            longitude: position.longitude,
            ..Default::default() // no altitude
        };
        assert_eq!(position.get_destination(100.0, 180.0), other_position);
        assert_eq!(position.get_destination(100.0, -180.0), other_position);
    }

    #[test]
    fn it_can_get_north_destination() {
        let position = teqmo_lane_merge_reference_postion();
        // I take a point at 100 meters on north
        let other_position = ReferencePosition {
            latitude: 486253862,
            longitude: position.longitude,
            ..Default::default() // no altitude
        };
        assert_eq!(position.get_destination(100.0, 0.0), other_position);
        assert_eq!(position.get_destination(100.0, 360.0), other_position);
    }

    #[test]
    fn it_can_get_east_destination() {
        let position = teqmo_lane_merge_reference_postion();
        // I take a point at 100 meters on south
        let other_position = ReferencePosition {
            latitude: position.latitude,
            longitude: 22449934,
            ..Default::default() // no altitude
        };
        assert_eq!(position.get_destination(100.0, 90.0), other_position);
        assert_eq!(position.get_destination(100.0, -270.0), other_position);
    }

    #[test]
    fn it_can_get_west_destination() {
        let position = teqmo_lane_merge_reference_postion();
        // I take a point at 100 meters on south
        let other_position = ReferencePosition {
            latitude: position.latitude,
            longitude: 22422805,
            ..Default::default() // no altitude
        };
        assert_eq!(position.get_destination(100.0, 270.0), other_position);
        assert_eq!(position.get_destination(100.0, -90.0), other_position);
    }
}
