use std::sync::RwLock;

use crate::mqtt::topic::geo_extension::GeoExtension;
use crate::reception::information::Information;

pub struct Configuration {
    client_id: String,
    information: RwLock<Information>,
    region_of_responsibility: bool,
    // TODO add the information's of the neighbourhood
    // TODO if you're a central node, remove from your Region Of Responsibility the Regions Of Responsibility of the neighbourhood
}

impl Configuration {
    pub fn new(client_id: String, region_of_responsibility: bool) -> Self {
        Configuration {
            client_id,
            information: RwLock::new(Information::new()),
            region_of_responsibility,
        }
    }

    pub fn gateway_component_name(&self) -> String {
        let information_guard = self.information.read().unwrap();
        // assumed clone :we don't maintain a link to the structure
        information_guard.instance_id.clone()
    }

    pub fn component_name(&self, added_number: Option<u32>) -> String {
        let information_guard = self.information.read().unwrap();
        let number = match added_number {
            Some(number) => information_guard.instance_id_number() + number,
            None => information_guard.instance_id_number() + 10000,
        };
        format!("{}_{}", self.client_id, number)
    }

    pub fn station_id(&self, added_number: Option<u32>) -> u32 {
        let information_guard = self.information.read().unwrap();
        match added_number {
            Some(number) => information_guard.instance_id_number() + number,
            None => information_guard.instance_id_number() + 10000,
        }
    }

    pub fn is_in_region_of_responsibility(&self, geo_extension: GeoExtension) -> bool {
        let information_guard = self.information.read().unwrap();
        //check broker (own) node
        !self.region_of_responsibility
            || information_guard.is_in_region_of_responsibility(geo_extension)
    }

    pub fn update(&self, new_information: Information) {
        let mut information_guard = self.information.write().unwrap();
        *information_guard = new_information;
    }
}
