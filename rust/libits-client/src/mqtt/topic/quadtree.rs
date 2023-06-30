use std::f64::consts::PI;

const MIN_LATITUDE: f64 = -85.05112878;
const MAX_LATITUDE: f64 = 85.05112878;
const MIN_LONGITUDE: f64 = -180.;
const MAX_LONGITUDE: f64 = 180.;

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

fn lat_long_to_pixel_xy(latitude: f64, longitude: f64, level_of_detail: u16) -> PixelXY {
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

pub fn lat_lng_to_quadkey(latitude: f64, longitude: f64, level_of_detail: u16) -> String {
    tile_xy_to_quadkey(
        pixel_xy_to_tile_xy(lat_long_to_pixel_xy(latitude, longitude, level_of_detail)),
        level_of_detail,
    )
}

#[cfg(test)]
mod tests {
    use crate::mqtt::topic::quadtree;

    fn position() -> (f64, f64) {
        (48.6263556, 2.2492123)
    }

    #[test]
    fn test_lat_lng_to_quad_key_zero() {
        assert_eq!(
            "033321211101",
            quadtree::lat_lng_to_quadkey(8.3689428, -14.3165555, 12,),
        )
    }

    #[test]
    fn test_lat_lng_to_quad_key_path() {
        let (latitude, longitude) = position();
        assert_eq!(
            "120220011203",
            quadtree::lat_lng_to_quadkey(latitude, longitude, 12),
        )
    }

    #[test]
    fn test_lat_lng_to_quad_key_path_with_a_high_level_of_detail() {
        let (latitude, longitude) = position();
        assert_eq!(
            "120220011203100323",
            quadtree::lat_lng_to_quadkey(latitude, longitude, 18),
        )
    }

    #[test]
    fn test_lat_lng_to_quad_key_path_with_a_very_high_level_of_detail() {
        let (latitude, longitude) = position();
        assert_eq!(
            "120220011203100323112320",
            quadtree::lat_lng_to_quadkey(latitude, longitude, 24),
        )
    }
}
