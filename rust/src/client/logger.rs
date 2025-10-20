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

use flexi_logger::Logger;
use log::info;

/// Creates a logger that outputs to stdout.
///
/// This function initialises a logger using the `flexi_logger` crate, which logs messages to the standard output (stdout).
/// Logger's log level is set based on the environment variable or defaults to "info".
///
/// # Returns
///
/// A `Result` containing a `flexi_logger::LoggerHandle` if the logger is successfully initialized, or a boxed `dyn std::error::Error` if an error occurs.
///
/// # Errors
///
/// This function will return an error if the logger fails to initialize.
///
/// # Examples
///
/// ```rust
/// use libits::client::logger::create_stdout_logger;
/// let _logger = create_stdout_logger().expect("Logger initialization failed");
/// ```
///
/// # Dependencies
///
/// This function requires the `flexi_logger` and `log` crates.
///
/// # Notes
///
/// Logger is configured to print messages to stdout and includes a message indicating that the logger is ready.
///
/// # See Also
///
/// - `flexi_logger::Logger`
/// - `log::info`
pub fn create_stdout_logger() -> Result<flexi_logger::LoggerHandle, Box<dyn std::error::Error>> {
    let logger = Logger::try_with_env_or_str("info")?
        .log_to_stdout()
        .print_message()
        .start()?;
    info!("Logger ready on stdout");
    Ok(logger)
}
