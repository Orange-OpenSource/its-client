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

use thiserror::Error;

#[derive(Error, Debug)]
pub enum ParseError {
    #[error("Cannot create key out of empty string")]
    EmptyString,
    #[error("Cannot convert empty string to quadkey element")]
    EmptyTileStr,
    #[error("'{0}' character is not a valid quadkey element")]
    InvalidTileChar(char),
}
