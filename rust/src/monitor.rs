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

use crate::exchange::Exchange;
use crate::exchange::cause::Cause;
use crate::exchange::etsi::collective_perception_message::CollectivePerceptionMessage;
use crate::exchange::etsi::cooperative_awareness_message::CooperativeAwarenessMessage;
use crate::exchange::etsi::cooperative_awareness_message_113::CooperativeAwarenessMessage113;
use crate::exchange::etsi::decentralized_environmental_notification_message::DecentralizedEnvironmentalNotificationMessage;
use crate::exchange::etsi::map_extended_message::MAPExtendedMessage;
use crate::exchange::etsi::signal_phase_and_timing_extended_message::SignalPhaseAndTimingExtendedMessage;
use crate::exchange::message::Message;
use crate::now;

pub fn trace_exchange(
    exchange: &Exchange,
    cause: Option<Cause>,
    direction: &str,
    component: &str,
    partner: String,
) {
    let message_part = match &exchange.message {
        Message::CAM(cam) => format_cam_trace(cam),
        Message::DENM(denm) => format_denm_trace(denm, cause),
        Message::CPM(cpm) => format_cpm_trace(cpm),
        Message::MAPEM(map) => format_mapem_trace(map),
        Message::SPATEM(spat) => format_spatem_trace(spat),
        Message::CAM113(cam) => format_cam_113_trace(cam),
    };
    println!(
        "{} {} {} {} {} at {}",
        component,
        exchange.message_type,
        direction,
        partner,
        message_part,
        now()
    );
}

fn format_trace(station_id: u32, generation_delta_time: u16) -> String {
    format!("{}/{}", station_id, generation_delta_time)
}

pub(crate) fn format_cam_trace(cam: &CooperativeAwarenessMessage) -> String {
    format_trace(cam.station_id, cam.generation_delta_time)
}

pub(crate) fn format_cam_113_trace(cam: &CooperativeAwarenessMessage113) -> String {
    format_trace(cam.station_id, cam.generation_delta_time)
}

fn format_cpm_trace(cpm: &CollectivePerceptionMessage) -> String {
    format!(
        "{}/{}",
        cpm.station_id, cpm.management_container.reference_time
    )
}

fn format_denm_trace(
    denm: &DecentralizedEnvironmentalNotificationMessage,
    cause: Option<Cause>,
) -> String {
    format!(
        "{}/{}/{}/{}/{}{}",
        denm.station_id,
        denm.management.action_id.originating_station_id,
        denm.management.action_id.sequence_number,
        denm.management.reference_time,
        denm.management.detection_time,
        get_cause_str(cause),
    )
}

fn format_mapem_trace(map: &MAPExtendedMessage) -> String {
    format!(
        "{}/{}/{}",
        map.sending_station_id.unwrap_or_default(),
        map.station_id,
        map.region.unwrap_or_default(),
    )
}

fn format_spatem_trace(spat: &SignalPhaseAndTimingExtendedMessage) -> String {
    format!(
        "{}/{}/{}",
        spat.sending_station_id.unwrap_or_default(),
        spat.station_id,
        spat.region.unwrap_or_default(),
    )
}

fn get_cause_str(cause: Option<Cause>) -> String {
    match cause {
        Some(cause) => format!("/cause_type:{}/cause_id:{}", cause.m_type, cause.id),
        None => String::new(),
    }
}
