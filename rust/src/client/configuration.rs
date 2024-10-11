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
use ini::{Ini, Properties};
use rumqttc::v5::MqttOptions;
use std::any::type_name;

use std::ops::Deref;
use std::str::FromStr;
#[cfg(feature = "mobility")]
use std::sync::RwLock;

use crate::client::configuration::configuration_error::ConfigurationError::{
    FieldNotFound, MissingMandatoryField, MissingMandatorySection, NoCustomSettings, NoPassword,
    TypeError,
};
use crate::transport::mqtt::{configure_tls, configure_transport};

#[cfg(feature = "telemetry")]
use crate::client::configuration::telemetry_configuration::{
    TelemetryConfiguration, TELEMETRY_SECTION,
};
#[cfg(feature = "mobility")]
use crate::client::configuration::{
    mobility_configuration::{MobilityConfiguration, STATION_SECTION},
    node_configuration::{NodeConfiguration, NODE_SECTION},
};

pub mod configuration_error;
#[cfg(feature = "mobility")]
pub mod mobility_configuration;
#[cfg(feature = "mobility")]
pub mod node_configuration;
#[cfg(feature = "telemetry")]
pub mod telemetry_configuration;

const MQTT_SECTION: &str = "mqtt";

pub struct Configuration {
    pub mqtt_options: MqttOptions,
    #[cfg(feature = "telemetry")]
    pub telemetry: TelemetryConfiguration,
    #[cfg(feature = "mobility")]
    pub mobility: MobilityConfiguration,
    #[cfg(feature = "mobility")]
    pub node: Option<RwLock<NodeConfiguration>>,
    custom_settings: Option<Ini>,
}

impl Configuration {
    #[cfg(feature = "mobility")]
    pub fn component_name(&self, modifier: Option<u32>) -> String {
        let station_id: String = match &self.node {
            Some(node_configuration) => node_configuration
                .read()
                .unwrap()
                .station_id(modifier)
                .to_string(),
            None => self.mobility.station_id.clone(),
        };
        format!("{}_{}", self.mqtt_options.client_id(), station_id)
    }

    #[cfg(feature = "mobility")]
    pub fn set_node_configuration(&mut self, node_configuration: NodeConfiguration) {
        self.node = Some(RwLock::new(node_configuration));
    }

    pub fn set_mqtt_credentials(&mut self, username: &str, password: &str) {
        self.mqtt_options.set_credentials(username, password);
    }

    pub fn get<T: FromStr>(
        &self,
        section: Option<&'static str>,
        key: &'static str,
    ) -> Result<T, ConfigurationError> {
        if self.custom_settings.is_some() {
            match get_optional_field(section, key, self.custom_settings.as_ref().unwrap()) {
                Ok(result) => {
                    if let Some(value) = result {
                        Ok(value)
                    } else {
                        Err(FieldNotFound(key))
                    }
                }
                Err(e) => Err(e),
            }
        } else {
            Err(NoCustomSettings)
        }
    }

    pub fn set<T: Into<String>>(&mut self, section: Option<&str>, key: &str, value: T) {
        if self.custom_settings.is_none() {
            self.custom_settings = Some(Ini::default())
        }
        self.custom_settings
            .as_mut()
            .unwrap()
            .with_section(section)
            .set(key, value);
    }
}

// FIXME maybe move this into a dedicated .rs file
struct MqttOptionWrapper(MqttOptions);
impl TryFrom<&Properties> for MqttOptionWrapper {
    type Error = ConfigurationError;

