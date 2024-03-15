// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2022 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas Buffon <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use log::warn;
use std::any::type_name;

use crate::exchange::etsi::reference_position::ReferencePosition;
use crate::exchange::message::content::Content;
use crate::exchange::message::content_error::ContentError;
use crate::exchange::message::content_error::ContentError::{NotAMobile, NotAMortal};
use crate::exchange::mortal::Mortal;
use crate::mobility::mobile::Mobile;
use crate::mobility::position::{distance_to_line, position_from_degrees, Position};
use serde::{Deserialize, Serialize};
use serde_repr::Deserialize_repr;
use std::hash::{Hash, Hasher};

/// MAPEM representation
///
/// **MAP** (topology) **E**xtended **M**essage
///
/// **See also:**
/// - [SignalPhaseAndTimingExtendedMessage][1]
///
/// [1]: crate::exchange::etsi::signal_phase_and_timing_extended_message
#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub struct MAPExtendedMessage {
    pub protocol_version: u16,
    pub id: u64,
    /// Reference time of the geometry present in the message
    pub timestamp: Option<u64>,
    pub sending_station_id: Option<u64>,
    pub region: Option<u64>,
    pub revision: Option<u16>,
    /// List of the lanes in the intersection
    pub lanes: Vec<Lane>,
}

impl Content for MAPExtendedMessage {
    fn get_type(&self) -> &str {
        "mapem"
    }

    /// TODO implement this (issue [#96](https://github.com/Orange-OpenSource/its-client/issues/96))
    fn appropriate(&mut self) {
        todo!()
    }

    fn as_mobile(&self) -> Result<&dyn Mobile, ContentError> {
        Err(NotAMobile(type_name::<MAPExtendedMessage>()))
    }

    fn as_mortal(&self) -> Result<&dyn Mortal, ContentError> {
        Err(NotAMortal(type_name::<MAPExtendedMessage>()))
    }
}

impl PartialEq<Self> for MAPExtendedMessage {
    fn eq(&self, other: &Self) -> bool {
        self.id.eq(&other.id) && self.timestamp.eq(&other.timestamp)
    }
}

impl Hash for MAPExtendedMessage {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.id.hash(state);
        self.timestamp.hash(state);
    }
}

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub struct Lane {
    pub id: u64,
    /// ID of the signal, corresponding to the [SPAT][1] signal ID
    ///
    /// [1]: crate::exchange::etsi::signal_phase_and_timing_extended_message
    pub signal_id: u64,
    /// ID of the approach (group of lanes)
    pub approach_id: Option<u32>,
    /// True if the lane allows a left turn at its end
    pub left: bool,
    /// True if the lane allows to go straight ahead at its end
    ///
    /// **Does not seem to be present in some MAPEM despite the documentation telling it is
    ///   mandatory; Defaulting value ([as false][1])**
    ///
    /// FIXME defaulting to false if missing does not fit reality, replace it by an Option
    ///
    /// [1]: https://doc.rust-lang.org/std/primitive.bool.html#impl-Default-for-bool
    #[serde(default)]
    pub straight: bool,
    /// True if this lane allows a right turn at its end
    pub right: bool,
    /// Speed limit on this lane (km/h)
    pub speed_limit: u16,
    pub ingress: bool,
    pub egress: bool,
    /// List of points representing the lane
    ///
    /// Each point is an array of numbers where the first value is the longitude
    /// and the second value is the latitude
    ///
    /// *Note: latitude and longitude refers to the [WGS84][1]*
    ///
    /// [1]: https://en.wikipedia.org/wiki/World_Geodetic_System#WGS84
    pub geom: Vec<[f32; 2]>,
    pub is_pedestrian_lane: Option<bool>,
    pub is_vehicle_lane: Option<bool>,
    pub is_bus_lane: Option<bool>,
    pub is_bike_lane: Option<bool>,
    #[serde(default)]
    pub connections: Vec<Connection>,
}

impl Hash for Lane {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.id.hash(state);
        self.signal_id.hash(state);
        self.approach_id.hash(state);
    }
}

#[derive(Serialize, Deserialize_repr, PartialEq, Eq, Debug, Clone)]
#[repr(u8)]
pub enum Action {
    Left = 0,
    Straight = 1,
    Right = 2,
}

