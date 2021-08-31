use serde::{Deserialize, Serialize};

use crate::analyse::configuration::Configuration;
use crate::reception::exchange::collective_perception_message::CollectivePerceptionMessage;
use crate::reception::exchange::cooperative_awareness_message::CooperativeAwarenessMessage;
use crate::reception::exchange::decentralized_environmental_notification_message::DecentralizedEnvironmentalNotificationMessage;
use crate::reception::exchange::mobile::Mobile;
use crate::reception::exchange::ReferencePosition;
use crate::reception::mortal::{etsi_timestamp, Mortal};
use crate::reception::typed::Typed;

#[derive(Clone, Debug, Hash, PartialEq, Serialize, Deserialize)]
#[serde(untagged)]
pub enum Message {
    ///
    CAM(CooperativeAwarenessMessage),
    ///
    DENM(DecentralizedEnvironmentalNotificationMessage),
    ///
    CPM(CollectivePerceptionMessage),
}

impl Message {
    pub fn get_type(&self) -> String {
        match self {
            // FIXME find how to call get_type() on any Message
            Message::CAM(_) => CooperativeAwarenessMessage::get_type(),
            Message::DENM(_) => DecentralizedEnvironmentalNotificationMessage::get_type(),
            Message::CPM(_) => CollectivePerceptionMessage::get_type(),
        }
    }

    pub fn appropriate(&mut self, configuration: &Configuration, timestamp: u128) -> u32 {
        match self {
            // FIXME find how to change the fileds on any Message
            Message::CAM(ref mut message) => {
                let station_id = configuration.station_id(Some(message.station_id));
                message.station_id = station_id;
                // TODO update the generation delta time
                station_id
            }
            Message::DENM(ref mut message) => {
                let station_id = configuration.station_id(Some(message.station_id));
                message.station_id = station_id;
                // FIXME find why the serde Serializer can't match the u128
                message.management_container.reference_time = etsi_timestamp(timestamp) as u64;
                station_id
            }
            Message::CPM(ref mut message) => {
                let station_id = configuration.station_id(Some(message.station_id));
                message.station_id = station_id;
                // TODO update the generation delta time
                station_id
            }
        }
    }
}

impl Mortal for Message {
    fn timeout(&self) -> u128 {
        if let Message::DENM(message) = self {
            return message.timeout();
        }
        // TODO implement a timeout on the cam and cpm
        0
    }

    fn terminate(&mut self) {
        if let Message::DENM(message) = self {
            message.terminate();
        }
        // TODO implement a terminate on the cam and cpm
    }

    fn terminated(&self) -> bool {
        if let Message::DENM(message) = self {
            return message.terminated();
        }
        // TODO implement a timeout on the cam and cpm
        false
    }
}

impl Mobile for Message {
    fn mobile_id(&self) -> u32 {
        match self {
            // FIXME find how to call mobile_id() on any Message implementing Mobile
            Message::CAM(message) => message.mobile_id(),
            Message::DENM(message) => message.mobile_id(),
            Message::CPM(message) => message.mobile_id(),
        }
    }

    fn position(&self) -> &ReferencePosition {
        match self {
            // FIXME find how to call position() on any Message implementing Mobile
            Message::CAM(message) => message.position(),
            Message::DENM(message) => message.position(),
            Message::CPM(message) => message.position(),
        }
    }

    fn speed(&self) -> Option<u16> {
        match self {
            // FIXME find how to call speed() on any Message implementing Mobile
            Message::CAM(message) => message.speed(),
            Message::DENM(message) => message.speed(),
            Message::CPM(message) => message.speed(),
        }
    }

    fn heading(&self) -> Option<u16> {
        match self {
            // FIXME find how to call speed() on any Message implementing Mobile
            Message::CAM(message) => message.heading(),
            Message::DENM(message) => message.heading(),
            Message::CPM(message) => message.heading(),
        }
    }

    fn stopped(&self) -> bool {
        match self {
            // FIXME find how to call stopped() on any Message implementing Mobile
            Message::CAM(message) => message.stopped(),
            Message::DENM(message) => message.stopped(),
            Message::CPM(message) => message.stopped(),
        }
    }
}
