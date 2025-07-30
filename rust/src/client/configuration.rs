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
use std::any::type_name;

use crate::client::configuration::configuration_error::ConfigurationError::{
    FieldNotFound, MissingMandatoryField, MissingMandatorySection, NoCustomSettings, TypeError,
};

use std::str::FromStr;

#[cfg(feature = "telemetry")]
use crate::client::configuration::telemetry_configuration::{
    TELEMETRY_SECTION, TelemetryConfiguration,
};

#[cfg(feature = "mobility")]
use crate::client::configuration::mobility_configuration::{
    MOBILITY_SECTION, MobilityConfiguration,
};

#[cfg(feature = "geo_routing")]
use crate::client::configuration::geo_configuration::{GEO_SECTION, GeoConfiguration};
use crate::client::configuration::mqtt_configuration::MqttConfiguration;

pub(crate) mod bootstrap_configuration;
pub mod configuration_error;
#[cfg(feature = "geo_routing")]
pub(crate) mod geo_configuration;
#[cfg(feature = "mobility")]
pub(crate) mod mobility_configuration;
pub mod mqtt_configuration;
#[cfg(feature = "telemetry")]
pub(crate) mod telemetry_configuration;

const MQTT_SECTION: &str = "mqtt";

#[derive(Clone, Debug, Default)]
pub struct Configuration {
    pub mqtt: MqttConfiguration,
    #[cfg(feature = "geo_routing")]
    pub geo: GeoConfiguration,
    #[cfg(feature = "telemetry")]
    pub telemetry: TelemetryConfiguration,
    #[cfg(feature = "mobility")]
    pub mobility: MobilityConfiguration,
    pub custom_settings: Option<Ini>,
}

impl Configuration {
    pub fn set_mqtt_credentials(&mut self, username: &str, password: &str) {
        self.mqtt.mqtt_options.set_credentials(username, password);
    }

