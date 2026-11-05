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
use log::warn;
use std::sync::OnceLock;

mod acceleration;
pub(crate) mod angle;
mod cause_code;
pub mod collective_perception_message;
mod confidence;
pub mod cooperative_awareness_message;
pub mod cooperative_awareness_message_113;
pub mod coordinate;
mod curvature;
pub mod decentralized_environmental_notification_message;
pub(crate) mod heading;
mod longitudinal_lane_position;
pub mod map_extended_message;
mod object_dimension;
pub mod perceived_object;
pub mod reference_position;
pub mod reference_position113;
mod shape;
pub mod signal_phase_and_timing_extended_message;
pub(crate) mod speed;
mod steering_wheel_angle;
mod vehicle_length;
pub(crate) mod velocity;
mod yaw_rate;

/// Unix milliseconds of 2004-01-01T00:00:00Z, the `TimestampIts` epoch
const ETSI_EPOCH_UNIX_MS: u64 = 1_072_915_200_000;
/// TAI-UTC at the `TimestampIts` epoch: leap seconds before 2004 are not counted
const TAI_UTC_AT_ETSI_EPOCH_S: u64 = 32;
const NTP_EPOCH_TO_UNIX_EPOCH_S: u64 = 2_208_988_800;
/// IERS leap second list maintained by the system tzdata package
const LEAP_SECONDS_LIST_PATH: &str = "/usr/share/zoneinfo/leap-seconds.list";
/// Leap seconds known at build time, used when the system list is unavailable:
/// 2006-01-01, 2009-01-01, 2012-07-01, 2015-07-01 and 2017-01-01 (Unix ms, TAI-UTC s)
const BUILT_IN_LEAP_SECONDS: [(u64, u64); 5] = [
    (1_136_073_600_000, 33),
    (1_230_768_000_000, 34),
    (1_341_100_800_000, 35),
    (1_435_708_800_000, 36),
    (1_483_228_800_000, 37),
];

/// TAI-UTC history used to convert Unix (UTC) dates into `TimestampIts` (TAI) dates
#[derive(Debug, PartialEq)]
struct LeapSeconds {
    /// Ascending (Unix ms, TAI-UTC s) changes
    changes: Vec<(u64, u64)>,
    /// Unix ms after which the list may miss an announced leap second
    expires_ms: Option<u64>,
}

impl LeapSeconds {
    /// Parses the IERS `leap-seconds.list` format: `NTP_seconds TAI-UTC` lines and an
    /// `#@ NTP_seconds` expiry line; returns `None` without any change
    fn parse(text: &str) -> Option<Self> {
        let ntp_to_unix_ms = |field: &str| {
            field
                .parse::<u64>()
                .ok()?
                .checked_sub(NTP_EPOCH_TO_UNIX_EPOCH_S)
                .map(|seconds| seconds * 1000)
        };
        let mut changes = Vec::new();
        let mut expires_ms = None;
        for line in text.lines() {
            if let Some(expiry) = line.strip_prefix("#@") {
                expires_ms = ntp_to_unix_ms(expiry.trim());
                continue;
            }
            let mut fields = line.split('#').next()?.split_whitespace();
            if let (Some(date), Some(offset)) = (fields.next(), fields.next()) {
                changes.push((ntp_to_unix_ms(date)?, offset.parse().ok()?));
            }
        }
        changes.sort_unstable();
        (!changes.is_empty()).then_some(Self {
            changes,
            expires_ms,
        })
    }

    fn built_in() -> Self {
        Self {
            changes: BUILT_IN_LEAP_SECONDS.to_vec(),
            expires_ms: None,
        }
    }

    /// Loads the system list, falling back to the built-in table with a warning
    fn load(path: &str, now_ms: u64) -> Self {
        match std::fs::read_to_string(path)
            .ok()
            .and_then(|text| Self::parse(&text))
        {
            Some(list) => {
                if list.expires_ms.is_some_and(|expiry| expiry < now_ms) {
                    warn!("{path} expired: update tzdata, a leap second may be missing");
                }
                list
            }
            None => {
                warn!("{path} unavailable: using built-in leap seconds up to 2017");
                Self::built_in()
            }
        }
    }

