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

use crate::database;
use anyhow::Result;
use libits::mobility::quadtree::{
    CONFIDENCE_UNAVAILABLE, ellipse_confidence_mean, lat_lon_to_quadkey, offset_to_coordinates,
    scaled_coordinates,
};
use libits::transport::mqtt::mqtt_client::MqttClient;
use rumqttc::v5::{Event, EventLoop, Incoming, MqttOptions};
use serde_json::Value;
use std::sync::{Arc, Mutex};
use std::time::{SystemTime, UNIX_EPOCH};

/// Configuration for the MQTT ingestor.
///
/// The MQTT connection options are built by the library (`libits`) from the
/// shared configuration file, so this example only carries the display-specific
/// settings on top of them.
pub struct MqttIngestorConfig {
    pub mqtt_options: MqttOptions,
    pub topics: Vec<String>,
    pub zoom: u8,
    pub db_path: String,
}

/// Extracts the message type from an MQTT topic.
/// Topic format: {context}/outQueue/v2x/{msgtype}/+/{quadkey_path}
/// or simpler: just look for known message types in the topic segments.
fn extract_message_type_from_topic(topic: &str) -> String {
    let segments: Vec<&str> = topic.split('/').collect();
    // Look for v2x segment, the next one is the message type
    for (index, segment) in segments.iter().enumerate() {
        if *segment == "v2x" && index + 1 < segments.len() {
            return segments[index + 1].to_lowercase();
        }
    }
    // Fallback: look for known message types anywhere in the topic
    let known_types = ["cam", "denm", "cpm", "mcm", "mapem", "spatem"];
    for segment in &segments {
        let lower = segment.to_lowercase();
        if known_types.contains(&lower.as_str()) {
            return lower;
        }
    }
    "unknown".to_string()
}

/// Extracts position from a JSON payload. Tries multiple known message formats.
fn extract_position_from_payload(message_type: &str, payload: &Value) -> Option<(f64, f64)> {
    // First try: the payload is the message directly
    if let Some(position) = try_extract_position(message_type, payload) {
        return Some(position);
    }
    // Second try: the payload has a "message" field wrapping the actual message
    if let Some(message) = payload.get("message")
        && let Some(position) = try_extract_position(message_type, message)
    {
        return Some(position);
    }
    None
}

fn try_extract_position(message_type: &str, message: &Value) -> Option<(f64, f64)> {
    let ref_pos = match message_type {
        "cam" => message
            .get("basic_container")
            .and_then(|container| container.get("reference_position")),
        "denm" => message
            .get("management_container")
            .and_then(|container| container.get("event_position")),
        "cpm" => message
            .get("management_container")
            .and_then(|container| container.get("reference_position")),
        _ => message
            .get("basic_container")
            .and_then(|container| container.get("reference_position"))
            .or_else(|| {
                message.get("management_container").and_then(|container| {
                    container
                        .get("reference_position")
                        .or_else(|| container.get("event_position"))
                })
            }),
    };

    let ref_pos = ref_pos?;
    let latitude = ref_pos.get("latitude")?.as_i64()?;
    let longitude = ref_pos.get("longitude")?.as_i64()?;

    scaled_coordinates(latitude, longitude)
}

fn extract_confidence_from_payload(message_type: &str, payload: &Value) -> f64 {
    let message = payload.get("message").unwrap_or(payload);

    let container = match message_type {
        "cam" => message.get("basic_container"),
        "denm" => message.get("management_container"),
        "cpm" => message.get("management_container"),
        _ => message
            .get("basic_container")
            .or_else(|| message.get("management_container")),
    };

    if let Some(container) = container {
        // Try confidence as sibling of reference_position (CAM v1.1.3 format)
        if let Some(confidence) = container.get("confidence")
            && let Some(ellipse) = confidence.get("position_confidence_ellipse")
        {
            return extract_ellipse_value(ellipse);
        }

        // Try confidence nested inside reference_position
        let ref_pos = container
            .get("reference_position")
            .or_else(|| container.get("event_position"));
        if let Some(ref_pos) = ref_pos
            && let Some(confidence) = ref_pos.get("confidence")
            && let Some(ellipse) = confidence.get("position_confidence_ellipse")
        {
            return extract_ellipse_value(ellipse);
        }
    }
    0.0
}

