use crate::reception::exchange::message::Message;
use crate::reception::exchange::Exchange;
use std::fmt::Formatter;

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

    pub fn from_exchange(exchange: &Exchange) -> Option<Cause> {
        return match &exchange.message {
            Message::CAM(message) => Some(Cause::new(
                exchange.type_field.clone(),
                format!("{}/{}", message.station_id, message.generation_delta_time),
            )),
            Message::CPM(message) => Some(Cause::new(
                exchange.type_field.clone(),
                format!("{}/{}", message.station_id, message.generation_delta_time),
            )),
            _ => None,
        };
    }
}
