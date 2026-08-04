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
use crate::client::configuration::configuration_error::ConfigurationError::{
    FileNotFound, InvalidFileType,
};
use crate::client::configuration::{
    Configuration, get_mandatory_from_properties, pick_mandatory_section,
};
use ini::Ini;
use log::{info, warn};
use serde::Deserialize;
use std::fs;

#[derive(Deserialize)]
struct Identity {
    #[serde(alias = "psk-identity")]
    username: String,
    #[serde(alias = "psk-secret-key")]
    password: String,
    #[serde(alias = "iot3-id")]
    id: String,
}

/// Overrides the [Configuration] parts related to service access
///
/// This function will:
/// - override the credentials attributes
/// - suffix the id attributes (MQTT client_id for example)
/// in all relevant configuration structures.
///
/// The username, password and id suffix will be read from the file provided through the
/// `identity.file` field in the INI configuration file (see [example/config.ini]).
///
/// The file must content a single JSON object with these three fields.
/// Example:
///  ```json
///   {
///     "id": "192837465",
///     "username": "zqsdfguiqjoizhfze57451zg5f4",
///     "password": "86e16f35a59311780627630d9357fbabe11dbd71f7c9ce662402cb846c5c24f0"
///   }
///   ```
pub(crate) fn override_with_identity(
    ini_configuration: &mut Ini,
    configuration: &mut Configuration,
) -> Result<(), ConfigurationError> {
    match pick_mandatory_section("identity", ini_configuration) {
        Ok(identity) => {
            let file_name = get_mandatory_from_properties::<String>("file", &identity)?;

            match fs::read_to_string(&file_name) {
                Ok(json_str) => match serde_json::from_str::<Identity>(&json_str) {
                    Ok(identity) => {
                        configuration.mqtt.suffix_client_id(&identity.id);
                        configuration
                            .mqtt
                            .mqtt_options
                            .set_credentials(&identity.username, &identity.password);

                        #[cfg(feature = "telemetry")]
                        {
                            configuration.telemetry.username = Some(identity.username);
                            configuration.telemetry.password = Some(identity.password);
                        }

                        Ok(())
                    }
                    Err(e) => {
                        warn!("{}", e);
                        Err(InvalidFileType(file_name))
                    }
                },
                Err(err) => {
                    warn!("{}", err);
                    Err(FileNotFound(file_name))
                }
            }
        }
        Err(error) => {
            info!(
                "No identity section found, using credentials from dedicated sections: {}",
                error
            );
            Ok(())
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use ini::Ini;

    #[test]
    fn missing_section_returns_ok() {
        let ini_str = r#"
        [not_identity]
        file=/dev/null        
        "#;
        let mut ini_cfg = Ini::load_from_str(ini_str).unwrap();
        let mut configuration = Configuration::default();

        let result = override_with_identity(&mut ini_cfg, &mut configuration);

        assert!(result.is_ok());
    }
}
