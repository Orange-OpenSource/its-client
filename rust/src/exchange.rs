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

pub(crate) mod cause;
pub mod etsi;
pub mod message;
pub mod mortal;
pub mod sequence_number;

use crate::exchange::message::Message;
use crate::exchange::message::content::Content;
use crate::mobility::position::Position;
use crate::transport::payload::Payload;

use crate::client::configuration::Configuration;
use crate::exchange::etsi::decentralized_environmental_notification_message::{
    DecentralizedEnvironmentalNotificationMessage, Distance, TrafficDirection,
};
use crate::exchange::etsi::heading::Heading;
use crate::exchange::etsi::reference_position::ReferencePosition;
use crate::exchange::etsi::speed::Speed;
use crate::exchange::etsi::{heading_to_etsi, speed_to_etsi, timestamp_to_etsi};
use crate::exchange::sequence_number::SequenceNumber;
use crate::mobility::mobile::Mobile;
use serde::{Deserialize, Serialize};

#[serde_with::skip_serializing_none]
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Exchange {
    pub message_type: String,
    pub source_uuid: String,
    pub timestamp: u64,
    pub version: String,
    pub message: Message,

    /// Only used into the DENM message.
    /// TODO study if the field detection_zones_to_event_position could be used instead.
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub path: Vec<PathElement>,
}

#[serde_with::skip_serializing_none]
#[derive(Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct PathElement {
    pub mobile_id: u32,
    pub position: Position,
    pub message_type: String,
}

impl Exchange {
    pub fn new(
        component: String,
        timestamp: u64,
        path: Vec<PathElement>,
        message: Message,
    ) -> Box<Exchange> {
        let content: &dyn Content = &message;

        Box::from(Exchange {
            message_type: content.get_type().to_string(),
            version: "2".to_string(),
            source_uuid: component,
            path,
            timestamp,
            message,
        })
    }

    pub fn appropriate(&mut self, timestamp: u64, new_station_id: u32, new_source_uuid: &str) {
        self.source_uuid = new_source_uuid.to_string();
        self.timestamp = timestamp;
        self.message
            .as_content()
            .appropriate(timestamp, new_station_id);
    }

    pub fn create_denm(
        detection_time: u64,
        configuration: &Configuration,
        cause: u8,
        subcause: Option<u8>,
        sequence_number: &mut SequenceNumber,
        mobile: &dyn Mobile,
        path: Vec<PathElement>,
    ) -> DecentralizedEnvironmentalNotificationMessage {
        let (relevance_distance, relevance_traffic_direction, event_speed, event_position_heading) =
            match path.len() {
                len if len <= 1 => {
                    let event_speed = mobile.speed().map(|speed| Speed {
                        value: speed_to_etsi(speed),
                        ..Default::default()
                    });
                    let event_position_heading = mobile.heading().map(|heading| Heading {
                        value: heading_to_etsi(heading),
                        ..Default::default()
                    });
                    (
                        Some(Distance::LessThan50m),
                        Some(TrafficDirection::UpstreamTraffic),
                        event_speed,
                        event_position_heading,
                    )
                }
                _ => {
                    todo!("\"extrapolate\" relevance distance and traffic direction from path")
                }
            };

        DecentralizedEnvironmentalNotificationMessage::new(
            mobile.id(),
            configuration.mobility.station_id,
            ReferencePosition::from(mobile.position()),
            sequence_number.get_next() as u16,
            timestamp_to_etsi(detection_time),
            cause,
            subcause,
            relevance_distance,
            relevance_traffic_direction,
            event_speed,
            event_position_heading,
            Some(10),
            Some(200),
        )
    }
}

impl Payload for Exchange {}

impl PartialEq for Exchange {
    fn eq(&self, other: &Self) -> bool {
        self.message == other.message
    }
}

impl Eq for Exchange {}

#[cfg(test)]
mod tests {
    use crate::exchange::Exchange;
    use crate::exchange::message::Message;