    pub fn get<T: FromStr>(
        &self,
        section: Option<&'static str>,
        key: &'static str,
    ) -> Result<T, ConfigurationError> {
        if self.custom_settings.is_some() {
            match get_optional(section, key, self.custom_settings.as_ref().unwrap()) {
                Ok(result) => match result {
                    Some(value) => Ok(value),
                    _ => Err(FieldNotFound(key)),
                },
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

    /// Get a list of values from configuration, separated by commas
    pub fn get_list<T: FromStr>(
        &self,
        section: Option<&'static str>,
        key: &'static str,
    ) -> Result<Vec<T>, ConfigurationError> {
        if self.custom_settings.is_some() {
            match get_optional_list(section, key, self.custom_settings.as_ref().unwrap()) {
                Ok(result) => match result {
                    Some(values) => Ok(values),
                    None => Ok(Vec::new()), // Return empty vec if not found
                },
                Err(e) => Err(e),
            }
        } else {
            Err(NoCustomSettings)
        }
    }
}

pub(crate) fn get_optional<T: FromStr>(
    section: Option<&'static str>,
    field: &'static str,
    ini_config: &Ini,
) -> Result<Option<T>, ConfigurationError> {
    let properties = if let Some(properties) = ini_config.section(section) {
        properties
    } else {
        ini_config.general_section()
    };
    get_optional_from_properties(field, properties)
}

pub fn get_optional_from_properties<T: FromStr>(
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

pub(crate) fn get_optional_list<T: FromStr>(
    section: Option<&'static str>,
    field: &'static str,
    ini_config: &Ini,
) -> Result<Option<Vec<T>>, ConfigurationError> {
    let properties = if let Some(properties) = ini_config.section(section) {
        properties
    } else {
        ini_config.general_section()
    };
    get_optional_list_from_properties(field, properties)
}

pub fn get_optional_list_from_properties<T: FromStr>(
    field: &'static str,
    properties: &Properties,
) -> Result<Option<Vec<T>>, ConfigurationError> {
    if let Some(value) = properties.get(field) {
        let cleaned_value = value.trim();

        // Handle JSON array format [item1, item2, ...]
        let items_str = if cleaned_value.starts_with('[') && cleaned_value.ends_with(']') {
            &cleaned_value[1..cleaned_value.len() - 1]
        } else {
            cleaned_value
        };

        let parsed_values: Result<Vec<T>, _> = items_str
            .split(',')
            .map(|s| s.trim())
            .map(|s| s.trim_matches('"')) // Remove quotes if present
            .filter(|s| !s.is_empty())
            .map(|item| T::from_str(item))
            .collect();

        match parsed_values {
            Ok(values) => Ok(Some(values)),
            Err(_) => Err(TypeError(field, type_name::<T>())),
        }
    } else {
        Ok(None)
    }
}

pub fn get_mandatory<T: FromStr>(
    section: Option<&'static str>,
    field: &'static str,
    ini_config: &Ini,
) -> Result<T, ConfigurationError> {
    let properties = if let Some(properties) = ini_config.section(section) {
        properties
    } else {
        ini_config.general_section()
    };
    get_mandatory_from_properties(field, properties)
}

pub(crate) fn get_mandatory_from_properties<T: FromStr>(
    field: &'static str,
    properties: &Properties,
) -> Result<T, ConfigurationError> {
    match properties.get(field) {
        Some(value) => match T::from_str(value) {
            Ok(value) => Ok(value),
            Err(_e) => Err(TypeError(field, type_name::<T>())),
        },
        None => Err(MissingMandatoryField(field)),
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

        Ok(Configuration {
            mqtt: MqttConfiguration::try_from(&pick_mandatory_section(
                MQTT_SECTION,
                &mut ini_config,
            )?)?,
            #[cfg(feature = "geo_routing")]
            geo: GeoConfiguration::try_from(&pick_mandatory_section(
                GEO_SECTION,
                &mut ini_config,
            )?)?,
            #[cfg(feature = "telemetry")]
            telemetry: TelemetryConfiguration::try_from(&pick_mandatory_section(
                TELEMETRY_SECTION,
                &mut ini_config,
            )?)?,
            #[cfg(feature = "mobility")]
            mobility: MobilityConfiguration::try_from(&pick_mandatory_section(
                MOBILITY_SECTION,
                &mut ini_config,
            )?)?,
            custom_settings: Some(ini_config),
        })
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::client::configuration::{
        Configuration, get_mandatory, get_optional, pick_mandatory_section,
    };
    use ini::Ini;

    const EXHAUSTIVE_CUSTOM_INI_CONFIG: &str = r#"
no_section = noitceson

[mqtt]
host = localhost
port = 1883
use_tls = false
use_websocket = false
client_id = com_myapplication
username = username
password = password

[geo]
prefix = sandbox
suffix = v2x

[mobility]
source_uuid = com_myapplication-1
station_id = 1
use_responsibility = true
thread_count = 4

[telemetry]
host = otlp.domain.com
port = 5418
use_tls = false
path = /custom/v1/traces
max_batch_size = 10
username = username
password = password

[custom]
test = success
"#;

    const MINIMAL_FEATURELESS_CONFIGURATION: &str = r#"
[mqtt]
host = localhost
port = 1883
use_tls = false
use_websocket = false
client_id = com_myapplication
"#;

    #[cfg(feature = "mobility")]
    const MINIMAL_MOBILITY_CONFIGURATION: &str = r#"
[mqtt]
host = localhost
port = 1883
use_tls = false
use_websocket = false
client_id = com_myapplication

[mobility]
source_uuid = com_myapplication-1
station_id = 1
use_responsibility = false
thread_count = 4
"#;

    #[cfg(feature = "mobility")]
    const MINIMAL_GEO_ROUTING_CONFIGURATION: &str = r#"
[mqtt]
host = localhost
port=1883
use_tls = false
use_websocket = false
client_id= com_myapplication

[mobility]
source_uuid = com_myapplication-1
station_id = 1
use_responsibility = false
thread_count = 4

[geo]
prefix = sandbox
suffix = v2x
"#;

    #[cfg(feature = "telemetry")]
    const MINIMAL_TELEMETRY_CONFIGURATION: &str = r#"
[mqtt]
host = localhost
port = 1883
use_tls = false
use_websocket = false
client_id = com_myapplication

[telemetry]
host = otlp.domain.com
port = 5418
use_tls = false
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

        let ok_none = get_optional::<String>(None, "pmloikjuyh", &ini);

        let none = ok_none.expect("Not set field should return Ok(None)");
        assert!(none.is_none());
    }

    #[test]
    fn optional_no_section_is_ok_some() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let ok_some = get_optional::<String>(None, "no_section", &ini);

        let some = ok_some.expect("Optional field must return Ok(Some(T)): found Err(_)");
        let value = some.expect("Optional field must return Ok(Some(T)): found OK(None)");
        assert_eq!(value, "noitceson");
    }

    #[test]
    fn optional_from_section_is_ok_some() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let ok_some = get_optional::<String>(Some("custom"), "test", &ini);

        let some = ok_some.expect("Optional field must return Ok(Some(T)): found Err(_)");
        let value = some.expect("Optional field must return Ok(Some(T)): found OK(None)");
        assert_eq!(value, "success");
    }

    #[test]
    fn optional_wrong_type_is_err() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let err = get_optional::<u16>(Some("custom"), "test", &ini);

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

        Configuration::try_from(ini)
            .expect("Failed to create Configuration with minimal mandatory sections and fields");
    }

    #[test]
    #[cfg(feature = "mobility")]
    #[cfg_attr(any(feature = "telemetry", feature = "geo_routing"), should_panic)]
    fn minimal_mobility_configuration() {
        let ini = Ini::load_from_str(MINIMAL_MOBILITY_CONFIGURATION)
            .expect("Ini creation should not fail");

        Configuration::try_from(ini)
            .expect("Failed to create Configuration with minimal mandatory sections and fields");
    }

    #[test]
    #[cfg(feature = "geo_routing")]
    #[cfg_attr(feature = "telemetry", should_panic)]
    fn minimal_geo_routing_configuration() {
        let ini = Ini::load_from_str(MINIMAL_GEO_ROUTING_CONFIGURATION)
            .expect("Ini creation should not fail");

        Configuration::try_from(ini)
            .expect("Failed to create Configuration with minimal mandatory sections and fields");
    }

    #[test]
    fn mandatory_no_section_is_ok() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let result = get_mandatory::<String>(None, "no_section", &ini);

        assert!(result.is_ok());
        assert_eq!(result.unwrap(), "noitceson");
    }

