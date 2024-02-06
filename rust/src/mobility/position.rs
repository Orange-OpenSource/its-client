// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2023 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas BUFFON <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use geo::EuclideanDistance;

use serde::{Deserialize, Serialize};
use std::fmt::{Display, Formatter, Result};
use std::hash::{Hash, Hasher};

const EARTH_RADIUS: f64 = 6_371_000.;
const EARTH_FLATTENING: f64 = 1. / 298.257223563;
const EQUATORIAL_RADIUS: f64 = 6_378_137.0;
const POLAR_RADIUS: f64 = 6_356_752.3;

/// Describes a geodesic position using SI units
#[derive(Clone, Copy, Default, Debug, PartialEq, Serialize, Deserialize)]
pub struct Position {
    /// Latitude in radians
    pub latitude: f64,
    /// Longitude in radians
    pub longitude: f64,
    /// Altitude in meters
    pub altitude: f64,
}

impl Display for Position {
    fn fmt(&self, f: &mut Formatter) -> Result {
        write!(
            f,
            "{{lat:{} ({}°), lon:{} ({}°), alt:{}}}",
            self.latitude,
            self.latitude.to_degrees(),
            self.longitude,
            self.longitude.to_degrees(),
            self.altitude,
        )
    }
}

impl Hash for Position {
    fn hash<H: Hasher>(&self, state: &mut H) {
        ((self.latitude * 1e8).round() as i64).hash(state);
        ((self.latitude * 1e8).round() as i64).hash(state);
        ((self.latitude * 1e3).round() as i64).hash(state);
    }
}

impl Eq for Position {}

pub fn position_from_degrees(lat: f64, lon: f64, alt: f64) -> Position {
    Position {
        latitude: lat.to_radians(),
        longitude: lon.to_radians(),
        altitude: alt,
    }
}

/// Returns the bearing from one Position to another
pub fn bearing(from: &Position, to: &Position) -> f64 {
    let (φ1, λ1) = (from.latitude, from.longitude);
    let (φ2, λ2) = (to.latitude, to.longitude);
    let δλ = λ2 - λ1;

    f64::atan2(
        δλ.sin() * φ2.cos(),
        φ1.cos() * φ2.sin() - φ1.sin() * φ2.cos() * δλ.cos(),
    )
}

pub fn haversine_distance(first: &Position, second: &Position) -> f64 {
    let longitude_distance = second.longitude - first.longitude;
    let latitude_distance = second.latitude - first.latitude;

    let a = (latitude_distance / 2.0).sin() * (latitude_distance / 2.0).sin()
        + first.latitude.cos()
            * second.latitude.cos()
            * (longitude_distance / 2.0).sin()
            * (longitude_distance / 2.0).sin();

    let c = 2.0 * a.sqrt().atan2((1.0 - a).sqrt());

    EARTH_RADIUS * c
}

/// φ is latitude, λ is longitude, θ is the bearing (clockwise from north), δ is the angular distance d/R; d being the distance travelled, R the earth’s radius
pub fn haversine_destination(position: &Position, bearing: f64, distance: f64) -> Position {
    let φ1 = position.latitude;
    let λ1 = position.longitude;
    let δ = distance / EARTH_RADIUS;

    let φ2 = f64::asin(φ1.sin() * δ.cos() + φ1.cos() * δ.sin() * bearing.cos());
    let λ2 = λ1
        + f64::atan2(
            bearing.sin() * δ.sin() * φ1.cos(),
            δ.cos() - φ1.sin() * φ2.sin(),
        );

    Position {
        latitude: φ2,
        longitude: λ2,
        altitude: position.altitude,
    }
}