fn extract_perceived_objects_from_payload(message: &Value) -> Vec<(f64, f64, f64)> {
    let management_ref = message
        .get("message")
        .unwrap_or(message)
        .get("management_container")
        .and_then(|container| container.get("reference_position"));

    let (anchor_lat, anchor_lon) = match management_ref {
        Some(ref_pos) => {
            let lat = ref_pos.get("latitude").and_then(|v| v.as_i64());
            let lon = ref_pos.get("longitude").and_then(|v| v.as_i64());
            match (lat, lon) {
                (Some(lat), Some(lon)) => match scaled_coordinates(lat, lon) {
                    Some(position) => position,
                    None => return Vec::new(),
                },
                _ => return Vec::new(),
            }
        }
        None => return Vec::new(),
    };

    let objects = match message
        .get("message")
        .unwrap_or(message)
        .get("perceived_object_container")
        .and_then(|container| container.as_array())
    {
        Some(array) => array,
        None => return Vec::new(),
    };

    let mut results = Vec::new();

    for object in objects {
        if let Some((lat, lon)) = extract_perceived_object_position(object, anchor_lat, anchor_lon)
        {
            let confidence = extract_perceived_object_confidence(object);
            results.push((lat, lon, confidence));
        }
    }

    results
}

fn extract_perceived_object_position(
    object: &Value,
    anchor_lat: f64,
    anchor_lon: f64,
) -> Option<(f64, f64)> {
    let position = object.get("position")?;

    if let (Some(lat_raw), Some(lon_raw)) = (
        position.get("latitude").and_then(|v| v.as_i64()),
        position.get("longitude").and_then(|v| v.as_i64()),
    ) && let Some(position) = scaled_coordinates(lat_raw, lon_raw)
    {
        return Some(position);
    }

    let x_cm = position
        .get("x_coordinate")
        .and_then(|coord| coord.get("value"))
        .and_then(|v| v.as_i64());
    let y_cm = position
        .get("y_coordinate")
        .and_then(|coord| coord.get("value"))
        .and_then(|v| v.as_i64());

    match (x_cm, y_cm) {
        (Some(x_cm), Some(y_cm)) => offset_to_coordinates(anchor_lat, anchor_lon, x_cm, y_cm),
        _ => None,
    }
}

fn extract_perceived_object_confidence(object: &Value) -> f64 {
    object
        .get("position")
        .and_then(|pos| pos.get("confidence"))
        .and_then(|conf| conf.get("position_confidence_ellipse"))
        .map(extract_ellipse_value)
        .unwrap_or(0.0)
}

fn extract_ellipse_value(ellipse: &Value) -> f64 {
    let semi_major = ellipse
        .get("semi_major_confidence")
        .and_then(|value| value.as_u64())
        .unwrap_or(CONFIDENCE_UNAVAILABLE as u64) as u32;
    let semi_minor = ellipse
        .get("semi_minor_confidence")
        .and_then(|value| value.as_u64())
        .unwrap_or(CONFIDENCE_UNAVAILABLE as u64) as u32;
    ellipse_confidence_mean(semi_major, semi_minor)
}

/// Returns current day as YYYY-MM-DD.
fn current_day() -> String {
    let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap();
    let days = (now.as_secs() / 86400) as i64;
    // Inline date conversion
    let shifted_days = days + 719468;
    let era = if shifted_days >= 0 {
        shifted_days / 146097
    } else {
        (shifted_days - 146096) / 146097
    };
    let day_of_era = (shifted_days - era * 146097) as u64;
    let year_of_era =
        (day_of_era - day_of_era / 1460 + day_of_era / 36524 - day_of_era / 146096) / 365;
    let year = year_of_era as i64 + era * 400;
    let day_of_year = day_of_era - (365 * year_of_era + year_of_era / 4 - year_of_era / 100);
    let month_index = (5 * day_of_year + 2) / 153;
    let day = day_of_year - (153 * month_index + 2) / 5 + 1;
    let month = if month_index < 10 {
        month_index + 3
    } else {
        month_index - 9
    };
    let year = if month <= 2 { year + 1 } else { year };
    format!("{:04}-{:02}-{:02}", year, month, day)
}

