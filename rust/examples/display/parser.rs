/*
 * Software Name : libits-client
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 */

use crate::quadtree::{ellipse_confidence_mean, lat_lon_to_quadkey};
use anyhow::Result;
use flate2::read::GzDecoder;
use rusqlite::{Connection, params};
use serde::Deserialize;
use std::fs::{self, File};
use std::io::{BufRead, BufReader};
use std::path::{Path, PathBuf};

#[derive(Deserialize)]
struct LogEntry {
    #[serde(rename = "type")]
    message_type: String,
    message: CamMessage,
}

#[derive(Deserialize)]
struct CamMessage {
    basic_container: BasicContainer,
}

#[derive(Deserialize)]
struct BasicContainer {
    reference_position: ReferencePosition,
}

#[derive(Deserialize)]
struct ReferencePosition {
    latitude: i64,
    longitude: i64,
    #[serde(default)]
    confidence: Option<Confidence>,
}

#[derive(Deserialize)]
struct Confidence {
    position_confidence_ellipse: PositionConfidenceEllipse,
}

#[derive(Deserialize)]
struct PositionConfidenceEllipse {
    semi_major_confidence: u32,
    semi_minor_confidence: u32,
}

fn find_cam_log_files(dir_path: &Path) -> Result<Vec<PathBuf>> {
    let mut files = Vec::new();

    for entry in fs::read_dir(dir_path)? {
        let entry = entry?;
        let path = entry.path();

        if !path.is_file() {
            continue;
        }

        let filename = path.file_name().unwrap().to_string_lossy();

        // Match files like: *_cam_*.log or *_cam_*.log.*.gz
        if filename.contains("_cam_") && filename.contains(".log") {
            files.push(path);
        }
    }

    files.sort();
    Ok(files)
}

fn process_log_file(
    file_path: &Path,
    conn: &Connection,
    zoom: u8,
    stats: &mut ParseStats,
) -> Result<()> {
    let filename = file_path.file_name().unwrap().to_string_lossy();
    println!("  📄 Processing: {}", filename);

    let file = File::open(file_path)?;
    let reader: Box<dyn BufRead> = if filename.ends_with(".gz") {
        Box::new(BufReader::new(GzDecoder::new(file)))
    } else {
        Box::new(BufReader::new(file))
    };

    for (line_num, line) in reader.lines().enumerate() {
        let line = match line {
            Ok(l) => l,
            Err(e) => {
                eprintln!("     Line {}: read error: {}", line_num + 1, e);
                stats.errors += 1;
                continue;
            }
        };

        if line.trim().is_empty() {
            continue;
        }

        let entry: LogEntry = match serde_json::from_str(&line) {
            Ok(e) => e,
            Err(e) => {
                eprintln!("     Line {}: JSON parse error: {}", line_num + 1, e);
                stats.errors += 1;
                continue;
            }
        };

        if entry.message_type != "cam" {
            continue;
        }

        let ref_pos = &entry.message.basic_container.reference_position;

        let lat = ref_pos.latitude as f64 / 1e7;
        let lon = ref_pos.longitude as f64 / 1e7;

        if !(-90.0..=90.0).contains(&lat) || !(-180.0..=180.0).contains(&lon) {
            eprintln!(
                "     Line {}: invalid coordinates lat={}, lon={}",
                line_num + 1,
                lat,
                lon
            );
            stats.errors += 1;
            continue;
        }

        // Extract confidence ellipse
        let conf_mean = if let Some(confidence) = &ref_pos.confidence {
            ellipse_confidence_mean(
                confidence.position_confidence_ellipse.semi_major_confidence,
                confidence.position_confidence_ellipse.semi_minor_confidence,
            )
        } else {
            0.0 // Default if confidence is missing
        };

        let quadkey = lat_lon_to_quadkey(lat, lon, zoom);

        conn.execute(
            "INSERT INTO quadtiles (quadkey, sum_confidence, count, mean_confidence)
             VALUES (?1, ?2, 1, ?2)
             ON CONFLICT(quadkey) DO UPDATE SET
               sum_confidence = sum_confidence + excluded.sum_confidence,
               count = count + 1,
               mean_confidence = sum_confidence / count",
            params![quadkey, conf_mean],
        )?;

        stats.processed += 1;

        if stats.processed.is_multiple_of(10000) {
            println!("     ✓ Processed {} messages...", stats.processed);
        }
    }

    Ok(())
}

struct ParseStats {
    processed: usize,
    errors: usize,
}

pub fn parse_and_store(input_path: &str, db_path: &str, zoom: u8) -> Result<()> {
    let input = Path::new(input_path);

    let files = if input.is_dir() {
        println!("📂 Scanning directory: {}", input_path);
        let found = find_cam_log_files(input)?;
        println!("   Found {} CAM log file(s)", found.len());
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
        println!("⚠️  No CAM log files found");
        return Ok(());
    }

    println!("🗄️  Opening database: {}", db_path);
    let conn = Connection::open(db_path)?;

    conn.execute(
        "CREATE TABLE IF NOT EXISTS quadtiles (
            quadkey TEXT PRIMARY KEY,
            sum_confidence REAL,
            count INTEGER,
            mean_confidence REAL
        )",
        [],
    )?;

    let mut stats = ParseStats {
        processed: 0,
        errors: 0,
    };

    println!("📖 Parsing at zoom level {}...", zoom);
    println!();

    for file_path in &files {
        if let Err(e) = process_log_file(file_path, &conn, zoom, &mut stats) {
            eprintln!("  ❌ Error processing {:?}: {}", file_path, e);
        }
    }

    println!();
    println!("✅ Parsing complete:");
    println!("   - Files processed: {}", files.len());
    println!("   - CAM messages: {}", stats.processed);
    println!("   - Errors: {}", stats.errors);
    println!("   - Database: {}", db_path);

    Ok(())
}
