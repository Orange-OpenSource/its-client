// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
use core::fmt;
use std::f64::consts;

use cheap_ruler::{CheapRuler, DistanceUnit};
use geo::Point;

use navigation::Location;
use ndarray::{arr1, arr2};
use serde::{Deserialize, Serialize};

const EARTH_RADIUS: u32 = 6_371_000; // in meters
const EQUATORIAL_RADIUS: f64 = 6_378_137.0; // in meters
const POLAR_RADIUS: f64 = 6_356_752.3; // in meters

const LG_MOD: u8 = 180; // Max longitude on WGS 84
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
            longitude: get_etsi_coordinate(destination.x()),
            latitude: get_etsi_coordinate(destination.y()),
            altitude: self.altitude,
        }
    }

    pub fn as_geo_point(&self) -> Point<f64> {
        let (lat, lon) = self.get_coordinates();
        (lon, lat).into()
    }

    fn get_coordinates(&self) -> (f64, f64) {
        (
            get_coordinate(self.latitude),
            get_coordinate(self.longitude),
        )
    }

    pub fn get_offset_destination(
        &self,
        easting_offset: f64,
        northing_offset: f64,
        up_offset: f64,
    ) -> Self {
        let (latitude, longitude, altitude) = map_3d::enu2geodetic(
            easting_offset,
            northing_offset,
            up_offset,
            get_coordinate(self.latitude).to_radians(),
            get_coordinate(self.longitude).to_radians(),
            self.altitude.into(),
            map_3d::Ellipsoid::WGS84,
        );

        ReferencePosition {
            latitude: get_etsi_coordinate(map_3d::rad2deg(latitude)),
            longitude: get_etsi_coordinate(map_3d::rad2deg(longitude)),
            altitude: altitude as i32,
        }
    }

    /// Converts this position to its [East North Up (ENU) coordinates][1]
    /// from the reference position `anchor`
    ///
    /// Computation is done by first converting the geodetic coordinates (lat, lon, alt)
    /// of both this point and the reference point to [ECEF][2] coordinates
    /// Then the [ENU computation][3] can be done
    ///
    /// [1]: https://en.wikipedia.org/wiki/Local_tangent_plane_coordinates
    /// [2]: https://en.wikipedia.org/wiki/Earth-centered,_Earth-fixed_coordinate_system
    /// [3]: https://gssc.esa.int/navipedia/index.php/Transformations_between_ECEF_and_ENU_coordinates
    pub fn to_enu(&self, anchor: &ReferencePosition) -> (f64, f64, f64) {
        let reference_ecef = anchor.to_ecef();
        let relative_ecef = self.to_ecef();

        let mut latitude = get_coordinate(anchor.latitude);
        let mut longitude = get_coordinate(anchor.longitude);

        latitude = latitude.to_radians();
        longitude = longitude.to_radians();

        let reference_matrix = arr2(&[
            [-longitude.sin(), longitude.cos(), 0.],
            [
                -latitude.sin() * longitude.cos(),
                -latitude.sin() * longitude.sin(),
                latitude.cos(),
            ],
            [
                latitude.cos() * longitude.cos(),
                latitude.cos() * longitude.sin(),
                latitude.sin(),
            ],
        ]);
        let relative_vector = arr1(&[
            relative_ecef.0 - reference_ecef.0,
            relative_ecef.1 - reference_ecef.1,
            relative_ecef.2 - reference_ecef.2,
        ]);

        let enu = reference_matrix.dot(&relative_vector).to_vec();
        let as_vec = enu.to_vec();

        let x_distance = as_vec[0];
        let y_distance = as_vec[1];
        let z_distance = as_vec[2];

        (x_distance, y_distance, z_distance)
    }

    /// Returns the corresponding [Earth Centered, Earth Fixed][1] coordinates for this position
    ///
    /// [1]: https://en.wikipedia.org/wiki/Earth-centered,_Earth-fixed_coordinate_system
    pub fn to_ecef(&self) -> (f64, f64, f64) {
        let latitude = get_coordinate(self.latitude).to_radians();
        let longitude = get_coordinate(self.longitude).to_radians();
        let altitude = get_altitude(self.altitude);

        let n_phi = prime_vertical_radius(latitude);

        let x = (n_phi + altitude) * latitude.cos() * longitude.cos();
        let y = (n_phi + altitude) * latitude.cos() * longitude.sin();
        let z = ((1. - ellipsoid_flattening()).powf(2.) * n_phi + altitude) * latitude.sin();

        (x, y, z)
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
    let base: f64 = 10.;
    f64::from(etsi_coordinate) / base.powf(f64::from(COORDINATE_SIGNIFICANT_DIGIT))
}

