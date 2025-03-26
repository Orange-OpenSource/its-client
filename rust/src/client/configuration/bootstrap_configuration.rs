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

use crate::client::configuration::configuration_error::ConfigurationError;
use crate::client::configuration::{
    get_mandatory_field, get_mandatory_from_section, pick_mandatory_section,
};
use ini::Ini;

#[derive(Debug)]
pub struct BootstrapConfiguration {
    pub endpoint: String,
    pub station_id: String,
    pub username: String,
    pub password: String,
    pub role: String,
}

impl TryFrom<&mut Ini> for BootstrapConfiguration {
    type Error = ConfigurationError;

    fn try_from(ini: &mut Ini) -> Result<Self, Self::Error> {
        let properties = &pick_mandatory_section("bootstrap", ini)?;
        let section = ("bootstrap", properties);

        let endpoint = format!(
            "http://{}:{}{}",
            get_mandatory_from_section::<String>("host", section)?,
            get_mandatory_from_section::<u16>("port", section)?,
            get_mandatory_from_section::<String>("path", section)?,
        );

        Ok(Self {
            endpoint,
            station_id: get_mandatory_field::<String>(Some("station"), "id", ini)?,
            username: get_mandatory_from_section::<String>("username", section)?,
            password: get_mandatory_from_section::<String>("password", section)?,
            role: get_mandatory_from_section::<String>("role", section)?,
        })
    }
}
