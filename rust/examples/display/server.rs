use axum::{
    Router,
    extract::State,
    http::StatusCode,
    response::{Html, IntoResponse, Json},
    routing::get,
};
use rusqlite::Connection;
use serde::Serialize;
use std::sync::{Arc, Mutex};
use tower_http::cors::CorsLayer;

#[derive(Clone)]
pub struct AppState {
    pub db: Arc<Mutex<Connection>>,
}

#[derive(Serialize)]
struct Tile {
    quadkey: String,
    mean_confidence: Option<f64>,
    count: i64,
}

pub fn create_router(db_path: &str) -> Router {
    let conn = Connection::open(db_path).expect("Failed to open database");
    let state = AppState {
        db: Arc::new(Mutex::new(conn)),
    };

    Router::new()
        .route("/", get(index_handler))
        .route("/app.js", get(app_js_handler))
        .route("/api/tiles", get(tiles_handler))
        .layer(CorsLayer::permissive())
        .with_state(state)
}

async fn index_handler() -> impl IntoResponse {
    Html(include_str!("templates/index.html"))
}

async fn app_js_handler() -> impl IntoResponse {
    let js = include_str!("templates/app.js");
    ([("content-type", "application/javascript")], js)
}

async fn tiles_handler(State(state): State<AppState>) -> Result<Json<Vec<Tile>>, StatusCode> {
    let db = state
        .db
        .lock()
        .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;

    let mut stmt = db
        .prepare("SELECT quadkey, mean_confidence, count FROM quadtiles")
        .map_err(|e| {
            eprintln!("Failed to prepare statement: {}", e);
            StatusCode::INTERNAL_SERVER_ERROR
        })?;

    let tiles: Vec<Tile> = stmt
        .query_map([], |row| {
            Ok(Tile {
                quadkey: row.get(0)?,
                mean_confidence: row.get(1)?,
                count: row.get(2)?,
            })
        })
        .map_err(|e| {
            eprintln!("Query failed: {}", e);
            StatusCode::INTERNAL_SERVER_ERROR
        })?
        .filter_map(Result::ok)
        .collect();

    Ok(Json(tiles))
}
