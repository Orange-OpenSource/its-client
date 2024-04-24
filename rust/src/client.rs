// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2023 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas BUFFON <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use crate::client::configuration::Configuration;
use crate::exchange::etsi::decentralized_environmental_notification_message::DecentralizedEnvironmentalNotificationMessage;
use crate::exchange::etsi::reference_position::ReferencePosition;
use crate::exchange::etsi::{etsi_now, heading_to_etsi, speed_to_etsi, timestamp_to_etsi};
use crate::exchange::sequence_number::SequenceNumber;
use crate::exchange::PathElement;
use crate::mobility::mobile::Mobile;

/// Structures, functions and traits in this mod are made to create applications that analyze
/// messages to either send new messages (e.g. creating DENM on odd behaviour from CAMs)
/// or to create/store data (e.g. counting pedestrian, vehicles, etc. in a specific area)
pub mod application;
pub mod configuration;

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
                    (Some(0), Some(1), event_speed, event_heading)
                }
                _ => {
                    // TODO "extrapolate" relevance distance and traffic direction from path
                    todo!()
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
