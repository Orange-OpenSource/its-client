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

use crate::client::configuration::Configuration;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::mortal::Mortal;
use crate::mobility::mobile::Mobile;
use enum_dispatch::enum_dispatch;

#[enum_dispatch(Message)]
pub trait Content {
    fn get_type(&self) -> &str;

    fn appropriate(&mut self, configuration: &Configuration, timestam: u64);

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError>;

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError>;
}
