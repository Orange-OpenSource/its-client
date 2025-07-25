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

use crate::client::bootstrap::bootstrap_error::BootstrapError;
use crate::client::configuration::bootstrap_configuration::BootstrapConfiguration;
use crate::client::configuration::configuration_error::ConfigurationError;
#[cfg(feature = "geo_routing")]
use crate::client::configuration::geo_configuration::GeoConfiguration;
use crate::client::configuration::mqtt_configuration::MqttConfiguration;
#[cfg(feature = "telemetry")]
use crate::client::configuration::telemetry_configuration::TelemetryConfiguration;
use crate::client::configuration::{Configuration, get_optional_from_properties};
#[cfg(feature = "mobility")]
use crate::client::configuration::{
    mobility_configuration::MOBILITY_SECTION, mobility_configuration::MobilityConfiguration,
    pick_mandatory_section,
};

use crate::client::bootstrap::bootstrap_error::BootstrapError::{
    InvalidResponse, MissingField, NotAString,
};
use crate::client::configuration::configuration_error::ConfigurationError::{
    BootstrapFailure, MissingMandatoryField,
};
use ini::{Ini, Properties};
use log::{debug, error, info, trace, warn};
use reqwest::Url;
use serde_json::{Value, json};
use std::collections::HashMap;

mod bootstrap_error;

#[derive(Debug)]
struct Bootstrap {
    id: String,
    username: String,
    password: String,
    protocols: HashMap<String, String>,
}

impl TryFrom<Value> for Bootstrap {
    type Error = BootstrapError;

    fn try_from(value: Value) -> Result<Self, Self::Error> {
        if let Some(protocols) = value.get("protocols") {
            if let Some(protocols) = protocols.as_object() {
                let protocols: Result<_, _> = protocols.iter().map(extract_protocol_pair).collect();
                let protocols = protocols?;

                Ok(Bootstrap {
                    id: extract_str("iot3_id", &value)?,
                    username: extract_str("psk_run_login", &value)?,
                    password: extract_str("psk_run_password", &value)?,
                    protocols,
                })
            } else {
                warn!("Failed to convert {protocols:?} as JSON object");
                Err(InvalidResponse("'protocols' field is not a JSON object"))
            }
        } else {
            Err(MissingField("protocols"))
        }
    }
}

/// Calls the bootstrap API and builds a Configuration out of bootstrap information
///
/// Bootstrap sequence will return the URL and credentials to use to connect to the services
/// (MQTT, OTLP collector, ...), any of these information already present in the configuration file
/// would be overridden  
/// All the other fields that are unrelated to services connection are still read from
/// the configuration file and set in the Configuration object
///
/// Example of the bootstrap configuration section:
/// ```ini
/// [bootstrap]
/// host="mydomain.com"
/// port=1234
/// path="/bootstrap"
/// role="external-app"
/// username="boot"
/// password="str4P!"
/// ```
pub async fn bootstrap(mut ini: Ini) -> Result<Configuration, ConfigurationError> {
    info!("Beginning bootstrap...");

    let bootstrap_configuration = BootstrapConfiguration::try_from(&mut ini)?;
    #[cfg(feature = "mobility")]
    let mobility_configuration =
        MobilityConfiguration::try_from(&pick_mandatory_section(MOBILITY_SECTION, &mut ini)?)?;
    #[cfg(feature = "mobility")]
    let id = mobility_configuration.source_uuid.as_str();
    #[cfg(not(feature = "mobility"))]
    let id = "iot3";
    match do_bootstrap(bootstrap_configuration, id).await {
        Ok(b) => {
            info!("Bootstrap call successful");
            debug!("Bootstrap received: {b:?}");

            Ok(Configuration {
                mqtt: mqtt_configuration_from_bootstrap(
                    &b,
                    ini.delete(Some("mqtt")).unwrap_or_default(),
                )?,
                #[cfg(feature = "geo_routing")]
                geo: GeoConfiguration::try_from(&pick_mandatory_section(
                    crate::client::configuration::geo_configuration::GEO_SECTION,
                    &mut ini,
                )?)?,
                #[cfg(feature = "telemetry")]
                telemetry: telemetry_configuration_from_bootstrap(
                    &b,
                    ini.delete(Some("telemetry")).unwrap_or_default(),
                )?,
                #[cfg(feature = "mobility")]
                mobility: mobility_configuration,
                custom_settings: Some(ini),
            })
        }
        Err(e) => {
            error!("Failed to proceed to bootstrap: {e:?}");
            Err(BootstrapFailure(format!("{e}")))
        }
    }
}

