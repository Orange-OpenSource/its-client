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

#![allow(dead_code)]

use anyhow::Result;
use rusqlite::{Connection, params};

/// Represents a single tile record returned by queries.
#[derive(Debug, Clone, serde::Serialize)]
#[serde(rename_all = "snake_case")]
pub struct TileRecord {
    pub quadkey: String,
    pub message_type: String,
    pub day: String,
    pub count: i64,
    pub mean_position_confidence: f64,
}

/// Opens (or creates) the SQLite database and ensures the schema exists.
pub fn open_database(db_path: &str) -> Result<Connection> {
    let conn = Connection::open(db_path)?;
    conn.execute_batch(
        "PRAGMA journal_mode=WAL; PRAGMA synchronous=NORMAL; PRAGMA cache_size=-64000;",
    )?;
    create_schema(&conn)?;
    Ok(conn)
}

/// Opens an in-memory SQLite database (for tests or ephemeral use).
pub fn open_memory_database() -> Result<Connection> {
    let conn = Connection::open_in_memory()?;
    create_schema(&conn)?;
    Ok(conn)
}

fn create_schema(conn: &Connection) -> Result<()> {
    conn.execute_batch(
        "CREATE TABLE IF NOT EXISTS message_types (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL UNIQUE
        );

        CREATE TABLE IF NOT EXISTS tile_metrics (
            quadkey TEXT NOT NULL,
            message_type_id INTEGER NOT NULL,
            day TEXT NOT NULL,
            count INTEGER NOT NULL DEFAULT 0,
            sum_position_confidence REAL NOT NULL DEFAULT 0.0,
            mean_position_confidence REAL NOT NULL DEFAULT 0.0,
            PRIMARY KEY (quadkey, message_type_id, day),
            FOREIGN KEY (message_type_id) REFERENCES message_types(id)
        );

        CREATE INDEX IF NOT EXISTS idx_tile_metrics_day ON tile_metrics(day);
        CREATE INDEX IF NOT EXISTS idx_tile_metrics_type ON tile_metrics(message_type_id);
        CREATE INDEX IF NOT EXISTS idx_tile_metrics_quadkey ON tile_metrics(quadkey);",
    )?;
    Ok(())
}

/// Ensures a message type exists and returns its id.
pub fn get_or_create_message_type(conn: &Connection, message_type: &str) -> Result<i64> {
    conn.execute(
        "INSERT OR IGNORE INTO message_types (name) VALUES (?1)",
        params![message_type],
    )?;
    let id: i64 = conn.query_row(
        "SELECT id FROM message_types WHERE name = ?1",
        params![message_type],
        |row| row.get(0),
    )?;
    Ok(id)
}

/// Updates or inserts a single message observation into the database using a pre-resolved type_id.
/// This is the fast path when the caller caches type IDs.
pub fn upsert_tile_with_type_id(
    conn: &Connection,
    quadkey: &str,
    type_id: i64,
    day: &str,
    position_confidence: f64,
) -> Result<()> {
    conn.execute(
        "INSERT INTO tile_metrics (quadkey, message_type_id, day, count, sum_position_confidence, mean_position_confidence)
         VALUES (?1, ?2, ?3, 1, ?4, ?4)
         ON CONFLICT(quadkey, message_type_id, day) DO UPDATE SET
           count = count + 1,
           sum_position_confidence = sum_position_confidence + excluded.sum_position_confidence,
           mean_position_confidence = (sum_position_confidence + excluded.sum_position_confidence) / (count + 1)",
        params![quadkey, type_id, day, position_confidence],
    )?;
    Ok(())
}

/// Updates or inserts a single message observation into the database.
/// - `quadkey`: the tile quadkey
/// - `message_type`: e.g. "cam", "denm", "cpm", "mcm"
/// - `day`: date string in "YYYY-MM-DD" format
/// - `position_confidence`: mean of the position confidence ellipse (0.0 if unavailable)
pub fn upsert_tile(
    conn: &Connection,
    quadkey: &str,
    message_type: &str,
    day: &str,
    position_confidence: f64,
) -> Result<()> {
    let type_id = get_or_create_message_type(conn, message_type)?;
    conn.execute(
        "INSERT INTO tile_metrics (quadkey, message_type_id, day, count, sum_position_confidence, mean_position_confidence)
         VALUES (?1, ?2, ?3, 1, ?4, ?4)
         ON CONFLICT(quadkey, message_type_id, day) DO UPDATE SET
           count = count + 1,
           sum_position_confidence = sum_position_confidence + excluded.sum_position_confidence,
           mean_position_confidence = (sum_position_confidence + excluded.sum_position_confidence) / (count + 1)",
        params![quadkey, type_id, day, position_confidence],
    )?;
    Ok(())
}

/// Returns all available days in the database, sorted ascending.
pub fn list_days(conn: &Connection) -> Result<Vec<String>> {
    let mut stmt = conn.prepare("SELECT DISTINCT day FROM tile_metrics ORDER BY day ASC")?;
    let days: Vec<String> = stmt
        .query_map([], |row| row.get(0))?
        .filter_map(Result::ok)
        .collect();
    Ok(days)
}

