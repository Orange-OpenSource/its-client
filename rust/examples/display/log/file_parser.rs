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
use flate2::read::GzDecoder;
use libits::mobility::quadtree::{
    CONFIDENCE_UNAVAILABLE, ellipse_confidence_mean, lat_lon_to_quadkey, offset_to_coordinates,
    scaled_coordinates,
};
use rusqlite::Connection;
use serde::Deserialize;
use serde_json::Value;
use std::collections::HashMap;
use std::fs::{self, File};
use std::io::{BufRead, BufReader, Read};
use std::path::{Path, PathBuf};
use tar::Archive;

/// ITS message envelope as defined by the JSON schema.
/// The `timestamp` field is mandatory and can be an integer (ms) or a float (seconds).
#[derive(Deserialize)]
struct LogEntry {
    #[serde(rename = "type", alias = "message_type")]
    message_type: String,
    message: Option<Value>,
    /// Timestamp: integer milliseconds or float seconds since Unix Epoch.
    #[serde(deserialize_with = "deserialize_timestamp")]
    timestamp: u64,
}

/// Deserializes a timestamp that can be either integer milliseconds or float seconds.
fn deserialize_timestamp<'de, D>(deserializer: D) -> std::result::Result<u64, D::Error>
where
    D: serde::Deserializer<'de>,
{
    let value = Value::deserialize(deserializer)?;
    match value {
        Value::Number(num) => {
            if let Some(int_value) = num.as_u64() {
                Ok(int_value)
            } else if let Some(float_value) = num.as_f64() {
                // Float timestamps are in seconds, convert to milliseconds
                Ok((float_value * 1000.0) as u64)
            } else {
                Err(serde::de::Error::custom("invalid timestamp number"))
            }
        }
        _ => Err(serde::de::Error::custom("timestamp must be a number")),
    }
}

fn message_container<'a>(message_type: &str, message: &'a Value) -> Option<&'a Value> {
    match message_type {
        "cam" => message.get("basic_container"),
        "denm" | "cpm" => message.get("management_container"),
        _ => message
            .get("basic_container")
            .or_else(|| message.get("management_container")),
    }
}

fn extract_lat_lon_raw(position: &Value) -> Option<(i64, i64)> {
    Some((
        position.get("latitude")?.as_i64()?,
        position.get("longitude")?.as_i64()?,
    ))
}

fn confidence_ellipse(value: &Value) -> Option<&Value> {
    value
        .get("confidence")
        .and_then(|confidence| confidence.get("position_confidence_ellipse"))
}

fn extract_position_from_ref(ref_pos: &Value) -> Option<(f64, f64)> {
    let (latitude, longitude) = extract_lat_lon_raw(ref_pos)?;
    scaled_coordinates(latitude, longitude)
}

/// Extracts lat/lon from a generic ITS message.
/// Supports CAM (basic_container.reference_position),
/// DENM (management_container.event_position),
/// CPM (management_container.reference_position),
/// and any other message with a reference_position at the top level.
fn extract_position(message_type: &str, message: &Value) -> Option<(f64, f64)> {
    let container = message_container(message_type, message)?;
    let ref_pos = match message_type {
        "denm" => container.get("event_position"),
        _ => container
            .get("reference_position")
            .or_else(|| container.get("event_position")),
    }?;
    extract_position_from_ref(ref_pos)
}

/// Extracts position confidence from the message.
/// Supports both layouts:
/// - confidence as sibling of reference_position (CAM v1.1.3: basic_container.confidence)
/// - confidence nested inside reference_position (reference_position.confidence)
fn extract_confidence(message_type: &str, message: &Value) -> f64 {
    let container = message_container(message_type, message);

    if let Some(container) = container {
        // Try confidence as sibling of reference_position (CAM v1.1.3 format)
        if let Some(confidence) = container.get("confidence")
            && let Some(ellipse) = confidence.get("position_confidence_ellipse")
        {
            return extract_ellipse_confidence(ellipse);
        }

        // Try confidence nested inside reference_position
        let ref_pos = container
            .get("reference_position")
            .or_else(|| container.get("event_position"));
        if let Some(ref_pos) = ref_pos
            && let Some(ellipse) = confidence_ellipse(ref_pos)
        {
            return extract_ellipse_confidence(ellipse);
        }
    }

    0.0
}