fn get_etsi_coordinate(coordinate: f64) -> i32 {
    let base: i32 = 10;
    (coordinate * f64::from(base.pow(u32::from(COORDINATE_SIGNIFICANT_DIGIT)))) as i32
}

fn get_altitude(etsi_altitude: i32) -> f64 {
    let base: f64 = 10.;
    f64::from(etsi_altitude) / base.powf(f64::from(ALTITUDE_SIGNIFICANT_DIGIT))
}

fn prime_vertical_radius(phi: f64) -> f64 {
    let e_square: f64 = 1. - (POLAR_RADIUS.powf(2.) / EQUATORIAL_RADIUS.powf(2.));
    let sin_phi = phi.sin();

    EQUATORIAL_RADIUS / (1. - e_square * sin_phi.powf(2.)).sqrt()
}

fn ellipsoid_flattening() -> f64 {
    1. - POLAR_RADIUS / EQUATORIAL_RADIUS
}

#[cfg(test)]
mod tests {
    use crate::reception::exchange::reference_position::{get_coordinate, get_etsi_coordinate};

    use navigation::Location;

    use crate::reception::exchange::ReferencePosition;

    fn teqmo_lane_merge_reference_position() -> ReferencePosition {
        // center is at TEQMO lane merge position
        ReferencePosition {
            latitude: 486244870,
            longitude: 22436370,
            ..Default::default() // no altitude
        }
    }

    fn teqmo_city_reference_position() -> ReferencePosition {
        // center is at TEQMO city
        ReferencePosition {
            latitude: 486249990,
            longitude: 22412116,
            ..Default::default() // no altitude
        }
    }

    #[test]
    fn compute_100_meters_distance() {
        let position = teqmo_lane_merge_reference_position();
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
        let position = teqmo_city_reference_position();
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
        let position = teqmo_lane_merge_reference_position();
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
        let position = teqmo_lane_merge_reference_position();
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
        let position = teqmo_lane_merge_reference_position();
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
        let position = teqmo_lane_merge_reference_position();
        // I take a point at 100 meters on south
        let other_position = ReferencePosition {
            latitude: position.latitude,
            longitude: 22422805,
            ..Default::default() // no altitude
        };
        assert_eq!(position.get_destination(100.0, 270.0), other_position);
        assert_eq!(position.get_destination(100.0, -90.0), other_position);
    }