/// Returns all available message types in the database.
pub fn list_message_types(conn: &Connection) -> Result<Vec<String>> {
    let mut stmt = conn.prepare("SELECT name FROM message_types ORDER BY name ASC")?;
    let types: Vec<String> = stmt
        .query_map([], |row| row.get(0))?
        .filter_map(Result::ok)
        .collect();
    Ok(types)
}

/// Queries tiles for a specific day and optional message type filter.
/// If `message_type` is None, aggregates across all message types.
pub fn query_tiles(
    conn: &Connection,
    day: &str,
    message_type: Option<&str>,
) -> Result<Vec<TileRecord>> {
    let tiles = if let Some(msg_type) = message_type {
        let type_id = conn.query_row(
            "SELECT id FROM message_types WHERE name = ?1",
            params![msg_type],
            |row| row.get::<_, i64>(0),
        );
        match type_id {
            Ok(tid) => {
                let mut stmt = conn.prepare(
                    "SELECT quadkey, day, count, mean_position_confidence
                     FROM tile_metrics
                     WHERE day = ?1 AND message_type_id = ?2",
                )?;
                stmt.query_map(params![day, tid], |row| {
                    Ok(TileRecord {
                        quadkey: row.get(0)?,
                        message_type: msg_type.to_string(),
                        day: row.get(1)?,
                        count: row.get(2)?,
                        mean_position_confidence: row.get(3)?,
                    })
                })?
                .filter_map(Result::ok)
                .collect()
            }
            Err(_) => Vec::new(),
        }
    } else {
        // Aggregate across all message types
        let mut stmt = conn.prepare(
            "SELECT tm.quadkey, tm.day,
                    SUM(tm.count) as total_count,
                    CASE WHEN SUM(tm.count) > 0
                         THEN SUM(tm.sum_position_confidence) / SUM(tm.count)
                         ELSE 0.0
                    END as mean_conf
             FROM tile_metrics tm
             WHERE tm.day = ?1
             GROUP BY tm.quadkey, tm.day",
        )?;
        stmt.query_map(params![day], |row| {
            Ok(TileRecord {
                quadkey: row.get(0)?,
                message_type: "all".to_string(),
                day: row.get(1)?,
                count: row.get(2)?,
                mean_position_confidence: row.get(3)?,
            })
        })?
        .filter_map(Result::ok)
        .collect()
    };

    Ok(tiles)
}

