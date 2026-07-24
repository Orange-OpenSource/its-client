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

        let ca_file =
            get_optional_from_properties::<String>("ca_file", properties).unwrap_or_default();
        let cert_file =
            get_optional_from_properties::<String>("cert_file", properties).unwrap_or_default();
        let key_file =
            get_optional_from_properties::<String>("key_file", properties).unwrap_or_default();

        configure_transport(
            use_tls,
            use_websocket,
            &mut mqtt_options,
            ca_file,
            cert_file,
            key_file,
        );

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

impl MqttConfiguration {
    #[cfg(feature = "identity")]
    pub(crate) fn suffix_client_id(&mut self, suffix: &str) {
        let old_mqtt_options = &self.mqtt_options.clone();
        let suffixed_id = format!("{}-{}", old_mqtt_options.client_id(), suffix);
        let mut mqtt_options = MqttOptions::new(
            suffixed_id,
            old_mqtt_options.broker_address().0,
            old_mqtt_options.broker_address().1,
        );

        match old_mqtt_options.credentials() {
            Some(credentials) => {
                mqtt_options.set_credentials(credentials.username, credentials.password);
            }
            None => {}
        }
        mqtt_options.set_connection_timeout(old_mqtt_options.connection_timeout());
        mqtt_options.set_keep_alive(old_mqtt_options.keep_alive());
        mqtt_options.set_clean_start(old_mqtt_options.clean_start());
        mqtt_options.set_transport(old_mqtt_options.transport());
        mqtt_options.set_request_channel_capacity(old_mqtt_options.request_channel_capacity());
        mqtt_options.set_pending_throttle(old_mqtt_options.pending_throttle());
        mqtt_options.set_manual_acks(old_mqtt_options.manual_acks());
        mqtt_options.set_network_options(old_mqtt_options.network_options());
        mqtt_options.set_receive_maximum(old_mqtt_options.receive_maximum());
        mqtt_options.set_max_packet_size(old_mqtt_options.max_packet_size());
        mqtt_options.set_topic_alias_max(old_mqtt_options.topic_alias_max());
        mqtt_options.set_request_response_info(old_mqtt_options.request_response_info());
        mqtt_options.set_request_problem_info(old_mqtt_options.request_problem_info());
        mqtt_options.set_user_properties(old_mqtt_options.user_properties());
        mqtt_options.set_authentication_method(old_mqtt_options.authentication_method());
        mqtt_options.set_authentication_data(old_mqtt_options.authentication_data());
        if let Some(connect_properties) = old_mqtt_options.connect_properties() {
            mqtt_options.set_connect_properties(connect_properties);
        }
        if let Some(upper_limit) = old_mqtt_options.get_outgoing_inflight_upper_limit() {
            mqtt_options.set_outgoing_inflight_upper_limit(upper_limit);
        }

        if let Some(last_will) = old_mqtt_options.last_will() {
            mqtt_options.set_last_will(last_will);
        }

        self.mqtt_options = mqtt_options;
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
            config
                .mqtt_options
                .credentials()
                .map(|c| (c.username, c.password)),
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

    #[cfg(feature = "identity")]
    #[test]
    fn suffix_client_id_changes_only_client_id() {
        let mut properties = create_properties();
        properties.insert("username", "user".to_string());
        properties.insert("password", "pass".to_string());
        let mut config = MqttConfiguration::try_from(&properties).unwrap();
        config
            .mqtt_options
            .set_keep_alive(std::time::Duration::from_secs(60));
        config.mqtt_options.set_clean_start(false);
        config.mqtt_options.set_connection_timeout(17);
        config.mqtt_options.set_request_channel_capacity(11);
        config
            .mqtt_options
            .set_pending_throttle(std::time::Duration::from_millis(123));
        config.mqtt_options.set_manual_acks(true);
        config.mqtt_options.set_receive_maximum(Some(7));
        config.mqtt_options.set_max_packet_size(Some(4096));
        config.mqtt_options.set_topic_alias_max(Some(9));
        config.mqtt_options.set_request_response_info(Some(128));
        config.mqtt_options.set_request_problem_info(None);
        let initial_client_id = config.mqtt_options.client_id();
        let initial_broker_address = config.mqtt_options.broker_address();
        let initial_credentials = config.mqtt_options.credentials();
        let initial_keep_alive = config.mqtt_options.keep_alive();
        let initial_clean_start = config.mqtt_options.clean_start();
        let initial_connection_timeout = config.mqtt_options.connection_timeout();
        let initial_request_channel_capacity = config.mqtt_options.request_channel_capacity();
        let initial_pending_throttle = config.mqtt_options.pending_throttle();
        let initial_manual_acks = config.mqtt_options.manual_acks();
        let initial_receive_maximum = config.mqtt_options.receive_maximum();
        let initial_max_packet_size = config.mqtt_options.max_packet_size();
        let initial_topic_alias_max = config.mqtt_options.topic_alias_max();
        let initial_request_response_info = config.mqtt_options.request_response_info();
        let initial_request_problem_info = config.mqtt_options.request_problem_info();

        config.suffix_client_id("suffix");

        assert_ne!(config.mqtt_options.client_id(), initial_client_id);
        assert_eq!(
            config.mqtt_options.client_id(),
            format!("{}-{}", initial_client_id, "suffix")
        );
        assert_eq!(config.mqtt_options.broker_address(), initial_broker_address);
        assert_eq!(config.mqtt_options.credentials(), initial_credentials);
        assert_eq!(config.mqtt_options.keep_alive(), initial_keep_alive);
        assert_eq!(config.mqtt_options.clean_start(), initial_clean_start);
        assert_eq!(
            config.mqtt_options.connection_timeout(),
            initial_connection_timeout
        );
        assert_eq!(
            config.mqtt_options.request_channel_capacity(),
            initial_request_channel_capacity
        );
        assert_eq!(
            config.mqtt_options.pending_throttle(),
            initial_pending_throttle
        );
        assert_eq!(config.mqtt_options.manual_acks(), initial_manual_acks);
        assert_eq!(
            config.mqtt_options.receive_maximum(),
            initial_receive_maximum
        );
        assert_eq!(
            config.mqtt_options.max_packet_size(),
            initial_max_packet_size
        );
        assert_eq!(
            config.mqtt_options.topic_alias_max(),
            initial_topic_alias_max
        );
        assert_eq!(
            config.mqtt_options.request_response_info(),
            initial_request_response_info
        );
        assert_eq!(
            config.mqtt_options.request_problem_info(),
            initial_request_problem_info
        );
    }
}