    fn minimal_cam() -> &'static str {
        r#"{
            "message_type": "cam",
            "version": "1.1.3",
            "source_uuid": "uuid14",
            "timestamp": 1574778515424,
            "message": {
                "protocol_version": 1,
                "station_id": 42,
                "generation_delta_time": 3,
                "basic_container": {
                    "reference_position": {
                        "latitude": 486263556,
                        "longitude": 22492123,
                        "altitude": 20000
                    }
                },
                "high_frequency_container": {}
            }
        }"#
    }

    fn minimal_denm() -> &'static str {
        r#"{
            "message_type": "denm",
            "version": "2.3.0",
            "source_uuid": "uuid_3101",
            "timestamp": 1574778515425,
            "message": {
                "protocol_version": 2,
                "station_id": 42,
                "management": {
                    "action_id": {
                        "originating_station_id": 41,
                        "sequence_number": 1
                    },
                    "detection_time": 503253331000,
                    "reference_time": 503253331050,
                    "event_position": {
                        "latitude": 486263556,
                        "longitude": 224921234
                    }
                }
            }
        }"#
    }

    fn minimal_cpm() -> &'static str {
        r#"{
            "message_type": "cpm",
            "version": "2.1.0",
            "source_uuid": "com_application_12345",
            "timestamp": 1574778515425,
            "message": {
                "protocol_version": 2,
                "station_id": 12345,
                "generation_delta_time": 65535,
                "management": {
                    "station_type": 5,
                    "reference_position": {
                        "latitude": 426263556,
                        "longitude": -82492123
                    }
                }
            }
        }"#
    }

    fn standard_spatem() -> &'static str {
        r#"{
            "message_type": "spatem",
            "version": "2.0.0",
            "source_uuid": "uuid_3101",
            "timestamp": 1665994085292,
            "message": {
                "sendingStationId": 2327711328,
                "protocolVersion": 1,
                "stationId": 1654,
                "region": 751,
                "timestamp": 1665994085248,
                "revision": 1,
                "states": [{
                    "ttc": 17352,
                    "toc": 1665994102644,
                    "nextChange": 1665994102644,
                    "id": 1,
                    "state": 6,
                    "nextChanges": [{
                        "ttc": 17352,
                        "toc": 1665994102644,
                        "nextChange": 1665994102644,
                        "state": 6
                    }]
                }, {
                    "ttc": 21352,
                    "toc": 1665994106644,
                    "nextChange": 1665994106644,
                    "id": 2,
                    "state": 3,
                    "nextChanges": [{
                        "ttc": 21352,
                        "toc": 1665994106644,
                        "nextChange": 1665994106644,
                        "state": 3
                    }]
                }, {
                    "ttc": 10452,
                    "toc": 1665994095744,
                    "nextChange": 1665994095744,
                    "id": 3,
                    "state": 6,
                    "nextChanges": [{
                        "ttc": 10452,
                        "toc": 1665994095744,
                        "nextChange": 1665994095744,
                        "state": 6
                    }]
                }, {
                    "ttc": 16352,
                    "toc": 1665994101644,
                    "nextChange": 1665994101644,
                    "id": 4,
                    "state": 3,
                    "nextChanges": [{
                        "ttc": 16352,
                        "toc": 1665994101644,
                        "nextChange": 1665994101644,
                        "state": 3
                    }]
                }, {
                    "ttc": 16352,
                    "toc": 1665994101644,
                    "nextChange": 1665994101644,
                    "id": 5,
                    "state": 3,
                    "nextChanges": [{
                        "ttc": 16352,
                        "toc": 1665994101644,
                        "nextChange": 1665994101644,
                        "state": 3
                    }]
                }, {
                    "ttc": 23352,
                    "toc": 1665994108644,
                    "nextChange": 1665994108644,
                    "id": 6,
                    "state": 3,
                    "nextChanges": [{
                        "ttc": 23352,
                        "toc": 1665994108644,
                        "nextChange": 1665994108644,
                        "state": 3
                    }]
                }, {
                    "ttc": 11352,
                    "toc": 1665994096644,
                    "nextChange": 1665994096644,
                    "id": 7,
                    "state": 6,
                    "nextChanges": [{
                        "ttc": 11352,
                        "toc": 1665994096644,
                        "nextChange": 1665994096644,
                        "state": 6
                    }]
                }, {
                    "ttc": 17352,
                    "toc": 1665994102644,
                    "nextChange": 1665994102644,
                    "id": 8,
                    "state": 3,
                    "nextChanges": [{
                        "ttc": 17352,
                        "toc": 1665994102644,
                        "nextChange": 1665994102644,
                        "state": 3
                    }]
                }, {
                    "ttc": 17352,
                    "toc": 1665994102644,
                    "nextChange": 1665994102644,
                    "id": 9,
                    "state": 6,
                    "nextChanges": [{
                        "ttc": 17352,
                        "toc": 1665994102644,
                        "nextChange": 1665994102644,
                        "state": 6
                    }]
                }, {
                    "ttc": 15352,
                    "toc": 1665994100644,
                    "nextChange": 1665994100644,
                    "id": 10,
                    "state": 6,
                    "nextChanges": [{
                        "ttc": 15352,
                        "toc": 1665994100644,
                        "nextChange": 1665994100644,
                        "state": 6
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 11,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 12,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 13,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 14,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 15,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 16,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 17,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 18,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 19,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 20,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 21,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 22,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 23,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 24,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 25,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 26,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 27,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 28,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 29,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 30,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 31,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 32,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 33,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 34,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 35,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }, {
                    "toc": 0,
                    "nextChange": 0,
                    "id": 36,
                    "state": 8,
                    "nextChanges": [{
                        "toc": 0,
                        "nextChange": 0,
                        "state": 8
                    }]
                }]
            }
        }"#
    }

    fn bad_cam_without_timestamp() -> &'static str {
        r#"{
            "message_type": "cam",
            "version": "2.1.1",
            "source_uuid": "uuid14",
            "message": {
                "protocol_version": 1,
                "station_id": 42,
                "generation_delta_time": 3,
                "basic_container": {
                    "reference_position": {
                        "latitude": 486263556,
                        "longitude": 22492123,
                        "altitude": 20000
                    }
                },
                "high_frequency_container": {}
            }
        }"#
    }

    fn bad_denm_with_string_timestamp() -> &'static str {
        r#"{
            "message_type": "denm",
            "version": "2.3.0",
            "source_uuid": "uuid14",
            "timestamp": "1574778515425",
            "message": {
                "protocol_version": 2,
                "station_id": 42,
                "management": {
                    "action_id": {
                        "originating_station_id": 41,
                        "sequence_number": 1
                    },
                    "detection_time": 503253332000,
                    "reference_time": 503253330000,
                    "event_position": {
                        "latitude": 486263556,
                        "longitude": 224921234
                    }
                }
            }
        }"#
    }

    fn bad_denm_with_protocol_version_u32() -> &'static str {
        r#"{
            "message_type": "denm",
            "version": "2.3.0",
            "source_uuid": "uuid14",
            "timestamp": 1574778515425,
            "message": {
                "protocol_version": 4242424242,
                "station_id": 42,
                "management": {
                    "action_id": {
                        "originating_station_id": 41,
                        "sequence_number": 1
                    },
                    "detection_time": 503253332000,
                    "reference_time": 503253330000,
                    "event_position": {
                        "latitude": 486263556,
                        "longitude": 224921234
                    }
                }
            }
        }"#
    }

    fn bad_cpm_with_negative_timestamp() -> &'static str {
        r#"{
            "message_type": "cpm",
            "origin": "self",
            "version": "1.0.0",
            "source_uuid": "uuid1",
            "timestamp": -1,
            "message": {
                "protocol_version": 1,
                "station_id": 12345,
                "message_id": 14,
                "generation_delta_time": 65535,
                "management": {
                    "station_type": 5,
                    "reference_position": {
                        "latitude": 426263556,
                        "longitude": -82492123,
                        "altitude": 800001
                    },
                    "confidence": {
                        "position_confidence_ellipse": {
                            "semi_major_confidence": 4095,
                            "semi_minor_confidence": 4095,
                            "semi_major_orientation": 3601
                        },
                        "altitude": 15
                    }
                },
                "numberOfPerceivedObjects": 1
            }
        }"#
    }

    fn remove_whitespace(s: &str) -> String {
        s.split_whitespace().collect()
    }

    #[test]
    fn it_can_deserialize_then_serialize_a_minimal_cam() {
        let json = minimal_cam();
        let cam: Exchange = serde_json::from_str(json).unwrap();
        assert_eq!(cam.timestamp, 1574778515424);
        assert_eq!(
            serde_json::to_string(&cam).unwrap(),
            remove_whitespace(json)
        );
    }

    #[test]
    fn it_can_deserialize_then_serialize_a_minimal_denm() {
        let json = minimal_denm();
        let denm: Exchange = serde_json::from_str(json).unwrap();
        assert_eq!(denm.timestamp, 1574778515425);
        if let Message::DENM(message) = &denm.message {
            assert_eq!(message.management.action_id.originating_station_id, 41);
            assert_eq!(
                serde_json::to_string(&denm).unwrap(),
                remove_whitespace(json)
            );
        } else {
            panic!("no denm deserialized");
        };
    }

    #[test]
    fn it_can_deserialize_then_serialize_a_minimal_cpm() {
        let json = minimal_cpm();
        let cpm: Exchange = serde_json::from_str(json).unwrap();
        assert_eq!(cpm.timestamp, 1574778515425);
        assert_eq!(
            serde_json::to_string(&cpm).unwrap(),
            remove_whitespace(json)
        );
    }

    #[test]
    fn it_can_deserialize_a_standard_spatem() {
        let spat_str = standard_spatem();

        match serde_json::from_str::<Exchange>(spat_str) {
            Ok(exchange) => {
                assert_eq!(exchange.message_type, "spatem");
            }
            Err(e) => {
                panic!("no spatem parsed: {e}");
            }
        }
    }

    #[test]
    #[should_panic]
    fn it_should_panic_when_deserializing_a_cam_without_timestamp() {
        let json = bad_cam_without_timestamp();
        let _: Exchange = serde_json::from_str(json).unwrap();
    }

    #[test]
    #[should_panic]
    fn it_should_panic_when_deserializing_a_denm_with_timestamp_as_string() {
        let json = bad_denm_with_string_timestamp();
        let _: Exchange = serde_json::from_str(json).unwrap();
    }

    #[test]
    #[should_panic]
    fn it_should_panic_when_deserializing_a_cpm_without_timestamp() {
        let json = bad_cpm_with_negative_timestamp();
        let _: Exchange = serde_json::from_str(json).unwrap();
    }

    #[test]
    #[should_panic]
    fn it_should_panic_when_deserializing_a_denm_with_protocol_version_bigger_than_u8() {
        let json = bad_denm_with_protocol_version_u32();
        let _: Exchange = serde_json::from_str(json).unwrap();
    }
}
