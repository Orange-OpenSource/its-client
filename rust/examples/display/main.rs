/*
 * Software Name : libits-client
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 */

mod database;
mod parser;
mod quadtree;
mod server;

use anyhow::Result;
use std::env;
use std::path::Path;

fn get_db_path_from_dir(dir_path: &str) -> String {
    let path = Path::new(dir_path);

    // Get the last directory name
    let dir_name = path.file_name().and_then(|n| n.to_str()).unwrap_or("cam");

    // Create <dir_path>/<dir_name>.db
    path.join(format!("{}.db", dir_name))
        .to_string_lossy()
        .to_string()
}

#[tokio::main]
async fn main() -> Result<()> {
    tracing_subscriber::fmt::init();

    let args: Vec<String> = env::args().collect();

    if args.len() < 2 {
        eprintln!("Usage:");
        eprintln!("  cargo run --example display parse <log_dir> [zoom]  - Parse CAM logs");
        eprintln!("  cargo run --example display check <log_dir>         - Check database stats");
        eprintln!("  cargo run --example display serve <log_dir>         - Start web server");
        return Ok(());
    }

    match args[1].as_str() {
        "parse" => {
            let input_path = args.get(2).expect("Missing log directory path");
            let zoom = args.get(3).and_then(|s| s.parse().ok()).unwrap_or(18);

            let db_path = get_db_path_from_dir(input_path);

            parser::parse_and_store(input_path, &db_path, zoom)?;
        }
        "check" => {
            let input_path = args.get(2).expect("Missing log directory path");
            let db_path = get_db_path_from_dir(input_path);

            if !Path::new(&db_path).exists() {
                eprintln!("❌ Database not found: {}", db_path);
                eprintln!("   Run 'parse' command first.");
                return Ok(());
            }

            database::check_database(&db_path)?;
        }
        "serve" => {
            let input_path = args.get(2).expect("Missing log directory path");
            let db_path = get_db_path_from_dir(input_path);

            if !Path::new(&db_path).exists() {
                eprintln!("❌ Database not found: {}", db_path);
                eprintln!("   Run 'parse' command first.");
                return Ok(());
            }

            let app = server::create_router(&db_path);
            let listener = tokio::net::TcpListener::bind("127.0.0.1:3000").await?;
            println!(
                "🚀 Server running on http://127.0.0.1:3000 (DB: {})",
                db_path
            );
            println!("📍 Open your browser and navigate to the URL above");
            axum::serve(listener, app).await?;
        }
        _ => {
            eprintln!("❌ Unknown command: {}", args[1]);
            eprintln!("Available commands: parse, check, serve");
        }
    }

    Ok(())
}
