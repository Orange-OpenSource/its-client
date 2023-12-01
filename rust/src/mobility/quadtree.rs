use crate::mobility::quadtree::quadkey::Quadkey;
use std::f64::consts::PI;

pub mod parse_error;
pub mod quadkey;
pub mod tile;

const MIN_LATITUDE: f64 = -85.05112878;
const MAX_LATITUDE: f64 = 85.05112878;
const MIN_LONGITUDE: f64 = -180.;
const MAX_LONGITUDE: f64 = 180.;

/// 26-char quadkey is the deepest quadkey that is needed
/// to represent a region that is at most 1m×1m in size
const DEFAULT_DEPTH: u16 = 26;

/// Convenience struct to hold a list of quadkeys
///
/// This is not a real tree representation but just a set of root-to-leaf branches
/// It might be interesting to look for en existing implementation of such a tree or to create one
pub type Quadtree = Vec<Quadkey>;

pub fn contains(quadtree: &Quadtree, quadkey: &Quadkey) -> bool {
    quadtree.iter().any(|qk| quadkey <= qk)
}

fn coordinates_to_quadkey(latitude: f64, longitude: f64, depth: u16) -> String {
    tile_xy_to_quadkey(
        pixel_xy_to_tile_xy(coordinates_to_pixel_xy(latitude, longitude, depth)),
        depth,
    )
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
        if n < max_value {
            n
        } else {
            max_value
        }
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
    let mut quad_key = String::new();
    for i in (1..level_of_detail + 1).rev() {
        let mut digit = 0;
        let mask = 1 << (i - 1);
        if (tile_x & mask) != 0 {
            digit += 1;
        }
        if (tile_y & mask) != 0 {
            digit += 2;
        }
        quad_key += digit.to_string().as_str();
    }
    quad_key
}

#[cfg(test)]
mod tests {
    use crate::mobility::quadtree;
    use crate::mobility::quadtree::quadkey::Quadkey;
    use crate::mobility::quadtree::{contains, Quadtree};
    use std::str::FromStr;

    use lazy_static::lazy_static;

    fn position() -> (f64, f64) {
        (48.6263556, 2.2492123)
    }

    #[test]
    fn test_lat_lng_to_quad_key_zero() {
        assert_eq!(
            "033321211101",
            quadtree::coordinates_to_quadkey(8.3689428, -14.3165555, 12,),
        )
    }

    #[test]
    fn test_lat_lng_to_quad_key_path() {
        let (latitude, longitude) = position();
        assert_eq!(
            "120220011203",
            quadtree::coordinates_to_quadkey(latitude, longitude, 12),
        )
    }

    #[test]
    fn test_lat_lng_to_quad_key_path_with_a_high_level_of_detail() {
        let (latitude, longitude) = position();
        assert_eq!(
            "120220011203100323",
            quadtree::coordinates_to_quadkey(latitude, longitude, 18),
        )
    }

    #[test]
    fn test_lat_lng_to_quad_key_path_with_a_very_high_level_of_detail() {
        let (latitude, longitude) = position();
        assert_eq!(
            "120220011203100323112320",
            quadtree::coordinates_to_quadkey(latitude, longitude, 24),
        )
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
}
