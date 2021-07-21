use core::fmt;
use std::cmp::Ordering;
use std::{convert, str};

use crate::mqtt::topic::parse_error::ParseError;

#[derive(Clone, Debug, Default, Eq, Hash, PartialEq)]
pub struct GeoExtension {
    pub(crate) tiles: Vec<Tile>,
}

#[derive(Debug, Copy, Clone, Eq, Hash, PartialEq)]
pub(crate) enum Tile {
    Zero = 0,
    One = 1,
    Two = 2,
    Three = 3,
    All,
}

impl convert::From<u8> for Tile {
    fn from(tile: u8) -> Self {
        match tile {
            0 => Tile::Zero,
            1 => Tile::One,
            2 => Tile::Two,
            3 => Tile::Three,
            _ => panic!("Unable to convert the number {} as a Tile", tile),
        }
    }
}

impl convert::From<char> for Tile {
    fn from(tile: char) -> Self {
        match tile {
            '#' => Tile::All,
            '0'..='3' => {
                let digit = tile.to_digit(4).unwrap();
                Tile::from(digit as u8)
            }
            _ => panic!("Unable to convert the char {} as a Tile", tile),
        }
    }
}

impl str::FromStr for Tile {
    type Err = ParseError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "#" => Ok(Tile::All),
            "0" | "1" | "2" | "3" => {
                let result = u8::from_str(s).unwrap();
                Ok(Tile::from(result))
            }
            _ => Err(ParseError {
                element: s.to_string(),
            }),
        }
    }
}

impl fmt::Display for Tile {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{}",
            match self {
                Tile::Zero => "0".to_string(),
                Tile::One => "1".to_string(),
                Tile::Two => "2".to_string(),
                Tile::Three => "3".to_string(),
                Tile::All => "#".to_string(),
            }
        )
    }
}

impl GeoExtension {
    fn empty() -> GeoExtension {
        GeoExtension {
            ..Default::default()
        }
    }
}

impl str::FromStr for GeoExtension {
    type Err = ParseError;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let geo_extension = s.split('/').fold(
            GeoExtension::empty(),
            |mut geo_extension_struct, element| {
                let result = Tile::from_str(element).unwrap();
                geo_extension_struct.tiles.push(result);
                geo_extension_struct
            },
        );
        Ok(geo_extension)
    }
}

impl fmt::Display for GeoExtension {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{}",
            self.tiles
                .iter()
                .fold(String::new(), |mut geo_extension_string, &tile| {
                    geo_extension_string.push_str(format!("/{}", tile).as_str());
                    geo_extension_string
                })
        )
    }
}

impl PartialOrd for GeoExtension {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        let matching = self
            .tiles
            .iter()
            .zip(other.tiles.iter())
            .filter(|&(myself, other)| myself == other)
            .count();

        let myself_size = self.tiles.len();
        let other_size = other.tiles.len();

        match myself_size {
            _nb if _nb == matching => match other_size {
                // myself_size == matching
                _nb if _nb == matching => Some(Ordering::Equal), // other_size == matching
                _ => Some(Ordering::Greater), // other_size > matching (no other_size < matching)
            },
            _ => match other_size {
                // myself_size > matching (no myself_size < matching)
                _nb if _nb == matching => Some(Ordering::Less), // other_size == matching
                _ => None, // other_size > matching (no other_size < matching)
            },
        }
    }
}

#[cfg(test)]
mod tests {
    use std::str::FromStr;

    use crate::mqtt::topic::geo_extension::GeoExtension;

    fn create_geo_extension(geo_extension_string: &str) -> GeoExtension {
        let geo_extension_result = GeoExtension::from_str(geo_extension_string);
        assert!(geo_extension_result.is_ok());
        geo_extension_result.unwrap()
    }

    #[test]
    fn test_geo_extension_1_tile_equal() {
        let mut geo_extension = create_geo_extension("0");
        let mut geo_extension2 = create_geo_extension("0");
        assert_eq!(geo_extension, geo_extension2);
        geo_extension = create_geo_extension("1");
        geo_extension2 = create_geo_extension("1");
        assert_eq!(geo_extension, geo_extension2);
        geo_extension = create_geo_extension("2");
        geo_extension2 = create_geo_extension("2");
        assert_eq!(geo_extension, geo_extension2);
        geo_extension = create_geo_extension("3");
        geo_extension2 = create_geo_extension("3");
        assert_eq!(geo_extension, geo_extension2);
    }

