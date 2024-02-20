// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use crate::mobility::position::Position;
use crate::mobility::quadtree::parse_error::ParseError;
use crate::mobility::quadtree::tile::Tile;
use crate::mobility::quadtree::{coordinates_to_quadkey, DEFAULT_DEPTH};
use core::fmt;
use std::cmp::Ordering;
use std::str;
use std::str::FromStr;

#[derive(Clone, Debug, Default, Eq, Hash, PartialEq)]
pub struct Quadkey {
    pub(crate) tiles: Vec<Tile>,
}

impl Quadkey {
    fn len(&self) -> usize {
        self.tiles.len()
    }

    pub fn push(&mut self, tile: Tile) {
        self.tiles.push(tile);
    }

    pub fn reduce(&mut self, depth: usize) {
        self.tiles.truncate(depth);
    }

    pub fn as_reduced(&self, depth: usize) -> Self {
        let mut truncated_tiles = self.tiles.clone();
        truncated_tiles.truncate(depth);
        Quadkey {
            tiles: truncated_tiles,
        }
    }
}

impl From<Position> for Quadkey {
    fn from(value: Position) -> Self {
        Quadkey::from(&value)
    }
}

impl From<&Position> for Quadkey {
    fn from(position: &Position) -> Self {
        Quadkey::from_str(
            coordinates_to_quadkey(
                position.latitude.to_degrees(),
                position.longitude.to_degrees(),
                DEFAULT_DEPTH,
            )
            .as_str(),
        )
        .expect("Failed to convert position {} into quadkey: {}")
    }
}

impl FromStr for Quadkey {
    type Err = ParseError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        if s.is_empty() {
            Err(ParseError::EmptyString)
        } else {
            let number_of_slash = s.chars().filter(|&character| character == '/').count();
            if number_of_slash > 0 && ((number_of_slash * 2) + 1 == s.len()) {
                // string with slash separator and one slash for each character except first
                Ok(s.split('/')
                    .fold(Quadkey::default(), |mut quadkey_struct, element| {
                        let result = Tile::from_str(element).unwrap();
                        quadkey_struct.tiles.push(result);
                        quadkey_struct
                    }))
            } else {
                // string without slash separator
                Ok(s.chars()
                    .fold(Quadkey::default(), |mut quadkey_struct, element| {
                        quadkey_struct.tiles.push(Tile::from(element));
                        quadkey_struct
                    }))
            }
        }
    }
}

impl fmt::Display for Quadkey {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{}",
            self.tiles
                .iter()
                .fold(String::new(), |mut quadkey_string, &tile| {
                    quadkey_string.push_str(format!("/{}", tile).as_str());
                    quadkey_string
                })
        )
    }
}

impl Ord for Quadkey {
    fn cmp(&self, other: &Self) -> Ordering {
        let matching = self
            .tiles
            .iter()
            .zip(other.tiles.iter())
            .filter(|&(myself, other)| myself == other)
            .count();

        if self.len() == matching {
            if self.len() == other.len() {
                Ordering::Equal
            } else {
                Ordering::Greater
            }
        } else if other.len() == matching {
            Ordering::Less
        } else if self.len() == other.len() {
            if let Some(self_significant) = self.tiles.get(matching) {
                if let Some(other_significant) = other.tiles.get(matching) {
                    return match self_significant.partial_cmp(other_significant) {
                        Some(ordering) => ordering,
                        None => Ordering::Equal,
                    };
                }
            }
            Ordering::Equal
        } else {
            self.len().cmp(&other.len())
        }
    }
}

#[allow(clippy::non_canonical_partial_ord_impl)]
impl PartialOrd for Quadkey {
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
    use crate::mobility::quadtree::quadkey::Quadkey;
    use crate::mobility::quadtree::tile::Tile;
    use std::cmp::Ordering::{Equal, Greater, Less};
    use std::str::FromStr;