/// Starts the MQTT ingestor that subscribes to topics and stores messages in the database.
pub async fn run_mqtt_ingestor(config: MqttIngestorConfig) -> Result<()> {
    let (broker_host, broker_port) = config.mqtt_options.broker_address();
    println!(
        "🔌 Connecting to MQTT broker: {}:{}",
        broker_host, broker_port
    );
    println!("   Client ID: {}", config.mqtt_options.client_id());
    println!("   Topics: {:?}", config.topics);
    println!("   Zoom level: {}", config.zoom);
    println!("   Database: {}", config.db_path);

    // Reuse the MQTT options built by the library (transport/TLS/credentials come
    // from the shared configuration) and only tune the display-specific packet size.
    let mut mqtt_options = config.mqtt_options;
    mqtt_options.set_max_packet_size(Some(256_000));

    let (mut client, event_loop) = MqttClient::new(&mqtt_options);

    // Subscribe to all topics through the library client.
    client.subscribe(&config.topics).await;
    println!("✅ Subscribed to topics");

    // Open database
    let conn = database::open_database(&config.db_path)?;
    let db_connection = Arc::new(Mutex::new(conn));

    let zoom = config.zoom;
    run_event_loop(event_loop, db_connection, zoom).await
}

/// Stores a single tile in the database, locking the shared connection.
///
/// Uses `?` for both the lock and the upsert so callers can propagate errors.
fn store_tile(
    db_connection: &Arc<Mutex<rusqlite::Connection>>,
    quadkey: &str,
    layer: &str,
    day: &str,
    confidence: f64,
) -> Result<()> {
    let conn_guard = db_connection
        .lock()
        .map_err(|error| anyhow::anyhow!("database connection mutex poisoned: {error}"))?;
    database::upsert_tile(&conn_guard, quadkey, layer, day, confidence)?;
    Ok(())
}

