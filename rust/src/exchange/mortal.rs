// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2023 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas BUFFON <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

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
