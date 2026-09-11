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

use crate::database::open_database;
use anyhow::Result;
use rusqlite::{Connection, params};

fn list_days(conn: &Connection) -> Result<Vec<String>> {
    let mut stmt = conn.prepare("SELECT DISTINCT day FROM tile_metrics ORDER BY day ASC")?;
    let days: Vec<String> = stmt
        .query_map([], |row| row.get(0))?
        .filter_map(Result::ok)
        .collect();
    Ok(days)
}

fn list_message_types(conn: &Connection) -> Result<Vec<String>> {
    let mut stmt = conn.prepare("SELECT name FROM message_types ORDER BY name ASC")?;
    let types: Vec<String> = stmt
        .query_map([], |row| row.get(0))?
        .filter_map(Result::ok)
        .collect();
    Ok(types)
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

    println!("📅 Days with data: {}", list_days(&conn)?.len());
    for day in &list_days(&conn)? {
        let day_count: i64 = conn.query_row(
            "SELECT COALESCE(SUM(count), 0) FROM tile_metrics WHERE day = ?1",
            params![day],
            |row| row.get(0),
        )?;
        println!("   {} : {} messages", day, day_count);
    }
    println!();

    println!("📨 Message types:");
    for msg_type in &list_message_types(&conn)? {
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
