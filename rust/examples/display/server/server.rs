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

use crate::database::{self, TileRecord};
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

    let days = database::list_days(&db_guard).map_err(|error| {
        eprintln!("Failed to list days: {}", error);
        StatusCode::INTERNAL_SERVER_ERROR
    })?;

    let message_types = database::list_message_types(&db_guard).map_err(|error| {
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
            let days = database::list_days(&db_guard).map_err(|error| {
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

    let mut tiles =
        database::query_tiles(&db_guard, &day, message_type_filter).map_err(|error| {
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

    #[test]
    fn test_create_router() {
        // Should not panic with in-memory db
        let conn = database::open_memory_database().unwrap();
        let _state = AppState {
            db: Arc::new(Mutex::new(conn)),
        };
    }
}