/// Destination computation from origin, bearing and distance using Vincenty formulae
///
///  Vincenty formulae written following:
/// - <http://www.geomidpoint.com/destination/calculation.html>
/// - <https://en.wikipedia.org/wiki/Vincenty%27s_formulae>
pub fn vincenty_destination(anchor: &Position, bearing: f64, distance: f64) -> Position {
    let sin_brg = bearing.sin();
    let cos_brg = bearing.cos();
    let tu1 = (1. - EARTH_FLATTENING) * anchor.latitude.tan();
    let cu1 = 1. / (1. + tu1 * tu1).sqrt();
    let su1 = tu1 * cu1;
    let σ = f64::atan2(tu1, cos_brg);
    let sin_α = cu1 * sin_brg;
    let cos_2_α = 1. - sin_α * sin_α;
    let u_2 = cos_2_α * (EQUATORIAL_RADIUS * EQUATORIAL_RADIUS - POLAR_RADIUS * POLAR_RADIUS)
        / (POLAR_RADIUS * POLAR_RADIUS);
    let a = 1. + u_2 / 16384. * (4096. + u_2 * (-768. + u_2 * (320. - 175. * u_2)));
    let b = u_2 / 1024. * (256. + u_2 * (-128. + u_2 * (74. - 47. * u_2)));
    let mut s1 = distance / (POLAR_RADIUS * a);

    let (ss1, cs1, cs1m) = loop {
        let cs1m = (2. * σ + s1).cos();
        let ss1 = s1.sin();
        let cs1 = s1.cos();
        let ds1 = b
            * ss1
            * (cs1m
                + b / 4.
                    * (cs1 * (-1. + 2. * cs1m * cs1m)
                        - b / 6. * cs1m * (-3. + 4. * ss1 * ss1) * (-3. + 4. * cs1m * cs1m)));
        let s1p = s1;
        s1 = distance / (POLAR_RADIUS * a) + ds1;

        if (s1 - s1p).abs() <= 1e-12 {
            break (ss1, cs1, cs1m);
        }
    };

    let t = su1 * ss1 - cu1 * cs1 * cos_brg;
    let lat2 = f64::atan2(
        su1 * cs1 + cu1 * ss1 * cos_brg,
        (1. - EARTH_FLATTENING) * (sin_α * sin_α + t * t).sqrt(),
    );
    let l2 = f64::atan2(ss1 * sin_brg, cu1 * cs1 - su1 * ss1 * cos_brg);
    let c = EARTH_FLATTENING / 16. * cos_2_α * (4. + EARTH_FLATTENING * (4. - 3. * cos_2_α));
    let l = l2
        - (1. - c)
            * EARTH_FLATTENING
            * sin_α
            * (s1 + c * ss1 * (cs1m + c * cs1 * (-1. + 2. * cs1m * cs1m)));
    let lon2 = anchor.longitude + l;

    Position {
        latitude: lat2,
        longitude: lon2,
        altitude: anchor.altitude,
    }
}

/// Returns the relative position of ENU coordinates from an anchor position
pub fn enu_destination(
    anchor: &Position,
    easting_offset: f64,
    northing_offset: f64,
    up_offset: f64,
) -> Position {
    let (latitude, longitude, altitude) = map_3d::enu2geodetic(
        easting_offset,
        northing_offset,
        up_offset,
        anchor.latitude,
        anchor.longitude,
        anchor.altitude,
        map_3d::Ellipsoid::WGS84,
    );

    Position {
        latitude,
        longitude,
        altitude,
    }
}

/// Returns the minimal distance from a Position to a list of Positions
///
/// FIXME this function requires testing and consolidation (follow up in issue [97][1])
///
/// [1]: https://github.com/Orange-OpenSource/its-client/issues/97
pub fn distance_to_line(position: &Position, line: &[Position]) -> f64 {
    let mut coordinates: Vec<geo::Coord<f64>> = Vec::new();

    for position in line {
        coordinates.push(
            geo::coord! { x: position.latitude.to_degrees(), y: position.longitude.to_degrees() },
        );
    }
    let lane_line = geo::LineString::new(coordinates);

    let reference_point: geo::Point<f64> = (
        position.latitude.to_degrees(),
        position.latitude.to_degrees(),
    )
        .into();

    reference_point.euclidean_distance(&lane_line)
}

#[cfg(test)]
mod tests {
    use crate::mobility::position::{
        bearing, enu_destination, haversine_destination, haversine_distance, position_from_degrees,
        vincenty_destination,
    };

