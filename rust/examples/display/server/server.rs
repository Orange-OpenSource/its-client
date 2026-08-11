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
use axum::{
    Router,
    extract::{Query, State},
    http::StatusCode,
    response::{Html, IntoResponse, Json},
    routing::get,
};
use libits::mobility::quadtree::quadkey_in_bbox;
use rusqlite::Connection;
use serde::{Deserialize, Serialize};
use std::sync::{Arc, Mutex};
use tower_http::cors::CorsLayer;

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "snake_case")]
struct TileRecord {
    quadkey: String,
    message_type: String,
    day: String,
    count: i64,
    mean_position_confidence: f64,
}

#[derive(Clone)]
pub struct AppState {
    pub db: Arc<Mutex<Connection>>,
}

#[derive(Deserialize)]
struct TilesQuery {
    day: Option<String>,
    message_type: Option<String>,
    min_lon: Option<f64>,
    min_lat: Option<f64>,
    max_lon: Option<f64>,
    max_lat: Option<f64>,
}

#[derive(Serialize)]
struct MetadataResponse {
    days: Vec<String>,
    message_types: Vec<String>,
}

pub fn create_router(db_path: &str) -> Router {
    let conn = database::open_database(db_path).expect("Failed to open database");
    let state = AppState {
        db: Arc::new(Mutex::new(conn)),
    };

    Router::new()
        .route("/", get(index_handler))
        .route("/app.js", get(app_js_handler))
        .route("/api/tiles", get(tiles_handler))
        .route("/api/metadata", get(metadata_handler))
        .layer(CorsLayer::permissive())
        .with_state(state)
}

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

