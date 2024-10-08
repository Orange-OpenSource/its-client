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
