// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2023 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas Buffon <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::message::content_error::ContentError::{NotAMobile, NotAMortal};
use crate::exchange::mortal::Mortal;
use crate::mobility::mobile::Mobile;
use serde::{Deserialize, Serialize};
use serde_repr::Deserialize_repr;
use std::any::type_name;
use std::fmt;
use std::fmt::Formatter;
use std::hash::{Hash, Hasher};

/// SPATEM representation
///
/// **S**ignal **P**hase **A**nd **T**iming **E**xtendedMessage
///
/// **See also:**
/// - [MAPExtendedMessage][1]
///
/// [1]: crate::exchange::etsi::map_extended_message
#[derive(Serialize, Deserialize, Clone, Default)]
#[serde(rename_all = "camelCase")]
pub struct SignalPhaseAndTimingExtendedMessage {
    /// Intersection id
    pub id: u64,
    /// Reference time of the signal state timing present in the message
    pub timestamp: Option<u64>,
    pub sending_station_id: Option<u64>,
    pub region: Option<u64>,
    pub revision: Option<u32>,
    pub protocol_version: Option<u16>,
    /// State list for each signal group ot he intersection
    pub states: Vec<State>,
}

impl Content for SignalPhaseAndTimingExtendedMessage {
    fn get_type(&self) -> &str {
        "spatem"
    }

    /// TODO implement this (issue [#96](https://github.com/Orange-OpenSource/its-client/issues/96))
    fn appropriate(&mut self) {
        todo!()
    }

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError> {
        Err(NotAMobile(
            type_name::<SignalPhaseAndTimingExtendedMessage>(),
        ))
    }

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError> {
        Err(NotAMortal(
            type_name::<SignalPhaseAndTimingExtendedMessage>(),
        ))
    }
}

impl fmt::Display for SignalPhaseAndTimingExtendedMessage {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{{ {}, {}, {}, {}, {}, {}, [{}] }}",
            self.id,
            self.timestamp.unwrap_or_default(),
            self.sending_station_id.unwrap_or_default(),
            self.region.unwrap_or_default(),
            self.revision.unwrap_or_default(),
            self.protocol_version.unwrap_or_default(),
            self.states
                .iter()
                .map(|s| s.to_string())
                .collect::<Vec<String>>()
                .join(", "),
        )
    }
}

impl fmt::Debug for SignalPhaseAndTimingExtendedMessage {
    fn fmt(&self, f: &mut Formatter) -> fmt::Result {
        write!(f, "{}", self)
    }
}

impl PartialEq<Self> for SignalPhaseAndTimingExtendedMessage {
    fn eq(&self, other: &Self) -> bool {
        self.id.eq(&other.id) && self.timestamp.eq(&other.timestamp)
    }
}

impl Hash for SignalPhaseAndTimingExtendedMessage {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.id.hash(state);
        self.timestamp.hash(state);
    }
}

#[derive(Serialize, Deserialize_repr, PartialEq, Eq, Debug, Clone, Hash, Copy)]
#[repr(u8)]
pub enum TrafficLightState {
    Unavailable = 0,
    Dark = 1,
    StopThenProceed = 2,
    StopAndRemain = 3,
    PreMovement = 4,
    PermissiveMovementAllowed = 5,
    ProtectedMovementAllowed = 6,
    PermissiveClearance = 7,
    ProtectedClearance = 8,
    CautionConflictingTraffic = 9,
}

impl fmt::Display for TrafficLightState {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{:?}", *self)
    }
}

#[derive(Serialize, Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct State {
    pub id: u64,
    pub state: TrafficLightState,
    /// Time before change on this signal group based on the server clock (in milliseconds)
    ///
    /// - Present only if the timing is known
    /// - Can be negative
    pub ttc: Option<i32>,
    /// Absolute time of the next state change on this signal group (in milliseconds)
    /// (this is a timestamp since 1st January 1970)
    pub next_change: u64,
    /// List of the next phases **if supported by the traffic light controller**
    #[serde(default)]
    pub next_changes: Vec<NextChange>,
}

impl PartialEq for State {
    fn eq(&self, other: &Self) -> bool {
        self.id == other.id && self.state == other.state
    }
}