/// Extracts perceived objects positions and confidences from a CPM message.
///
/// Tries multiple layouts:
/// - Geodetic coordinates directly on the perceived object position (latitude/longitude)
/// - Cartesian offsets (x/y in centimeters) relative to the management reference position
fn extract_perceived_objects(message: &Value) -> Vec<(f64, f64, f64)> {
    let management_ref = message
        .get("management_container")
        .and_then(|container| container.get("reference_position"));

    let (anchor_lat, anchor_lon) = match management_ref.and_then(extract_position_from_ref) {
        Some(position) => position,
        None => return Vec::new(),
    };

    let objects = match message
        .get("perceived_object_container")
        .and_then(|container| container.as_array())
    {
        Some(array) => array,
        None => return Vec::new(),
    };

    objects
        .iter()
        .filter_map(|object| {
            extract_perceived_object_position(object, anchor_lat, anchor_lon)
                .map(|(lat, lon)| (lat, lon, extract_perceived_object_confidence(object)))
        })
        .collect()
}

fn extract_perceived_object_position(
    object: &Value,
    anchor_lat: f64,
    anchor_lon: f64,
) -> Option<(f64, f64)> {
    let position = object.get("position")?;

    // 1) Geodetic position directly present
    if let Some((lat_raw, lon_raw)) = extract_lat_lon_raw(position)
        && let Some((lat, lon)) = scaled_coordinates(lat_raw, lon_raw)
    {
        return Some((lat, lon));
    }

    // 2) Cartesian offsets relative to the CPM reference position (values are centimeters in ETSI schema)
    let x_cm = position
        .get("x_coordinate")
        .and_then(|coord| coord.get("value"))
        .and_then(|v| v.as_i64());
    let y_cm = position
        .get("y_coordinate")
        .and_then(|coord| coord.get("value"))
        .and_then(|v| v.as_i64());

    match (x_cm, y_cm) {
        (Some(x_cm), Some(y_cm)) => {
            // Approximate geodetic conversion assuming ENU frame with x=East, y=North
            offset_to_coordinates(anchor_lat, anchor_lon, x_cm, y_cm)
        }
        _ => None,
    }
}

fn extract_perceived_object_confidence(object: &Value) -> f64 {
    object
        .get("position")
        .and_then(confidence_ellipse)
        .map(extract_ellipse_confidence)
        .unwrap_or(0.0)
}