    /// Leap seconds inserted between the `TimestampIts` epoch and `unix_ms`, in ms
    fn offset_ms(&self, unix_ms: u64) -> u64 {
        self.changes
            .iter()
            .take_while(|(date, _)| *date <= unix_ms)
            .last()
            .map_or(0, |(_, tai_utc)| {
                tai_utc.saturating_sub(TAI_UTC_AT_ETSI_EPOCH_S) * 1000
            })
    }

    fn etsi_from_unix(&self, unix_ms: u64) -> u64 {
        unix_ms.saturating_sub(ETSI_EPOCH_UNIX_MS) + self.offset_ms(unix_ms)
    }

    fn unix_from_etsi(&self, etsi_ms: u64) -> u64 {
        let approximate = etsi_ms + ETSI_EPOCH_UNIX_MS;
        approximate - self.offset_ms(approximate - self.offset_ms(approximate))
    }
}

fn leap_seconds() -> &'static LeapSeconds {
    static LEAP_SECONDS: OnceLock<LeapSeconds> = OnceLock::new();
    LEAP_SECONDS.get_or_init(|| LeapSeconds::load(LEAP_SECONDS_LIST_PATH, now()))
}

/// Converts heading from decidegrees to radians
pub(crate) fn heading_from_etsi(decidegrees: u16) -> f64 {
    (f64::from(decidegrees) / 10.).to_radians()
}

/// Converts heading from radians to decidegrees
pub(crate) fn heading_to_etsi(radians: f64) -> u16 {
    ((radians.to_degrees() * 10_f64) % 3600.) as u16
}

/// Converts speed from cm/s to m/s
pub(crate) fn speed_from_etsi(cm_per_sec: u16) -> f64 {
    f64::from(cm_per_sec) / 100.
}

/// Converts speed from m/s to cm/s
pub(crate) fn speed_to_etsi(meters_per_sec: f64) -> u16 {
    (meters_per_sec * 100.) as u16
}

/// Converts acceleration from dm/s² to m/s²
pub(crate) fn acceleration_from_etsi(dm_per_sec_2: i16) -> f64 {
    f64::from(dm_per_sec_2) / 10.
}

/// Converts acceleration from m/s² to dm/s²
///
/// FIXME use this function and remove this clause once mobility message creation is implemented
///       (cf. [Github issue][1])
///
/// [1]: https://github.com/orgs/Orange-OpenSource/projects/3/views/8?pane=issue&itemId=69693871&issue=Orange-OpenSource%7Cits-client%7C131
#[allow(unused)]
pub(crate) fn acceleration_to_etsi(m_per_s_2: f64) -> i16 {
    (m_per_s_2 * 10.) as i16
}

/// Converts an ETSI `TimestampIts` (TAI ms since 2004) into Unix (UTC) milliseconds
pub fn timestamp_from_etsi(etsi_timestamp: u64) -> u64 {
    leap_seconds().unix_from_etsi(etsi_timestamp)
}

/// Converts Unix (UTC) milliseconds into an ETSI `TimestampIts`, leap seconds included
pub fn timestamp_to_etsi(unix_timestamp: u64) -> u64 {
    leap_seconds().etsi_from_unix(unix_timestamp)
}

/// Current ETSI `TimestampIts` from the OS clock
pub fn etsi_now() -> u64 {
    timestamp_to_etsi(now())
}

