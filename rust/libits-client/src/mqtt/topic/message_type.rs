use crate::mqtt::topic::parse_error::ParseError;
use std::{cmp, convert, fmt, hash, str};

#[derive(Debug, Clone)]
pub(crate) enum MessageType {
    Any,
    CAM,
    DENM,
    CPM,
    INFO,
}

impl fmt::Display for MessageType {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{}",
            match self {
                MessageType::Any => "+".to_string(),
                MessageType::CAM => "cam".to_string(),
                MessageType::DENM => "denm".to_string(),
                MessageType::CPM => "cpm".to_string(),
                MessageType::INFO => "info".to_string(),
            }
        )
    }
}

impl cmp::PartialEq for MessageType {
    fn eq(&self, other: &Self) -> bool {
        self.to_string() == other.to_string()
    }
}

impl Default for MessageType {
    fn default() -> Self {
        MessageType::Any
    }
}

impl convert::From<&str> for MessageType {
    fn from(s: &str) -> Self {
        match s {
            "+" => MessageType::Any,
            "cam" => MessageType::CAM,
            "denm" => MessageType::DENM,
            "cpm" => MessageType::CPM,
            "info" => MessageType::INFO,
            // no Result on the From trait : use FromStr trait instead
            element => panic!(
                "Unable to convert from the element {} as a MessageType, use from_str instead",
                element
            ),
        }
    }
}

impl convert::From<String> for MessageType {
    fn from(s: String) -> Self {
        MessageType::from(s.as_str())
    }
}

impl hash::Hash for MessageType {
    fn hash<H: hash::Hasher>(&self, state: &mut H) {
        self.to_string().hash(state);
    }
}

impl str::FromStr for MessageType {
    type Err = ParseError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "+" | "cam" | "denm" | "cpm" | "info" => Ok(MessageType::from(s)),
            element => Err(ParseError {
                element: element.to_string(),
            }),
        }
    }
}
