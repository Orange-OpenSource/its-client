use std::{error, fmt};

/// An error which can be returned when parsing a Topic string.
#[derive(Debug)]
pub struct ParseError {
    pub element: String,
}

impl fmt::Display for ParseError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "Unable to parse the element {} as a Topic part",
            self.element
        )
    }
}

impl error::Error for ParseError {}