/// `generationDeltaTime` is `TimestampIts` modulo 65536
pub(crate) fn timestamp_to_generation_delta_time(unix_timestamp: u64) -> u16 {
    (timestamp_to_etsi(unix_timestamp) % 65536) as u16
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::{
        ETSI_EPOCH_UNIX_MS, LEAP_SECONDS_LIST_PATH, LeapSeconds, acceleration_from_etsi,
        acceleration_to_etsi, etsi_now, heading_from_etsi, heading_to_etsi, speed_from_etsi,
        speed_to_etsi, timestamp_from_etsi, timestamp_to_etsi, timestamp_to_generation_delta_time,
    };
    use crate::now;
    use std::f64::consts::PI;

    macro_rules! test_from_etsi {
        ($func:ident, $test_name:ident, $value:expr, $expected:expr) => {
            #[test]
            fn $test_name() {
                let epsilon = 1e-11;

                let as_si = $func($value);
                let delta = (as_si - $expected).abs();

                assert!(
                    delta < epsilon,
                    "Actual: {} (expected: {})",
                    as_si,
                    $expected
                );
            }
        };
    }
    test_from_etsi!(heading_from_etsi, north_0_deg_heading_from_etsi, 0, 0_f64);
    test_from_etsi!(
        heading_from_etsi,
        north_3600_deg_heading_from_etsi,
        3600,
        2. * PI
    );
    test_from_etsi!(heading_from_etsi, east_heading_from_etsi, 900, PI / 2.);
    test_from_etsi!(
        heading_from_etsi,
        west_heading_from_etsi,
        2700,
        3. * PI / 2.
    );
    test_from_etsi!(heading_from_etsi, south_heading_from_etsi, 1800, PI);
    test_from_etsi!(speed_from_etsi, nul_speed_from_etsi, 0, 0.);
    test_from_etsi!(speed_from_etsi, non_nul_speed_from_etsi, 2753, 27.53);
    test_from_etsi!(speed_from_etsi, max_speed_from_etsi, u16::MAX, 655.35);
    test_from_etsi!(acceleration_from_etsi, nul_acceleration_from_etsi, 0, 0.);
    test_from_etsi!(
        acceleration_from_etsi,
        negative_acceleration_from_etsi,
        -100,
        -10.
    );
    test_from_etsi!(
        acceleration_from_etsi,
        positive_acceleration_from_etsi,
        123,
        12.3_f64
    );
    test_from_etsi!(
        acceleration_from_etsi,
        min_acceleration_from_etsi,
        -160,
        -16_f64
    );
    test_from_etsi!(
        acceleration_from_etsi,
        max_acceleration_from_etsi,
        161,
        16.1_f64
    );

    macro_rules! test_to_etsi {
        ($func:ident, $test_name:ident, $value:expr, $expected:expr) => {
            #[test]
            fn $test_name() {
                let as_etsi = $func($value);

                assert_eq!(as_etsi, $expected);
            }
        };
    }
    test_to_etsi!(heading_to_etsi, north_0_deg_heading_to_etsi, 0_f64, 0);
    test_to_etsi!(heading_to_etsi, north_3600_deg_heading_to_etsi, 2. * PI, 0);
    test_to_etsi!(heading_to_etsi, east_heading_to_etsi, PI / 2., 900);
    test_to_etsi!(heading_to_etsi, west_heading_to_etsi, 3. * PI / 2., 2700);
    test_to_etsi!(heading_to_etsi, south_heading_to_etsi, PI, 1800);
    test_to_etsi!(speed_to_etsi, nul_speed_to_etsi, 0., 0);
    test_to_etsi!(speed_to_etsi, non_nul_speed_to_etsi, 27.53, 2753);
    test_to_etsi!(speed_to_etsi, extra_decimal_speed_to_etsi, 34.123456, 3412);
    test_to_etsi!(speed_to_etsi, max_speed_to_etsi, 655.35, u16::MAX);
    test_to_etsi!(acceleration_to_etsi, nul_acceleration_to_etsi, 0., 0);
    test_to_etsi!(
        acceleration_to_etsi,
        negative_acceleration_to_etsi,
        -10_f64,
        -100
    );
    test_to_etsi!(
        acceleration_to_etsi,
        positive_acceleration_to_etsi,
        12.3_f64,
        123
    );
    test_to_etsi!(
        acceleration_to_etsi,
        min_acceleration_to_etsi,
        -16_f64,
        -160
    );
    test_to_etsi!(
        acceleration_to_etsi,
        max_acceleration_to_etsi,
        16.1_f64,
        161
    );

    const LEAP_SECONDS_LIST: &str = "\
#\tleap-seconds.list excerpt
#$\t 3913697179
#@\t 4023129600
#
2272060800\t10\t# 1 Jan 1972
3439756800\t34\t# 1 Jan 2009
3550089600\t35\t# 1 Jul 2012
3644697600\t36\t# 1 Jul 2015
3692217600\t37\t# 1 Jan 2017
";
    const JAN_2008_MS: u64 = 1_199_145_600_000;
    const JAN_2017_MS: u64 = 1_483_228_800_000;
    const NOV_2026_MS: u64 = 1_793_722_954_430;

    #[test]
    fn leap_seconds_list_is_parsed_with_its_expiry() {
        let list = LeapSeconds::parse(LEAP_SECONDS_LIST).unwrap();

        assert_eq!(list.changes.len(), 5);
        assert_eq!(list.changes[0], (63_072_000_000, 10));
        assert_eq!(list.changes[4], (JAN_2017_MS, 37));
        // 28 June 2027
        assert_eq!(list.expires_ms, Some(1_814_140_800_000));
        assert_eq!(LeapSeconds::parse("# comments only\n"), None);
        assert_eq!(LeapSeconds::parse("not a list\n"), None);
    }

    #[test]
    fn etsi_timestamps_count_the_leap_seconds_inserted_since_2004() {
        let list = LeapSeconds::parse(LEAP_SECONDS_LIST).unwrap();

        assert_eq!(list.etsi_from_unix(ETSI_EPOCH_UNIX_MS), 0);
        assert_eq!(list.etsi_from_unix(ETSI_EPOCH_UNIX_MS - 1), 0);
        // TAI-UTC was 32 s at the epoch and is absent from this excerpt before 2009.
        assert_eq!(
            list.etsi_from_unix(JAN_2008_MS),
            JAN_2008_MS - ETSI_EPOCH_UNIX_MS
        );
        assert_eq!(
            list.etsi_from_unix(JAN_2017_MS - 1),
            JAN_2017_MS - 1 - ETSI_EPOCH_UNIX_MS + 4000
        );
        assert_eq!(
            list.etsi_from_unix(JAN_2017_MS),
            JAN_2017_MS - ETSI_EPOCH_UNIX_MS + 5000
        );
        assert_eq!(
            list.etsi_from_unix(NOV_2026_MS),
            NOV_2026_MS - ETSI_EPOCH_UNIX_MS + 5000
        );
        assert_eq!(
            LeapSeconds::built_in().etsi_from_unix(NOV_2026_MS),
            list.etsi_from_unix(NOV_2026_MS)
        );
    }

    #[test]
    fn etsi_timestamps_round_trip_to_unix() {
        let list = LeapSeconds::built_in();

        for unix_ms in [ETSI_EPOCH_UNIX_MS, JAN_2008_MS, JAN_2017_MS, NOV_2026_MS] {
            assert_eq!(list.unix_from_etsi(list.etsi_from_unix(unix_ms)), unix_ms);
        }
    }

    #[test]
    fn missing_or_expired_leap_seconds_list_keeps_converting() {
        let missing = LeapSeconds::load("/nonexistent/leap-seconds.list", NOV_2026_MS);
        assert_eq!(missing, LeapSeconds::built_in());

        let path = std::env::temp_dir().join(format!("leap-seconds-{}.list", std::process::id()));
        std::fs::write(&path, LEAP_SECONDS_LIST).unwrap();
        let expired = LeapSeconds::load(path.to_str().unwrap(), u64::MAX);
        std::fs::remove_file(path).unwrap();
        assert_eq!(expired, LeapSeconds::parse(LEAP_SECONDS_LIST).unwrap());
    }

    #[test]
    fn system_leap_seconds_list_gives_five_seconds_since_2004() {
        let system = LeapSeconds::load(LEAP_SECONDS_LIST_PATH, NOV_2026_MS);

        assert_eq!(system.offset_ms(NOV_2026_MS), 5000);
        assert_eq!(
            timestamp_to_etsi(NOV_2026_MS),
            NOV_2026_MS - ETSI_EPOCH_UNIX_MS + 5000
        );
    }

    #[test]
    fn etsi_now_follows_the_os_clock() {
        let before = timestamp_to_etsi(now());
        let etsi_now = etsi_now();

        assert!(etsi_now >= before);
        assert!(timestamp_from_etsi(etsi_now) <= now());
    }

    #[test]
    fn generation_delta_time_is_timestamp_its_modulo_65536() {
        assert_eq!(timestamp_to_generation_delta_time(ETSI_EPOCH_UNIX_MS), 0);
        assert_eq!(timestamp_to_generation_delta_time(0), 0);
        assert_eq!(
            timestamp_to_generation_delta_time(NOV_2026_MS),
            (timestamp_to_etsi(NOV_2026_MS) % 65536) as u16
        );
        assert_eq!(
            timestamp_to_generation_delta_time(u64::MAX),
            (timestamp_to_etsi(u64::MAX) % 65536) as u16
        );
    }
}
