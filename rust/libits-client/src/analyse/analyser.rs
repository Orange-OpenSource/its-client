use std::sync::Arc;

use crate::analyse::configuration::Configuration;
use crate::analyse::item::Item;
use crate::reception::exchange::Exchange;

pub trait Analyser {
    fn new(configuration: Arc<Configuration>) -> Self
    where
        Self: Sized;

    fn analyze(&mut self, item: Item<Exchange>) -> Vec<Item<Exchange>>;
}