/// Represents a connection between lanes
#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub struct Connection {
    pub id: Option<u64>,
    /// `intersection_id` of the connected lane
    /// Value is 0 if the connected lane is in the same intersection
    pub intersection_id: u64,
    /// `lane_id` of the connected lane
    /// Value is 0 if the connected lane is unknown, but the action is still possible
    pub lane_id: u64,
    /// Turn direction leading to the connected lane
    pub action: Action,
    /// True if taking this pas has to be executed with caution
    /// (e.g. pedestrian crossing on a right or left turn)
    pub caution: Option<bool>,
}

impl MAPExtendedMessage {
    /// Lookup the existing lanes to find the one the position is in or close to
    ///
    /// FIXME this requires unit tests and consolidation
    pub fn get_lane_from_position(&self, reference_position: &ReferencePosition) -> Option<&Lane> {
        let mut best_lane: Option<(&Lane, f64)> = None;

        let reference_position = reference_position.as_position();

        for lane in &self.lanes {
            match &lane.geom {
                geometry if geometry.len() > 1 => {
                    let mut positions: Vec<Position> = Vec::new();
                    for point in geometry {
                        positions.push(position_from_degrees(point[0].into(), point[1].into(), 0.));
                    }

                    let distance_to_lane = distance_to_line(&reference_position, &positions);
                    if best_lane.is_none() || best_lane.unwrap().1 > distance_to_lane {
                        best_lane = Some((lane, distance_to_lane));
                    }
                }
                _ => warn!(
                    "Found a lane's geometry with one or no coordinates, excluding it for now..."
                ),
            }
        }

        best_lane.map(|lane| lane.0)
    }
}

#[cfg(test)]
mod test {
    use crate::exchange::etsi::map_extended_message::{Action, MAPExtendedMessage};

    #[test]
    fn test_complete_deserialization() {
        let data = r#"
        {
            "protocolVersion": 1,
            "id": 10,
            "timestamp": 123456789,
            "sendingStationId": 11,
            "region": 12,
            "revision": 13,
            "lanes":
            [
                {
                    "id": 14,
                    "signalId": 15,
                    "approachId": 16,
                    "left": true,
                    "straight": true,
                    "right": false,
                    "speedLimit": 50,
                    "ingress": false,
                    "egress": false,
                    "geom":
                    [
                        [11.1, 2.2],
                        [33.3, 4.4],
                        [55.5, 6.6]
                    ],
                    "isVehicleLane": true,
                    "isBusLane": false,
                    "isBikeLane": false,
                    "connections":
                    [
                        {
                            "intersectionId": 17,
                            "laneId": 18,
                            "action": 0,
                            "id": 19,
                            "caution": true
                        },
                        {
                            "intersectionId": 20,
                            "laneId": 21,
                            "action": 1,
                            "id": 22,
                            "caution": false
                        }
                    ]
                }
            ]
        }
        "#;

        match serde_json::from_str::<MAPExtendedMessage>(data) {
            Ok(map) => {
                assert_eq!(map.id, 10);
                assert_eq!(map.protocol_version, 1);
                assert_eq!(map.timestamp.unwrap(), 123456789);
                assert_eq!(map.sending_station_id.unwrap(), 11);
                assert_eq!(map.region.unwrap(), 12);
                assert_eq!(map.revision.unwrap(), 13);
                assert_eq!(map.lanes.len(), 1);
                let lane = map.lanes.first().unwrap();
                assert_eq!(lane.id, 14);
                assert_eq!(lane.signal_id, 15);
                assert_eq!(lane.approach_id.unwrap(), 16);
                assert!(lane.left);
                assert!(lane.straight);
                assert!(!lane.right);
                assert_eq!(lane.speed_limit, 50);
                assert!(!lane.ingress);
                assert!(!lane.egress);
                assert_eq!(lane.geom.len(), 3);
                assert_eq!(lane.geom[0][0], 11.1);
                assert_eq!(lane.geom[0][1], 2.2);
                assert_eq!(lane.geom[1][0], 33.3);
                assert_eq!(lane.geom[1][1], 4.4);
                assert_eq!(lane.geom[2][0], 55.5);
                assert_eq!(lane.geom[2][1], 6.6);
                assert!(lane.is_vehicle_lane.unwrap());
                assert!(!lane.is_bus_lane.unwrap());
                assert!(!lane.is_bike_lane.unwrap());
                assert_eq!(lane.connections.len(), 2);
                let connection_one = lane.connections.first().unwrap();
                assert_eq!(connection_one.intersection_id, 17);
                assert_eq!(connection_one.lane_id, 18);
                assert_eq!(connection_one.action, Action::Left);
                assert_eq!(connection_one.id.unwrap(), 19);
                assert!(connection_one.caution.unwrap());
                let connection_two = lane.connections.last().unwrap();
                assert_eq!(connection_two.intersection_id, 20);
                assert_eq!(connection_two.lane_id, 21);
                assert_eq!(connection_two.action, Action::Straight);
                assert_eq!(connection_two.id.unwrap(), 22);
                assert!(!connection_two.caution.unwrap());
            }
            Err(e) => {
                panic!("Failed to deserialize MAPEM from JSON: '{}'", e);
            }
        }
    }