impl Hash for State {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.id.hash(state);
        self.state.hash(state);
    }
}

impl fmt::Display for State {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{{ {}, {}, {}, {}, [{}] }}",
            self.id,
            self.state,
            self.ttc.unwrap_or_default(),
            self.next_change,
            self.next_changes
                .iter()
                .map(|nc| nc.to_string())
                .collect::<Vec<String>>()
                .join(", "),
        )
    }
}

impl fmt::Debug for State {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self)
    }
}

#[derive(Serialize, Deserialize, Hash, Clone)]
#[serde(rename_all = "camelCase")]
pub struct NextChange {
    pub state: TrafficLightState,
    pub ttc: Option<i32>,
    pub next_change: u64,
}

impl fmt::Display for NextChange {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{{{}, {}, {}}}",
            self.state,
            self.ttc.unwrap_or(-1),
            self.next_change
        )
    }
}

#[cfg(test)]
mod test {
    use crate::exchange::etsi::signal_phase_and_timing_extended_message::{
        SignalPhaseAndTimingExtendedMessage, TrafficLightState,
    };

    #[test]
    fn test_root_optional_fields() {
        let data = r#"
        {
            "id": 11,
            "states":
            [
                {
                    "id": 16,
                    "state": 3,
                    "nextChange": 1000000000,
                    "nextChanges":
                    [
                        {
                            "state": 4,
                            "nextChange": 1000000030
                        }
                    ]
                }
            ]
        }
        "#;

        match serde_json::from_str::<SignalPhaseAndTimingExtendedMessage>(data) {
            Ok(spat) => {
                assert_eq!(spat.id, 11);
                assert!(spat.timestamp.is_none());
                assert!(spat.sending_station_id.is_none());
                assert!(spat.region.is_none());
                assert!(spat.revision.is_none());
                assert!(spat.protocol_version.is_none());
                assert_eq!(spat.states.len(), 1);
                assert!(spat.states.first().is_some());
                let state = spat.states.first().unwrap();
                assert_eq!(state.id, 16);
                assert_eq!(state.state, TrafficLightState::StopAndRemain);
                assert!(state.ttc.is_none());
                assert_eq!(state.next_change, 1000000000);
                assert_eq!(state.next_changes.len(), 1);
                assert_eq!(state.next_changes[0].state, TrafficLightState::PreMovement);
                assert!(state.next_changes[0].ttc.is_none());
                assert_eq!(state.next_changes[0].next_change, 1000000030);
            }
            Err(e) => {
                panic!("{:?}", e);
            }
        }
    }

    #[test]
    fn test_ttc_is_optional_in_state() {
        let data = r#"
        {
            "id": 11,
            "timestamp": 123456789,
            "sendingStationId": 12,
            "region": 13,
            "revision": 14,
            "protocolVersion": 15,
            "states":
            [
                {
                    "id": 16,
                    "state": 3,
                    "nextChange": 1000000000,
                    "nextChanges":
                    [
                        {
                            "state": 4,
                            "nextChange": 1000000030
                        }
                    ]
                }
            ]
        }
        "#;

        match serde_json::from_str::<SignalPhaseAndTimingExtendedMessage>(data) {
            Ok(spat) => {
                assert_eq!(spat.id, 11);
                assert_eq!(spat.timestamp.unwrap(), 123456789);
                assert_eq!(spat.sending_station_id.unwrap(), 12);
                assert_eq!(spat.region.unwrap(), 13);
                assert_eq!(spat.revision.unwrap(), 14);
                assert_eq!(spat.protocol_version.unwrap(), 15);
                assert_eq!(spat.states.len(), 1);
                assert!(spat.states.first().is_some());
                let state = spat.states.first().unwrap();
                assert_eq!(state.id, 16);
                assert_eq!(state.state, TrafficLightState::StopAndRemain);
                assert!(state.ttc.is_none());
                assert_eq!(state.next_change, 1000000000);
                assert_eq!(state.next_changes.len(), 1);
                assert_eq!(state.next_changes[0].state, TrafficLightState::PreMovement);
                assert!(state.next_changes[0].ttc.is_none());
                assert_eq!(state.next_changes[0].next_change, 1000000030);
            }
            Err(e) => {
                panic!("{:?}", e);
            }
        }
    }

