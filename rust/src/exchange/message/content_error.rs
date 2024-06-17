/*
 * Software Name : libits
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 * Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) library based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
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