    #[test]
    fn test_optional_fields() {
        let data = r#"
        {
            "protocolVersion": 1,
            "id": 10,
            "lanes":
            [
                {
                    "id": 14,
                    "signalId": 15,
                    "left": true,
                    "straight": true,
                    "right": false,
                    "speedLimit": 50,
                    "ingress": false,
                    "egress": false,
                    "geom":
                    [
                        [11.1, 2.2],
                        [33.3, 4.4],
                        [55.5, 6.6]
                    ],
                    "connections":
                    [
                        {
                            "intersectionId": 17,
                            "laneId": 18,
                            "action": 0
                        },
                        {
                            "intersectionId": 20,
                            "laneId": 21,
                            "action": 1
                        }
                    ]
                },
                {
                    "id": 22,
                    "signalId": 23,
                    "left": true,
                    "straight": true,
                    "right": false,
                    "speedLimit": 50,
                    "ingress": false,
                    "egress": false,
                    "geom":
                    [
                        [11.1, 2.2],
                        [33.3, 4.4],
                        [55.5, 6.6]
                    ]
                }
            ]
        }
        "#;

        match serde_json::from_str::<MAPExtendedMessage>(data) {
            Ok(map) => {
                assert_eq!(map.id, 10);
                assert_eq!(map.protocol_version, 1);
                assert!(map.timestamp.is_none());
                assert!(map.region.is_none());
                assert!(map.revision.is_none());
                assert!(map.sending_station_id.is_none());
                assert_eq!(map.lanes.len(), 2);
                let lane = map.lanes.first().unwrap();
                assert_eq!(lane.id, 14);
                assert_eq!(lane.signal_id, 15);
                assert!(lane.approach_id.is_none());
                assert!(lane.left);
                assert!(lane.straight);
                assert!(!lane.right);
                assert_eq!(lane.speed_limit, 50);
                assert!(!lane.ingress);
                assert!(!lane.egress);
                assert_eq!(lane.geom.len(), 3);
                assert_eq!(lane.geom[0][0], 11.1);
                assert_eq!(lane.geom[0][1], 2.2);
                assert_eq!(lane.geom[1][0], 33.3);
                assert_eq!(lane.geom[1][1], 4.4);
                assert_eq!(lane.geom[2][0], 55.5);
                assert_eq!(lane.geom[2][1], 6.6);
                assert!(lane.is_vehicle_lane.is_none());
                assert!(lane.is_bus_lane.is_none());
                assert!(lane.is_bike_lane.is_none());
                assert_eq!(lane.connections.len(), 2);
                let connection_one = lane.connections.first().unwrap();
                assert_eq!(connection_one.intersection_id, 17);
                assert_eq!(connection_one.lane_id, 18);
                assert_eq!(connection_one.action, Action::Left);
                assert!(connection_one.id.is_none());
                assert!(connection_one.caution.is_none());
                let connection_two = lane.connections.last().unwrap();
                assert_eq!(connection_two.intersection_id, 20);
                assert_eq!(connection_two.lane_id, 21);
                assert_eq!(connection_two.action, Action::Straight);
                assert!(connection_one.id.is_none());
                assert!(connection_one.caution.is_none());
                let lane2 = map.lanes.last().unwrap();
                assert_eq!(lane2.id, 22);
                assert_eq!(lane2.signal_id, 23);
                assert!(lane2.approach_id.is_none());
                assert!(lane2.left);
                assert!(lane2.straight);
                assert!(!lane2.right);
                assert_eq!(lane2.speed_limit, 50);
                assert!(!lane2.ingress);
                assert!(!lane2.egress);
                assert_eq!(lane2.geom.len(), 3);
                assert_eq!(lane2.geom[0][0], 11.1);
                assert_eq!(lane2.geom[0][1], 2.2);
                assert_eq!(lane2.geom[1][0], 33.3);
                assert_eq!(lane2.geom[1][1], 4.4);
                assert_eq!(lane2.geom[2][0], 55.5);
                assert_eq!(lane2.geom[2][1], 6.6);
                assert!(lane2.is_vehicle_lane.is_none());
                assert!(lane2.is_bus_lane.is_none());
                assert!(lane2.is_bike_lane.is_none());
                assert_eq!(lane2.connections.len(), 0);
            }
            Err(e) => {
                panic!("Failed to deserialize MAPEM from JSON: '{}'", e);
            }
        }
    }