    fn create_quadkey(quadkey_string: &str) -> Quadkey {
        let quadkey_result = Quadkey::from_str(quadkey_string);
        assert!(quadkey_result.is_ok());
        quadkey_result.unwrap()
    }

    #[test]
    fn test_create_quadkey_with_slash() {
        let quadkey = create_quadkey("0/1/2/3");
        assert_eq!(quadkey.tiles[0], Tile::Zero);
        assert_eq!(quadkey.tiles[1], Tile::One);
        assert_eq!(quadkey.tiles[2], Tile::Two);
        assert_eq!(quadkey.tiles[3], Tile::Three);
    }

    #[test]
    fn test_create_quadkey_without_slash() {
        let quadkey = create_quadkey("0123");
        assert_eq!(quadkey.tiles[0], Tile::Zero);
        assert_eq!(quadkey.tiles[1], Tile::One);
        assert_eq!(quadkey.tiles[2], Tile::Two);
        assert_eq!(quadkey.tiles[3], Tile::Three);
    }

    #[test]
    #[should_panic]
    fn test_fail_create_quadkey_empty() {
        create_quadkey("");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_quadkey_with_slash() {
        create_quadkey("/");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_quadkey_with_a_character() {
        create_quadkey("a");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_quadkey_with_a_too_big_number() {
        create_quadkey("4");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_quadkey_with_slash_with_a_character_at_the_beginning() {
        create_quadkey("a/1/2/3");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_quadkey_with_slash_with_a_character_in_the_midlle() {
        create_quadkey("0/1/a/3");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_quadkey_with_slash_with_a_character_at_the_end() {
        create_quadkey("0/1/2/a");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_quadkey_without_slash_with_a_character_at_the_beginning() {
        create_quadkey("a123");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_quadkey_without_slash_with_a_character_in_the_midlle() {
        create_quadkey("01a3");
    }

    #[test]
    #[should_panic]
    fn test_fail_create_quadkey_without_slash_with_a_character_at_the_end() {
        create_quadkey("012a");
    }

    #[test]
    fn test_quadkey_1_tile_equal() {
        let mut quadkey = create_quadkey("0");
        let mut quadkey2 = create_quadkey("0");
        assert_eq!(quadkey, quadkey2);
        quadkey = create_quadkey("1");
        quadkey2 = create_quadkey("1");
        assert_eq!(quadkey, quadkey2);
        quadkey = create_quadkey("2");
        quadkey2 = create_quadkey("2");
        assert_eq!(quadkey, quadkey2);
        quadkey = create_quadkey("3");
        quadkey2 = create_quadkey("3");
        assert_eq!(quadkey, quadkey2);
    }

    #[test]
    fn test_quadkey_1_tile_greater() {
        let quadkey = create_quadkey("0");
        let mut quadkey2 = create_quadkey("0/0");
        assert!(quadkey >= quadkey2);
        assert!(quadkey > quadkey2);
        quadkey2 = create_quadkey("0/1");
        assert!(quadkey >= quadkey2);
        assert!(quadkey > quadkey2);
        quadkey2 = create_quadkey("0/2");
        assert!(quadkey >= quadkey2);
        assert!(quadkey > quadkey2);
        quadkey2 = create_quadkey("0/3");
        assert!(quadkey >= quadkey2);
        assert!(quadkey > quadkey2);
        quadkey2 = create_quadkey("0/1/2");
        assert!(quadkey >= quadkey2);
        assert!(quadkey > quadkey2);
        quadkey2 = create_quadkey("0/0/0");
        assert!(quadkey >= quadkey2);
        assert!(quadkey > quadkey2);
    }

    #[test]
    fn test_quadkey_1_tile_less() {
        let quadkey = create_quadkey("0");
        let empty_quadkey = Quadkey::default();
        assert!(quadkey <= empty_quadkey);
        assert!(quadkey < empty_quadkey);
    }

    #[test]
    fn test_quadkey_1_tile_different() {
        let quadkey = create_quadkey("0");
        let mut quadkey2 = create_quadkey("1");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
        quadkey2 = create_quadkey("2");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
        quadkey2 = create_quadkey("3");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
        quadkey2 = create_quadkey("1/0");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
        quadkey2 = create_quadkey("2/1");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
        quadkey2 = create_quadkey("3/2");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
        quadkey2 = create_quadkey("3/3");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
        quadkey2 = create_quadkey("1/0/0");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
        quadkey2 = create_quadkey("1/2/3");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
    }

    #[test]
    fn test_quadkey_2_tiles_equal() {
        let quadkey = create_quadkey("0/1");
        let quadkey2 = create_quadkey("0/1");
        assert_eq!(quadkey, quadkey2);
    }

    #[test]
    fn test_quadkey_2_tiles_greater() {
        let quadkey = create_quadkey("0/1");
        let mut quadkey2 = create_quadkey("0/1/2");
        assert!(quadkey >= quadkey2);
        assert!(quadkey > quadkey2);
        quadkey2 = create_quadkey("0/1/2/3");
        assert!(quadkey >= quadkey2);
        assert!(quadkey > quadkey2);
    }

    #[test]
    fn test_quadkey_2_tiles_less() {
        let quadkey = create_quadkey("0/1");
        let quadkey2 = create_quadkey("0");
        assert!(quadkey <= quadkey2);
        assert!(quadkey < quadkey2);
        let empty_quadkey = Quadkey::default();
        assert!(quadkey <= empty_quadkey);
        assert!(quadkey < empty_quadkey);
    }

    #[test]
    fn test_quadkey_2_tiles_different() {
        let quadkey = create_quadkey("0/1");
        let mut quadkey2 = create_quadkey("0/2");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
        quadkey2 = create_quadkey("1");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
        quadkey2 = create_quadkey("0/2/3");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
    }

    #[test]
    fn test_quadkey_22_tiles_equal() {
        let quadkey = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        let quadkey2 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        assert_eq!(quadkey, quadkey2);
    }

    #[test]
    fn test_quadkey_22_tiles_greater() {
        let quadkey = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        let mut quadkey2 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2");
        assert!(quadkey >= quadkey2);
        assert!(quadkey > quadkey2);
        quadkey2 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3");
        assert!(quadkey >= quadkey2);
        assert!(quadkey > quadkey2);
    }

    #[test]
    fn test_quadkey_22_tiles_less() {
        let quadkey = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        let mut quadkey2 = create_quadkey("0");
        assert!(quadkey <= quadkey2);
        assert!(quadkey < quadkey2);
        quadkey2 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0");
        assert!(quadkey <= quadkey2);
        assert!(quadkey < quadkey2);
        let empty_quadkey = Quadkey::default();
        assert!(quadkey <= empty_quadkey);
        assert!(quadkey < empty_quadkey);
    }

    #[test]
    fn test_quadkey_22_tiles_different() {
        let quadkey = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        let mut quadkey2 = create_quadkey("1");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
        quadkey2 = create_quadkey("0/0");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
        quadkey2 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/2");
        assert_eq!(quadkey.partial_cmp(&quadkey2), None);
    }

    #[test]
    fn test_equal_quadkeys_are_equal() {
        let twin_1 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        let twin_2 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");

        assert_eq!(twin_1.partial_cmp(&twin_2), Some(Equal));
        assert_eq!(twin_1.cmp(&twin_2), Equal);
    }

    #[test]
    fn test_same_length_but_not_siblings_are_not_partially_ordered() {
        let linas = create_quadkey("1/2/0/2/2/2/2/3/3/0/0/3/2/0/2/0/1/0/1/0/3/1");
        let barcelona = create_quadkey("1/2/0/2/2/0/0/1/1/2/0/3/1/0/2/1/0/1/2/1/0/3");
        assert_eq!(linas.partial_cmp(&barcelona), None);
    }

    #[test]
    fn test_same_length_but_not_siblings_are_ordered() {
        let linas = create_quadkey("1/2/0/2/2/2/2/3/3/0/0/3/2/0/2/0/1/0/1/0/3/1");
        let barcelona = create_quadkey("1/2/0/2/2/0/0/1/1/2/0/3/1/0/2/1/0/1/2/1/0/3");

        assert_eq!(linas.cmp(&barcelona), Less);
    }

    #[test]
    fn test_siblings_are_not_partially_ordered() {
        let sibling_0 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/0");
        let sibling_1 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        let sibling_2 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/2");
        let sibling_3 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/3");

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
    fn test_siblings_are_ordered() {
        let sibling_0 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/0");
        let sibling_1 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");
        let sibling_2 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/2");
        let sibling_3 = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/3");

        assert_eq!(sibling_0.cmp(&sibling_1), Less);
        assert_eq!(sibling_0.cmp(&sibling_2), Less);
        assert_eq!(sibling_0.cmp(&sibling_3), Less);

        assert_eq!(sibling_1.cmp(&sibling_0), Greater);
        assert_eq!(sibling_1.cmp(&sibling_2), Less);
        assert_eq!(sibling_1.cmp(&sibling_3), Less);

        assert_eq!(sibling_2.cmp(&sibling_0), Greater);
        assert_eq!(sibling_2.cmp(&sibling_1), Greater);
        assert_eq!(sibling_2.cmp(&sibling_3), Less);

        assert_eq!(sibling_3.cmp(&sibling_0), Greater);
        assert_eq!(sibling_3.cmp(&sibling_1), Greater);
        assert_eq!(sibling_3.cmp(&sibling_2), Greater);
    }

    #[test]
    fn test_deeper_is_lesser() {
        let less_deep = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0");
        let deeper = create_quadkey("0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1/2/3/0/1");

        assert_eq!(less_deep.partial_cmp(&deeper), Some(Greater));
        assert_eq!(deeper.partial_cmp(&less_deep), Some(Less));
        assert_eq!(less_deep.cmp(&deeper), Greater);
        assert_eq!(deeper.cmp(&less_deep), Less);
    }

    macro_rules! test_reduce {
        ($test_name:ident, $k:expr, $d:expr, $e:expr) => {
            #[test]
            fn $test_name() {
                let mut quadkey = Quadkey::from_str($k).expect("Failed to convert '{}' to quadkey");
                let expected = Quadkey::from_str($e).expect("Failed to convert '{}' to quadkey");

                quadkey.reduce($d);

                assert_eq!(quadkey.len(), expected.len());
                assert_eq!(quadkey, expected);
            }
        };
    }
    test_reduce!(reduce_into_smaller, "0/1/2/3/1/3/2/0/3/1", 5, "0/1/2/3/1");
    test_reduce!(
        reduce_into_deeper,
        "0/1/2/3/1/3/2/0/3/1",
        30,
        "0/1/2/3/1/3/2/0/3/1"
    );

    macro_rules! test_as_reduced {
        ($test_name:ident, $k:expr, $d:expr, $e:expr) => {
            #[test]
            fn $test_name() {
                let quadkey = Quadkey::from_str($k).expect("Failed to convert '{}' to quadkey");
                let initial_length = quadkey.len();
                let expected = Quadkey::from_str($e).expect("Failed to convert '{}' to quadkey");

                let reduced = quadkey.as_reduced($d);

                assert_eq!(quadkey.len(), initial_length);
                assert_eq!(reduced, expected);
            }
        };
    }
    test_as_reduced!(
        as_reduced_into_smaller,
        "0/1/2/3/1/3/2/0/3/1",
        5,
        "0/1/2/3/1"
    );
    test_as_reduced!(
        as_reduced_into_deeper,
        "0/1/2/3/1/3/2/0/3/1",
        30,
        "0/1/2/3/1/3/2/0/3/1"
    );
}
