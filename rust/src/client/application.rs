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
use crate::exchange::PathElement;
use crate::exchange::etsi::cooperative_awareness_message::{
    BasicContainer, CooperativeAwarenessMessage, HighFrequencyContainer,
};
use crate::exchange::etsi::decentralized_environmental_notification_message::{
    DecentralizedEnvironmentalNotificationMessage, RelevanceDistance, RelevanceTrafficDirection,
};
use crate::exchange::etsi::reference_position::ReferencePosition;
use crate::exchange::etsi::{etsi_now, heading_to_etsi, speed_to_etsi, timestamp_to_etsi};
use crate::exchange::sequence_number::SequenceNumber;
use crate::mobility::mobile::Mobile;
use crate::mobility::position::Position;

pub mod analyzer;
pub mod pipeline;

/// Creates a [CAM][1] message from minimal required information
///
/// This function is a helper to quickly create messages, the remaining information is defaulted
/// If you want to fill more information you can still edit the returned struct, or you can
/// initialize the struct directly with more information
///
/// **Note: All mobility arguments have to be using SI units**
///
/// [1]: CooperativeAwarenessMessage
pub fn create_cam(
    station_id: u32,
    station_type: u8,
    position: Position,
    speed: f64,
    heading: f64,
) -> CooperativeAwarenessMessage {
    CooperativeAwarenessMessage {
        station_id,
        basic_container: BasicContainer {
            station_type: Some(station_type),
            reference_position: ReferencePosition::from(position),
            ..Default::default()
        },
        high_frequency_container: HighFrequencyContainer {
            heading: Some(heading_to_etsi(heading)),
            speed: Some(speed_to_etsi(speed)),
            ..Default::default()
        },
        ..Default::default()
    }
}

// FIXME use custom errors
pub fn create_denm(
    detection_time: u64,
    configuration: &Configuration,
    cause: u8,
    subcause: Option<u8>,
    sequence_number: &mut SequenceNumber,
    mobile: &dyn Mobile,
    path: Vec<PathElement>,
) -> DecentralizedEnvironmentalNotificationMessage {
    if let Some(node_configuration) = &configuration.node {
        let read_lock = node_configuration.read().unwrap();
        let station_id = read_lock.station_id(None);
        drop(read_lock);

        let (relevance_distance, relevance_traffic_direction, event_speed, event_heading) =
            match path.len() {
                len if len <= 1 => {
                    let event_speed = mobile.speed().map(speed_to_etsi);
                    let event_heading = mobile.heading().map(heading_to_etsi);

                    (
                        Some(RelevanceDistance::LessThan50m.into()),
                        Some(RelevanceTrafficDirection::UpstreamTraffic.into()),
                        event_speed,
                        event_heading,
                    )
                }
                _ => {
                    todo!("\"extrapolate\" relevance distance and traffic direction from path")
                }
            };

        DecentralizedEnvironmentalNotificationMessage::new(
            mobile.id(),
            station_id,
            ReferencePosition::from(mobile.position()),
            sequence_number.get_next() as u16,
            timestamp_to_etsi(detection_time),
            cause,
            subcause,
            relevance_distance,
            relevance_traffic_direction,
            event_speed,
            event_heading,
            Some(10),
            Some(200),
        )
    } else {
        todo!("Ego DENM creation not managed yet")
    }
}

/// Creates an updated copy of the provided DENM
///
/// FIXME check for appropriation
pub fn update_denm(
    detection_time: u64,
    denm: &DecentralizedEnvironmentalNotificationMessage,
    mobile: &dyn Mobile,
) -> DecentralizedEnvironmentalNotificationMessage {
    let mut copy = denm.clone();

    copy.management_container.detection_time = timestamp_to_etsi(detection_time);
    copy.management_container.reference_time = etsi_now();
    copy.management_container.event_position = ReferencePosition::from(mobile.position());

    copy
}
