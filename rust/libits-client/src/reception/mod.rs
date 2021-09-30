pub mod exchange;
pub(crate) mod information;
pub mod mortal;
pub(crate) mod typed;

use crate::reception::mortal::Mortal;
use std::fmt::Debug;

pub trait Reception: Clone + Debug + PartialEq + Mortal {}