    #[test]
    fn test_real_map_extended_message() {
        let data = r#"{
            "id": 243,
            "lanes": [{
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 3
                }, {
                    "action": 2,
                    "intersectionId": 243,
                    "laneId": 23
                }],
                "egress": false,
                "geom": [
                    [2.37637806826, 48.8390563094],
                    [2.37688503009, 48.8393363062],
                    [2.37699197717, 48.839368933],
                    [2.37868368303, 48.840288106]
                ],
                "id": 1,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": true,
                "signalId": 18,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 4
                }],
                "egress": false,
                "geom": [
                    [2.37639955026, 48.8390335466],
                    [2.37690916997, 48.8393160046],
                    [2.37868377467, 48.8402841536]
                ],
                "id": 2,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 18,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 478,
                    "laneId": 1
                }, {
                    "action": 1,
                    "intersectionId": 478,
                    "laneId": 2
                }],
                "egress": false,
                "geom": [
                    [2.37578497727, 48.8387570374],
                    [2.37585605581, 48.8388038198],
                    [2.37603576381, 48.8388973847],
                    [2.37631507084, 48.8390196778]
                ],
                "id": 3,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 3,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 478,
                    "laneId": 2
                }, {
                    "action": 1,
                    "intersectionId": 478,
                    "laneId": 3
                }],
                "egress": false,
                "geom": [
                    [2.37582655151, 48.8387464451],
                    [2.37607331474, 48.8388735521],
                    [2.37632982299, 48.8390029068]
                ],
                "id": 4,
                "ingress": true,
                "isVehicleLane": false,
                "isPedestrianLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 3,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 9
                }, {
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 7
                }],
                "egress": false,
                "geom": [
                    [2.37558122849, 48.8388314062],
                    [2.37543638921, 48.838955865],
                    [2.37493615722, 48.8393936749],
                    [2.37458076453, 48.8397255605]
                ],
                "id": 5,
                "ingress": true,
                "isVehicleLane": true,
                "left": true,
                "nextChange": 0,
                "right": false,
                "signalId": 1,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 8
                }, {
                    "action": 2,
                    "intersectionId": 478,
                    "laneId": 1
                }, {
                    "action": 2,
                    "intersectionId": 478,
                    "laneId": 2
                }],
                "egress": false,
                "geom": [
                    [2.37551613847, 48.8388372148],
                    [2.37492202917, 48.839370355],
                    [2.37455993096, 48.8397172462]
                ],
                "id": 6,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": true,
                "signalId": 1,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 23
                }, {
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 22
                }],
                "egress": false,
                "geom": [
                    [2.37591712872, 48.838937841],
                    [2.3758527557, 48.8388813491],
                    [2.37577765385, 48.8388319187],
                    [2.37568243543, 48.83880279]
                ],
                "id": 7,
                "ingress": true,
                "isVehicleLane": true,
                "left": true,
                "nextChange": 0,
                "right": false,
                "signalId": 21,
                "speedLimit": 50,
                "state": 0,
                "straight": false,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 25
                }],
                "egress": false,
                "geom": [
                    [2.37586312126, 48.838571306],
                    [2.37580008935, 48.8386163233]
                ],
                "id": 8,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 7,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 13
                }, {
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 14
                }],
                "egress": false,
                "geom": [
                    [2.37596495285, 48.8386145236],
                    [2.375889851, 48.8386683677]
                ],
                "id": 9,
                "ingress": true,
                "isVehicleLane": true,
                "left": true,
                "nextChange": 0,
                "right": false,
                "signalId": 7,
                "speedLimit": 50,
                "state": 0,
                "straight": false,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 13
                }],
                "egress": false,
                "geom": [
                    [2.37573303412, 48.8384336059],
                    [2.37562038134, 48.83839565],
                    [2.37484656404, 48.837979899],
                    [2.37434633206, 48.8377071432],
                    [2.37402742449, 48.8375315839]
                ],
                "id": 10,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 5,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 14
                }, {
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 15
                }],
                "egress": false,
                "geom": [
                    [2.3757641693, 48.8384126331],
                    [2.37562737664, 48.8383587887],
                    [2.37438703743, 48.8376853595],
                    [2.37406019701, 48.8374982236]
                ],
                "id": 11,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 5,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 2,
                    "intersectionId": 243,
                    "laneId": 25
                }, {
                    "action": 2,
                    "intersectionId": 243,
                    "laneId": 24
                }],
                "egress": false,
                "geom": [
                    [2.37579385606, 48.8383888724],
                    [2.37565169898, 48.8383306145],
                    [2.37442190615, 48.8376624091],
                    [2.37409506573, 48.8374682115]
                ],
                "id": 12,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": true,
                "signalId": 5,
                "speedLimit": 50,
                "state": 0,
                "straight": false,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 20
                }],
                "egress": false,
                "geom": [
                    [2.37630683272, 48.838713871],
                    [2.37601715415, 48.8385805847]
                ],
                "id": 13,
                "ingress": true,
                "isVehicleLane": true,
                "left": true,
                "nextChange": 0,
                "right": false,
                "signalId": 11,
                "speedLimit": 50,
                "state": 0,
                "straight": false,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 21
                }, {
                    "action": 1,
                    "intersectionId": 664,
                    "laneId": 18
                }, {
                    "action": 1,
                    "intersectionId": 664,
                    "laneId": 19
                }, {
                    "action": 1,
                    "intersectionId": 664,
                    "laneId": 17
                }],
                "egress": false,
                "geom": [
                    [2.37632694929, 48.8386865076],
                    [2.37604665844, 48.8385541039]
                ],
                "id": 14,
                "ingress": true,
                "isVehicleLane": true,
                "left": true,
                "nextChange": 0,
                "right": false,
                "signalId": 11,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 664,
                    "laneId": 19
                }, {
                    "action": 1,
                    "intersectionId": 664,
                    "laneId": 18
                }],
                "egress": false,
                "geom": [
                    [2.37634840696, 48.8386626749],
                    [2.37607884495, 48.8385320366]
                ],
                "id": 15,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 11,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 2,
                    "intersectionId": 664,
                    "laneId": 19
                }, {
                    "action": 2,
                    "intersectionId": 664,
                    "laneId": 18
                }],
                "egress": false,
                "geom": [
                    [2.37653213828, 48.8386291327],
                    [2.37665551989, 48.8384208172],
                    [2.37706992118, 48.8380818614]
                ],
                "id": 16,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": true,
                "signalId": 9,
                "speedLimit": 50,
                "state": 0,
                "straight": false,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 21
                }],
                "egress": false,
                "geom": [
                    [2.37648802704, 48.838627208],
                    [2.37664091295, 48.8384153618],
                    [2.37705933756, 48.8380808194]
                ],
                "id": 17,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 9,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 20
                }],
                "egress": false,
                "geom": [
                    [2.37643706506, 48.838615954],
                    [2.37661543196, 48.8384014596],
                    [2.37702044552, 48.8380775095]
                ],
                "id": 18,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 9,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 24
                }, {
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 25
                }],
                "egress": false,
                "geom": [
                    [2.37614872759, 48.8384402981],
                    [2.37621310061, 48.8384976732],
                    [2.37631100124, 48.8385347463],
                    [2.37643035954, 48.8385550482]
                ],
                "id": 19,
                "ingress": true,
                "isVehicleLane": true,
                "left": true,
                "nextChange": 0,
                "right": false,
                "signalId": 20,
                "speedLimit": 50,
                "state": 0,
                "straight": false,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 4
                }, {
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 3
                }],
                "egress": false,
                "geom": [
                    [2.37632307118, 48.8388127937],
                    [2.37636598653, 48.8387660112]
                ],
                "id": 20,
                "ingress": true,
                "isVehicleLane": true,
                "left": true,
                "nextChange": 0,
                "right": false,
                "signalId": 15,
                "speedLimit": 50,
                "state": 0,
                "straight": false,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 23
                }, {
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 22
                }],
                "egress": false,
                "geom": [
                    [2.37638610309, 48.8388428051],
                    [2.3764182896, 48.8387880784]
                ],
                "id": 21,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 15,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "egress": true,
                "geom": [
                    [2.37588473425, 48.8389893805],
                    [2.37559002896, 48.8392122546]
                ],
                "id": 22,
                "ingress": false,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 0,
                "speedLimit": 0,
                "state": 0,
                "straight": false,
                "toc": 0
            }, {
                "approachId": 0,
                "egress": true,
                "geom": [
                    [2.37591725846, 48.8390065896],
                    [2.37564367314, 48.8392299082]
                ],
                "id": 23,
                "ingress": false,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 0,
                "speedLimit": 0,
                "state": 0,
                "straight": false,
                "toc": 0
            }, {
                "approachId": 0,
                "egress": true,
                "geom": [
                    [2.37606090593, 48.8384318606],
                    [2.37662953425, 48.8379084201]
                ],
                "id": 24,
                "ingress": false,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 0,
                "speedLimit": 0,
                "state": 0,
                "straight": false,
                "toc": 0
            }, {
                "approachId": 0,
                "egress": true,
                "geom": [
                    [2.37601799059, 48.8384089106],
                    [2.37662685204, 48.8379004758]
                ],
                "id": 25,
                "ingress": false,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 0,
                "speedLimit": 0,
                "state": 0,
                "straight": false,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 24
                }],
                "egress": false,
                "geom": [
                    [2.3758911921, 48.8385827467],
                    [2.37581743135, 48.8386339428]
                ],
                "id": 26,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 7,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 24
                }, {
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 15
                }, {
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 14
                }],
                "egress": false,
                "geom": [
                    [2.37593678966, 48.8386004005],
                    [2.37585766449, 48.8386463005]
                ],
                "id": 27,
                "ingress": true,
                "isVehicleLane": true,
                "left": true,
                "nextChange": 0,
                "right": false,
                "signalId": 7,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 1,
                    "intersectionId": 243,
                    "laneId": 29
                }],
                "egress": false,
                "geom": [
                    [2.37642654158, 48.839012331],
                    [2.37871921826, 48.8402696812]
                ],
                "id": 28,
                "ingress": true,
                "isVehicleLane": true,
                "left": false,
                "nextChange": 0,
                "right": false,
                "signalId": 18,
                "speedLimit": 50,
                "state": 0,
                "straight": true,
                "toc": 0
            }, {
                "approachId": 0,
                "connections": [{
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 9
                }, {
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 27
                }, {
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 26
                }, {
                    "action": 0,
                    "intersectionId": 243,
                    "laneId": 8
                }],
                "egress": false,
                "geom": [
                    [2.37586220728, 48.8387201307],
                    [2.37635573374, 48.8389822887]
                ],
                "id": 29,
                "ingress": true,
                "isVehicleLane": true,
                "left": true,
                "nextChange": 0,
                "right": false,
                "signalId": 3,
                "speedLimit": 50,
                "state": 0,
                "straight": false,
                "toc": 0
            }],
            "protocolVersion": 1,
            "region": 751,
            "revision": 11,
            "sendingStationId": 75000
        }"#;

        match serde_json::from_str::<MAPExtendedMessage>(data) {
            Ok(map) => {
                assert_eq!(243, map.id);
            }
            Err(e) => {
                panic!("Failed to deserialize MAPEM: '{}'", e);
            }
        }
    }
}
