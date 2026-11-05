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

use enum_dispatch::enum_dispatch;

#[enum_dispatch(Message)]
pub trait Content {
    fn get_type(&self) -> &str;

    fn appropriate(&mut self, timestamp: u64, station_id: u32);

    fn as_mobile(
        &self,
    ) -> Result<
        &dyn crate::mobility::mobile::Mobile,
        crate::exchange::message::content_error::ContentError,
    >;

    fn as_mortal(
        &self,
    ) -> Result<
        &dyn crate::exchange::mortal::Mortal,
        crate::exchange::message::content_error::ContentError,
    >;
}
