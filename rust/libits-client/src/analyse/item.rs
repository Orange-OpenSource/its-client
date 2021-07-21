use crate::mqtt::topic::Topic;
use crate::reception::Reception;

#[derive(Clone, Debug, Eq, Hash, PartialEq)]
pub struct Item<T>
where
    T: Reception,
{
    pub topic: Topic,
    pub reception: T,
}

impl<T: Reception> Item<T> {
    pub fn new(topic: Topic, reception: T) -> Self {
        Item { topic, reception }
    }
}
