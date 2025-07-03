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
use crate::client::configuration::get_mandatory_from_section;

pub(crate) const MOBILITY_SECTION: &str = "mobility";

const STATION_ID_FIELD: &str = "station_id";
const SOURCE_UUID_FIELD: &str = "source_uuid";
const USE_RESPONSIBILITY_FIELD: &str = "use_responsibility";

const THREAD_COUNT_FIELD: &str = "thread_count";

#[derive(Clone, Debug, Default)]
pub struct MobilityConfiguration {
    pub source_uuid: String,
    pub station_id: u32,
    pub use_responsibility: bool,
    pub thread_count: usize,
}

impl TryFrom<&Properties> for MobilityConfiguration {
    type Error = ConfigurationError;

    fn try_from(properties: &Properties) -> Result<Self, Self::Error> {
        let s = MobilityConfiguration {
            source_uuid: get_mandatory_from_section(
                SOURCE_UUID_FIELD,
                (MOBILITY_SECTION, properties),
            )?,
            station_id: get_mandatory_from_section(
                STATION_ID_FIELD,
                (MOBILITY_SECTION, properties),
            )?,
            use_responsibility: get_mandatory_from_section(
                USE_RESPONSIBILITY_FIELD,
                (MOBILITY_SECTION, properties),
            )?,
            thread_count: get_mandatory_from_section(
                THREAD_COUNT_FIELD,
                (MOBILITY_SECTION, properties),
            )?,
        };
        Ok(s)
    }
}
