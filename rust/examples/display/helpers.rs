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

use libits::exchange::etsi::collective_perception_message::CollectivePerceptionMessage;
use libits::exchange::message::Message;
use libits::exchange::message::content::Content;
use libits::mobility::mobile::Mobile;
use libits::mobility::quadtree::validate_coordinates;
use std::time::{SystemTime, UNIX_EPOCH};

/// Extracts a validated (latitude, longitude) in degrees from a [`Message`].
///
/// Delegates to [`Content::as_mobile`] for all message types and falls back to
/// CPM reference position when no mobile projection is available.
pub fn extract_position(message: &Message) -> Option<(f64, f64)> {
    let position = message.as_mobile().map_or_else(
        |_| match message {
            Message::CPM(cpm) => Some(cpm.position()),
            _ => None,
        },
        |mobile| Some(mobile.position()),
    )?;
    validate_coordinates(
        position.latitude.to_degrees(),
        position.longitude.to_degrees(),
    )
}

/// Extracts position confidence from a [`Message`].
///
/// Returns `0.0` when the message does not implement [`Mobile`] or confidence
/// is unavailable.
pub fn extract_confidence(message: &Message) -> f64 {
    message
        .as_mobile()
        .map(|mobile| mobile.position_confidence())
        .unwrap_or(0.0)
}

/// Extracts perceived-object positions and confidences from a
/// [`CollectivePerceptionMessage`].
///
/// Uses the SDK's [`MobilePerceivedObject`] to compute each object's geodetic
/// position from the CPM's reference position and Cartesian offsets.
pub fn extract_perceived_objects(cpm: &CollectivePerceptionMessage) -> Vec<(f64, f64, f64)> {
    cpm.mobile_perceived_object_list(None)
        .iter()
        .filter_map(|mobile_perceived_object| {
            let position = mobile_perceived_object.position();
            let latitude = position.latitude.to_degrees();
            let longitude = position.longitude.to_degrees();
            validate_coordinates(latitude, longitude).map(|(latitude, longitude)| {
                (
                    latitude,
                    longitude,
                    mobile_perceived_object.position_confidence(),
                )
            })
        })
        .collect()
}

/// Extracts the day string (YYYY-MM-DD) from a timestamp in milliseconds since Unix Epoch.
#[allow(dead_code)]
pub fn day_from_timestamp_ms(timestamp_ms: u64) -> String {
    let seconds = timestamp_ms / 1000;
    let days_since_epoch = (seconds / 86400) as i64;
    epoch_days_to_date(days_since_epoch)
}

/// Returns the current day as YYYY-MM-DD.
#[allow(dead_code)]
pub fn current_day() -> String {
    let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap();
    let days = (now.as_secs() / 86400) as i64;
    epoch_days_to_date(days)
}

/// Converts a count of days since 1970-01-01 to a YYYY-MM-DD string.
/// Algorithm from <https://howardhinnant.github.io/date_algorithms.html>.
fn epoch_days_to_date(days: i64) -> String {
    let shifted_days = days + 719468;
    let era = if shifted_days >= 0 {
        shifted_days / 146097
    } else {
        (shifted_days - 146096) / 146097
    };
    let day_of_era = (shifted_days - era * 146097) as u64;
    let year_of_era =
        (day_of_era - day_of_era / 1460 + day_of_era / 36524 - day_of_era / 146096) / 365;
    let year = year_of_era as i64 + era * 400;
    let day_of_year = day_of_era - (365 * year_of_era + year_of_era / 4 - year_of_era / 100);
    let month_index = (5 * day_of_year + 2) / 153;
    let day = day_of_year - (153 * month_index + 2) / 5 + 1;
    let month = if month_index < 10 {
        month_index + 3
    } else {
        month_index - 9
    };
    let year = if month <= 2 { year + 1 } else { year };
    format!("{:04}-{:02}-{:02}", year, month, day)
}

#[cfg(test)]
mod tests {
    use super::*;
    use libits::exchange::etsi::collective_perception_message::{
        CollectivePerceptionMessage, ManagementContainer as CpmManagement,
        OriginatingVehicleContainer,
    };
    use libits::exchange::etsi::cooperative_awareness_message::{
        BasicContainer, CooperativeAwarenessMessage,
    };
    use libits::exchange::etsi::cooperative_awareness_message_113::{
        BasicContainer as BasicContainer113, CooperativeAwarenessMessage113,
    };
    use libits::exchange::etsi::decentralized_environmental_notification_message::{
        ActionId, DecentralizedEnvironmentalNotificationMessage,
        ManagementContainer as DenmManagement,
    };
    use libits::exchange::etsi::reference_position::ReferencePosition;
    use libits::exchange::etsi::reference_position113::{
        PositionConfidence, PositionConfidenceEllipse as Ellipse113, ReferencePosition113,
    };

