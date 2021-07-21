use crate::reception::exchange::ReferencePosition;

pub trait Mobile {
    fn mobile_id(&self) -> u32;

    fn position(&self) -> &ReferencePosition;

    fn speed(&self) -> Option<u16>;

    fn heading(&self) -> Option<u16>;

    fn stopped(&self) -> bool {
        if let Some(speed) = self.speed() {
            return speed <= 36;
        }
        false
    }
}
