use crate::mqtt::topic::parse_error::ParseError;
use std::{cmp, convert, fmt, hash, str};

#[derive(Debug, Clone)]
pub(crate) enum Queue {
    // To V2X server
    In,
    // From V2X server
    Out,
}

impl fmt::Display for Queue {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{}",
            match self {
                Queue::In => "inQueue".to_string(),
                Queue::Out => "outQueue".to_string(),
            }
        )
    }
}

impl Default for Queue {
    fn default() -> Self {
        Queue::In
    }
}

impl convert::From<&str> for Queue {
    fn from(s: &str) -> Self {
        match s {
            "inQueue" => Queue::In,
            "outQueue" => Queue::Out,
            // no Result on the From trait : use FromStr trait instead
            element => panic!(
                "Unable to convert from the element {} as a Queue, use from_str instead",
                element
            ),
        }
    }
}

impl convert::From<String> for Queue {
    fn from(s: String) -> Self {
        Queue::from(s.as_str())
    }
}

impl hash::Hash for Queue {
    fn hash<H: hash::Hasher>(&self, state: &mut H) {
        self.to_string().hash(state);
    }
}

impl cmp::PartialEq for Queue {
    fn eq(&self, other: &Self) -> bool {
        self.to_string() == other.to_string()
    }
}

impl str::FromStr for Queue {
    type Err = ParseError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "inQueue" | "outQueue" => Ok(Queue::from(s)),
            element => Err(ParseError {
                element: element.to_string(),
            }),
        }
    }
}
