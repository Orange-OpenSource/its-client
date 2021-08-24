use crate::reception::exchange::message::Message;
use crate::reception::exchange::Exchange;
use crate::reception::mortal::now;

// TODO implement the Rust macro monitor!
pub fn monitor(exchange: &Exchange, direction: &str, component: String, partner: String) {
    match &exchange.message {
        // FIXME find how to call position() on any Message implementing Mobile
        Message::CAM(message) => {
            // log to monitoring platform
            println!(
                "{} {} {} {} {}/{} at {}",
                component,
                exchange.type_field,
                direction,
                partner,
                message.station_id,
                message.generation_delta_time,
                now()
            );
        }
        Message::DENM(message) => {
            // log to monitoring platform
            println!(
                "{} {} {} {} {}/{}/{}/{}/{} at {}",
                component,
                exchange.type_field,
                direction,
                partner,
                message.station_id,
                message
                    .management_container
                    .action_id
                    .originating_station_id,
                message.management_container.action_id.sequence_number,
                message.management_container.reference_time,
                message.management_container.detection_time,
                now()
            );
        }
        Message::CPM(message) => {
            // log to monitoring platform
            println!(
                "{} {} {} {} {}/{} at {}",
                component,
                exchange.type_field,
                direction,
                partner,
                message.station_id,
                message.generation_delta_time,
                now()
            );
        }
    };
}
