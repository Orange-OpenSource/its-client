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

use crate::now;

pub trait Mortal {
    /// Returns the milliseconds timestamp at which this mortal item
    /// will be considered expired
    fn timeout(&self) -> u64;

    fn terminate(&mut self);

    fn terminated(&self) -> bool;

    fn expired(&self) -> bool {
        now() > self.timeout()
    }

    fn remaining_time(&self) -> u64 {
        if self.timeout() > now() {
            (self.timeout() - now()) / 1000
        } else {
            0
        }
    }
}
