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
mod file_parser;

use anyhow::Result;
use clap::{Arg, Command};
use libits::client::logger::create_stdout_logger;

fn main() -> Result<()> {
    let _logger = create_stdout_logger().expect("Logger initialization failed");

    let matches = Command::new("Display Log Reader")
        .version("0.1.0")
        .about("Parses ITS log files and ingests messages into a SQLite database")
        .arg(
            Arg::new("log_dir")
                .value_name("LOG_DIR")
                .required(true)
                .help("Directory containing .log, .gz or .tar.gz files to parse"),
        )
        .arg(
            Arg::new("zoom")
                .value_name("ZOOM")
                .required(true)
                .help("Quadkey zoom level, e.g. 18"),
        )
        .arg(
            Arg::new("database")
                .value_name("DATABASE")
                .required(true)
                .help("Output database path"),
        )
        .get_matches();

    let input_path = matches.get_one::<String>("log_dir").unwrap();
    let zoom: u8 = matches
        .get_one::<String>("zoom")
        .unwrap()
        .parse()
        .expect("Invalid zoom level: must be a number");
    let db_path = matches.get_one::<String>("database").unwrap();

    println!("📂 Input directory: {}", input_path);
    println!("📊 Zoom level: {}", zoom);
    println!("🗄️  Database: {}", db_path);
    println!();

    file_parser::parse_and_store(input_path, db_path, zoom, None)?;
    database::check_database(db_path)?;

    Ok(())
}
