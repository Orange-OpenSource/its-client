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
use flate2::read::GzDecoder;
use libits::exchange::message::Message;
use libits::exchange::message::content::Content;
use libits::mobility::quadtree::lat_lon_to_quadkey;
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
    #[allow(dead_code)]
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

        // Skip entries without a message body (e.g. broker/status messages)
        let raw_message = match entry.message {
            Some(ref msg) => msg,
            None => {
                stats.skipped += 1;
                continue;
            }
        };

        // Try to parse the raw JSON as an SDK Message (CAM, DENM, CPM, CAM113, etc.)
        let message: Message = match serde_json::from_value(raw_message.clone()) {
            Ok(msg) => msg,
            Err(_) => {
                stats.skipped += 1;
                continue;
            }
        };

        let message_type = message.get_type().to_string();
        let day = helpers::day_from_timestamp_ms(entry.timestamp);

        // For CPM, also ingest perceived objects as separate message type "cpm po"
        // (one entry per perceived object position)
        if let Message::CPM(ref cpm) = message {
            let perceived_objects = helpers::extract_perceived_objects(cpm);
            if perceived_objects.is_empty() {
                stats.skipped += 1;
                continue;
            }

            let type_id_cpm = *type_id_cache
                .entry(message_type.clone())
                .or_insert_with(|| {
                    ingest::get_or_create_message_type(conn, &message_type).unwrap()
                });
            let type_id_po = *type_id_cache
                .entry("cpm po".to_string())
                .or_insert_with(|| ingest::get_or_create_message_type(conn, "cpm po").unwrap());

            // Anchor CPM reference position for the message itself
            if let Some((lat, lon)) = helpers::extract_position(&message) {
                let confidence = helpers::extract_confidence(&message);
                let quadkey = lat_lon_to_quadkey(lat, lon, zoom);
                ingest::upsert_tile_with_type_id(conn, &quadkey, type_id_cpm, &day, confidence)?;
                stats.processed += 1;
                batch_count += 1;
            }

            for (lat, lon, po_conf) in perceived_objects {
                let quadkey = lat_lon_to_quadkey(lat, lon, zoom);
                ingest::upsert_tile_with_type_id(conn, &quadkey, type_id_po, &day, po_conf)?;
                stats.processed += 1;
                batch_count += 1;
            }
        } else {
            // Extract position for other message types
            let position = match helpers::extract_position(&message) {
                Some(pos) => pos,
                None => {
                    stats.skipped += 1;
                    continue;
                }
            };

            let (lat, lon) = position;
            let confidence = helpers::extract_confidence(&message);
            let quadkey = lat_lon_to_quadkey(lat, lon, zoom);

            let type_id = if let Some(&cached_id) = type_id_cache.get(&message_type) {
                cached_id
            } else {
                let new_id = ingest::get_or_create_message_type(conn, &message_type)?;
                type_id_cache.insert(message_type.clone(), new_id);
                new_id
            };

            ingest::upsert_tile_with_type_id(conn, &quadkey, type_id, &day, confidence)?;

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
    fn test_day_from_timestamp_ms() {
        // 2025-01-15 12:00:00 UTC = 1736942400000 ms
        let day = helpers::day_from_timestamp_ms(1736942400000);
        assert_eq!(day, "2025-01-15");
    }

    #[test]
    fn test_day_from_timestamp_ms_epoch() {
        let day = helpers::day_from_timestamp_ms(0);
        assert_eq!(day, "1970-01-01");
    }

    #[test]
    fn test_day_from_timestamp_ms_schema_example() {
        // Example from the CAM schema: 1574778515424 ms
        let day = helpers::day_from_timestamp_ms(1574778515424);
        assert_eq!(day, "2019-11-26");
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