    #[test]
    fn test_next_changes_is_optional_in_state() {
        let data = r#"
        {
            "id": 11,
            "timestamp": 123456789,
            "sendingStationId": 12,
            "region": 13,
            "revision": 14,
            "protocolVersion": 15,
            "states":
            [
                {
                    "id": 16,
                    "state": 3,
                    "ttc": 30,
                    "nextChange": 1000000000
                }
            ]
        }
        "#;

        match serde_json::from_str::<SignalPhaseAndTimingExtendedMessage>(data) {
            Ok(spat) => {
                assert_eq!(spat.id, 11);
                assert_eq!(spat.timestamp.unwrap(), 123456789);
                assert_eq!(spat.sending_station_id.unwrap(), 12);
                assert_eq!(spat.region.unwrap(), 13);
                assert_eq!(spat.revision.unwrap(), 14);
                assert_eq!(spat.protocol_version.unwrap(), 15);
                assert_eq!(spat.states.len(), 1);
                assert!(spat.states.first().is_some());
                let state = spat.states.first().unwrap();
                assert_eq!(state.id, 16);
                assert_eq!(state.state, TrafficLightState::StopAndRemain);
                assert_eq!(state.ttc.unwrap(), 30);
                assert_eq!(state.next_change, 1000000000);
                assert_eq!(state.next_changes.len(), 0);
            }
            Err(e) => {
                panic!("{:?}", e);
            }
        }
    }

    #[test]
    fn test_next_changes_can_be_empty_in_state() {
        let data = r#"
        {
            "id": 11,
            "timestamp": 123456789,
            "sendingStationId": 12,
            "region": 13,
            "revision": 14,
            "protocolVersion": 15,
            "states":
            [
                {
                    "id": 16,
                    "state": 3,
                    "ttc": 30,
                    "nextChange": 1000000000,
                    "nextChanges": []
                }
            ]
        }
        "#;

        match serde_json::from_str::<SignalPhaseAndTimingExtendedMessage>(data) {
            Ok(spat) => {
                assert_eq!(spat.id, 11);
                assert_eq!(spat.timestamp.unwrap(), 123456789);
                assert_eq!(spat.sending_station_id.unwrap(), 12);
                assert_eq!(spat.region.unwrap(), 13);
                assert_eq!(spat.revision.unwrap(), 14);
                assert_eq!(spat.protocol_version.unwrap(), 15);
                assert_eq!(spat.states.len(), 1);
                assert!(spat.states.first().is_some());
                let state = spat.states.first().unwrap();
                assert_eq!(state.id, 16);
                assert_eq!(state.state, TrafficLightState::StopAndRemain);
                assert_eq!(state.ttc.unwrap(), 30);
                assert_eq!(state.next_change, 1000000000);
                assert_eq!(state.next_changes.len(), 0);
            }
            Err(e) => {
                panic!("{:?}", e);
            }
        }
    }

    #[test]
    fn test_ttc_is_optional_in_next_state() {
        let data = r#"
        {
            "id": 11,
            "timestamp": 123456789,
            "sendingStationId": 12,
            "region": 13,
            "revision": 14,
            "protocolVersion": 15,
            "states":
            [
                {
                    "id": 16,
                    "state": 3,
                    "ttc": 30,
                    "nextChange": 1000000000,
                    "nextChanges":
                    [
                        {
                            "state": 4,
                            "nextChange": 1000000030
                        }
                    ]
                }
            ]
        }
        "#;

        match serde_json::from_str::<SignalPhaseAndTimingExtendedMessage>(data) {
            Ok(spat) => {
                assert_eq!(spat.id, 11);
                assert_eq!(spat.timestamp.unwrap(), 123456789);
                assert_eq!(spat.sending_station_id.unwrap(), 12);
                assert_eq!(spat.region.unwrap(), 13);
                assert_eq!(spat.revision.unwrap(), 14);
                assert_eq!(spat.protocol_version.unwrap(), 15);
                assert_eq!(spat.states.len(), 1);
                assert!(spat.states.first().is_some());
                let state = spat.states.first().unwrap();
                assert_eq!(state.id, 16);
                assert_eq!(state.state, TrafficLightState::StopAndRemain);
                assert_eq!(state.ttc.unwrap(), 30);
                assert_eq!(state.next_change, 1000000000);
                assert_eq!(state.next_changes.len(), 1);
                assert_eq!(state.next_changes[0].state, TrafficLightState::PreMovement);
                assert!(state.next_changes[0].ttc.is_none());
                assert_eq!(state.next_changes[0].next_change, 1000000030);
            }
            Err(e) => {
                panic!("{:?}", e);
            }
        }
    }

