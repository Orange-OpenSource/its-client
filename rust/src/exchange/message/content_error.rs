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

#[derive(Debug, Error)]
pub enum ContentError {
    #[error("Struct {0} does not implement Mobile trait")]
    NotAMobile(&'static str),
    #[error("Struct {0} does not implement Mortal trait")]
    NotAMortal(&'static str),
    #[error("Missing station data container for {0} type message")]
    MissingStationDataContainer(&'static str),
    #[error("{0} type message has been sent by a RSU station")]
    RsuOriginatingMessage(&'static str),
}