fn extract_ellipse_confidence(ellipse: &Value) -> f64 {
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

/// Extracts the day string (YYYY-MM-DD) from a timestamp in milliseconds since Unix Epoch.
fn day_from_timestamp_ms(timestamp_ms: u64) -> String {
    let seconds = timestamp_ms / 1000;
    let days_since_epoch = (seconds / 86400) as i64;
    epoch_days_to_date(days_since_epoch)
}

/// Converts a count of days since 1970-01-01 to a YYYY-MM-DD string.
/// Algorithm from <https://howardhinnant.github.io/date_algorithms.html>.
fn epoch_days_to_date(days: i64) -> String {
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

/// Recursively finds all log files and tar.gz archives in a directory tree.
fn find_log_files(dir_path: &Path) -> Result<Vec<PathBuf>> {
    let mut files = Vec::new();
    find_log_files_recursive(dir_path, &mut files)?;
    files.sort();
    Ok(files)
}

fn find_log_files_recursive(dir_path: &Path, files: &mut Vec<PathBuf>) -> Result<()> {
    for entry in fs::read_dir(dir_path)? {
        let entry = entry?;
        let path = entry.path();

        if path.is_dir() {
            find_log_files_recursive(&path, files)?;
        } else if path.is_file() {
            let filename = path.file_name().unwrap().to_string_lossy();
            if filename.contains(".log") || filename.ends_with(".tar.gz") {
                files.push(path);
            }
        }
    }

    Ok(())
}

struct ParseStats {
    processed: usize,
    errors: usize,
    skipped: usize,
    duplicates: usize,
}

fn process_log_file(
    file_path: &Path,
    conn: &Connection,
    zoom: u8,
    stats: &mut ParseStats,
    type_id_cache: &mut HashMap<String, i64>,
    before_timestamp_ms: Option<u64>,
) -> Result<()> {
    let filename = file_path.file_name().unwrap().to_string_lossy();

    if filename.ends_with(".tar.gz") {
        return process_tar_gz_file(
            file_path,
            conn,
            zoom,
            stats,
            type_id_cache,
            before_timestamp_ms,
        );
    }

    println!("  📄 Processing: {}", file_path.display());

    let file = File::open(file_path)?;
    let reader: Box<dyn BufRead> = if filename.ends_with(".gz") {
        Box::new(BufReader::new(GzDecoder::new(file)))
    } else {
        Box::new(BufReader::new(file))
    };

    process_lines(
        reader,
        conn,
        zoom,
        stats,
        type_id_cache,
        before_timestamp_ms,
    )
}

/// Processes a .tar.gz archive: extracts each .log entry and parses it.
fn process_tar_gz_file(
    file_path: &Path,
    conn: &Connection,
    zoom: u8,
    stats: &mut ParseStats,
    type_id_cache: &mut HashMap<String, i64>,
    before_timestamp_ms: Option<u64>,
) -> Result<()> {
    println!("  📦 Processing tar.gz: {}", file_path.display());

    let file = File::open(file_path)?;
    let decoder = GzDecoder::new(file);
    let mut archive = Archive::new(decoder);

    for entry in archive.entries()? {
        let mut entry = entry?;
        let entry_path = entry.path()?.to_path_buf();
        let entry_name = entry_path.to_string_lossy().to_string();

        if !entry_name.contains(".log") {
            continue;
        }

        println!("    📄 Archive entry: {}", entry_name);

        let mut content = Vec::new();
        entry.read_to_end(&mut content)?;
        let reader = BufReader::new(std::io::Cursor::new(content));

        process_lines(
            reader,
            conn,
            zoom,
            stats,
            type_id_cache,
            before_timestamp_ms,
        )?;
    }

    Ok(())
}

/// Processes lines from any BufRead source (file, gz, tar entry).
fn process_lines(
    reader: impl BufRead,
    conn: &Connection,
    zoom: u8,
    stats: &mut ParseStats,
    type_id_cache: &mut HashMap<String, i64>,
    before_timestamp_ms: Option<u64>,
) -> Result<()> {
    let mut previous_line: Option<String> = None;
    let mut batch_count: usize = 0;
    const BATCH_SIZE: usize = 10000;

    conn.execute_batch("BEGIN TRANSACTION")?;

    for (line_number, line) in reader.lines().enumerate() {
        let line = match line {
            Ok(line_content) => line_content,
            Err(error) => {
                eprintln!("     Line {}: read error: {}", line_number + 1, error);
                stats.errors += 1;
                continue;
            }
        };

        if line.trim().is_empty() {
            continue;
        }

        // Deduplicate consecutive identical lines
        if let Some(ref prev) = previous_line
            && prev == &line
        {
            stats.duplicates += 1;
            continue;
        }
        previous_line = Some(line.clone());

        let entry: LogEntry = match serde_json::from_str(&line) {
            Ok(parsed_entry) => parsed_entry,
            Err(error) => {
                if stats.errors < 10 {
                    eprintln!("     Line {}: JSON parse error: {}", line_number + 1, error);
                }
                stats.errors += 1;
                continue;
            }
        };

        // Filter by timestamp if --before was specified
        if let Some(cutoff) = before_timestamp_ms
            && entry.timestamp >= cutoff
        {
            stats.skipped += 1;
            continue;
        }

        let message_type = entry.message_type.to_lowercase();

        // Skip entries without a message body (e.g. broker/status messages)
        let message = match entry.message {
            Some(ref msg) => msg,
            None => {
                stats.skipped += 1;
                continue;
            }
        };

        let day = day_from_timestamp_ms(entry.timestamp);

        // For CPM, also ingest perceived objects as separate message type "cpm po"
        // (one entry per perceived object position)
        if message_type == "cpm" {
            let perceived_objects = extract_perceived_objects(message);
            if perceived_objects.is_empty() {
                stats.skipped += 1;
                continue;
            }

            let type_id_cpm = *type_id_cache
                .entry(message_type.clone())
                .or_insert_with(|| {
                    database::get_or_create_message_type(conn, &message_type).unwrap()
                });
            let type_id_po = *type_id_cache
                .entry("cpm po".to_string())
                .or_insert_with(|| database::get_or_create_message_type(conn, "cpm po").unwrap());

            // Anchor CPM reference position for the message itself
            if let Some((lat, lon)) = extract_position(&message_type, message) {
                let confidence = extract_confidence(&message_type, message);
                let quadkey = lat_lon_to_quadkey(lat, lon, zoom);
                database::upsert_tile_with_type_id(conn, &quadkey, type_id_cpm, &day, confidence)?;
                stats.processed += 1;
                batch_count += 1;
            }

            for (lat, lon, po_conf) in perceived_objects {
                let quadkey = lat_lon_to_quadkey(lat, lon, zoom);
                database::upsert_tile_with_type_id(conn, &quadkey, type_id_po, &day, po_conf)?;
                stats.processed += 1;
                batch_count += 1;
            }
        } else {
            // Extract position for other message types
            let position = match extract_position(&message_type, message) {
                Some(pos) => pos,
                None => {
                    stats.skipped += 1;
                    continue;
                }
            };

            let (lat, lon) = position;
            let confidence = extract_confidence(&message_type, message);
            let quadkey = lat_lon_to_quadkey(lat, lon, zoom);

            let type_id = if let Some(&cached_id) = type_id_cache.get(&message_type) {
                cached_id
            } else {
                let new_id = database::get_or_create_message_type(conn, &message_type)?;
                type_id_cache.insert(message_type.clone(), new_id);
                new_id
            };

            database::upsert_tile_with_type_id(conn, &quadkey, type_id, &day, confidence)?;

            stats.processed += 1;
            batch_count += 1;
        }

        if batch_count >= BATCH_SIZE {
            conn.execute_batch("COMMIT; BEGIN TRANSACTION")?;
            batch_count = 0;
            if stats.processed.is_multiple_of(100000) {
                println!(
                    "     ✓ Processed {} messages (skipped {} dupes)...",
                    stats.processed, stats.duplicates
                );
            }
        }
    }

    conn.execute_batch("COMMIT")?;

    Ok(())
}

pub fn parse_and_store(
    input_path: &str,
    db_path: &str,
    zoom: u8,
    before_timestamp_ms: Option<u64>,
) -> Result<()> {
    let input = Path::new(input_path);

    let files = if input.is_dir() {
        println!("📂 Scanning directory: {}", input_path);
        let found = find_log_files(input)?;
        println!("   Found {} log file(s)", found.len());
        found
    } else if input.is_file() {
        println!("📄 Processing single file: {}", input_path);
        vec![input.to_path_buf()]
    } else {
        anyhow::bail!(
            "Input path does not exist or is not accessible: {}",
            input_path
        );
    };

    if files.is_empty() {
        println!("⚠️  No log files found");
        return Ok(());
    }

    println!("🗄️  Opening database: {}", db_path);
    let conn = database::open_database(db_path)?;

    let mut stats = ParseStats {
        processed: 0,
        errors: 0,
        skipped: 0,
        duplicates: 0,
    };

    println!("📖 Parsing at zoom level {}...", zoom);
    if let Some(cutoff) = before_timestamp_ms {
        println!(
            "   ⏱️  Filtering: only messages before timestamp {}",
            cutoff
        );
    }
    println!();

    let mut type_id_cache: HashMap<String, i64> = HashMap::new();

    for file_path in &files {
        if let Err(error) = process_log_file(
            file_path,
            &conn,
            zoom,
            &mut stats,
            &mut type_id_cache,
            before_timestamp_ms,
        ) {
            eprintln!("  ❌ Error processing {:?}: {}", file_path, error);
        }
    }

    println!();
    println!("✅ Parsing complete:");
    println!("   - Files processed: {}", files.len());
    println!("   - Messages stored: {}", stats.processed);
    println!("   - Messages skipped (no position): {}", stats.skipped);
    println!("   - Duplicates skipped: {}", stats.duplicates);
    println!("   - Errors: {}", stats.errors);
    println!("   - Database: {}", db_path);

    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_extract_position_cam() {
        let message: Value = serde_json::from_str(
            r#"{"basic_container":{"reference_position":{"latitude":488566000,"longitude":23522000}}}"#,
        )
            .unwrap();
        let result = extract_position("cam", &message);
        assert!(result.is_some());
        let (lat, lon) = result.unwrap();
        assert!((lat - 48.8566).abs() < 0.0001);
        assert!((lon - 2.3522).abs() < 0.0001);
    }

    #[test]
    fn test_extract_position_denm() {
        let message: Value = serde_json::from_str(
            r#"{"management_container":{"event_position":{"latitude":488566000,"longitude":23522000}}}"#,
        )
            .unwrap();
        let result = extract_position("denm", &message);
        assert!(result.is_some());
    }

    #[test]
    fn test_extract_position_cpm() {
        let message: Value = serde_json::from_str(
            r#"{"management_container":{"reference_position":{"latitude":488566000,"longitude":23522000}}}"#,
        )
            .unwrap();
        let result = extract_position("cpm", &message);
        assert!(result.is_some());
    }

    #[test]
    fn test_extract_position_invalid() {
        let message: Value = serde_json::from_str(r#"{"no_position": true}"#).unwrap();
        let result = extract_position("cam", &message);
        assert!(result.is_none());
    }

    #[test]
    fn test_extract_confidence_sibling_format() {
        // CAM v1.1.3 format: confidence is sibling of reference_position in basic_container
        let message: Value = serde_json::from_str(
            r#"{"basic_container":{"reference_position":{"latitude":488566000,"longitude":23522000},"confidence":{"position_confidence_ellipse":{"semi_major_confidence":100,"semi_minor_confidence":200}}}}"#,
        )
            .unwrap();
        let confidence = extract_confidence("cam", &message);
        assert!((confidence - 150.0).abs() < f64::EPSILON);
    }

    #[test]
    fn test_extract_confidence_nested_format() {
        // Alternate format: confidence nested inside reference_position
        let message: Value = serde_json::from_str(
            r#"{"basic_container":{"reference_position":{"latitude":488566000,"longitude":23522000,"confidence":{"position_confidence_ellipse":{"semi_major_confidence":100,"semi_minor_confidence":200}}}}}"#,
        )
            .unwrap();
        let confidence = extract_confidence("cam", &message);
        assert!((confidence - 150.0).abs() < f64::EPSILON);
    }

    #[test]
    fn test_extract_confidence_real_data() {
        // Real data from xHub CAM
        let message: Value = serde_json::from_str(
            r#"{"protocol_version": 1, "station_id": 7544457, "generation_delta_time": 62809, "basic_container": {"station_type": 5, "reference_position": {"latitude": 436352386, "longitude": 13749545, "altitude": 20976}, "confidence": {"position_confidence_ellipse": {"semi_major_confidence": 10, "semi_minor_confidence": 50, "semi_major_orientation": 1}, "altitude": 1}}, "high_frequency_container": {"heading": 3156, "speed": 2, "longitudinal_acceleration": 161, "drive_direction": 0, "vehicle_length": 40, "vehicle_width": 20, "confidence": {"heading": 2, "speed": 3, "vehicle_length": 0}}}"#,
        )
            .unwrap();
        let confidence = extract_confidence("cam", &message);
        assert!((confidence - 30.0).abs() < f64::EPSILON); // (10+50)/2 = 30
    }

    #[test]
    fn test_extract_confidence_without_data() {
        let message: Value = serde_json::from_str(
            r#"{"basic_container":{"reference_position":{"latitude":488566000,"longitude":23522000}}}"#,
        )
            .unwrap();
        let confidence = extract_confidence("cam", &message);
        assert!((confidence - 0.0).abs() < f64::EPSILON);
    }

    #[test]
    fn test_day_from_timestamp_ms() {
        // 2025-01-15 12:00:00 UTC = 1736942400000 ms
        let day = day_from_timestamp_ms(1736942400000);
        assert_eq!(day, "2025-01-15");
    }

    #[test]
    fn test_day_from_timestamp_ms_epoch() {
        let day = day_from_timestamp_ms(0);
        assert_eq!(day, "1970-01-01");
    }

    #[test]
    fn test_day_from_timestamp_ms_schema_example() {
        // Example from the CAM schema: 1574778515424 ms
        let day = day_from_timestamp_ms(1574778515424);
        assert_eq!(day, "2019-11-26");
    }

    #[test]
    fn test_epoch_days_to_date_known_dates() {
        assert_eq!(epoch_days_to_date(0), "1970-01-01");
        assert_eq!(epoch_days_to_date(365), "1971-01-01");
        assert_eq!(epoch_days_to_date(18628), "2021-01-01");
    }

    #[test]
    fn test_log_entry_deserialization_with_message_type_field() {
        let json = r#"{"message_type":"cam","timestamp":1574778515424,"source_uuid":"test","version":"2.4.0","message":{}}"#;
        let entry: LogEntry = serde_json::from_str(json).unwrap();
        assert_eq!(entry.message_type, "cam");
        assert_eq!(entry.timestamp, 1574778515424);
        assert!(entry.message.is_some());
    }

    #[test]
    fn test_log_entry_deserialization_with_type_field() {
        let json = r#"{"type":"denm","timestamp":1574778515424,"source_uuid":"test","version":"2.4.0","message":{}}"#;
        let entry: LogEntry = serde_json::from_str(json).unwrap();
        assert_eq!(entry.message_type, "denm");
        assert_eq!(entry.timestamp, 1574778515424);
    }

    #[test]
    fn test_log_entry_deserialization_float_timestamp() {
        let json = r#"{"type":"cam","timestamp":1772612387.2833765,"source_uuid":"test","version":"2.4.0","message":{}}"#;
        let entry: LogEntry = serde_json::from_str(json).unwrap();
        assert_eq!(entry.message_type, "cam");
        assert_eq!(entry.timestamp, 1772612387283); // float seconds → ms
    }

    #[test]
    fn test_log_entry_deserialization_without_message() {
        let json =
            r#"{"type":"broker","timestamp":1772612353061,"instance_id":"test","running":true}"#;
        let entry: LogEntry = serde_json::from_str(json).unwrap();
        assert_eq!(entry.message_type, "broker");
        assert!(entry.message.is_none());
    }
}
