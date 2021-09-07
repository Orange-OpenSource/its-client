use std::str::FromStr;

use serde::{Deserialize, Serialize};

use crate::reception::mortal::Mortal;
use crate::reception::typed::Typed;
use crate::reception::Reception;

#[serde_with::skip_serializing_none]
#[derive(Clone, Debug, Default, Eq, Hash, PartialEq, Serialize, Deserialize)]
pub struct Information {
    #[serde(rename = "type")]
    pub type_field: String,
    pub instance_id: String,
    pub running: bool,
    pub timestamp: u128,
    pub validity_duration: u32,
}

impl Information {
    pub(crate) fn new() -> Self {
        Information {
            instance_id: "broker".to_string(),
            ..Default::default()
        }
    }

    pub(crate) fn instance_id_number(&self) -> u32 {
        let instance_id_split: Vec<&str> = self.instance_id.split("_").collect();
        // TODO generate a cache to not compute the same value each time again
        match instance_id_split.get(2) {
            Some(number) => u32::from_str(number).unwrap_or(31470),
            None => 31470,
        }
    }
}

impl Typed for Information {
    fn get_type() -> String {
        "info".to_string()
    }
}

impl Mortal for Information {
    fn timeout(&self) -> u128 {
        self.timestamp + self.validity_duration as u128 * 1000
    }

    fn terminate(&mut self) {
        self.validity_duration = 0
    }

    fn terminated(&self) -> bool {
        self.expired()
    }
}

impl Reception for Information {}