/// Prints database statistics to stdout.
pub fn check_database(db_path: &str) -> Result<()> {
    let conn = open_database(db_path)?;

    let total_tiles: i64 = conn.query_row(
        "SELECT COUNT(DISTINCT quadkey) FROM tile_metrics",
        [],
        |row| row.get(0),
    )?;

    let total_messages: i64 = conn.query_row(
        "SELECT COALESCE(SUM(count), 0) FROM tile_metrics",
        [],
        |row| row.get(0),
    )?;

    println!("📊 Database Statistics");
    println!("   Database: {}", db_path);
    println!("   Total unique quadtiles: {}", total_tiles);
    println!("   Total messages: {}", total_messages);
    println!();

    if total_messages == 0 {
        println!("⚠️  Database is empty. Run 'display_log_reader' or 'display_mqtt_reader' first.");
        return Ok(());
    }

    // Per-day stats
    let days = list_days(&conn)?;
    println!("📅 Days with data: {}", days.len());
    for day in &days {
        let day_count: i64 = conn.query_row(
            "SELECT COALESCE(SUM(count), 0) FROM tile_metrics WHERE day = ?1",
            params![day],
            |row| row.get(0),
        )?;
        println!("   {} : {} messages", day, day_count);
    }
    println!();

    // Per-type stats
    let types = list_message_types(&conn)?;
    println!("📨 Message types:");
    for msg_type in &types {
        let type_count: i64 = conn.query_row(
            "SELECT COALESCE(SUM(tm.count), 0) FROM tile_metrics tm
             JOIN message_types mt ON tm.message_type_id = mt.id
             WHERE mt.name = ?1",
            params![msg_type],
            |row| row.get(0),
        )?;
        println!("   {} : {} messages", msg_type, type_count);
    }
    println!();

    // Top 10 quadkeys
    println!("🔝 Top 10 quadkeys (global):");
    println!(
        "{:<24} {:>10} {:>15} {:>10}",
        "Quadkey", "Count", "Mean Confidence", "Type(s)"
    );
    println!("{:-<70}", "");

    let mut stmt = conn.prepare(
        "SELECT tm.quadkey, SUM(tm.count) as total_count,
                CASE WHEN SUM(tm.count) > 0
                     THEN SUM(tm.sum_position_confidence) / SUM(tm.count)
                     ELSE 0.0
                END as mean_conf,
                GROUP_CONCAT(DISTINCT mt.name) as types
         FROM tile_metrics tm
         JOIN message_types mt ON tm.message_type_id = mt.id
         GROUP BY tm.quadkey
         ORDER BY total_count DESC
         LIMIT 10",
    )?;

    let rows: Vec<(String, i64, f64, String)> = stmt
        .query_map([], |row| {
            Ok((row.get(0)?, row.get(1)?, row.get(2)?, row.get(3)?))
        })?
        .filter_map(Result::ok)
        .collect();

    for (quadkey, count, mean_conf, types) in &rows {
        println!(
            "{:<24} {:>10} {:>15.1} {:>10}",
            quadkey, count, mean_conf, types
        );
    }

    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_open_memory_database() {
        let conn = open_memory_database().unwrap();
        let count: i64 = conn
            .query_row(
                "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='tile_metrics'",
                [],
                |row| row.get(0),
            )
            .unwrap();
        assert_eq!(count, 1);
    }

    #[test]
    fn test_upsert_tile_single() {
        let conn = open_memory_database().unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-15", 10.0).unwrap();

        let record: (i64, f64) = conn
            .query_row(
                "SELECT count, mean_position_confidence FROM tile_metrics WHERE quadkey = '0123'",
                [],
                |row| Ok((row.get(0)?, row.get(1)?)),
            )
            .unwrap();
        assert_eq!(record.0, 1);
        assert!((record.1 - 10.0).abs() < f64::EPSILON);
    }

    #[test]
    fn test_upsert_tile_multiple_same_key() {
        let conn = open_memory_database().unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-15", 10.0).unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-15", 20.0).unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-15", 30.0).unwrap();

        let record: (i64, f64) = conn
            .query_row(
                "SELECT count, mean_position_confidence FROM tile_metrics WHERE quadkey = '0123'",
                [],
                |row| Ok((row.get(0)?, row.get(1)?)),
            )
            .unwrap();
        assert_eq!(record.0, 3);
        assert!((record.1 - 20.0).abs() < f64::EPSILON);
    }

    #[test]
    fn test_upsert_tile_different_days() {
        let conn = open_memory_database().unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-15", 10.0).unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-16", 20.0).unwrap();

        let count: i64 = conn
            .query_row("SELECT COUNT(*) FROM tile_metrics", [], |row| row.get(0))
            .unwrap();
        assert_eq!(count, 2);
    }

    #[test]
    fn test_upsert_tile_different_types() {
        let conn = open_memory_database().unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-15", 10.0).unwrap();
        upsert_tile(&conn, "0123", "denm", "2025-01-15", 20.0).unwrap();

        let count: i64 = conn
            .query_row("SELECT COUNT(*) FROM tile_metrics", [], |row| row.get(0))
            .unwrap();
        assert_eq!(count, 2);
    }

    #[test]
    fn test_list_days() {
        let conn = open_memory_database().unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-15", 10.0).unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-16", 20.0).unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-14", 5.0).unwrap();

        let days = list_days(&conn).unwrap();
        assert_eq!(days, vec!["2025-01-14", "2025-01-15", "2025-01-16"]);
    }

    #[test]
    fn test_list_message_types() {
        let conn = open_memory_database().unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-15", 10.0).unwrap();
        upsert_tile(&conn, "0123", "denm", "2025-01-15", 20.0).unwrap();
        upsert_tile(&conn, "0123", "cpm", "2025-01-15", 5.0).unwrap();

        let types = list_message_types(&conn).unwrap();
        assert_eq!(types, vec!["cam", "cpm", "denm"]);
    }

    #[test]
    fn test_query_tiles_by_day() {
        let conn = open_memory_database().unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-15", 10.0).unwrap();
        upsert_tile(&conn, "0456", "cam", "2025-01-15", 20.0).unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-16", 30.0).unwrap();

        let tiles = query_tiles(&conn, "2025-01-15", None).unwrap();
        assert_eq!(tiles.len(), 2);
    }

    #[test]
    fn test_query_tiles_by_day_and_type() {
        let conn = open_memory_database().unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-15", 10.0).unwrap();
        upsert_tile(&conn, "0123", "denm", "2025-01-15", 20.0).unwrap();

        let cam_tiles = query_tiles(&conn, "2025-01-15", Some("cam")).unwrap();
        assert_eq!(cam_tiles.len(), 1);
        assert_eq!(cam_tiles[0].count, 1);

        let denm_tiles = query_tiles(&conn, "2025-01-15", Some("denm")).unwrap();
        assert_eq!(denm_tiles.len(), 1);
    }

    #[test]
    fn test_query_tiles_unknown_type() {
        let conn = open_memory_database().unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-15", 10.0).unwrap();

        let tiles = query_tiles(&conn, "2025-01-15", Some("unknown")).unwrap();
        assert!(tiles.is_empty());
    }

    #[test]
    fn test_query_tiles_aggregation() {
        let conn = open_memory_database().unwrap();
        upsert_tile(&conn, "0123", "cam", "2025-01-15", 10.0).unwrap();
        upsert_tile(&conn, "0123", "denm", "2025-01-15", 20.0).unwrap();

        let tiles = query_tiles(&conn, "2025-01-15", None).unwrap();
        assert_eq!(tiles.len(), 1);
        assert_eq!(tiles[0].count, 2);
        assert!((tiles[0].mean_position_confidence - 15.0).abs() < f64::EPSILON);
    }
}
