// Software Name: its-client
// SPDX-FileCopyrightText: Copyright (c) 2016-2023 Orange
// SPDX-License-Identifier: MIT License
//
// This software is distributed under the MIT license, see LICENSE.txt file for more details.
//
// Author: Nicolas BUFFON <nicolas.buffon@orange.com> et al.
// Software description: This Intelligent Transportation Systems (ITS) [MQTT](https://mqtt.org/) client based on the [JSon](https://www.json.org) [ETSI](https://www.etsi.org/committee/its) specification transcription provides a ready to connect project for the mobility (connected and autonomous vehicles, road side units, vulnerable road users,...).

use crate::client::configuration::configuration_error::ConfigurationError;
use crate::client::configuration::{get_mandatory_field, get_optional_from_section, NODE_SECTION};
use crate::exchange::message::information::Information;
use crate::mobility::quadtree;
use crate::mobility::quadtree::quadkey::Quadkey;
use crate::mobility::quadtree::Quadtree;
use ini::Properties;
use log::{error, info, warn};
use std::str::FromStr;

/// Configuration of the node the client is hosted on
///
/// This is the case for backend running application that would consume and/or produce messages
/// from the broker
/// Clients running in on-board units or road-side units might not use this
///
/// TODO add the information's of the neighbourhood
//  TODO if you're a central node, remove from your Region Of Responsibility the Regions Of Responsibility of the neighbourhood
#[derive(Default)]
pub struct NodeConfiguration {
    pub responsibility_enabled: bool,
    pub thread_count: Option<usize>,
    gateway_component_name: String,
    instance_id: u32,
    region_of_responsibility: Quadtree,
}

impl NodeConfiguration {
    pub fn gateway_component_name(&self) -> Option<&str> {
        if self.gateway_component_name.is_empty() {
            None
        } else {
            Some(&self.gateway_component_name)
        }
    }

    pub fn station_id(&self, modifier: Option<u32>) -> u32 {
        if let Some(modifier) = modifier {
            self.instance_id + modifier
        } else {
            self.instance_id + 10_000
        }
    }

    pub fn is_in_region_of_responsibility(&self, quadkey: &Quadkey) -> bool {
        !self.responsibility_enabled || quadtree::contains(&self.region_of_responsibility, quadkey)
    }

    pub fn update(&mut self, information: Information) {
        info!("Updating node configuration...");
        self.gateway_component_name = information.instance_id;
        self.instance_id = Self::extract_instance_id(&self.gateway_component_name);

        match information.service_area {
            Some(area) => {
                self.region_of_responsibility.clear();
                for key in area.quadkeys {
                    match Quadkey::from_str(key.as_str()) {
                        Ok(quadkey) => self.region_of_responsibility.push(quadkey),
                        Err(e) => warn!("Failed to parse '{}' as a quadkey: {}", key, e),
                    }
                }

                if self.region_of_responsibility.is_empty() && self.responsibility_enabled {
                    info!("RoR is enabled but region of responsibility is empty");
                }
            }
            None => {
                if self.responsibility_enabled {
                    info!("RoR is enabled but no service area found in information message");
                }
            }
        }

        info!("Node configuration updated!");
    }

    fn extract_instance_id(gw_component_name: &String) -> u32 {
        match gw_component_name.split('_').collect::<Vec<&str>>().last() {
            Some(id_as_str) => id_as_str.parse::<u32>().unwrap_or_else(|e| {
                warn!("Failed to parse id ({}) as u32: {}", id_as_str, e);
                0
            }),
            None => {
                error!(
                    "Gateway component name is empty or is malformed ({})",
                    gw_component_name
                );
                0
            }
        }
    }
}

impl TryFrom<&Properties> for NodeConfiguration {
    type Error = ConfigurationError;

    fn try_from(_properties: &Properties) -> Result<Self, Self::Error> {
        let section = (NODE_SECTION, _properties);

        let mut thread_count = None;
        match get_optional_from_section::<usize>("thread_count", _properties) {
            Ok(count) => thread_count = count,
            Err(e) => info!("Could not read thread_count: {}", e),
        }

        let s = Self {
            responsibility_enabled: get_mandatory_field::<bool>("responsibility_enabled", section)?,
            thread_count,
            ..Default::default()
        };

        Ok(s)
    }
}
