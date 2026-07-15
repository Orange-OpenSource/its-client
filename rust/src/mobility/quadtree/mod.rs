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

use self::quadkey::Quadkey;
use std::f64::consts::PI;

pub mod parse_error;
pub mod quadkey;
pub mod tile;

const MIN_LATITUDE: f64 = -85.05112878;
const MAX_LATITUDE: f64 = 85.05112878;
const MIN_LONGITUDE: f64 = -180.;
const MAX_LONGITUDE: f64 = 180.;

/// Geographic latitude lower bound used for position validation.
pub const GEO_MIN_LATITUDE: f64 = -90.0;
/// Geographic latitude upper bound used for position validation.
pub const GEO_MAX_LATITUDE: f64 = 90.0;
/// Geographic longitude lower bound used for position validation.
pub const GEO_MIN_LONGITUDE: f64 = -180.0;
/// Geographic longitude upper bound used for position validation.
pub const GEO_MAX_LONGITUDE: f64 = 180.0;
/// Scale factor used by ETSI encoded latitude/longitude integer values.
pub const GEO_COORDINATE_SCALE: f64 = 1e7;
/// Approximate meters represented by one degree of latitude.
pub const METERS_PER_DEGREE_LATITUDE: f64 = 111_320.0;
/// Minimum absolute cosine value used to avoid division by zero near poles.
pub const MIN_ABS_COS_LATITUDE: f64 = 1e-6;
/// Number of centimeters in one meter.
pub const CENTIMETERS_PER_METER: f64 = 100.0;
/// Sentinel value used by ETSI confidence ellipse to indicate unavailable confidence.
pub const CONFIDENCE_UNAVAILABLE: u32 = 4095;

/// 26-char quadkey is the deepest quadkey that is needed
/// to represent a region that is at most 1m × 1m in size
const DEFAULT_DEPTH: u16 = 26;

/// Convenience struct to hold a list of quadkeys
///
/// This is not a real tree representation but just a set of root-to-leaf branches
/// It might be interesting to look for en existing implementation of such a tree or to create one
pub type Quadtree = Vec<Quadkey>;

pub fn contains(quadtree: &Quadtree, quadkey: &Quadkey) -> bool {
    quadtree.iter().any(|qk| quadkey <= qk)
}

/// Converts geographic coordinates to a quadkey at a given depth.
///
/// Naming rationale:
/// - `coordinates_*` is the canonical API in this module (`latitude`, `longitude`)
/// - `lat_lon_*` aliases are kept for compatibility with display examples.
pub fn coordinates_to_quadkey(latitude: f64, longitude: f64, depth: u16) -> String {
    tile_xy_to_quadkey(
        pixel_xy_to_tile_xy(coordinates_to_pixel_xy(latitude, longitude, depth)),
        depth,
    )
}

/// Backward-compatible alias used by display examples.
pub fn lat_lon_to_quadkey(lat: f64, lon: f64, zoom: u8) -> String {
    coordinates_to_quadkey(lat, lon, zoom as u16)
}

struct PixelXY {
    x: i64,
    y: i64,
}

struct TileXY {
    x: i64,
    y: i64,
}

fn compute_map_size(level_of_detail: u16) -> i64 {
    256 << level_of_detail
}

fn clip(n: f64, min_value: f64, max_value: f64) -> f64 {
    if n > min_value {
        if n < max_value { n } else { max_value }
    } else {
        min_value
    }
}

fn coordinates_to_pixel_xy(latitude: f64, longitude: f64, level_of_detail: u16) -> PixelXY {
    let latitude = clip(latitude, MIN_LATITUDE, MAX_LATITUDE);
    let longitude = clip(longitude, MIN_LONGITUDE, MAX_LONGITUDE);

    let x = (longitude + 180.) / 360.;
    let sin_latitude = (latitude * PI / 180.).sin();
    let basis = (1. + sin_latitude) / (1. - sin_latitude);
    let y = 0.5 - basis.ln() / (4. * PI);

    let map_size = compute_map_size(level_of_detail);
    let pixel_x = clip(x * map_size as f64 + 0.5, 0., (map_size - 1) as f64) as i64;
    let pixel_y = clip(y * map_size as f64 + 0.5, 0., (map_size - 1) as f64) as i64;
    PixelXY {
        x: pixel_x,
        y: pixel_y,
    }
}

