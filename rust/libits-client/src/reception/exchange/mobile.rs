// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).
extern crate integer_sqrt;

use crate::reception::exchange::ReferencePosition;

use self::integer_sqrt::IntegerSquareRoot;

pub trait Mobile {
    fn mobile_id(&self) -> u32;

    fn position(&self) -> &ReferencePosition;

    fn speed(&self) -> Option<u16>;

    fn heading(&self) -> Option<u16>;

    fn stopped(&self) -> bool {
        if let Some(speed) = self.speed() {
            return speed <= 36;
        }
        false
    }

    fn heading_in_degrees(&self) -> Option<f64> {
        if let Some(heading) = self.heading() {
            return Some(heading_in_degrees(heading));
        }
        None
    }

    fn speed_in_meter_per_second(&self) -> Option<f64> {
        if let Some(speed) = self.speed() {
            return Some(speed_in_meter_per_second(speed));
        }
        None
    }

    fn speed_in_kilometer_per_hour(&self) -> Option<f64> {
        if let Some(speed) = self.speed() {
            return Some(speed_in_kilometer_per_hour(speed));
        }
        None
    }
}

pub(crate) fn heading_in_degrees(heading: u16) -> f64 {
    heading as f64 / 10.0
}

pub(crate) fn speed_in_meter_per_second(speed: u16) -> f64 {
    speed as f64 / 100.0
}

pub(crate) fn speed_in_kilometer_per_hour(speed: u16) -> f64 {
    speed_in_meter_per_second(speed) * 3.6
}

pub fn speed_from_yaw_angle(x_speed: i16, y_speed: i16) -> u16 {
    ((x_speed.unsigned_abs() as u32).pow(2) + (y_speed.unsigned_abs() as u32).pow(2)).integer_sqrt()
        as u16
}

#[cfg(test)]
mod tests {
    use crate::reception::exchange::mobile::{speed_from_yaw_angle, Mobile};
    use crate::reception::exchange::reference_position::ReferencePosition;

    #[test]
    fn it_can_compute_speed() {
        assert_eq!(speed_from_yaw_angle(1400, 500), 1486);
        assert_eq!(speed_from_yaw_angle(-1400, 500), 1486);
        assert_eq!(speed_from_yaw_angle(1400, -500), 1486);
        assert_eq!(speed_from_yaw_angle(-1400, -500), 1486);
    }

    struct StoppedMobileStub {}

    impl Mobile for StoppedMobileStub {
        fn mobile_id(&self) -> u32 {
            todo!()
        }

        fn position(&self) -> &ReferencePosition {
            todo!()
        }

        fn speed(&self) -> Option<u16> {
            Some(35)
        }

        fn heading(&self) -> Option<u16> {
            Some(1800) // south
        }
    }

    struct MovingMobileStub {}

    impl Mobile for MovingMobileStub {
        fn mobile_id(&self) -> u32 {
            todo!()
        }

        fn position(&self) -> &ReferencePosition {
            todo!()
        }

        fn speed(&self) -> Option<u16> {
            Some(37)
        }

        fn heading(&self) -> Option<u16> {
            Some(900) // east
        }
    }

    #[test]
    fn it_can_check_if_stopped() {
        assert!(StoppedMobileStub {}.stopped());
    }

    #[test]
    fn it_can_check_if_moving() {
        assert!(!MovingMobileStub {}.stopped());
    }

    #[test]
    fn it_can_provide_heading_in_degrees() {
        // south
        assert_eq!(StoppedMobileStub {}.heading_in_degrees(), Some(180.0));
        // north
        assert_eq!(MovingMobileStub {}.heading_in_degrees(), Some(90.0));
    }

    #[test]
    fn it_can_provide_speed_in_meter_per_second() {
        // south
        assert_eq!(StoppedMobileStub {}.speed_in_meter_per_second(), Some(0.35));
        // north
        assert_eq!(MovingMobileStub {}.speed_in_meter_per_second(), Some(0.37));
    }

    #[test]
    fn it_can_provide_speed_in_kilometer_per_hour() {
        // south
        assert_eq!(
            StoppedMobileStub {}.speed_in_kilometer_per_hour(),
            Some(1.26)
        );
        // north
        assert_eq!(
            MovingMobileStub {}.speed_in_kilometer_per_hour(),
            Some(1.332)
        );
    }
}