    #[test]
    fn test_geo_extension_1_tile_greater() {
        let geo_extension = create_geo_extension("0");
        let mut geo_extension2 = create_geo_extension("0/0");
        assert!(geo_extension >= geo_extension2);
        assert!(geo_extension > geo_extension2);
        geo_extension2 = create_geo_extension("0/1");
        assert!(geo_extension >= geo_extension2);
        assert!(geo_extension > geo_extension2);
        geo_extension2 = create_geo_extension("0/2");
        assert!(geo_extension >= geo_extension2);
        assert!(geo_extension > geo_extension2);
        geo_extension2 = create_geo_extension("0/3");
        assert!(geo_extension >= geo_extension2);
        assert!(geo_extension > geo_extension2);
        geo_extension2 = create_geo_extension("0/1/2");
        assert!(geo_extension >= geo_extension2);
        assert!(geo_extension > geo_extension2);
        geo_extension2 = create_geo_extension("0/0/0");
        assert!(geo_extension >= geo_extension2);
        assert!(geo_extension > geo_extension2);
    }

    #[test]
    fn test_geo_extension_1_tile_less() {
        let geo_extension = create_geo_extension("0");
        let empty_geo_extension = GeoExtension::empty();
        assert!(geo_extension <= empty_geo_extension);
        assert!(geo_extension < empty_geo_extension);
    }

    #[test]
    fn test_geo_extension_1_tile_different() {
        let geo_extension = create_geo_extension("0");
        let mut geo_extension2 = create_geo_extension("1");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
        geo_extension2 = create_geo_extension("2");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
        geo_extension2 = create_geo_extension("3");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
        geo_extension2 = create_geo_extension("1/0");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
        geo_extension2 = create_geo_extension("2/1");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
        geo_extension2 = create_geo_extension("3/2");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
        geo_extension2 = create_geo_extension("3/3");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
        geo_extension2 = create_geo_extension("1/0/0");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
        geo_extension2 = create_geo_extension("1/2/3");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
    }

    #[test]
    fn test_geo_extension_2_tiles_equal() {
        let geo_extension = create_geo_extension("0/1");
        let geo_extension2 = create_geo_extension("0/1");
        assert_eq!(geo_extension, geo_extension2);
    }

    #[test]
    fn test_geo_extension_2_tiles_greater() {
        let geo_extension = create_geo_extension("0/1");
        let mut geo_extension2 = create_geo_extension("0/1/2");
        assert!(geo_extension >= geo_extension2);
        assert!(geo_extension > geo_extension2);
        geo_extension2 = create_geo_extension("0/1/2/3");
        assert!(geo_extension >= geo_extension2);
        assert!(geo_extension > geo_extension2);
    }

    #[test]
    fn test_geo_extension_2_tiles_less() {
        let geo_extension = create_geo_extension("0/1");
        let geo_extension2 = create_geo_extension("0");
        assert!(geo_extension <= geo_extension2);
        assert!(geo_extension < geo_extension2);
        let empty_geo_extension = GeoExtension::empty();
        assert!(geo_extension <= empty_geo_extension);
        assert!(geo_extension < empty_geo_extension);
    }

    #[test]
    fn test_geo_extension_2_tiles_different() {
        let geo_extension = create_geo_extension("0/1");
        let mut geo_extension2 = create_geo_extension("0/2");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
        geo_extension2 = create_geo_extension("1");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
        geo_extension2 = create_geo_extension("0/2/3");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
    }

    #[test]
    fn test_geo_extension_22_tiles_equal() {
        let geo_extension = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        let geo_extension2 = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        assert_eq!(geo_extension, geo_extension2);
    }

    #[test]
    fn test_geo_extension_22_tiles_greater() {
        let geo_extension = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        let mut geo_extension2 =
            create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2");
        assert!(geo_extension >= geo_extension2);
        assert!(geo_extension > geo_extension2);
        geo_extension2 = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3");
        assert!(geo_extension >= geo_extension2);
        assert!(geo_extension > geo_extension2);
    }

    #[test]
    fn test_linas_geo_extension_22_tiles_not_lesser_than_barcelona_geo_extension_22_tiles() {
        let geo_extension = create_geo_extension("1/2/0/2/2/2/2/3/3/0/0/3/2/0/2/0/1/0/1/0/3/1");
        let geo_extension2 = create_geo_extension("1/2/0/2/2/0/0/1/1/2/0/3/1/0/2/1/0/1/2/1/0/3");
        assert!(!(geo_extension <= geo_extension2));
        assert!(!(geo_extension < geo_extension2));
    }

    #[test]
    fn test_geo_extension_22_tiles_less() {
        let geo_extension = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        let mut geo_extension2 = create_geo_extension("0");
        assert!(geo_extension <= geo_extension2);
        assert!(geo_extension < geo_extension2);
        geo_extension2 = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0");
        assert!(geo_extension <= geo_extension2);
        assert!(geo_extension < geo_extension2);
        let empty_geo_extension = GeoExtension::empty();
        assert!(geo_extension <= empty_geo_extension);
        assert!(geo_extension < empty_geo_extension);
    }

    #[test]
    fn test_geo_extension_22_tiles_different() {
        let geo_extension = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        let mut geo_extension2 = create_geo_extension("1");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
        geo_extension2 = create_geo_extension("0/0");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
        geo_extension2 = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/2");
        assert_eq!(geo_extension.partial_cmp(&geo_extension2), None);
    }
}