    #[test]
    fn mandatory_from_section_is_ok() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let result = get_mandatory::<String>(Some("custom"), "test", &ini);

        assert!(result.is_ok());
        assert_eq!(result.unwrap(), "success");
    }

    #[test]
    fn mandatory_missing_field_is_err() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let result = get_mandatory::<String>(None, "non_existent", &ini);

        assert!(result.is_err());
    }

    #[test]
    fn mandatory_wrong_type_is_err() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let result = get_mandatory::<u16>(Some("custom"), "test", &ini);

        assert!(result.is_err());
    }

    #[test]
    fn optional_missing_section_returns_none() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).expect("Ini creation should not fail");

        let result = get_optional::<String>(Some("non_existent_section"), "field", &ini);

        assert!(result.is_ok());
        assert!(result.unwrap().is_none());
    }

    #[test]
    fn get_mandatory_ok() {
        let ini = Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).unwrap();
        let value = get_mandatory::<String>(Some("custom"), "test", &ini).unwrap();
        assert_eq!(value, "success");
    }

    #[test]
    fn get_mandatory_missing_section() {
        let ini = Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).unwrap();
        let result = get_mandatory::<String>(Some("missing"), "test", &ini);
        assert!(result.is_err());
    }

    #[test]
    fn get_mandatory_missing_field() {
        let ini = Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).unwrap();
        let result = get_mandatory::<String>(Some("custom"), "missing", &ini);
        assert!(result.is_err());
    }

    #[test]
    fn get_mandatory_type_error() {
        let ini = Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).unwrap();
        let result = get_mandatory::<u16>(Some("custom"), "test", &ini);
        assert!(result.is_err());
    }

    #[test]
    fn get_optional_ok() {
        let ini = Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).unwrap();
        let value = get_optional::<String>(Some("custom"), "test", &ini)
            .unwrap()
            .unwrap();
        assert_eq!(value, "success");
    }

    #[test]
    fn get_optional_missing_section() {
        let ini = Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).unwrap();
        let result = get_optional::<String>(Some("missing"), "test", &ini).unwrap();
        assert!(result.is_none());
    }

    #[test]
    fn get_optional_missing_field() {
        let ini = Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).unwrap();
        let result = get_optional::<String>(Some("custom"), "missing", &ini).unwrap();
        assert!(result.is_none());
    }

    #[test]
    fn get_optional_type_error() {
        let ini = Ini::load_from_str(EXHAUSTIVE_CUSTOM_INI_CONFIG).unwrap();
        let result = get_optional::<u16>(Some("custom"), "test", &ini);
        assert!(result.is_err());
    }

    #[test]
    fn get_list_with_comma_separated_values() {
        let mut configuration = Configuration::default();
        configuration.set(Some("test"), "list_field", "item1,item2,item3");
        let result = configuration
            .get_list::<String>(Some("test"), "list_field")
            .unwrap();
        assert_eq!(result, vec!["item1", "item2", "item3"]);
    }

    #[test]
    fn get_list_no_custom_settings_error() {
        let mut configuration = Configuration::default();
        configuration.custom_settings = None;
        let result = configuration.get_list::<String>(Some("test"), "list_field");
        assert!(matches!(result, Err(NoCustomSettings)));
    }

    #[test]
    fn get_list_with_spaces_and_trimming() {
        let mut configuration = Configuration::default();
        configuration.set(Some("test"), "list_field", " item1 , item2 , item3 ");
        let result = configuration
            .get_list::<String>(Some("test"), "list_field")
            .unwrap();
        assert_eq!(result, vec!["item1", "item2", "item3"]);
    }

    #[test]
    fn get_list_with_empty_items_filtered() {
        let mut configuration = Configuration::default();
        configuration.set(Some("test"), "list_field", "item1,,item3,");
        let result = configuration
            .get_list::<String>(Some("test"), "list_field")
            .unwrap();
        assert_eq!(result, vec!["item1", "item3"]);
    }

    #[test]
    fn get_list_type_conversion_error() {
        let mut configuration = Configuration::default();
        configuration.set(Some("test"), "list_field", "not_a_number,123");
        let result = configuration.get_list::<u32>(Some("test"), "list_field");
        assert!(result.is_err());
    }

    // Tests for get_optional_list functionality
    #[test]
    fn get_optional_list_ok() {
        let mut properties = ini::Properties::new();
        properties.insert("test_list", "a,b,c".to_string());
        let result = get_optional_list_from_properties::<String>("test_list", &properties).unwrap();
        assert_eq!(
            result,
            Some(vec!["a".to_string(), "b".to_string(), "c".to_string()])
        );
    }

    #[test]
    fn get_optional_list_missing_field() {
        let properties = ini::Properties::new();
        let result = get_optional_list_from_properties::<String>("missing", &properties).unwrap();
        assert!(result.is_none());
    }

    #[test]
    fn get_optional_list_type_error() {
        let mut properties = ini::Properties::new();
        properties.insert("test_list", "not_a_number,123".to_string());
        let result = get_optional_list_from_properties::<u32>("test_list", &properties);
        assert!(result.is_err());
    }

    // Tests for get_optional_list functionality with Ini
    #[test]
    fn get_optional_list_from_ini_ok() {
        let ini_str = r#"
        [test_section]
        list_field = item1,item2,item3
        "#;
        let ini = Ini::load_from_str(ini_str).unwrap();
        let result = get_optional_list::<String>(Some("test_section"), "list_field", &ini).unwrap();
        assert_eq!(
            result,
            Some(vec![
                "item1".to_string(),
                "item2".to_string(),
                "item3".to_string()
            ])
        );
    }

    #[test]
    fn get_optional_list_from_ini_missing_section() {
        let ini_str = r#"
        [other_section]
        list_field = item1,item2,item3
        "#;
        let ini = Ini::load_from_str(ini_str).unwrap();
        let result =
            get_optional_list::<String>(Some("missing_section"), "list_field", &ini).unwrap();
        assert!(result.is_none());
    }

    #[test]
    fn get_optional_list_from_general_section() {
        let ini_str = r#"
        list_field = item1,item2,item3
        [other_section]
        other = value
        "#;
        let ini = Ini::load_from_str(ini_str).unwrap();
        let result = get_optional_list::<String>(None, "list_field", &ini).unwrap();
        assert_eq!(
            result,
            Some(vec![
                "item1".to_string(),
                "item2".to_string(),
                "item3".to_string()
            ])
        );
    }

    // Test for set_mqtt_credentials
    #[test]
    fn set_mqtt_credentials_test() {
        let mut configuration = Configuration::default();
        configuration.set_mqtt_credentials("testuser", "testpass");
        assert_eq!(
            configuration.mqtt.mqtt_options.credentials(),
            Some(("testuser".to_string(), "testpass".to_string()))
        );
    }
}
