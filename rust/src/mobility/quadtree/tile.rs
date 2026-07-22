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

use crate::mobility::quadtree::parse_error::ParseError;
use std::fmt;
use std::fmt::{Display, Formatter};
use std::str::FromStr;

#[derive(Debug, Copy, Clone, Eq, Hash, PartialEq, PartialOrd)]
pub enum Tile {
    Zero = 0,
    One = 1,
    Two = 2,
    Three = 3,
    All,
}

impl Tile {
    pub(crate) fn try_from_char(tile: char) -> Result<Self, ParseError> {
        match tile {
            '#' => Ok(Tile::All),
            '0'..='3' => {
                let digit = tile.to_digit(4).unwrap();
                Ok(Tile::from(digit as u8))
            }
            _ => Err(ParseError::InvalidTileChar(tile)),
        }
    }
}

impl From<u8> for Tile {
    fn from(tile: u8) -> Self {
        match tile {
            0 => Tile::Zero,
            1 => Tile::One,
            2 => Tile::Two,
            3 => Tile::Three,
            _ => panic!("Unable to convert the number {tile} as a Tile"),
        }
    }
}

impl From<char> for Tile {
    fn from(tile: char) -> Self {
        Tile::try_from_char(tile)
            .unwrap_or_else(|_| panic!("Unable to convert the char {tile} as a Tile"))
    }
}

impl FromStr for Tile {
    type Err = ParseError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        if let Some(c) = s.as_bytes().first() {
            let element = char::from(*c);
            Tile::try_from_char(element)
        } else {
            Err(ParseError::EmptyTileStr)
        }
    }
}

impl Display for Tile {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
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
