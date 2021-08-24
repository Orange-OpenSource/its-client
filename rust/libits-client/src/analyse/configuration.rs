use std::sync::RwLock;

use crate::reception::information::Information;

pub struct Configuration {
    client_id: String,
    information: RwLock<Information>,
}

impl Configuration {
    pub fn new(client_id: String) -> Self {
        Configuration {
            client_id,
            information: RwLock::new(Information::new()),
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

    pub fn update(&self, new_information: Information) {
        let mut information_guard = self.information.write().unwrap();
        *information_guard = new_information;
    }
}
