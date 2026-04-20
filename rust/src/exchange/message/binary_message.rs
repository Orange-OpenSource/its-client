use crate::exchange::Content;

use serde::{Deserialize, Serialize};

/// Represents a message in a binary format.
///
/// When `message_format` is "asn1", the message content is a base64-encoded binary payload.
/// When `message_format` is "bson", the message content is a JSON binary-encoded serialized format.
#[derive(Clone, Debug, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct BinaryMessage {
    /// Message format version of the payload.
    pub version: String,
    /// Binary encoded payload.
    pub payload: String,
}

impl Content for BinaryMessage {
    // TODO Exchange::new() uses this method to fill its `message_type`
    fn get_type(&self) -> &str {
        "binary"
    }

    fn appropriate(&mut self, _timestamp: u64, _new_station_id: u32) {
        unimplemented!("Binary payloads cannot be modified")
    }

    fn as_mobile(
        &self,
    ) -> Result<
        &dyn crate::mobility::mobile::Mobile,
        crate::exchange::message::content_error::ContentError,
    > {
        Err(crate::exchange::message::content_error::ContentError::NotAMobile("BinaryMessage"))
    }

    fn as_mortal(
        &self,
    ) -> Result<
        &dyn crate::exchange::mortal::Mortal,
        crate::exchange::message::content_error::ContentError,
    > {
        Err(crate::exchange::message::content_error::ContentError::NotAMortal("BinaryMessage"))
    }
}
