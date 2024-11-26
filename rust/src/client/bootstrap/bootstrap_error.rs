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

#[derive(Error, Debug, PartialEq)]
pub(crate) enum BootstrapError {
    #[error("Bootstrap response is invalid: {0}")]
    InvalidResponse(&'static str),
    #[error("Boostrap response is missing required field '{0}'")]
    MissingField(&'static str),
    #[error("Could not convert bootstrap response as string: {0}")]
    ContentError(String),
    #[error("Bootstrap request failed: {0}")]
    NetworkError(String),
    #[error("Could not parse value of field '{0}' as a string")]
    NotAString(String),
}
