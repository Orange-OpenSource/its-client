// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Frédéric GARDES <frederic.gardes@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use crate::exchange::cause::Cause;
use crate::exchange::etsi::collective_perception_message::CollectivePerceptionMessage;
use crate::exchange::etsi::cooperative_awareness_message::CooperativeAwarenessMessage;
use crate::exchange::etsi::decentralized_environmental_notification_message::DecentralizedEnvironmentalNotificationMessage;
use crate::exchange::etsi::map_extended_message::MAPExtendedMessage;
use crate::exchange::etsi::signal_phase_and_timing_extended_message::SignalPhaseAndTimingExtendedMessage;
use crate::exchange::message::Message;
use crate::exchange::Exchange;
use crate::now;

pub fn trace_exchange(
    exchange: &Exchange,
    cause: Option<Cause>,
    direction: &str,
    component: String,
    partner: String,
) {
    let message_part = match &exchange.message {
        // FIXME find how to call position() on any Message implementing Mobile
        Message::CAM(cam) => format_cam_trace(cam),
        Message::DENM(denm) => format_denm_trace(denm, cause),
        Message::CPM(cpm) => format_cpm_trace(cpm),
        Message::MAPEM(map) => format_mapem_trace(map),
        Message::SPATEM(spat) => format_spatem_trace(spat),
        Message::INFO(info) => info.instance_id.to_string(),
    };
    println!(
        "{} {} {} {} {} at {}",
        component,
        exchange.type_field,
        direction,
        partner,
        message_part,
        now()
    );
}

pub(crate) fn format_cam_trace(cam: &CooperativeAwarenessMessage) -> String {
    format!("{}/{}", cam.station_id, cam.generation_delta_time)
}

fn format_cpm_trace(cpm: &CollectivePerceptionMessage) -> String {
    format!("{}/{}", cpm.station_id, cpm.generation_delta_time)
}

fn format_denm_trace(
    denm: &DecentralizedEnvironmentalNotificationMessage,
    cause: Option<Cause>,
) -> String {
    format!(
        "{}/{}/{}/{}/{}{}",
        denm.station_id,
        denm.management_container.action_id.originating_station_id,
        denm.management_container.action_id.sequence_number,
        denm.management_container.reference_time,
        denm.management_container.detection_time,
        get_cause_str(cause),
    )
}

fn format_mapem_trace(map: &MAPExtendedMessage) -> String {
    format!(
        "{}/{}/{}",
        map.sending_station_id.unwrap_or_default(),
        map.id,
        map.region.unwrap_or_default(),
    )
}

fn format_spatem_trace(spat: &SignalPhaseAndTimingExtendedMessage) -> String {
    format!(
        "{}/{}/{}",
        spat.sending_station_id.unwrap_or_default(),
        spat.id,
        spat.region.unwrap_or_default(),
    )
}

fn get_cause_str(cause: Option<Cause>) -> String {
    match cause {
        Some(cause) => format!("/cause_type:{}/cause_id:{}", cause.m_type, cause.id),
        None => String::new(),
    }
}