    macro_rules! test_haversine_distance {
        ($test_name:ident, $f:expr, $s:expr, $e:expr) => {
            #[test]
            fn $test_name() {
                let distance = haversine_distance(&$f, &$s);
                let distance_delta = (distance - $e).abs();

                let epsilon = 1e-2;
                assert!(
                    distance_delta < epsilon,
                    "{} !< {}",
                    distance_delta,
                    epsilon
                );
            }
        };
    }
    test_haversine_distance!(
        haversine_distance_100_meters,
        position_from_degrees(48.6244870, 2.2436370, 0.),
        position_from_degrees(48.6237420, 2.2428750, 0.),
        100.
    );
    test_haversine_distance!(
        haversine_distance_30_meters,
        position_from_degrees(48.6250049, 2.2412209, 0.),
        position_from_degrees(48.6251958, 2.2415093, 0.),
        30.
    );

    macro_rules! test_enu_destination {
        ($test_name:ident, $anchor:expr, $e:expr, $n:expr, $u:expr, $exp_dst:expr) => {
            #[test]
            fn $test_name() {
                let offset_destination = enu_destination(&$anchor, $e, $n, $u);
                let lat_abs_diff =
                    (offset_destination.latitude.abs() - $exp_dst.latitude.abs()).abs();
                let lon_abs_diff =
                    (offset_destination.longitude.abs() - $exp_dst.longitude.abs()).abs();

                assert!(lat_abs_diff < 1e-8);
                assert!(lon_abs_diff < 1e-8);
            }
        };
    }
    test_enu_destination!(
        enu_destination_hunder_meters_north,
        position_from_degrees(43.63816914950018, 1.4031882, 0.),
        0.,
        100.,
        0.,
        position_from_degrees(43.63906919748, 1.4031882, 0.)
    );
    test_enu_destination!(
        enu_destination_hundred_meters_east,
        position_from_degrees(43.63816914950018, 1.4031882, 0.),
        100.,
        0.,
        0.,
        position_from_degrees(43.63816914950018, 1.40442743, 0.)
    );

    macro_rules! test_bearing {
        ($test_name:ident, $dst:expr, $exp_bearing:expr) => {
            #[test]
            fn $test_name() {
                let anchor = position_from_degrees(48.62519582726, 2.24150938995, 0.);
                let epsilon = 1e-2;

                let bearing = bearing(&anchor, &$dst);
                let bearing_in_degrees = (bearing.to_degrees() + 360.) % 360.;
                let bearing_delta = ($exp_bearing - bearing_in_degrees).abs();

                assert!(bearing_delta < epsilon, "{} !< {}", bearing_delta, epsilon);
            }
        };
    }
    test_bearing!(
        bearing_north_0_deg,
        position_from_degrees(48.80504512538, 2.24150940001, 0.),
        0.
    );
    test_bearing!(
        bearing_east_90_deg,
        position_from_degrees(48.62487660338, 2.5128078045, 0.),
        90.
    );
    test_bearing!(
        bearing_south_180_deg,
        position_from_degrees(48.44534088416, 2.24150940001, 0.),
        180.
    );
    test_bearing!(
        bearing_west_270_deg,
        position_from_degrees(48.62487660336, 1.9702109754, 0.),
        270.
    );
    test_bearing!(
        bearing_south_west_225_deg,
        position_from_degrees(48.62500535973, 2.24122119038, 0.),
        225.
    );
    test_bearing!(
        bearing_south_east_40_deg,
        position_from_degrees(48.76266875163, 2.41667377595, 0.),
        40.
    );
    test_bearing!(
        bearing_north_east_40_deg,
        position_from_degrees(47.12910495406, 4.26723335764, 0.),
        137.
    );
    test_bearing!(
        bearing_north_west_330_deg,
        position_from_degrees(48.78075523914, 2.1051415518, 0.),
        330.
    );

