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

#[path = "../database.rs"]
mod database;
mod server;
#[path = "../database/stats.rs"]
mod stats;

use anyhow::{Result, ensure};
use clap::{Arg, Command};
use ini::Ini;
use libits::client::logger::create_stdout_logger;
use std::path::Path;

const DISPLAY_SECTION: &str = "display";

//noinspection HttpUrlsUsage
#[tokio::main]
async fn main() -> Result<()> {
    let _logger = create_stdout_logger().expect("Logger initialization failed");

    let matches = Command::new("Display Server")
        .version("0.1.0")
        .about("Serves the display web UI from a SQLite database")
        .arg(
            Arg::new("config-file-path")
                .short('c')
                .long("config")
                .value_name("CONFIG_FILE_PATH")
                .default_value("examples/config.ini")
                .help("Path to the configuration file"),
        )
        .get_matches();

    let config_path = matches.get_one::<String>("config-file-path").unwrap();
    let ini = Ini::load_from_file(Path::new(config_path))
        .unwrap_or_else(|error| panic!("Failed to load config file {}: {}", config_path, error));

    // The server only needs the display-specific settings, so read the [display]
    // section directly instead of requiring the full library configuration (which
    // would mandate unrelated [mqtt]/[mobility] sections).
    let display_section = ini
        .section(Some(DISPLAY_SECTION))
        .expect("Missing [display] section in config file");

    let db_path = display_section
        .get("db_path")
        .expect("Missing 'db_path' in [display] section")
        .to_string();

    let port: u16 = display_section
        .get("port")
        .and_then(|value| value.parse().ok())
        .unwrap_or(3000);

    ensure!(
        Path::new(&db_path).try_exists()?,
        "Database not found: {}. Run 'display_log_reader' or 'display_mqtt_reader' first.",
        db_path
    );

    // Print database stats on startup
    stats::check_database(&db_path)?;

    let app = server::create_router(&db_path);
    let bind_address = format!("0.0.0.0:{}", port);
    let listener = tokio::net::TcpListener::bind(&bind_address).await?;
    println!(
        "🚀 Server running on http://{} (DB: {})",
        bind_address, db_path
    );
    println!("📍 Open your browser and navigate to the URL above");
    axum::serve(listener, app).await?;

    Ok(())
}