    #[test]
    fn offset_destination_hunder_meters_north() {
        let reference_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63816914950018),
            longitude: get_etsi_coordinate(1.4031882),
            altitude: 0,
        };
        let expected_destination = ReferencePosition {
            latitude: get_etsi_coordinate(43.63906919748),
            longitude: get_etsi_coordinate(1.4031882),
            altitude: 0,
        };

        let offset_destination = reference_point.get_offset_destination(0., 100., 0.);

        assert_eq!(offset_destination.latitude, expected_destination.latitude);
        assert_eq!(offset_destination.longitude, expected_destination.longitude);
    }

    #[test]
    fn offset_destination_hundred_meters_east() {
        let reference_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63816914950018),
            longitude: get_etsi_coordinate(1.4031881568425872),
            altitude: 0,
        };
        let expected_destination = ReferencePosition {
            latitude: get_etsi_coordinate(43.63816910008),
            longitude: get_etsi_coordinate(1.4044273),
            altitude: 0,
        };

        let offset_destination = reference_point.get_offset_destination(100., 0., 0.);

        let lat_abs_diff = (get_coordinate(offset_destination.latitude).abs()
            - get_coordinate(expected_destination.latitude).abs())
        .abs();

        assert!(lat_abs_diff < 1e-6);
        assert_eq!(offset_destination.longitude, expected_destination.longitude);
    }

    #[test]
    fn geodetic_to_enu_150m_east_200m_north() {
        let reference_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63816914950018),
            longitude: get_etsi_coordinate(1.4031881568425872),
            altitude: 0,
        };
        let relative_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63996919589),
            longitude: get_etsi_coordinate(1.40504710005),
            altitude: 0,
        };
        let expected_x_distance = 150.;
        let expected_y_distance = 200.;
        let expected_z_distance = 0.;

        let (x_distance, y_distance, z_distance) = relative_point.to_enu(&reference_point);

        assert_eq!(x_distance.round(), expected_x_distance);
        assert_eq!(y_distance.round(), expected_y_distance);
        assert_eq!(z_distance.round(), expected_z_distance);
    }

    #[test]
    fn geodetic_to_enu_150m_west_200m_south() {
        let reference_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63996919589),
            longitude: get_etsi_coordinate(1.40504710005),
            altitude: 0,
        };
        let relative_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63816914950018),
            longitude: get_etsi_coordinate(1.4031881568425872),
            altitude: 0,
        };
        let expected_x_distance = -150.;
        let expected_y_distance = -200.;
        let expected_z_distance = 0.;

        let (x_distance, y_distance, z_distance) = relative_point.to_enu(&reference_point);

        assert_eq!(x_distance.round(), expected_x_distance);
        assert_eq!(y_distance.round(), expected_y_distance);
        assert_eq!(z_distance.round(), expected_z_distance);
    }

    #[test]
    fn geodetic_to_enu_150m_east() {
        let reference_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63816914950018),
            longitude: get_etsi_coordinate(1.4031881568425872),
            altitude: 0,
        };
        let relative_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63816910008),
            longitude: get_etsi_coordinate(1.40504707684),
            altitude: 0,
        };
        let expected_x_distance = 150.;
        let expected_y_distance = 0.;
        let expected_z_distance = 0.;

        let (x_distance, y_distance, z_distance) = relative_point.to_enu(&reference_point);

        assert_eq!(x_distance.round(), expected_x_distance);
        assert_eq!(y_distance.round(), expected_y_distance);
        assert_eq!(z_distance.round(), expected_z_distance);
    }

    #[test]
    fn geodetic_to_enu_200m_north() {
        let reference_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63816910008),
            longitude: get_etsi_coordinate(1.40504707684),
            altitude: 0,
        };
        let relative_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63996919589),
            longitude: get_etsi_coordinate(1.40504710005),
            altitude: 0,
        };
        let expected_x_distance = 0.;
        let expected_y_distance = 200.;
        let expected_z_distance = 0.;

        let (x_distance, y_distance, z_distance) = relative_point.to_enu(&reference_point);

        assert_eq!(x_distance.round(), expected_x_distance);
        assert_eq!(y_distance.round(), expected_y_distance);
        assert_eq!(z_distance.round(), expected_z_distance);
    }

    #[test]
    fn geodetic_to_enu_150m_west() {
        let reference_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63816910008),
            longitude: get_etsi_coordinate(1.40504707684),
            altitude: 0,
        };
        let relative_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63816914950018),
            longitude: get_etsi_coordinate(1.4031881568425872),
            altitude: 0,
        };
        let expected_x_distance = -150.;
        let expected_y_distance = 0.;
        let expected_z_distance = 0.;

        let (x_distance, y_distance, z_distance) = relative_point.to_enu(&reference_point);

        assert_eq!(x_distance.round(), expected_x_distance);
        assert_eq!(y_distance.round(), expected_y_distance);
        assert_eq!(z_distance.round(), expected_z_distance);
    }

    #[test]
    fn geodetic_to_enu_200m_south() {
        let reference_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63996919589),
            longitude: get_etsi_coordinate(1.40504710005),
            altitude: 0,
        };
        let relative_point = ReferencePosition {
            latitude: get_etsi_coordinate(43.63816910008),
            longitude: get_etsi_coordinate(1.40504707684),
            altitude: 0,
        };
        let expected_x_distance = 0.;
        let expected_y_distance = -200.;
        let expected_z_distance = 0.;

        let (x_distance, y_distance, z_distance) = relative_point.to_enu(&reference_point);

        assert_eq!(x_distance.round(), expected_x_distance);
        assert_eq!(y_distance.round(), expected_y_distance);
        assert_eq!(z_distance.round(), expected_z_distance);
    }

    #[test]
    fn geodetic_to_enu_same_point_gives_zero_relative_distance() {
        let reference_point = ReferencePosition {
            latitude: 488417860,
            longitude: 23678940,
            altitude: 0,
        };
        let relative_point = ReferencePosition {
            latitude: 488417860,
            longitude: 23678940,
            altitude: 0,
        };
        let expected_x_distance = 0.;
        let expected_y_distance = 0.;
        let expected_z_distance = 0.;

        let (x_distance, y_distance, z_distance) = relative_point.to_enu(&reference_point);

        assert_eq!(x_distance.round(), expected_x_distance);
        assert_eq!(y_distance.round(), expected_y_distance);
        assert_eq!(z_distance.round(), expected_z_distance);
    }

    #[test]
    fn geodetic_to_enu_to_geodetic_goes_back_to_initial_position() {
        let reference_point = ReferencePosition {
            latitude: 488417860,
            longitude: 23678940,
            altitude: 0,
        };
        let relative_point = ReferencePosition {
            latitude: 488417059,
            longitude: 23678940,
            altitude: 0,
        };

        let (e, n, u) = relative_point.to_enu(&reference_point);
        let destination = reference_point.get_offset_destination(e, n, u);

        assert_eq!(destination.latitude, relative_point.latitude);
        assert_eq!(destination.longitude, relative_point.longitude);
        assert_eq!(destination.altitude, relative_point.altitude);
    }
}
