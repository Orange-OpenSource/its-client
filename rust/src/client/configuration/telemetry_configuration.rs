use base64::Engine;
use ini::Properties;
use log::warn;
use std::string::ToString;

use crate::client::configuration::configuration_error::ConfigurationError;
use crate::client::configuration::{get_mandatory_field, get_optional_from_section};

pub(crate) const TELEMETRY_SECTION: &str = "telemetry";
pub(crate) const DEFAULT_PATH: &str = "v1/traces";

/// OpenTelemetry configuration
///
/// Ini configuration example:
/// ```ini
/// [telemetry]
/// ; IP or hostname of the OTLP collector
/// host="otlp.company.com"
/// ; Port of the OTLP collector
/// port=14125
/// ; Optionnal, defaults to v1/traces
/// path="custom/v1/traces"
/// ; Optionnal, defaults to 2048
/// batch_size=1024
///```
#[derive(Clone, Debug, Default)]
pub struct TelemetryConfiguration {
    pub host: String,
    pub port: u16,
    pub path: String,
    pub batch_size: usize,
    username: Option<String>,
    password: Option<String>,
}

impl TelemetryConfiguration {
    pub(crate) fn basic_auth_header(&self) -> Option<String> {
        if let (Some(username), Some(password)) = (&self.username, &self.password) {
            let raw = format!("{}:{}", username, password);
            let as_b64 = base64::prelude::BASE64_STANDARD.encode(raw.as_bytes());
            Some(format!("Basic {}", as_b64))
        } else {
            None
        }
    }
}

impl TryFrom<&Properties> for TelemetryConfiguration {
    type Error = ConfigurationError;

    fn try_from(properties: &Properties) -> Result<Self, Self::Error> {
        let section = ("telemetry", properties);

        let path = match get_optional_from_section::<String>("path", properties) {
            Ok(value) => value.unwrap_or(DEFAULT_PATH.to_string()),
            Err(e) => {
                warn!(
                    "OLTP collector path could not be read from configuration: {}",
                    e
                );
                warn!("Defaulting to '{}'", DEFAULT_PATH);
                DEFAULT_PATH.to_string()
            }
        };

        let batch_size = match get_optional_from_section::<usize>("batch_size", properties) {
            Ok(value) => value.unwrap_or(2048),
            Err(e) => {
                if let ConfigurationError::TypeError(_, _) = e {
                    panic!("{}", e);
                }
                2048
            }
        };

        let (username, password) =
            match get_optional_from_section::<String>("username", properties)? {
                Some(username) => {
                    let password = get_mandatory_field::<String>("password", section)?;
                    (Some(username), Some(password))
                }
                None => (None, None),
            };

        let s = TelemetryConfiguration {
            host: get_mandatory_field::<String>("host", section)?,
            port: get_mandatory_field::<u16>("port", section)?,
            path,
            batch_size,
            username,
            password,
        };

        Ok(s)
    }
}

#[cfg(test)]
mod test {
    use crate::client::configuration::telemetry_configuration::TelemetryConfiguration;
    use ini::Ini;

    const EXHAUSTIVE_TELEMETRY_CONF: &str = r#"
[telemetry]
host="tel.emetry.com"
port=1234
path="unusual/v1/traces"
batch_size=4096
"#;

    const MINIMAL_TELEMETRY_CONF: &str = r#"
[telemetry]
host="tel.emetry.com"
port=1234
"#;

    #[test]
    fn values_are_read_from_conf() {
        let ini =
            Ini::load_from_str(EXHAUSTIVE_TELEMETRY_CONF).expect("Failed to load string as Ini");

        let telemetry_conf =
            TelemetryConfiguration::try_from(ini.section(Some("telemetry")).unwrap());

        let telemetry_conf =
            telemetry_conf.expect("Failed to create TelemetryConfiguration from config");
        assert_eq!("tel.emetry.com", telemetry_conf.host);
        assert_eq!(1234, telemetry_conf.port);
        assert_eq!("unusual/v1/traces", telemetry_conf.path);
        assert_eq!(4096, telemetry_conf.batch_size);
    }

    #[test]
    fn default_values() {
        let ini = Ini::load_from_str(MINIMAL_TELEMETRY_CONF).expect("Failed to load string as Ini");

        let telemetry_conf =
            TelemetryConfiguration::try_from(ini.section(Some("telemetry")).unwrap());

        let telemetry_conf =
            telemetry_conf.expect("Failed to create TelemetryConfiguration from config");
        assert_eq!("v1/traces", telemetry_conf.path);
        assert_eq!(2048, telemetry_conf.batch_size);
    }
}