fn pixel_xy_to_tile_xy(pixel: PixelXY) -> TileXY {
    let tile_x = pixel.x / 256;
    let tile_y = pixel.y / 256;
    TileXY {
        x: tile_x,
        y: tile_y,
    }
}

fn tile_xy_to_quadkey(tile: TileXY, level_of_detail: u16) -> String {
    let tile_x = tile.x;
    let tile_y = tile.y;
    let mut quadkey = String::new();
    for i in (1..level_of_detail + 1).rev() {
        let mut digit = 0;
        let mask = 1 << (i - 1);
        if (tile_x & mask) != 0 {
            digit += 1;
        }
        if (tile_y & mask) != 0 {
            digit += 2;
        }
        quadkey.push(char::from(b'0' + digit as u8));
    }
    quadkey
}

fn quadkey_to_tile_xy(quadkey: &str) -> (u32, u32, usize) {
    let mut tile_x = 0u32;
    let mut tile_y = 0u32;
    let zoom = quadkey.len();

    for (index, character) in quadkey.chars().enumerate() {
        let bit = zoom - index;
        let mask = 1 << (bit - 1);
        match character {
            '1' => tile_x |= mask,
            '2' => tile_y |= mask,
            '3' => {
                tile_x |= mask;
                tile_y |= mask;
            }
            _ => {}
        }
    }

    (tile_x, tile_y, zoom)
}

/// Converts a quadkey to its bounding box: `(min_lon, min_lat, max_lon, max_lat)`.
pub fn quadkey_to_bbox(quadkey: &str) -> (f64, f64, f64, f64) {
    let (tile_x, tile_y, zoom) = quadkey_to_tile_xy(quadkey);
    let n = 2_u32.pow(zoom as u32) as f64;
    let min_lon = tile_x as f64 / n * 360.0 - 180.0;
    let max_lon = (tile_x + 1) as f64 / n * 360.0 - 180.0;
    let north_lat_rad = PI * (1.0 - 2.0 * tile_y as f64 / n);
    let south_lat_rad = PI * (1.0 - 2.0 * (tile_y + 1) as f64 / n);
    let max_lat = north_lat_rad.sinh().atan() * 180.0 / PI;
    let min_lat = south_lat_rad.sinh().atan() * 180.0 / PI;

    (min_lon, min_lat, max_lon, max_lat)
}

/// Converts a quadkey to the centre of the tile as `(latitude, longitude)`.
pub fn quadkey_to_coordinates(quadkey: &str) -> (f64, f64) {
    let (south_west_lon, south_west_lat, north_east_lon, north_east_lat) = quadkey_to_bbox(quadkey);
    let center_lat = (south_west_lat + north_east_lat) / 2.0;
    let center_lon = (south_west_lon + north_east_lon) / 2.0;
    (center_lat, center_lon)
}

/// Backward-compatible alias used by display examples.
pub fn quadkey_to_lat_lon(quadkey: &str) -> (f64, f64) {
    quadkey_to_coordinates(quadkey)
}

/// Checks whether a quadkey tile overlaps a bounding box.
pub fn quadkey_in_bbox(
    quadkey: &str,
    min_lon: f64,
    min_lat: f64,
    max_lon: f64,
    max_lat: f64,
) -> bool {
    let (tile_min_lon, tile_min_lat, tile_max_lon, tile_max_lat) = quadkey_to_bbox(quadkey);
    tile_max_lon > min_lon
        && tile_min_lon < max_lon
        && tile_max_lat > min_lat
        && tile_min_lat < max_lat
}

/// Computes the mean of the position confidence ellipse.
/// Returns 0.0 if either semi-axis is UNAVAILABLE (4095).
pub fn ellipse_confidence_mean(semi_major: u32, semi_minor: u32) -> f64 {
    if semi_major == CONFIDENCE_UNAVAILABLE || semi_minor == CONFIDENCE_UNAVAILABLE {
        return 0.0;
    }
    (semi_major as f64 + semi_minor as f64) / 2.0
}