    macro_rules! test_vincenty_destination {
        ($test_name:ident, $bearing:expr, $distance:expr, $exp_dst:expr) => {
            #[test]
            fn $test_name() {
                let position = position_from_degrees(48.62519582726, 2.24150938995, 0.);
                let epsilon = 1e-7;
                let _position_deg_precision = 10e7;

                let destination = vincenty_destination(&position, $bearing.to_radians(), $distance);
                let lat_delta =
                    (destination.latitude.to_degrees() - $exp_dst.latitude.to_degrees()).abs();
                let lon_delta =
                    (destination.longitude.to_degrees() - $exp_dst.longitude.to_degrees()).abs();

                println!("Expected: {}", $exp_dst);
                println!("Actual: {}", destination);

                assert!(
                    lat_delta < epsilon,
                    "{} !< {} (expected: {}, actual: {})",
                    lat_delta,
                    epsilon,
                    $exp_dst.latitude.to_degrees(),
                    destination.latitude.to_degrees()
                );

                assert!(
                    lon_delta < epsilon,
                    "{} !< {} (expected: {}, actual: {})",
                    lon_delta,
                    epsilon,
                    $exp_dst.longitude.to_degrees(),
                    destination.longitude.to_degrees()
                );
            }
        };
    }
    test_vincenty_destination!(
        vincenty_north_bearing_360_deg_100m_destination,
        360f64,
        100.,
        position_from_degrees(48.62609508779, 2.24150940001, 0.)
    );
    test_vincenty_destination!(
        vincenty_north_bearing_0_deg_100m_destination,
        0f64,
        100.,
        position_from_degrees(48.62609508779, 2.24150940001, 0.)
    );
    test_vincenty_destination!(
        vincenty_south_bearing_180_deg_100m_destination,
        180f64,
        100.,
        position_from_degrees(48.62429656659, 2.24150940001, 0.)
    );
    test_vincenty_destination!(
        vincenty_south_bearing_minus_180_deg_100m_destination,
        -180f64,
        100.,
        position_from_degrees(48.62429656659, 2.24150940001, 0.)
    );
    test_vincenty_destination!(
        vincenty_east_bearing_90_deg_100m_destination,
        90f64,
        100.,
        position_from_degrees(48.62519580005, 2.24286588773, 0.)
    );
    test_vincenty_destination!(
        vincenty_east_bearing_minus_270_deg_100m_destination,
        -270f64,
        100.,
        position_from_degrees(48.62519580005, 2.24286588773, 0.)
    );
    test_vincenty_destination!(
        vincenty_west_bearing_270_deg_100m_destination,
        270f64,
        100.,
        position_from_degrees(48.62519580005, 2.24015289217, 0.)
    );
    test_vincenty_destination!(
        vincenty_west_bearing_minus_90_deg_100m_destination,
        -90f64,
        100.,
        position_from_degrees(48.62519580005, 2.24015289217, 0.)
    );

    macro_rules! test_haversine_destination {
        ($test_name:ident, $bearing:expr, $distance:expr, $exp_dst:expr) => {
            #[test]
            fn $test_name() {
                let position = position_from_degrees(48.62519582726, 2.24150938995, 0.);
                let epsilon = 1e-7;
                let _position_deg_precision = 10e7;

                let destination =
                    haversine_destination(&position, $bearing.to_radians(), $distance);
                let lat_delta =
                    (destination.latitude.to_degrees() - $exp_dst.latitude.to_degrees()).abs();
                let lon_delta =
                    (destination.longitude.to_degrees() - $exp_dst.longitude.to_degrees()).abs();

                println!("Expected: {}", $exp_dst);
                println!("Actual: {}", destination);

                assert!(
                    lat_delta < epsilon,
                    "{} !< {} (expected: {}, actual: {})",
                    lat_delta,
                    epsilon,
                    $exp_dst.latitude.to_degrees(),
                    destination.latitude.to_degrees()
                );

                assert!(
                    lon_delta < epsilon,
                    "{} !< {} (expected: {}, actual: {})",
                    lon_delta,
                    epsilon,
                    $exp_dst.longitude.to_degrees(),
                    destination.longitude.to_degrees()
                );
            }
        };
    }
    test_haversine_destination!(
        haversine_north_bearing_360_deg_100m_destination,
        360f64,
        100.,
        position_from_degrees(48.62609508779, 2.24150940001, 0.)
    );
    test_haversine_destination!(
        haversine_north_bearing_0_deg_100m_destination,
        0f64,
        100.,
        position_from_degrees(48.62609508779, 2.24150940001, 0.)
    );
    test_haversine_destination!(
        haversine_south_bearing_180_deg_100m_destination,
        180f64,
        100.,
        position_from_degrees(48.62429656659, 2.24150940001, 0.)
    );
    test_haversine_destination!(
        haversine_south_bearing_minus_180_deg_100m_destination,
        -180f64,
        100.,
        position_from_degrees(48.62429656659, 2.24150940001, 0.)
    );
}