    fn cam_message(lat: i32, lon: i32) -> Message {
        Message::CAM(CooperativeAwarenessMessage {
            basic_container: BasicContainer {
                reference_position: ReferencePosition {
                    latitude: lat,
                    longitude: lon,
                    ..Default::default()
                },
                ..Default::default()
            },
            ..Default::default()
        })
    }

    #[test]
    fn extract_position_cam() {
        let message = cam_message(488566000, 23522000);
        let result = extract_position(&message);
        assert!(result.is_some());
        let (lat, lon) = result.unwrap();
        assert!((lat - 48.8566).abs() < 0.0001);
        assert!((lon - 2.3522).abs() < 0.0001);
    }

    #[test]
    fn extract_position_denm() {
        let message = Message::DENM(DecentralizedEnvironmentalNotificationMessage {
            management: DenmManagement {
                event_position: ReferencePosition {
                    latitude: 488566000,
                    longitude: 23522000,
                    ..Default::default()
                },
                action_id: ActionId::default(),
                detection_time: 0,
                reference_time: 0,
                station_type: 0,
                ..Default::default()
            },
            ..Default::default()
        });
        assert!(extract_position(&message).is_some());
    }

    #[test]
    fn extract_position_cpm_without_originating_vehicle() {
        let message = Message::CPM(CollectivePerceptionMessage {
            management_container: CpmManagement {
                reference_position: ReferencePosition {
                    latitude: 488566000,
                    longitude: 23522000,
                    ..Default::default()
                },
                ..Default::default()
            },
            ..Default::default()
        });
        let result = extract_position(&message);
        assert!(
            result.is_some(),
            "CPM position should be extractable even without originating_vehicle_container"
        );
    }

    #[test]
    fn extract_position_cpm_with_originating_vehicle() {
        let message = Message::CPM(CollectivePerceptionMessage {
            management_container: CpmManagement {
                reference_position: ReferencePosition {
                    latitude: 488566000,
                    longitude: 23522000,
                    ..Default::default()
                },
                ..Default::default()
            },
            originating_vehicle_container: Some(OriginatingVehicleContainer::default()),
            ..Default::default()
        });
        assert!(extract_position(&message).is_some());
    }

    #[test]
    fn extract_confidence_with_data() {
        use libits::exchange::etsi::reference_position::PositionConfidenceEllipse;

        let message = Message::CAM(CooperativeAwarenessMessage {
            basic_container: BasicContainer {
                reference_position: ReferencePosition {
                    latitude: 488566000,
                    longitude: 23522000,
                    position_confidence_ellipse: PositionConfidenceEllipse {
                        semi_major: 100,
                        semi_minor: 200,
                        ..Default::default()
                    },
                    ..Default::default()
                },
                ..Default::default()
            },
            ..Default::default()
        });
        assert!((extract_confidence(&message) - 150.0).abs() < f64::EPSILON);
    }

    #[test]
    fn extract_confidence_cam113_format() {
        let message = Message::CAM113(CooperativeAwarenessMessage113 {
            basic_container: BasicContainer113 {
                reference_position: ReferencePosition113 {
                    latitude: 436352386,
                    longitude: 13749545,
                    altitude: 20976,
                },
                confidence: Some(PositionConfidence {
                    position_confidence_ellipse: Some(Ellipse113 {
                        semi_major_confidence: Some(10),
                        semi_minor_confidence: Some(50),
                        semi_major_orientation: Some(1),
                    }),
                    altitude: Some(1),
                }),
                ..Default::default()
            },
            ..Default::default()
        });
        assert!((extract_confidence(&message) - 30.0).abs() < f64::EPSILON);
    }

    #[test]
    fn extract_confidence_without_data() {
        let message = cam_message(488566000, 23522000);
        assert!((extract_confidence(&message) - 0.0).abs() < f64::EPSILON);
    }

    #[test]
    fn day_from_known_timestamp() {
        assert_eq!(day_from_timestamp_ms(1736942400000), "2025-01-15");
    }

    #[test]
    fn day_from_epoch() {
        assert_eq!(day_from_timestamp_ms(0), "1970-01-01");
    }

    #[test]
    fn current_day_format() {
        let day = current_day();
        assert_eq!(day.len(), 10);
        assert_eq!(day.chars().nth(4), Some('-'));
        assert_eq!(day.chars().nth(7), Some('-'));
    }
}