/// Validates that `latitude`/`longitude` fall within the valid geographic ranges.
///
/// Returns `Some((latitude, longitude))` when both values are within bounds, `None` otherwise.
pub fn validate_coordinates(latitude: f64, longitude: f64) -> Option<(f64, f64)> {
    if (GEO_MIN_LATITUDE..=GEO_MAX_LATITUDE).contains(&latitude)
        && (GEO_MIN_LONGITUDE..=GEO_MAX_LONGITUDE).contains(&longitude)
    {
        Some((latitude, longitude))
    } else {
        None
    }
}

/// Converts ETSI encoded integer latitude/longitude to decimal degrees and validates them.
///
/// The encoded values are divided by [`GEO_COORDINATE_SCALE`] before validation.
pub fn scaled_coordinates(latitude: i64, longitude: i64) -> Option<(f64, f64)> {
    let latitude = latitude as f64 / GEO_COORDINATE_SCALE;
    let longitude = longitude as f64 / GEO_COORDINATE_SCALE;
    validate_coordinates(latitude, longitude)
}

/// Converts a local ENU offset (in centimeters, x=East, y=North) around an anchor
/// position into geographic coordinates using an approximate geodetic conversion.
///
/// Returns the validated `(latitude, longitude)` or `None` if out of bounds.
pub fn offset_to_coordinates(
    anchor_latitude: f64,
    anchor_longitude: f64,
    x_cm: i64,
    y_cm: i64,
) -> Option<(f64, f64)> {
    let x_m = x_cm as f64 / CENTIMETERS_PER_METER;
    let y_m = y_cm as f64 / CENTIMETERS_PER_METER;

    let latitude = anchor_latitude + y_m / METERS_PER_DEGREE_LATITUDE;
    let cos_latitude = anchor_latitude
        .to_radians()
        .cos()
        .abs()
        .max(MIN_ABS_COS_LATITUDE);
    let longitude = anchor_longitude + x_m / (METERS_PER_DEGREE_LATITUDE * cos_latitude);

    validate_coordinates(latitude, longitude)
}

#[cfg(test)]
mod tests {
    use crate::mobility::quadtree;
    use crate::mobility::quadtree::quadkey::Quadkey;
    use crate::mobility::quadtree::{Quadtree, contains};
    use std::str::FromStr;

    use lazy_static::lazy_static;

    fn position() -> (f64, f64) {
        (48.6263556, 2.2492123)
    }

    #[test]
    fn test_coordinates_to_quadkey_zero() {
        assert_eq!(
            "033321211101",
            quadtree::coordinates_to_quadkey(8.3689428, -14.3165555, 12,),
        )
    }

    #[test]
    fn test_coordinates_to_quadkey_path() {
        let (latitude, longitude) = position();
        assert_eq!(
            "120220011203",
            quadtree::coordinates_to_quadkey(latitude, longitude, 12),
        )
    }

    #[test]
    fn test_coordinates_to_quadkey_path_with_high_level_of_detail() {
        let (latitude, longitude) = position();
        assert_eq!(
            "120220011203100323",
            quadtree::coordinates_to_quadkey(latitude, longitude, 18),
        )
    }

    #[test]
    fn test_coordinates_to_quadkey_path_with_very_high_level_of_detail() {
        let (latitude, longitude) = position();
        assert_eq!(
            "120220011203100323112320",
            quadtree::coordinates_to_quadkey(latitude, longitude, 24),
        )
    }

    #[test]
    fn test_lat_lon_alias_to_quadkey_known_location() {
        let quadkey = quadtree::lat_lon_to_quadkey(48.8566, 2.3522, 10);
        assert_eq!(quadkey.len(), 10);
        assert!(quadkey.starts_with("120"));
    }

    #[test]
    fn test_quadkey_round_trip_coordinates() {
        let lat = 43.3;
        let lon = -0.37;
        let zoom = 18;
        let quadkey = quadtree::lat_lon_to_quadkey(lat, lon, zoom);
        let (recovered_lat, recovered_lon) = quadtree::quadkey_to_lat_lon(&quadkey);

        assert!((recovered_lat - lat).abs() < 0.01);
        assert!((recovered_lon - lon).abs() < 0.01);
    }