fn mqtt_configuration_from_bootstrap(
    bootstrap: &Bootstrap,
    mut mqtt_properties: Properties,
) -> Result<MqttConfiguration, ConfigurationError> {
    let tls = get_optional_from_properties("use_tls", &mqtt_properties)?.unwrap_or_default();
    let ws = get_optional_from_properties("use_websocket", &mqtt_properties)?.unwrap_or_default();

    let uri = match (tls, ws) {
        (true, true) => bootstrap
            .protocols
            .get("mqtt-wss")
            .ok_or(MissingMandatoryField("mqtt-wss")),
        (false, true) => bootstrap
            .protocols
            .get("mqtt-ws")
            .ok_or(MissingMandatoryField("mqtt-ws")),
        (true, false) => bootstrap
            .protocols
            .get("mqtts")
            .ok_or(MissingMandatoryField("mqtts")),
        (false, false) => bootstrap
            .protocols
            .get("mqtt")
            .ok_or(MissingMandatoryField("mqtt")),
    }?;

    let url: Url = {
        if let Ok(url) = Url::parse(uri) {
            Ok(url)
        } else {
            Err(BootstrapFailure(format!(
                "Failed to convert '{uri}' as Url"
            )))
        }
    }?;

    if ws {
        mqtt_properties.insert("host", url.authority());
    } else {
        mqtt_properties.insert(
            "host",
            url.host_str()
                .ok_or(BootstrapFailure("URL must have a host".to_string()))?,
        );
    }

    mqtt_properties.insert(
        "port",
        url.port()
            .ok_or(BootstrapFailure("URL must have a port".to_string()))?
            .to_string(),
    );
    mqtt_properties.insert("client_id", &bootstrap.id);
    mqtt_properties.insert("username", &bootstrap.username);
    mqtt_properties.insert("password", &bootstrap.password);

    MqttConfiguration::try_from(&mqtt_properties)
}

#[cfg(feature = "telemetry")]
fn telemetry_configuration_from_bootstrap(
    bootstrap: &Bootstrap,
    mut telemetry_section: Properties,
) -> Result<TelemetryConfiguration, ConfigurationError> {
    let tls = get_optional_from_properties("use_tls", &telemetry_section)?.unwrap_or_default();

    let uri = if tls {
        bootstrap
            .protocols
            .get("otlp-https")
            .ok_or(MissingMandatoryField("otlp-https"))
    } else {
        bootstrap
            .protocols
            .get("otlp-http")
            .ok_or(MissingMandatoryField("otlp-http"))
    }?;

    let url = Url::parse(uri).expect("Not an URL");

    // FIXME wouldn't it be more simple to use the endpoint directly...
    telemetry_section.insert(
        "host",
        url.host_str()
            .ok_or(BootstrapFailure("URL must have a host".to_string()))?,
    );
    telemetry_section.insert(
        "port",
        url.port()
            .ok_or(BootstrapFailure("URL must have a port".to_string()))?
            .to_string(),
    );
    telemetry_section.insert("path", url.path());
    telemetry_section.insert("username", &bootstrap.username);
    telemetry_section.insert("password", &bootstrap.password);

    TelemetryConfiguration::try_from(&telemetry_section)
}

async fn do_bootstrap(
    bootstrap_configuration: BootstrapConfiguration,
    id: &str,
) -> Result<Bootstrap, BootstrapError> {
    info!(
        "Calling bootstrap on '{}'...",
        bootstrap_configuration.endpoint
    );

    let client = reqwest::ClientBuilder::new()
        .build()
        .expect("Failed to create telemetry HTTP client");

    let body = json!({
        "ue_id": id,
        "psk_login": bootstrap_configuration.username,
        "psk_password": bootstrap_configuration.password,
        "role": bootstrap_configuration.role
    })
    .to_string();

    match client
        .post(bootstrap_configuration.endpoint)
        .basic_auth(
            bootstrap_configuration.username,
            Some(bootstrap_configuration.password),
        )
        .body(body)
        .send()
        .await
    {
        Ok(response) => match response.text().await {
            Ok(body) => {
                trace!("Bootstrap body = {body:?}");
                match serde_json::from_str::<Value>(body.as_str()) {
                    Ok(json_value) => Bootstrap::try_from(json_value),
                    Err(e) => {
                        warn!("Unable to parse the JSon {body}");
                        debug!("Parsing error: {e:?}");
                        Err(InvalidResponse("Failed to parse response as JSON"))
                    }
                }
            }
            Err(e) => {
                debug!("Error: {e:?}");
                Err(BootstrapError::ContentError(e.to_string()))
            }
        },
        Err(e) => {
            debug!("Request error: {e:?}");
            Err(BootstrapError::NetworkError(e.to_string()))
        }
    }
}

