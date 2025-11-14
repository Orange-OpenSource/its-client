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
use crate::client::configuration::configuration_error::ConfigurationError::NoPassword;
use crate::client::configuration::{get_mandatory_from_properties, get_optional_from_properties};
use crate::transport::mqtt::configure_transport;
use ini::Properties;
use rumqttc::v5::MqttOptions;
use std::ops::Deref;

/// Represents the MQTT configuration.
#[derive(Clone, Debug)]
pub struct MqttConfiguration {
    pub mqtt_options: MqttOptions,
}

impl Default for MqttConfiguration {
    fn default() -> Self {
        Self {
            mqtt_options: MqttOptions::new("default_client", "localhost", 1883),
        }
    }
}

impl TryFrom<&Properties> for MqttConfiguration {
    type Error = ConfigurationError;

    /// Tries to create an `MqttConfiguration` from the given properties.
    ///
    /// # Arguments
    ///
    /// * `properties` - Properties to create the configuration from.
    ///
    /// # Returns
    ///
    /// A result containing the `MqttConfiguration` or an error.
    fn try_from(properties: &Properties) -> Result<Self, Self::Error> {
        let mut mqtt_options = MqttOptions::new(
            get_mandatory_from_properties::<String>("client_id", properties)?,
            get_mandatory_from_properties::<String>("host", properties)?,
            get_mandatory_from_properties::<u16>("port", properties)?,
        );

        if let Ok(Some(username)) = get_optional_from_properties::<String>("username", properties) {
            if let Ok(Some(password)) =
                get_optional_from_properties::<String>("password", properties)
            {
                mqtt_options.set_credentials(username, password);
            } else {
                return Err(NoPassword);
            }
        }
        if let Ok(Some(connection_timeout)) =
            get_optional_from_properties::<u64>("connection_timeout", properties)
        {
            mqtt_options.set_connection_timeout(connection_timeout);
        }

        let use_tls = get_optional_from_properties::<bool>("use_tls", properties)
            .unwrap_or_default()
            .unwrap_or_default();
        let use_websocket = get_optional_from_properties::<bool>("use_websocket", properties)
            .unwrap_or_default()
            .unwrap_or_default();

        configure_transport(use_tls, use_websocket, &mut mqtt_options);

        Ok(Self { mqtt_options })
    }
}

impl Deref for MqttConfiguration {
    type Target = MqttOptions;

    /// Dereferences the `MqttConfiguration` to `MqttOptions`.
    ///
    /// # Returns
    ///
    /// A reference to the `MqttOptions`.
    fn deref(&self) -> &Self::Target {
        &self.mqtt_options
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::client::configuration::MQTT_SECTION;
    use ini::Ini;

    /// Creates properties for testing.
    ///
    /// # Returns
    ///
    /// A `Properties` instance with test values.
    fn create_properties() -> Properties {
        let mut ini = Ini::new();
        ini.with_section(Some(MQTT_SECTION))
            .set("client_id", "test_client")
            .set("host", "localhost")
            .set("port", "1883");
        ini.section(Some(MQTT_SECTION)).unwrap().clone()
    }

    #[test]
    fn mqtt_configuration_from_valid_properties() {
        let properties = create_properties();
        let config = MqttConfiguration::try_from(&properties).unwrap();
        assert_eq!(config.mqtt_options.client_id(), "test_client");
        assert_eq!(
            format!(
                "{}:{}",
                config.mqtt_options.broker_address().0,
                config.mqtt_options.broker_address().1
            ),
            "localhost:1883"
        );
    }

    #[test]
    fn mqtt_configuration_missing_mandatory_fields() {
        let mut properties = create_properties();
        properties.remove("client_id");
        let result = MqttConfiguration::try_from(&properties);
        assert!(result.is_err());
    }

    #[test]
    fn mqtt_configuration_with_credentials() {
        let mut properties = create_properties();
        properties.insert("username", "user".to_string());
        properties.insert("password", "pass".to_string());
        let config = MqttConfiguration::try_from(&properties).unwrap();
        assert_eq!(
            config.mqtt_options.credentials(),
            Some(("user".to_string(), "pass".to_string()))
        );
    }

    #[test]
    fn mqtt_configuration_missing_password() {
        let mut properties = create_properties();
        properties.insert("username", "user".to_string());
        let result = MqttConfiguration::try_from(&properties);
        assert!(matches!(result, Err(NoPassword)));
    }

    #[test]
    fn mqtt_configuration_with_tls_and_websocket() {
        let mut properties = create_properties();
        properties.insert("use_tls", "true".to_string());
        properties.insert("use_websocket", "true".to_string());
        let config = MqttConfiguration::try_from(&properties).unwrap();
        assert!(matches!(
            config.mqtt_options.transport(),
            rumqttc::Transport::Wss(_)
        ));
    }

    #[test]
    fn mqtt_configuration_with_tls() {
        let mut properties = create_properties();
        properties.insert("use_tls", "true".to_string());
        properties.insert("use_websocket", "false".to_string());
        let config = MqttConfiguration::try_from(&properties).unwrap();
        assert!(matches!(
            config.mqtt_options.transport(),
            rumqttc::Transport::Tls(_)
        ));
    }

    #[test]
    fn mqtt_configuration_with_websocket() {
        let mut properties = create_properties();
        properties.insert("use_tls", "false".to_string());
        properties.insert("use_websocket", "true".to_string());
        let config = MqttConfiguration::try_from(&properties).unwrap();
        assert!(matches!(
            config.mqtt_options.transport(),
            rumqttc::Transport::Ws
        ));
    }

    #[test]
    fn mqtt_configuration_without_tls_and_websocket() {
        let mut properties = create_properties();
        properties.insert("use_tls", "false".to_string());
        properties.insert("use_websocket", "false".to_string());
        let config = MqttConfiguration::try_from(&properties).unwrap();
        assert!(matches!(
            config.mqtt_options.transport(),
            rumqttc::Transport::Tcp
        ));
    }
}