    #[test]
    fn test_complete_deserialization() {
        let data = r#"
        {
            "id": 11,
            "timestamp": 123456789,
            "sendingStationId": 12,
            "region": 13,
            "revision": 14,
            "protocolVersion": 15,
            "states":
            [
                {
                    "id": 16,
                    "state": 3,
                    "ttc": 30,
                    "nextChange": 1000000000,
                    "nextChanges":
                    [
                        {
                            "state": 4,
                            "ttc": 30,
                            "nextChange": 1000000030
                        },
                        {
                            "state": 5,
                            "ttc": 30,
                            "nextChange": 1000000060
                        }
                    ]
                }
            ]
        }
        "#;

        match serde_json::from_str::<SignalPhaseAndTimingExtendedMessage>(data) {
            Ok(spat) => {
                assert_eq!(spat.id, 11);
                assert_eq!(spat.timestamp.unwrap(), 123456789);
                assert_eq!(spat.sending_station_id.unwrap(), 12);
                assert_eq!(spat.region.unwrap(), 13);
                assert_eq!(spat.revision.unwrap(), 14);
                assert_eq!(spat.protocol_version.unwrap(), 15);
                assert_eq!(spat.states.len(), 1);
                assert!(spat.states.first().is_some());
                let state = spat.states.first().unwrap();
                assert_eq!(state.id, 16);
                assert_eq!(state.state, TrafficLightState::StopAndRemain);
                assert_eq!(state.ttc.unwrap(), 30);
                assert_eq!(state.next_change, 1000000000);
                assert_eq!(state.next_changes.len(), 2);
                assert_eq!(state.next_changes[0].state, TrafficLightState::PreMovement);
                assert!(state.next_changes[0].ttc.is_some());
                assert_eq!(state.next_changes[0].ttc.unwrap(), 30);
                assert_eq!(state.next_changes[0].next_change, 1000000030);
                assert_eq!(
                    state.next_changes[1].state,
                    TrafficLightState::PermissiveMovementAllowed
                );
                assert_eq!(state.next_changes[1].next_change, 1000000060);
                assert!(state.next_changes[1].ttc.is_some());
                assert_eq!(state.next_changes[1].ttc.unwrap(), 30);
            }
            Err(e) => {
                panic!("{:?}", e);
            }
        }
    }

