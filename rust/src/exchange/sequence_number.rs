/*
 * Software Name : libits-client
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 * Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) library based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
 */

use std::fmt::Formatter;
use std::sync::Mutex;

#[derive(Default)]
pub struct SequenceNumber {
    current: Mutex<u128>,
    max: u128,
}

impl SequenceNumber {
    #[allow(dead_code)]
    pub fn new(max: u128) -> Self {
        Self {
            current: Mutex::new(0),
            max,
        }
    }

    #[allow(dead_code)]
    pub fn get_next(&mut self) -> u128 {
        let mut write_lock = self.current.lock().unwrap();
        *write_lock = (*write_lock + 1) % self.max;
        *write_lock
    }
}

impl std::fmt::Display for SequenceNumber {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", *self.current.lock().unwrap())
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::sequence_number::SequenceNumber;

    #[test]
    fn next_sequence_number() {
        let mut sequence_number = SequenceNumber::new(u16::MAX.into());

        let next = sequence_number.get_next();

        assert_eq!(next, 1);
    }

    #[test]
    fn next_sequence_number_modulo() {
        let mut sequence_number = SequenceNumber::new(u16::MAX.into());
        for _i in 0..65534 {
            sequence_number.get_next();
        }

        let next = sequence_number.get_next();

        assert_eq!(next, 0);
    }
}
