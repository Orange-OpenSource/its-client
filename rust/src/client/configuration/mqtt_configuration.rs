use crate::client::configuration::configuration_error::ConfigurationError;
use crate::client::configuration::configuration_error::ConfigurationError::NoPassword;
use crate::client::configuration::{
    MQTT_SECTION, get_mandatory_from_section, get_optional_from_section,
};
use crate::transport::mqtt::configure_transport;
use ini::Properties;
use rumqttc::v5::MqttOptions;
use std::ops::Deref;

pub struct MqttConfiguration {
    pub mqtt_options: MqttOptions,
}

impl TryFrom<&Properties> for MqttConfiguration {
    type Error = ConfigurationError;

    fn try_from(properties: &Properties) -> Result<Self, Self::Error> {
        let section = (MQTT_SECTION, properties);
        let mut mqtt_options = MqttOptions::new(
            get_mandatory_from_section::<String>("client_id", section)?,
            get_mandatory_from_section::<String>("host", section)?,
            get_mandatory_from_section::<u16>("port", section)?,
        );

        if let Ok(Some(username)) = get_optional_from_section::<String>("username", section.1) {
            if let Ok(Some(password)) = get_optional_from_section::<String>("password", section.1) {
                mqtt_options.set_credentials(username, password);
            } else {
                return Err(NoPassword);
            }
        }

        // TODO manage other optional

        let use_tls = get_optional_from_section::<bool>("use_tls", properties)
            .unwrap_or_default()
            .unwrap_or_default();
        let use_websocket = get_optional_from_section::<bool>("use_websocket", properties)
            .unwrap_or_default()
            .unwrap_or_default();

        configure_transport(use_tls, use_websocket, &mut mqtt_options);

        Ok(Self { mqtt_options })
    }
}
impl Deref for MqttConfiguration {
    type Target = MqttOptions;
    fn deref(&self) -> &Self::Target {
        &self.mqtt_options
    }
}