    fn try_from(properties: &Properties) -> Result<Self, Self::Error> {
        let section = (MQTT_SECTION, properties);
        let mut mqtt_options = MqttOptions::new(
            get_mandatory_field::<String>("client_id", section)?,
            get_mandatory_field::<String>("host", section)?,
            get_mandatory_field::<u16>("port", section)?,
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

        // FIXME manage ALPN, and authentication
        let tls_configuration = if use_tls {
            let ca_path = get_mandatory_field::<String>("tls_certificate", ("mqtt", properties))
                .expect("TLS enabled but no certificate path provided");
            Some(configure_tls(&ca_path, None, None))
        } else {
            None
        };

        configure_transport(tls_configuration, use_websocket, &mut mqtt_options);

        Ok(MqttOptionWrapper(mqtt_options))
    }
}
impl Deref for MqttOptionWrapper {
    type Target = MqttOptions;
    fn deref(&self) -> &Self::Target {
        &self.0
    }
}

pub(crate) fn get_optional_field<T: FromStr>(
    section: Option<&'static str>,
    field: &'static str,
    ini_config: &Ini,
) -> Result<Option<T>, ConfigurationError> {
    let section = if let Some(section) = ini_config.section(section) {
        section
    } else {
        ini_config.general_section()
    };
    get_optional_from_section(field, section)
}

pub(crate) fn get_optional_from_section<T: FromStr>(
    field: &'static str,
    properties: &Properties,
) -> Result<Option<T>, ConfigurationError> {
    if let Some(value) = properties.get(field) {
        match T::from_str(value) {
            Ok(value) => Ok(Some(value)),
            Err(_) => Err(TypeError(field, type_name::<T>())),
        }
    } else {
        Ok(None)
    }
}

pub(crate) fn get_mandatory_field<T: FromStr>(
    field: &'static str,
    section: (&'static str, &Properties),
) -> Result<T, ConfigurationError> {
    match section.1.get(field) {
        Some(value) => match T::from_str(value) {
            Ok(value) => Ok(value),
            Err(_e) => Err(TypeError(field, type_name::<T>())),
        },
        None => Err(MissingMandatoryField(field, section.0)),
    }
}

pub(crate) fn pick_mandatory_section(
    section: &'static str,
    ini_config: &mut Ini,
) -> Result<Properties, ConfigurationError> {
    match ini_config.delete(Some(section)) {
        Some(properties) => Ok(properties),
        None => Err(MissingMandatorySection(section)),
    }
}

impl TryFrom<Ini> for Configuration {
    type Error = ConfigurationError;

    fn try_from(ini_config: Ini) -> Result<Self, Self::Error> {
        let mut ini_config = ini_config;
        let mqtt_properties = pick_mandatory_section(MQTT_SECTION, &mut ini_config)?;

        Ok(Configuration {
            mqtt_options: MqttOptionWrapper::try_from(&mqtt_properties)?
                .deref()
                .clone(),
            #[cfg(feature = "telemetry")]
            telemetry: TelemetryConfiguration::try_from(&pick_mandatory_section(
                TELEMETRY_SECTION,
                &mut ini_config,
            )?)?,
            #[cfg(feature = "mobility")]
            mobility: MobilityConfiguration::try_from(&pick_mandatory_section(
                STATION_SECTION,
                &mut ini_config,
            )?)?,
            #[cfg(feature = "mobility")]
            node: match ini_config.section(Some(NODE_SECTION)) {
                Some(properties) => Some(RwLock::new(NodeConfiguration::try_from(properties)?)),
                None => None,
            },
            custom_settings: Some(ini_config),
        })
    }
}

#[cfg(test)]
mod tests {
    use crate::client::configuration::{get_optional_field, pick_mandatory_section, Configuration};
    use ini::Ini;

    #[cfg(feature = "telemetry")]
    use crate::client::configuration::telemetry_configuration;

    const EXHAUSTIVE_CUSTOM_INI_CONFIG: &str = r#"
no_section="noitceson"

[station]
id="com_myapplication"
type="mec_application"

[mqtt]
host="localhost"
port=1883
client_id="com_myapplication"

[node]
responsibility_enabled=true

[telemetry]
host="otlp.domain.com"
port=5418
path="/custom/v1/traces"

[custom]
test="success"
"#;

    const MINIMAL_FEATURELESS_CONFIGURATION: &str = r#"
[mqtt]
host="localhost"
port=1883
client_id="com_myapplication"
"#;

    #[cfg(feature = "mobility")]
    const MINIMAL_MOBILITY_CONFIGURATION: &str = r#"
[station]
id="com_myapplication"
type="mec_application"

[mqtt]
host="localhost"
port=1883
client_id="com_myapplication"
"#;

    #[cfg(feature = "telemetry")]
    const MINIMAL_TELEMETRY_CONFIGURATION: &str = r#"
[mqtt]
host="localhost"
port=1883
client_id="com_myapplication"

[telemetry]
host="otlp.domain.com"
port=5418
"#;

    #[test]
    fn custom_settings() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let configuration = Configuration::try_from(ini).expect("Minimal config should not fail");
        let no_section = configuration
            .get::<String>(None, "no_section")
            .expect("Failed to get field with no section");
        let custom_test = configuration
            .get::<String>(Some("custom"), "test")
            .expect("Failed to get field under custom section");

        assert_eq!(no_section, "noitceson");
        assert_eq!(custom_test, "success");
    }

    #[test]
    fn set_custom_setting() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");
        let mut configuration =
            Configuration::try_from(ini).expect("Minimal config should not fail");

        configuration.set(Some("my_section"), "cool_key", "cool_value");
        configuration.set(None, "no_section", "updated");
        let no_section = configuration
            .get::<String>(None, "no_section")
            .expect("Failed to get field with no section");
        let cool_value = configuration
            .get::<String>(Some("my_section"), "cool_key")
            .expect("Failed to get field under custom section");

        assert_eq!(no_section, "updated");
        assert_eq!(cool_value, "cool_value");
    }