fn extract_str(field: &'static str, json_value: &Value) -> Result<String, BootstrapError> {
    if let Some(value) = json_value.get(field) {
        if let Some(as_str) = value.as_str() {
            Ok(as_str.to_string())
        } else {
            Err(NotAString(field.to_string()))
        }
    } else {
        Err(MissingField(field))
    }
}

fn extract_protocol_pair(entry: (&String, &Value)) -> Result<(String, String), BootstrapError> {
    let key = entry.0.to_string();
    if let Some(value) = entry.1.as_str() {
        Ok((key, value.to_string()))
    } else {
        Err(NotAString(key))
    }
}

#[cfg(test)]
mod tests {
    use crate::client::bootstrap::Bootstrap;
    use serde_json::Value;

    #[test]
    fn try_from_valid_response() {
        let response = serde_json::from_str::<Value>(
            r#"
            {
                "iot3_id": "cool_id",
                "psk_run_login": "notadmin",
                "psk_run_password": "!s3CuR3",
                "protocols": {
                    "mqtt": "mqtt://mqtt.domain.com:1884",
                    "mqtt-ws": "https://domain.com:8000/message",
                    "otlp-http": "https://domain.com:8000/collector",
                    "jaeger-http": "https://domain.com:8000/jaeger"
                }
            }"#,
        )
        .expect("Failed to create JSON from string");

        let result = Bootstrap::try_from(response);

        assert!(result.is_ok());
    }

    macro_rules! try_from_invalid_response_returns_error {
        ($test_name:ident, $response:expr) => {
            #[test]
            fn $test_name() {
                let response = serde_json::from_str::<Value>($response)
                    .expect("Failed to create JSON from string");

                let result = Bootstrap::try_from(response);

                assert!(result.is_err());
            }
        };
    }
    try_from_invalid_response_returns_error!(
        iot3_id_is_not_a_string,
        r#"
        {
            "iot3_id": ["cool_id"],
            "psk_run_login": "notadmin",
            "psk_run_password": "!s3CuR3",
            "protocols": {
                "mqtt": "mqtt://mqtt.domain.com:1884",
                "mqtt-ws": "https://domain.com:8000/message",
                "otlp-http": "https://domain.com:8000/collector",
                "jaeger-http": "https://domain.com:8000/jaeger"
            }
        }"#
    );
    try_from_invalid_response_returns_error!(
        psk_login_is_not_a_string,
        r#"
        {
            "iot3_id": "cool_id",
            "psk_run_login": {"value": "notadmin"},
            "psk_run_password": "!s3CuR3",
            "protocols": {
                "mqtt": "mqtt://mqtt.domain.com:1884",
                "mqtt-ws": "https://domain.com:8000/message",
                "otlp-http": "https://domain.com:8000/collector",
                "jaeger-http": "https://domain.com:8000/jaeger"
            }
        }"#
    );
    try_from_invalid_response_returns_error!(
        psk_password_is_not_a_string,
        r#"
        {
            "iot3_id": "cool_id",
            "psk_run_login": "notadmin",
            "psk_run_password": {"plain": "!s3CuR3"},
            "protocols": {
                "mqtt": "mqtt://mqtt.domain.com:1884",
                "mqtt-ws": "https://domain.com:8000/message",
                "otlp-http": "https://domain.com:8000/collector",
                "jaeger-http": "https://domain.com:8000/jaeger"
            }
        }"#
    );
    try_from_invalid_response_returns_error!(
        missing_protocols,
        r#"
        {
            "iot3_id": "cool_id",
            "psk_run_login": "notadmin",
            "psk_run_password": "!s3CuR3",
            "protocol": {
                "mqtt": "mqtt://mqtt.domain.com:1884",
                "mqtt-ws": "https://domain.com:8000/message",
                "otlp-http": "https://domain.com:8000/collector",
                "jaeger-http": "https://domain.com:8000/jaeger"
            }
        }"#
    );
    try_from_invalid_response_returns_error!(
        protocols_is_not_an_object,
        r#"
        {
            "iot3_id": "cool_id",
            "psk_run_login": "notadmin",
            "psk_run_password": "!s3CuR3",
            "protocols": [
                "mqtt://mqtt.domain.com:1884",
                "https://domain.com:8000/message",
                "https://domain.com:8000/collector",
                "https://domain.com:8000/jaeger"
            ]
        }"#
    );
    try_from_invalid_response_returns_error!(
        protocol_value_is_not_a_string,
        r#"
        {
            "iot3_id": "cool_id",
            "psk_run_login": "notadmin",
            "psk_run_password": "!s3CuR3",
            "protocols": {
                "mqtt": ["mqtt://mqtt.domain.com:1884", "mqtts://mqtt.domain.com:8884"]
            }
        }"#
    );
}
