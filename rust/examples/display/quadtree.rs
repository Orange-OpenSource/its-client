pub fn lat_lon_to_quadkey(lat: f64, lon: f64, zoom: u8) -> String {
    let (x, y) = lat_lon_to_tile(lat, lon, zoom);
    tile_to_quadkey(x, y, zoom)
}

fn lat_lon_to_tile(lat: f64, lon: f64, zoom: u8) -> (u32, u32) {
    let n = 2_u32.pow(zoom as u32) as f64;
    let x = ((lon + 180.0) / 360.0 * n).floor() as u32;
    let lat_rad = lat.to_radians();
    let y = ((1.0 - (lat_rad.tan() + 1.0 / lat_rad.cos()).ln() / std::f64::consts::PI) / 2.0 * n)
        .floor() as u32;
    (x.min(n as u32 - 1), y.min(n as u32 - 1))
}

fn tile_to_quadkey(x: u32, y: u32, zoom: u8) -> String {
    let mut quadkey = String::with_capacity(zoom as usize);
    for i in (0..zoom).rev() {
        let mut digit = 0;
        let mask = 1 << i;
        if (x & mask) != 0 {
            digit += 1;
        }
        if (y & mask) != 0 {
            digit += 2;
        }
        quadkey.push(char::from_digit(digit, 10).unwrap());
    }
    quadkey
}

pub fn ellipse_confidence_mean(semi_major: u32, semi_minor: u32) -> f64 {
    const UNAVAILABLE: u32 = 4095;
    if semi_major == UNAVAILABLE || semi_minor == UNAVAILABLE {
        return 0.0; // ou une valeur par défaut
    }
    (semi_major as f64 + semi_minor as f64) / 2.0
}