    #[test]
    fn test_real_spat_extended_message_deserialization() {
        let data = r#"{
            "sendingStationId": 49828819,
            "protocolVersion": 1,
            "id": 243,
            "region": 751,
            "timestamp": 1661861522210,
            "revision": 1,
            "states": [{
              "ttc": 58990,
              "toc": 1661861581246,
              "nextChange": 1661861581246,
              "id": 1,
              "state": 8,
              "nextChanges": [{
                "ttc": 58990,
                "toc": 1661861581246,
                "nextChange": 1661861581246,
                "state": 8
              }]
            }, {
              "ttc": 2690,
              "toc": 1661861524946,
              "nextChange": 1661861524946,
              "id": 2,
              "state": 3,
              "nextChanges": [{
                "ttc": 2690,
                "toc": 1661861524946,
                "nextChange": 1661861524946,
                "state": 3
              }]
            }, {
              "ttc": 3690,
              "toc": 1661861525946,
              "nextChange": 1661861525946,
              "id": 3,
              "state": 3,
              "nextChanges": [{
                "ttc": 3690,
                "toc": 1661861525946,
                "nextChange": 1661861525946,
                "state": 3
              }]
            }, {
              "ttc": 60689,
              "toc": 1661861582945,
              "nextChange": 1661861582945,
              "id": 4,
              "state": 3,
              "nextChanges": [{
                "ttc": 60689,
                "toc": 1661861582945,
                "nextChange": 1661861582945,
                "state": 3
              }]
            }, {
              "ttc": 6689,
              "toc": 1661861528945,
              "nextChange": 1661861528945,
              "id": 5,
              "state": 3,
              "nextChanges": [{
                "ttc": 6689,
                "toc": 1661861528945,
                "nextChange": 1661861528945,
                "state": 3
              }]
            }, {
              "ttc": 46689,
              "toc": 1661861568945,
              "nextChange": 1661861568945,
              "id": 6,
              "state": 3,
              "nextChanges": [{
                "ttc": 46689,
                "toc": 1661861568945,
                "nextChange": 1661861568945,
                "state": 3
              }]
            }, {
              "ttc": 1689,
              "toc": 1661861523945,
              "nextChange": 1661861523945,
              "id": 7,
              "state": 6,
              "nextChanges": [{
                "ttc": 1689,
                "toc": 1661861523945,
                "nextChange": 1661861523945,
                "state": 6
              }]
            }, {
              "ttc": 7689,
              "toc": 1661861529945,
              "nextChange": 1661861529945,
              "id": 8,
              "state": 3,
              "nextChanges": [{
                "ttc": 7689,
                "toc": 1661861529945,
                "nextChange": 1661861529945,
                "state": 3
              }]
            }, {
              "ttc": 13689,
              "toc": 1661861535945,
              "nextChange": 1661861535945,
              "id": 9,
              "state": 6,
              "nextChanges": [{
                "ttc": 13689,
                "toc": 1661861535945,
                "nextChange": 1661861535945,
                "state": 6
              }]
            }, {
              "ttc": 17689,
              "toc": 1661861539945,
              "nextChange": 1661861539945,
              "id": 10,
              "state": 3,
              "nextChanges": [{
                "ttc": 17689,
                "toc": 1661861539945,
                "nextChange": 1661861539945,
                "state": 3
              }]
            }, {
              "ttc": 18689,
              "toc": 1661861540945,
              "nextChange": 1661861540945,
              "id": 11,
              "state": 3,
              "nextChanges": [{
                "ttc": 18689,
                "toc": 1661861540945,
                "nextChange": 1661861540945,
                "state": 3
              }]
            }, {
              "ttc": 9689,
              "toc": 1661861531945,
              "nextChange": 1661861531945,
              "id": 12,
              "state": 6,
              "nextChanges": [{
                "ttc": 9689,
                "toc": 1661861531945,
                "nextChange": 1661861531945,
                "state": 6
              }]
            }, {
              "ttc": 18689,
              "toc": 1661861540945,
              "nextChange": 1661861540945,
              "id": 13,
              "state": 6,
              "nextChanges": [{
                "ttc": 18689,
                "toc": 1661861540945,
                "nextChange": 1661861540945,
                "state": 6
              }]
            }, {
              "ttc": 18789,
              "toc": 1661861541045,
              "nextChange": 1661861541045,
              "id": 14,
              "state": 6,
              "nextChanges": [{
                "ttc": 18789,
                "toc": 1661861541045,
                "nextChange": 1661861541045,
                "state": 6
              }]
            }, {
              "ttc": 18789,
              "toc": 1661861541045,
              "nextChange": 1661861541045,
              "id": 15,
              "state": 6,
              "nextChanges": [{
                "ttc": 18789,
                "toc": 1661861541045,
                "nextChange": 1661861541045,
                "state": 6
              }]
            }, {
              "ttc": 25689,
              "toc": 1661861547945,
              "nextChange": 1661861547945,
              "id": 16,
              "state": 3,
              "nextChanges": [{
                "ttc": 25689,
                "toc": 1661861547945,
                "nextChange": 1661861547945,
                "state": 3
              }]
            }, {
              "ttc": 25689,
              "toc": 1661861547945,
              "nextChange": 1661861547945,
              "id": 17,
              "state": 3,
              "nextChanges": [{
                "ttc": 25689,
                "toc": 1661861547945,
                "nextChange": 1661861547945,
                "state": 3
              }]
            }, {
              "ttc": 22689,
              "toc": 1661861544945,
              "nextChange": 1661861544945,
              "id": 18,
              "state": 3,
              "nextChanges": [{
                "ttc": 22689,
                "toc": 1661861544945,
                "nextChange": 1661861544945,
                "state": 3
              }]
            }, {
              "ttc": 10689,
              "toc": 1661861532945,
              "nextChange": 1661861532945,
              "id": 19,
              "state": 6,
              "nextChanges": [{
                "ttc": 10689,
                "toc": 1661861532945,
                "nextChange": 1661861532945,
                "state": 6
              }]
            }, {
              "ttc": 1689,
              "toc": 1661861523945,
              "nextChange": 1661861523945,
              "id": 20,
              "state": 6,
              "nextChanges": [{
                "ttc": 1689,
                "toc": 1661861523945,
                "nextChange": 1661861523945,
                "state": 6
              }]
            }, {
              "ttc": 17689,
              "toc": 1661861539945,
              "nextChange": 1661861539945,
              "id": 21,
              "state": 6,
              "nextChanges": [{
                "ttc": 17689,
                "toc": 1661861539945,
                "nextChange": 1661861539945,
                "state": 6
              }]
            }, {
              "ttc": 22689,
              "toc": 1661861544945,
              "nextChange": 1661861544945,
              "id": 22,
              "state": 3,
              "nextChanges": [{
                "ttc": 22689,
                "toc": 1661861544945,
                "nextChange": 1661861544945,
                "state": 3
              }]
            }, {
              "ttc": 22689,
              "toc": 1661861544945,
              "nextChange": 1661861544945,
              "id": 23,
              "state": 3,
              "nextChanges": [{
                "ttc": 22689,
                "toc": 1661861544945,
                "nextChange": 1661861544945,
                "state": 3
              }]
            }, {
              "ttc": 689,
              "toc": 1661861522945,
              "nextChange": 1661861522945,
              "id": 24,
              "state": 6,
              "nextChanges": [{
                "ttc": 689,
                "toc": 1661861522945,
                "nextChange": 1661861522945,
                "state": 6
              }]
            }, {
              "ttc": 689,
              "toc": 1661861522945,
              "nextChange": 1661861522945,
              "id": 25,
              "state": 6,
              "nextChanges": [{
                "ttc": 689,
                "toc": 1661861522945,
                "nextChange": 1661861522945,
                "state": 6
              }]
            }, {
              "ttc": 5689,
              "toc": 1661861527945,
              "nextChange": 1661861527945,
              "id": 26,
              "state": 3,
              "nextChanges": [{
                "ttc": 5689,
                "toc": 1661861527945,
                "nextChange": 1661861527945,
                "state": 3
              }]
            }, {
              "ttc": 5689,
              "toc": 1661861527945,
              "nextChange": 1661861527945,
              "id": 27,
              "state": 3,
              "nextChanges": [{
                "ttc": 5689,
                "toc": 1661861527945,
                "nextChange": 1661861527945,
                "state": 3
              }]
            }, {
              "ttc": 689,
              "toc": 1661861522945,
              "nextChange": 1661861522945,
              "id": 28,
              "state": 6,
              "nextChanges": [{
                "ttc": 689,
                "toc": 1661861522945,
                "nextChange": 1661861522945,
                "state": 6
              }]
            }, {
              "ttc": 6689,
              "toc": 1661861528945,
              "nextChange": 1661861528945,
              "id": 29,
              "state": 3,
              "nextChanges": [{
                "ttc": 6689,
                "toc": 1661861528945,
                "nextChange": 1661861528945,
                "state": 3
              }]
            }, {
              "ttc": 58689,
              "toc": 1661861580945,
              "nextChange": 1661861580945,
              "id": 30,
              "state": 3,
              "nextChanges": [{
                "ttc": 58689,
                "toc": 1661861580945,
                "nextChange": 1661861580945,
                "state": 3
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
          }"#;

        match serde_json::from_str::<SignalPhaseAndTimingExtendedMessage>(data) {
            Ok(spat) => {
                assert_eq!(spat.id, 243);
            }
            Err(e) => {
                panic!("Failed to deserialize SPATEM: '{:?}'", e);
            }
        }
    }
}
