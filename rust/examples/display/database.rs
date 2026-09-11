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
use rusqlite::Connection;

/// Opens (or creates) the SQLite database and ensures the schema exists.
pub fn open_database(db_path: &str) -> Result<Connection> {
    let conn = Connection::open(db_path)?;
    conn.execute_batch(
        "PRAGMA journal_mode=WAL; PRAGMA synchronous=NORMAL; PRAGMA cache_size=-64000;",
    )?;
    create_schema(&conn)?;
    Ok(conn)
}

/// Opens an in-memory SQLite database for tests.
#[cfg(test)]
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
}
