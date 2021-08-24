use std::time::{SystemTime, UNIX_EPOCH};
// use log::{debug};

pub trait Mortal {
    fn timeout(&self) -> u128;

    fn expired(&self) -> bool {
        // debug!("we check if {} greater than {}", now() , self.timeout());
        now() > self.timeout()
    }

    fn terminate(&mut self);

    fn terminated(&self) -> bool;

    fn remaining_time(&self) -> u128 {
        return (self.timeout() - now()) / 1000;
    }
}

pub fn now() -> u128 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_millis()
}

/// Unit: millisecond since ETSI epoch (2004/01/01, so 1072915195000).
/// Time at which a new DENM, an update DENM or a cancellation DENM is generated.
/// utcStartOf2004(0), oneMillisecAfterUTCStartOf2004(1)
pub(crate) fn etsi_now() -> u128 {
    etsi_timestamp(now())
}

pub(crate) fn timestamp(etsi_timestamp: u128) -> u128 {
    etsi_timestamp + 1072915195000
}

/// Unit: millisecond since ETSI epoch (2004/01/01, so 1072915195000).
/// Time at which a new DENM, an update DENM or a cancellation DENM is generated.
/// utcStartOf2004(0), oneMillisecAfterUTCStartOf2004(1)
pub(crate) fn etsi_timestamp(timestamp: u128) -> u128 {
    timestamp - 1072915195000
}