    #[test]
    fn test_quadkey_to_bbox() {
        let quadkey = quadtree::lat_lon_to_quadkey(48.8566, 2.3522, 10);
        let (min_lon, min_lat, max_lon, max_lat) = quadtree::quadkey_to_bbox(&quadkey);
        assert!(min_lon < max_lon);
        assert!(min_lat < max_lat);
        assert!(min_lat < 48.8566 && max_lat > 48.8566);
        assert!(min_lon < 2.3522 && max_lon > 2.3522);
    }

    #[test]
    fn test_ellipse_confidence_mean_normal() {
        assert!((quadtree::ellipse_confidence_mean(100, 200) - 150.0).abs() < f64::EPSILON);
    }

    #[test]
    fn test_ellipse_confidence_mean_unavailable() {
        assert!((quadtree::ellipse_confidence_mean(4095, 200) - 0.0).abs() < f64::EPSILON);
        assert!((quadtree::ellipse_confidence_mean(100, 4095) - 0.0).abs() < f64::EPSILON);
    }

    #[test]
    fn test_validate_coordinates_in_range() {
        assert_eq!(
            Some((48.8566, 2.3522)),
            quadtree::validate_coordinates(48.8566, 2.3522)
        );
    }

    #[test]
    fn test_validate_coordinates_out_of_range() {
        assert_eq!(None, quadtree::validate_coordinates(91.0, 2.0));
        assert_eq!(None, quadtree::validate_coordinates(48.0, 181.0));
    }

    #[test]
    fn test_scaled_coordinates() {
        assert_eq!(
            Some((48.8566, 2.3522)),
            quadtree::scaled_coordinates(488_566_000, 23_522_000)
        );
    }

    #[test]
    fn test_scaled_coordinates_out_of_range() {
        assert_eq!(None, quadtree::scaled_coordinates(2_000_000_000, 0));
    }

    #[test]
    fn test_offset_to_coordinates_zero_offset() {
        let result = quadtree::offset_to_coordinates(48.8566, 2.3522, 0, 0);
        assert_eq!(Some((48.8566, 2.3522)), result);
    }

    #[test]
    fn test_offset_to_coordinates_north_east() {
        // 111320 cm north == 1 metre north == ~1/111320 degree of latitude
        let (lat, lon) = quadtree::offset_to_coordinates(0.0, 0.0, 11_132_000, 11_132_000).unwrap();
        assert!((lat - 1.0).abs() < 1e-6);
        assert!((lon - 1.0).abs() < 1e-6);
    }

    #[test]
    fn test_quadkey_in_bbox_inside() {
        let quadkey = quadtree::lat_lon_to_quadkey(48.8566, 2.3522, 14);
        assert!(quadtree::quadkey_in_bbox(&quadkey, 2.0, 48.5, 2.8, 49.0));
    }

    #[test]
    fn test_quadkey_in_bbox_outside() {
        let quadkey = quadtree::lat_lon_to_quadkey(48.8566, 2.3522, 14);
        assert!(!quadtree::quadkey_in_bbox(&quadkey, -1.0, 51.0, 0.5, 52.0));
    }

    lazy_static! {
        static ref SHORT_ROOT_TREE: Quadtree = vec![Quadkey::from_str("12020").unwrap()];
        static ref DEEP_LEAVES_TREE: Quadtree = vec![
            Quadkey::from_str("12020322313211").unwrap(),
            Quadkey::from_str("12020322313213").unwrap(),
            Quadkey::from_str("12020322313302").unwrap(),
            Quadkey::from_str("12020322313230").unwrap(),
            Quadkey::from_str("12020322313221").unwrap(),
            Quadkey::from_str("12020322313222").unwrap(),
            Quadkey::from_str("120203223133032").unwrap(),
            Quadkey::from_str("120203223133030").unwrap(),
            Quadkey::from_str("120203223133012").unwrap(),
            Quadkey::from_str("120203223133003").unwrap(),
            Quadkey::from_str("120203223133002").unwrap(),
            Quadkey::from_str("120203223133000").unwrap(),
            Quadkey::from_str("120203223132103").unwrap(),
            Quadkey::from_str("120203223132121").unwrap(),
            Quadkey::from_str("120203223132123").unwrap(),
            Quadkey::from_str("120203223132310").unwrap(),
            Quadkey::from_str("120203223132311").unwrap(),
            Quadkey::from_str("120203223132122").unwrap(),
            Quadkey::from_str("120203223132033").unwrap(),
            Quadkey::from_str("120203223132032").unwrap(),
            Quadkey::from_str("120203223132023").unwrap(),
            Quadkey::from_str("120203223132201").unwrap(),
            Quadkey::from_str("120203223132203").unwrap(),
            Quadkey::from_str("120203223132202").unwrap(),
            Quadkey::from_str("120203223123313").unwrap(),
            Quadkey::from_str("120203223123331").unwrap(),
            Quadkey::from_str("120203223123333").unwrap(),
            Quadkey::from_str("120203223132230").unwrap(),
            Quadkey::from_str("1202032231330103").unwrap(),
            Quadkey::from_str("12020322313300133").unwrap(),
            Quadkey::from_str("12020322313301022").unwrap(),
            Quadkey::from_str("12020322313301023").unwrap(),
        ];
    }

