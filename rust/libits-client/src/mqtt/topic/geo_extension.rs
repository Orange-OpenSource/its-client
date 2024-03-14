// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
use core::fmt;
use std::cmp::Ordering;
use std::str;

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

impl From<u8> for Tile {
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

impl From<char> for Tile {
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
        if s.is_empty() {
            Err(ParseError {
                element: s.to_string(),
            })
        } else {
            let number_of_slash = s.chars().filter(|&character| character == '/').count();
            if number_of_slash > 0 && ((number_of_slash * 2) + 1 == s.len()) {
                // string with slash separator and one slash for each character except first
                Ok(s.split('/').fold(
                    GeoExtension::empty(),
                    |mut geo_extension_struct, element| {
                        let result = Tile::from_str(element).unwrap();
                        geo_extension_struct.tiles.push(result);
                        geo_extension_struct
                    },
                ))
            } else {
                // string without slash separator
                Ok(s.chars().fold(
                    GeoExtension::empty(),
                    |mut geo_extension_struct, element| {
                        geo_extension_struct.tiles.push(Tile::from(element));
                        geo_extension_struct
                    },
                ))
            }
        }
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
    use std::cmp::Ordering::{Greater, Less};
    use std::str::FromStr;

    use crate::mqtt::topic::geo_extension::{GeoExtension, Tile};

    fn create_geo_extension(geo_extension_string: &str) -> GeoExtension {
        let geo_extension_result = GeoExtension::from_str(geo_extension_string);
        assert!(geo_extension_result.is_ok());
        geo_extension_result.unwrap()
    }

    #[test]
    fn test_create_geo_extension_with_slash() {
        let geo_extension = create_geo_extension("0/1/2/3");
        assert_eq!(geo_extension.tiles[0], Tile::Zero);
        assert_eq!(geo_extension.tiles[1], Tile::One);
        assert_eq!(geo_extension.tiles[2], Tile::Two);
        assert_eq!(geo_extension.tiles[3], Tile::Three);
    }

    #[test]
    fn test_create_geo_extension_without_slash() {
        let geo_extension = create_geo_extension("0123");
        assert_eq!(geo_extension.tiles[0], Tile::Zero);
        assert_eq!(geo_extension.tiles[1], Tile::One);
        assert_eq!(geo_extension.tiles[2], Tile::Two);
        assert_eq!(geo_extension.tiles[3], Tile::Three);
    }

    #[test]
    #[should_panic]
    fn test_fail_create_geo_extension_empty() {
        create_geo_extension("");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_geo_extension_with_slash() {
        create_geo_extension("/");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_geo_extension_with_a_character() {
        create_geo_extension("a");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_geo_extension_with_a_too_big_number() {
        create_geo_extension("4");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_geo_extension_with_slash_with_a_character_at_the_beginning() {
        create_geo_extension("a/1/2/3");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_geo_extension_with_slash_with_a_character_in_the_midlle() {
        create_geo_extension("0/1/a/3");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_geo_extension_with_slash_with_a_character_at_the_end() {
        create_geo_extension("0/1/2/a");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_geo_extension_without_slash_with_a_character_at_the_beginning() {
        create_geo_extension("a123");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_geo_extension_without_slash_with_a_character_in_the_midlle() {
        create_geo_extension("01a3");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_geo_extension_without_slash_with_a_character_at_the_end() {
        create_geo_extension("012a");
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

    #[test]
    fn test_same_length_but_not_siblings_are_not_partially_ordered() {
        let linas = create_geo_extension("1/2/0/2/2/2/2/3/3/0/0/3/2/0/2/0/1/0/1/0/3/1");
        let barcelona = create_geo_extension("1/2/0/2/2/0/0/1/1/2/0/3/1/0/2/1/0/1/2/1/0/3");
        assert_eq!(linas.partial_cmp(&barcelona), None);
    }

    #[test]
    fn test_siblings_are_not_partially_ordered() {
        let sibling_0 = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/0");
        let sibling_1 = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        let sibling_2 = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/2");
        let sibling_3 = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/3");

        assert_eq!(sibling_0.partial_cmp(&sibling_1), None);
        assert_eq!(sibling_0.partial_cmp(&sibling_2), None);
        assert_eq!(sibling_0.partial_cmp(&sibling_3), None);

        assert_eq!(sibling_1.partial_cmp(&sibling_0), None);
        assert_eq!(sibling_1.partial_cmp(&sibling_2), None);
        assert_eq!(sibling_1.partial_cmp(&sibling_3), None);

        assert_eq!(sibling_2.partial_cmp(&sibling_0), None);
        assert_eq!(sibling_2.partial_cmp(&sibling_1), None);
        assert_eq!(sibling_2.partial_cmp(&sibling_3), None);

        assert_eq!(sibling_3.partial_cmp(&sibling_0), None);
        assert_eq!(sibling_3.partial_cmp(&sibling_1), None);
        assert_eq!(sibling_3.partial_cmp(&sibling_2), None);
    }

    #[test]
    fn test_deeper_is_lesser() {
        let less_deep = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0");
        let deeper = create_geo_extension("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");

        assert_eq!(less_deep.partial_cmp(&deeper), Some(Greater));
        assert_eq!(deeper.partial_cmp(&less_deep), Some(Less));
    }
}
