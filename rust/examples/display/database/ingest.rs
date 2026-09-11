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

use anyhow::Result;
use rusqlite::{Connection, params};

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

#[cfg(test)]
mod tests {
    use super::*;
    use crate::database::open_memory_database;

    #[test]
    fn test_upsert_tile_with_type_id_single() {
        let conn = open_memory_database().unwrap();
        let type_id = get_or_create_message_type(&conn, "cam").unwrap();
        upsert_tile_with_type_id(&conn, "0123", type_id, "2025-01-15", 10.0).unwrap();

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
    fn test_upsert_tile_with_type_id_multiple_same_key() {
        let conn = open_memory_database().unwrap();
        let type_id = get_or_create_message_type(&conn, "cam").unwrap();
        upsert_tile_with_type_id(&conn, "0123", type_id, "2025-01-15", 10.0).unwrap();
        upsert_tile_with_type_id(&conn, "0123", type_id, "2025-01-15", 20.0).unwrap();
        upsert_tile_with_type_id(&conn, "0123", type_id, "2025-01-15", 30.0).unwrap();

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
    fn test_upsert_tile_with_type_id_different_days() {
        let conn = open_memory_database().unwrap();
        let type_id = get_or_create_message_type(&conn, "cam").unwrap();
        upsert_tile_with_type_id(&conn, "0123", type_id, "2025-01-15", 10.0).unwrap();
        upsert_tile_with_type_id(&conn, "0123", type_id, "2025-01-16", 20.0).unwrap();

        let count: i64 = conn
            .query_row("SELECT COUNT(*) FROM tile_metrics", [], |row| row.get(0))
            .unwrap();
        assert_eq!(count, 2);
    }

    #[test]
    fn test_upsert_tile_with_type_id_different_types() {
        let conn = open_memory_database().unwrap();
        let cam_type_id = get_or_create_message_type(&conn, "cam").unwrap();
        let denm_type_id = get_or_create_message_type(&conn, "denm").unwrap();
        upsert_tile_with_type_id(&conn, "0123", cam_type_id, "2025-01-15", 10.0).unwrap();
        upsert_tile_with_type_id(&conn, "0123", denm_type_id, "2025-01-15", 20.0).unwrap();

        let count: i64 = conn
            .query_row("SELECT COUNT(*) FROM tile_metrics", [], |row| row.get(0))
            .unwrap();
        assert_eq!(count, 2);
    }
}
