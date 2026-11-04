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
use crate::exchange::message::Message;
use serde_json::Value;
use std::fmt::Formatter;

/// Cause is hereby used pass information to the monitoring functions about which message a DENM
/// was emitted from
///
/// If the `analyze(...)` call of an Analyzer implementation return some DENM to send, the info in
/// the message that has provided to the method will be used to build a Caused that will allow to
/// tell that this DENM was detected using this message
#[derive(Debug, PartialEq)]
pub struct Cause {
    pub m_type: String,
    pub id: String,
}

impl std::fmt::Display for Cause {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "/cause_type:{}/cause_id:{}", self.m_type, self.id)
    }
}

impl Cause {
    fn new(m_type: String, id: String) -> Self {
        Self { m_type, id }
    }

    pub fn from_json_value(value: &Value) -> Option<Cause> {
        let mut cause = None;

        if let Some(message_type) = value.get("message_type") {
            if let Some(message) = value.get("message") {
                if let Some(station_id) = message.get("station_id") {
                    if let Some(generation_delta_time) = message.get("generation_delta_time") {
                        cause = Some(Cause::new(
                            message_type.as_str().unwrap().to_string(),
                            format!("{}/{}", station_id, generation_delta_time),
                        ));
                    } else if let Some(management_container) = message.get("management_container") {
                        if let Some(reference_time) = management_container.get("reference_time") {
                            cause = Some(Cause::new(
                                message_type.as_str().unwrap().to_string(),
                                format!("{}/{}", station_id, reference_time),
                            ));
                        }
                    }
                }
            }
        }

        cause
    }

    pub fn from_exchange(exchange: &Exchange) -> Option<Cause> {
        match &exchange.message {
            Message::CAM(message) => Some(Cause::new(
                exchange.message_type.clone(),
                format!("{}/{}", message.station_id, message.generation_delta_time),
            )),
            Message::CPM(message) => Some(Cause::new(
                exchange.message_type.clone(),
                format!(
                    "{}/{}",
                    message.station_id, message.management_container.reference_time
                ),
            )),
            _ => None,
        }
    }
}

#[cfg(test)]
mod tests {
    use crate::exchange::Exchange;
    use crate::exchange::cause::Cause;

    const MINIMAL_CAM_JSON_EXCHANGE: &'static str = r#"{
            "message_type": "cam",
            "source_uuid": "com_application_12345",
            "timestamp": 1574778515424,
            "version": "2.2.0",
            "message": {
                "protocol_version": 255,
                "station_id": 4294967295,
                "generation_delta_time": 65535,
                "basic_container": {
                    "station_type": 255,
                    "reference_position": {
                        "latitude": 900000001,
                        "longitude": 1800000001,
                         "altitude": {
                            "value": 800001,
                            "confidence": 15
                        },
                        "position_confidence_ellipse": {
                            "semi_major": 4095,
                            "semi_minor": 0,
                            "semi_major_orientation": 3601
                        }
                    }
                },
                "high_frequency_container": {
                    "basic_vehicle_container_high_frequency": {
                        "heading": {
                            "value": 3601,
                            "confidence": 127
                        },
                        "speed": {
                            "value": 16383,
                            "confidence": 127
                        },
                        "drive_direction": 2,
                        "vehicle_length": {
                            "value": 1023,
                            "confidence": 4
                        },
                        "vehicle_width": 62,
                        "longitudinal_acceleration": {
                            "value": 161,
                            "confidence": 102
                        },
                        "curvature": {
                            "value": 1023,
                            "confidence": 7
                        },
                        "curvature_calculation_mode": 2,
                        "yaw_rate": {
                            "value": 32767,
                            "confidence": 8
                        }
                    }
                }
           }
        }"#;

    const MINIMAL_JSON_CPM_EXCHANGE: &'static str = r#"{
            "message_type": "cpm",
            "source_uuid": "com_application_12345",
            "timestamp": 1574778515424,
            "version": "2.1.0",
            "message": {
                "protocol_version": 255,
                "station_id": 4294967295,
                "management_container": {
                    "reference_time": 4398046511103,
                    "reference_position": {
                        "latitude": 900000001,
                        "longitude": -1800000001,
                        "altitude": {
                            "value": 800001,
                            "confidence": 15
                        },
                        "position_confidence_ellipse": {
                            "semi_major": 4095,
                            "semi_minor": 4095,
                            "semi_major_orientation": 3601
                        }
                    }
                }
            }
        }"#;

    #[test]
    fn from_json_and_from_exchange_behave_the_same_on_cam() {
        let data = serde_json::from_str(MINIMAL_CAM_JSON_EXCHANGE).unwrap();
        let cam = serde_json::from_str::<Exchange>(MINIMAL_CAM_JSON_EXCHANGE).unwrap();

        let cause_from_json = Cause::from_json_value(&data).unwrap();
        let cause_from_struct = Cause::from_exchange(&cam).unwrap();

        assert_eq!(cause_from_struct, cause_from_json);
    }

    #[test]
    fn from_json_and_from_exchange_behave_the_same_on_cpm() {
        let data = serde_json::from_str(MINIMAL_JSON_CPM_EXCHANGE).unwrap();
        let cpm = serde_json::from_str::<Exchange>(MINIMAL_JSON_CPM_EXCHANGE).unwrap();

        let cause_from_json = Cause::from_json_value(&data).unwrap();
        let cause_from_struct = Cause::from_exchange(&cpm).unwrap();

        assert_eq!(cause_from_struct, cause_from_json);
    }
}