fn query_tiles(
    conn: &Connection,
    day: &str,
    message_type: Option<&str>,
) -> Result<Vec<TileRecord>> {
    let tiles = if let Some(msg_type) = message_type {
        let type_id = conn.query_row(
            "SELECT id FROM message_types WHERE name = ?1",
            rusqlite::params![msg_type],
            |row| row.get::<_, i64>(0),
        );
        match type_id {
            Ok(tid) => {
                let mut stmt = conn.prepare(
                    "SELECT quadkey, day, count, mean_position_confidence
                     FROM tile_metrics
                     WHERE day = ?1 AND message_type_id = ?2",
                )?;
                stmt.query_map(rusqlite::params![day, tid], |row| {
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
        stmt.query_map(rusqlite::params![day], |row| {
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

async fn index_handler() -> impl IntoResponse {
    Html(include_str!("templates/index.html"))
}

async fn app_js_handler() -> impl IntoResponse {
    let js_content = include_str!("templates/app.js");
    ([("content-type", "application/javascript")], js_content)
}

async fn metadata_handler(
    State(state): State<AppState>,
) -> Result<Json<MetadataResponse>, StatusCode> {
    let db_guard = state
        .db
        .lock()
        .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;

    let days = list_days(&db_guard).map_err(|error| {
        eprintln!("Failed to list days: {}", error);
        StatusCode::INTERNAL_SERVER_ERROR
    })?;

    let message_types = list_message_types(&db_guard).map_err(|error| {
        eprintln!("Failed to list message types: {}", error);
        StatusCode::INTERNAL_SERVER_ERROR
    })?;

    Ok(Json(MetadataResponse {
        days,
        message_types,
    }))
}

async fn tiles_handler(
    State(state): State<AppState>,
    Query(query): Query<TilesQuery>,
) -> Result<Json<Vec<TileRecord>>, StatusCode> {
    let db_guard = state
        .db
        .lock()
        .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;

    // Use the first available day if none specified
    let day = match &query.day {
        Some(day_value) => day_value.clone(),
        None => {
            let days = list_days(&db_guard).map_err(|error| {
                eprintln!("Failed to list days: {}", error);
                StatusCode::INTERNAL_SERVER_ERROR
            })?;
            match days.last() {
                Some(last_day) => last_day.clone(),
                None => return Ok(Json(Vec::new())),
            }
        }
    };

    let message_type_filter = query.message_type.as_deref();

    let mut tiles = query_tiles(&db_guard, &day, message_type_filter).map_err(|error| {
        eprintln!("Query failed: {}", error);
        StatusCode::INTERNAL_SERVER_ERROR
    })?;

    // Apply bounding box filter if provided
    if let (Some(min_lon), Some(min_lat), Some(max_lon), Some(max_lat)) =
        (query.min_lon, query.min_lat, query.max_lon, query.max_lat)
    {
        tiles.retain(|tile| quadkey_in_bbox(&tile.quadkey, min_lon, min_lat, max_lon, max_lat));
    }

    Ok(Json(tiles))
}

#[cfg(test)]
mod tests {
    use super::*;
    use rusqlite::params;

    fn insert_tile_metric(
        conn: &Connection,
        quadkey: &str,
        message_type: &str,
        day: &str,
        count: i64,
        sum_position_confidence: f64,
        mean_position_confidence: f64,
    ) {
        conn.execute(
            "INSERT INTO message_types (name) VALUES (?1)",
            params![message_type],
        )
        .unwrap();

        let type_id: i64 = conn
            .query_row(
                "SELECT id FROM message_types WHERE name = ?1",
                params![message_type],
                |row| row.get(0),
            )
            .unwrap();

        conn.execute(
            "INSERT INTO tile_metrics
                 (quadkey, message_type_id, day, count, sum_position_confidence, mean_position_confidence)
             VALUES (?1, ?2, ?3, ?4, ?5, ?6)",
            params![
                quadkey,
                type_id,
                day,
                count,
                sum_position_confidence,
                mean_position_confidence
            ],
        )
        .unwrap();
    }

    #[test]
    fn test_create_router() {
        // Should not panic with in-memory db
        let conn = database::open_memory_database().unwrap();
        let _state = AppState {
            db: Arc::new(Mutex::new(conn)),
        };
    }

    #[test]
    fn test_query_tiles_by_day() {
        let conn = database::open_memory_database().unwrap();
        insert_tile_metric(&conn, "0123", "cam", "2025-01-15", 1, 10.0, 10.0);
        insert_tile_metric(&conn, "0456", "cam-2", "2025-01-15", 1, 20.0, 20.0);
        insert_tile_metric(&conn, "0123", "cam-3", "2025-01-16", 1, 30.0, 30.0);

        let tiles = query_tiles(&conn, "2025-01-15", None).unwrap();
        assert_eq!(tiles.len(), 2);
    }

    #[test]
    fn test_query_tiles_by_day_and_type() {
        let conn = database::open_memory_database().unwrap();
        insert_tile_metric(&conn, "0123", "cam", "2025-01-15", 1, 10.0, 10.0);
        insert_tile_metric(&conn, "0123", "denm", "2025-01-15", 1, 20.0, 20.0);

        let cam_tiles = query_tiles(&conn, "2025-01-15", Some("cam")).unwrap();
        assert_eq!(cam_tiles.len(), 1);
        assert_eq!(cam_tiles[0].count, 1);

        let denm_tiles = query_tiles(&conn, "2025-01-15", Some("denm")).unwrap();
        assert_eq!(denm_tiles.len(), 1);
    }

    #[test]
    fn test_query_tiles_unknown_type() {
        let conn = database::open_memory_database().unwrap();
        insert_tile_metric(&conn, "0123", "cam", "2025-01-15", 1, 10.0, 10.0);

        let tiles = query_tiles(&conn, "2025-01-15", Some("unknown")).unwrap();
        assert!(tiles.is_empty());
    }

    #[test]
    fn test_query_tiles_aggregation() {
        let conn = database::open_memory_database().unwrap();
        insert_tile_metric(&conn, "0123", "cam", "2025-01-15", 1, 10.0, 10.0);
        insert_tile_metric(&conn, "0123", "denm", "2025-01-15", 1, 20.0, 20.0);

        let tiles = query_tiles(&conn, "2025-01-15", None).unwrap();
        assert_eq!(tiles.len(), 1);
        assert_eq!(tiles[0].count, 2);
        assert!((tiles[0].mean_position_confidence - 15.0).abs() < f64::EPSILON);
    }
}