    #[test]
    fn pick_section() {
        let mut ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let s = pick_mandatory_section("mqtt", &mut ini);

        assert!(s.is_ok());
        assert!(ini.section(Some("mqtt")).is_none());
    }

    #[test]
    fn not_set_optional_returns_none() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let ok_none = get_optional_field::<String>(None, "pmloikjuyh", &ini);

        let none = ok_none.expect("Not set field should return Ok(None)");
        assert!(none.is_none());
    }

    #[test]
    fn optional_no_section_is_ok_some() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let ok_some = get_optional_field::<String>(None, "no_section", &ini);

        let some = ok_some.expect("Optional field must return Ok(Some(T)): found Err(_)");
        let value = some.expect("Optional field must return Ok(Some(T)): found OK(None)");
        assert_eq!(value, "noitceson");
    }

    #[test]
    fn optional_from_section_is_ok_some() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let ok_some = get_optional_field::<String>(Some("custom"), "test", &ini);

        let some = ok_some.expect("Optional field must return Ok(Some(T)): found Err(_)");
        let value = some.expect("Optional field must return Ok(Some(T)): found OK(None)");
        assert_eq!(value, "success");
    }

    #[test]
    fn optional_wrong_type_is_err() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let err = get_optional_field::<u16>(Some("custom"), "test", &ini);

        assert!(err.is_err());
    }

    #[test]
    #[cfg_attr(any(feature = "telemetry", feature = "mobility"), should_panic)]
    fn minimal_featureless_configuration() {
        let ini = Ini::load_from_str(MINIMAL_FEATURELESS_CONFIGURATION)
            .expect("Ini creation should not fail");

        let _ = Configuration::try_from(ini)
            .expect("Failed to create Configuration with minimal mandatory sections and fields");
    }

    #[test]
    #[cfg(feature = "telemetry")]
    #[cfg_attr(feature = "mobility", should_panic)]
    fn minimal_telemetry_configuration() {
        let ini = Ini::load_from_str(MINIMAL_TELEMETRY_CONFIGURATION)
            .expect("Ini creation should not fail");

        let configuration = Configuration::try_from(ini)
            .expect("Failed to create Configuration with minimal mandatory sections and fields");

        assert_eq!(
            telemetry_configuration::DEFAULT_PATH.to_string(),
            configuration.telemetry.path,
            "Telemetry path must default to {}",
            telemetry_configuration::DEFAULT_PATH
        );
    }

    #[test]
    #[cfg(feature = "mobility")]
    #[cfg_attr(feature = "telemetry", should_panic)]
    fn minimal_mobility_configuration() {
        let ini = Ini::load_from_str(MINIMAL_MOBILITY_CONFIGURATION)
            .expect("Ini creation should not fail");

        let _ = Configuration::try_from(ini)
            .expect("Failed to create Configuration with minimal mandatory sections and fields");
    }
}