    macro_rules! test_quadtree_contains {
        ($test_name:ident, $tree:expr, $key:expr) => {
            #[test]
            fn $test_name() {
                let contained = contains(&$tree, &$key);

                assert!(contained);
            }
        };
    }
    test_quadtree_contains!(
        several_deep_leaves_quadtree_contains_exact_quadkey,
        DEEP_LEAVES_TREE,
        Quadkey::from_str("12020322313301023").unwrap()
    );
    test_quadtree_contains!(
        several_deep_leaves_quadtree_contains_deeper_quadkey,
        DEEP_LEAVES_TREE,
        Quadkey::from_str("1202032231330102321").unwrap()
    );
    test_quadtree_contains!(
        short_root_only_tree_contains_exact_key,
        SHORT_ROOT_TREE,
        Quadkey::from_str("12020").unwrap()
    );
    test_quadtree_contains!(
        short_root_only_tree_contains_deeper_key,
        SHORT_ROOT_TREE,
        Quadkey::from_str("12020123").unwrap()
    );

    macro_rules! test_quadtree_does_not_contain {
        ($test_name:ident, $tree:expr, $key:expr) => {
            #[test]
            fn $test_name() {
                let contained = contains(&$tree, &$key);

                assert!(!contained);
            }
        };
    }
    test_quadtree_does_not_contain!(
        quadtree_does_not_contain_shorter_quadkey,
        DEEP_LEAVES_TREE,
        Quadkey::from_str("12020322").unwrap()
    );
    test_quadtree_does_not_contain!(
        quadtree_does_not_contain_same_depth_quadkey,
        DEEP_LEAVES_TREE,
        Quadkey::from_str("12020322313300130").unwrap()
    );
    test_quadtree_does_not_contain!(
        quadtree_does_not_contain_same_deeper_quadkey,
        DEEP_LEAVES_TREE,
        Quadkey::from_str("02020322313300130").unwrap()
    );

    #[test]
    fn test_toulouse_zoom26() {
        let lat = 43.6004;
        let lon = 1.4500;
        let qk = quadtree::lat_lon_to_quadkey(lat, lon, 26);
        eprintln!("Toulouse zoom 26: {}", qk);

        let lat2 = 43.6005;
        let lon2 = 1.4501;
        let qk2 = quadtree::lat_lon_to_quadkey(lat2, lon2, 26);
        eprintln!("Toulouse offset zoom 26: {}", qk2);

        // PO positions
        let (po_lat, po_lon) = quadtree::offset_to_coordinates(lat, lon, 500, -200).unwrap();
        let po_qk = quadtree::lat_lon_to_quadkey(po_lat, po_lon, 26);
        eprintln!("PO1 zoom 26: {} ({}, {})", po_qk, po_lat, po_lon);

        let (po_lat2, po_lon2) = quadtree::offset_to_coordinates(lat2, lon2, 600, -300).unwrap();
        let po_qk2 = quadtree::lat_lon_to_quadkey(po_lat2, po_lon2, 26);
        eprintln!("PO2 zoom 26: {} ({}, {})", po_qk2, po_lat2, po_lon2);
    }
}