async fn run_event_loop(
    mut event_loop: EventLoop,
    db_connection: Arc<Mutex<rusqlite::Connection>>,
    zoom: u8,
) -> Result<()> {
    let mut message_count: u64 = 0;
    let mut stored_count: u64 = 0;
    let mut error_count: u64 = 0;

    println!("📡 Listening for messages...");

    loop {
        match event_loop.poll().await {
            Ok(Event::Incoming(Incoming::Publish(publish))) => {
                message_count += 1;

                let topic = String::from_utf8_lossy(&publish.topic).to_string();
                let payload_bytes = publish.payload.to_vec();

                let message_type = extract_message_type_from_topic(&topic);

                match serde_json::from_slice::<Value>(&payload_bytes) {
                    Ok(payload) => {
                        let day = current_day();

                        if message_type == "cpm" {
                            let mut stored = false;

                            // Store CPM reference position under "cpm"
                            if let Some((lat, lon)) =
                                extract_position_from_payload(&message_type, &payload)
                            {
                                let confidence =
                                    extract_confidence_from_payload(&message_type, &payload);
                                let quadkey = lat_lon_to_quadkey(lat, lon, zoom);

                                match store_tile(
                                    &db_connection,
                                    &quadkey,
                                    &message_type,
                                    &day,
                                    confidence,
                                ) {
                                    Ok(()) => stored = true,
                                    Err(error) => {
                                        eprintln!("DB error: {}", error);
                                        error_count += 1;
                                    }
                                }
                            }

                            // Store each perceived object position under "cpm po"
                            for (lat, lon, po_conf) in
                                extract_perceived_objects_from_payload(&payload)
                            {
                                let quadkey = lat_lon_to_quadkey(lat, lon, zoom);
                                match store_tile(&db_connection, &quadkey, "cpm po", &day, po_conf)
                                {
                                    Ok(()) => stored = true,
                                    Err(error) => {
                                        eprintln!("DB error: {}", error);
                                        error_count += 1;
                                    }
                                }
                            }

                            if stored {
                                stored_count += 1;
                            }
                        } else if let Some((lat, lon)) =
                            extract_position_from_payload(&message_type, &payload)
                        {
                            let confidence =
                                extract_confidence_from_payload(&message_type, &payload);
                            let quadkey = lat_lon_to_quadkey(lat, lon, zoom);

                            match store_tile(
                                &db_connection,
                                &quadkey,
                                &message_type,
                                &day,
                                confidence,
                            ) {
                                Ok(()) => stored_count += 1,
                                Err(error) => {
                                    eprintln!("DB error: {}", error);
                                    error_count += 1;
                                }
                            }
                        }
                    }
                    Err(error) => {
                        eprintln!("JSON parse error on topic {}: {}", topic, error);
                        error_count += 1;
                    }
                }

                if message_count.is_multiple_of(1000) {
                    println!(
                        "📊 Messages: {} received, {} stored, {} errors",
                        message_count, stored_count, error_count
                    );
                }
            }
            Ok(_) => {
                // Other MQTT events (ConnAck, SubAck, PingResp, etc.)
            }
            Err(error) => {
                eprintln!("❌ MQTT event loop error: {:?}", error);
                eprintln!("   Retrying in 5 seconds...");
                tokio::time::sleep(tokio::time::Duration::from_secs(5)).await;
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_extract_message_type_from_topic_v2x() {
        let topic = "mycontext/outQueue/v2x/cam/+/0/1/2/3";
        assert_eq!(extract_message_type_from_topic(topic), "cam");
    }

    #[test]
    fn test_extract_message_type_from_topic_denm() {
        let topic = "mycontext/outQueue/v2x/denm/some-uuid";
        assert_eq!(extract_message_type_from_topic(topic), "denm");
    }

    #[test]
    fn test_extract_message_type_from_topic_unknown() {
        let topic = "some/random/topic";
        assert_eq!(extract_message_type_from_topic(topic), "unknown");
    }

    #[test]
    fn test_extract_message_type_from_topic_case_insensitive() {
        let topic = "context/outQueue/v2x/CAM/uuid/0/1";
        assert_eq!(extract_message_type_from_topic(topic), "cam");
    }

    #[test]
    fn test_extract_position_from_payload_cam() {
        let payload: Value = serde_json::from_str(
            r#"{"basic_container":{"reference_position":{"latitude":488566000,"longitude":23522000}}}"#,
        ).unwrap();
        let result = extract_position_from_payload("cam", &payload);
        assert!(result.is_some());
        let (lat, lon) = result.unwrap();
        assert!((lat - 48.8566).abs() < 0.0001);
        assert!((lon - 2.3522).abs() < 0.0001);
    }

    #[test]
    fn test_extract_position_from_payload_wrapped() {
        let payload: Value = serde_json::from_str(
            r#"{"message":{"basic_container":{"reference_position":{"latitude":488566000,"longitude":23522000}}}}"#,
        ).unwrap();
        let result = extract_position_from_payload("cam", &payload);
        assert!(result.is_some());
    }

    #[test]
    fn test_extract_position_from_payload_no_position() {
        let payload: Value = serde_json::from_str(r#"{"data": "something"}"#).unwrap();
        let result = extract_position_from_payload("cam", &payload);
        assert!(result.is_none());
    }

    #[test]
    fn test_current_day_format() {
        let day = current_day();
        assert_eq!(day.len(), 10);
        assert_eq!(day.chars().nth(4), Some('-'));
        assert_eq!(day.chars().nth(7), Some('-'));
    }
}
