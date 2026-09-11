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
use crate::helpers;
use crate::ingest;
use anyhow::Result;
use libits::exchange::Exchange;
use libits::exchange::message::Message;
use libits::exchange::message::content::Content;
use libits::mobility::quadtree::lat_lon_to_quadkey;
use libits::transport::mqtt::mqtt_client::MqttClient;
use rumqttc::v5::{Event, EventLoop, Incoming, MqttOptions};
use std::sync::{Arc, Mutex};

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
    let type_id = ingest::get_or_create_message_type(&conn_guard, layer)?;
    ingest::upsert_tile_with_type_id(&conn_guard, quadkey, type_id, day, confidence)?;
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

                // Try to deserialize the payload as an Exchange (containing Message)
                let exchange: Exchange = match serde_json::from_slice(&payload_bytes) {
                    Ok(exch) => exch,
                    Err(error) => {
                        eprintln!("JSON parse error on topic {}: {}", topic, error);
                        error_count += 1;
                        continue;
                    }
                };

                let message = exchange.message;
                let message_type = message.get_type().to_string();
                let day = helpers::current_day();

                if let Message::CPM(ref cpm) = message {
                    let mut stored = false;

                    // Store CPM reference position under "cpm"
                    if let Some((lat, lon)) = helpers::extract_position(&message) {
                        let confidence = helpers::extract_confidence(&message);
                        let quadkey = lat_lon_to_quadkey(lat, lon, zoom);

                        match store_tile(&db_connection, &quadkey, &message_type, &day, confidence)
                        {
                            Ok(()) => stored = true,
                            Err(error) => {
                                eprintln!("DB error: {}", error);
                                error_count += 1;
                            }
                        }
                    }

                    // Store each perceived object position under "cpm po"
                    for (lat, lon, po_conf) in helpers::extract_perceived_objects(cpm) {
                        let quadkey = lat_lon_to_quadkey(lat, lon, zoom);
                        match store_tile(&db_connection, &quadkey, "cpm po", &day, po_conf) {
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
                } else if let Some((lat, lon)) = helpers::extract_position(&message) {
                    let confidence = helpers::extract_confidence(&message);
                    let quadkey = lat_lon_to_quadkey(lat, lon, zoom);

                    match store_tile(&db_connection, &quadkey, &message_type, &day, confidence) {
                        Ok(()) => stored_count += 1,
                        Err(error) => {
                            eprintln!("DB error: {}", error);
                            error_count += 1;
                        }
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
    fn test_store_tile_reports_mutex_poisoning() {
        let database_connection = Arc::new(Mutex::new(database::open_memory_database().unwrap()));
        let poisoned_connection = Arc::clone(&database_connection);

        let poison_result = std::panic::catch_unwind(move || {
            let _guard = poisoned_connection.lock().unwrap();
            panic!("poison");
        });
        assert!(poison_result.is_err());

        let error = store_tile(&database_connection, "120", "cam", "2025-01-15", 0.0).unwrap_err();
        assert!(error.to_string().contains("mutex poisoned"));
    }
}
