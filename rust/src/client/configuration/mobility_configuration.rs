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

use ini::Properties;

use crate::client::configuration::configuration_error::ConfigurationError;
use crate::client::configuration::get_mandatory_field;

pub(crate) const STATION_SECTION: &str = "station";

const STATION_ID_FIELD: &str = "id";
const STATION_TYPE_FIELD: &str = "type";

pub struct MobilityConfiguration {
    pub station_id: String,
    pub station_type: String,
}

impl TryFrom<&Properties> for MobilityConfiguration {
    type Error = ConfigurationError;

    fn try_from(properties: &Properties) -> Result<Self, Self::Error> {
        let s = MobilityConfiguration {
            station_id: get_mandatory_field(STATION_ID_FIELD, (STATION_SECTION, properties))?,
            station_type: get_mandatory_field(STATION_TYPE_FIELD, (STATION_SECTION, properties))?,
        };

        Ok(s)
    }
}
