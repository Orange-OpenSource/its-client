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
use crate::client::configuration::{get_mandatory_from_properties, pick_mandatory_section};
use ini::Ini;

#[derive(Debug)]
pub struct BootstrapConfiguration {
    pub endpoint: String,
    pub username: String,
    pub password: String,
    pub role: String,
}

impl TryFrom<&mut Ini> for BootstrapConfiguration {
    type Error = ConfigurationError;

    fn try_from(ini: &mut Ini) -> Result<Self, Self::Error> {
        let properties = &pick_mandatory_section("bootstrap", ini)?;

        let endpoint = format!(
            "http://{}:{}{}",
            get_mandatory_from_properties::<String>("host", properties)?,
            get_mandatory_from_properties::<u16>("port", properties)?,
            get_mandatory_from_properties::<String>("path", properties)?,
        );

        Ok(Self {
            endpoint,
            username: get_mandatory_from_properties::<String>("username", properties)?,
            password: get_mandatory_from_properties::<String>("password", properties)?,
            role: get_mandatory_from_properties::<String>("role", properties)?,
        })
    }
}
