use crate::transport::mqtt::topic::Topic;
use std::fmt::{Display, Formatter};
use std::str::FromStr;

#[derive(Clone, Default, Debug, Hash, PartialEq, Eq)]
pub struct StrTopic {
    topic: String,
}
impl Display for StrTopic {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.topic)
    }
}
impl FromStr for StrTopic {
    type Err = std::str::Utf8Error;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        Ok(StrTopic {
            topic: String::from(s),
        })
    }
}
impl Topic for StrTopic {
    fn as_route(&self) -> String {
        //assume the topic is the route
        //assumed clone is cheap
        self.topic.clone()
    }
}
