use std::fmt::Formatter;
use std::sync::Mutex;

#[derive(Default)]
pub struct SequenceNumber {
    current: Mutex<u16>,
}

impl SequenceNumber {
    #[allow(dead_code)]
    pub fn new() -> Self {
        Self {
            current: Mutex::new(0),
        }
    }

    #[allow(dead_code)]
    pub fn get_next(&mut self) -> u16 {
        let mut write_lock = self.current.lock().unwrap();
        *write_lock = (*write_lock + 1) % 65535;
        *write_lock
    }
}

impl std::fmt::Display for SequenceNumber {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", *self.current.lock().unwrap())
    }
}

#[cfg(test)]
mod tests {
    use crate::reception::exchange::sequence_number::SequenceNumber;

    #[test]
    fn next_sequence_number_modulo() {
        let mut sequence_number = SequenceNumber::new();
        for _i in 0..65534 {
            sequence_number.get_next();
        }
        assert_eq!(sequence_number.get_next(), 0);
    }
}
