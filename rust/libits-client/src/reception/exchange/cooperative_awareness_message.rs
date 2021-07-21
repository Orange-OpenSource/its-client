use serde::{Deserialize, Serialize};

use crate::reception::exchange::mobile::Mobile;
use crate::reception::exchange::{PathHistory, PositionConfidence, ReferencePosition};
use crate::reception::typed::Typed;

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct CooperativeAwarenessMessage {
    pub protocol_version: u8,
    pub station_id: u32,
    pub generation_delta_time: u16,
    pub basic_container: BasicContainer,
    pub high_frequency_container: HighFrequencyContainer,
    pub low_frequency_container: Option<LowFrequencyContainer>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct BasicContainer {
    pub station_type: Option<u8>,
    pub reference_position: ReferencePosition,
    pub confidence: Option<PositionConfidence>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct HighFrequencyContainer {
    pub heading: Option<u16>,
    pub speed: Option<u16>,
    pub drive_direction: Option<u8>,
    pub vehicle_length: Option<u16>,
    pub vehicle_width: Option<u16>,
    pub curvature: Option<i16>,
    pub curvature_calculation_mode: Option<u8>,
    pub longitudinal_acceleration: Option<i16>,
    pub yaw_rate: Option<i16>,
    pub acceleration_control: Option<String>,
    pub lane_position: Option<i8>,
    pub lateral_acceleration: Option<i16>,
    pub vertical_acceleration: Option<i16>,
    pub confidence: Option<HighFrequencyConfidence>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct LowFrequencyContainer {
    pub vehicle_role: Option<u8>,
    pub exterior_lights: String,
    pub path_history: Vec<PathHistory>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct HighFrequencyConfidence {
    pub heading: Option<u8>,
    pub speed: Option<u8>,
    pub vehicle_length: Option<u8>,
    pub yaw_rate: Option<u8>,
    pub longitudinal_acceleration: Option<u8>,
    pub curvature: Option<u8>,
    pub lateral_acceleration: Option<u8>,
    pub vertical_acceleration: Option<u8>,
}

impl Mobile for CooperativeAwarenessMessage {
    fn mobile_id(&self) -> u32 {
        self.station_id
    }

    fn position(&self) -> &ReferencePosition {
        &self.basic_container.reference_position
    }

    fn speed(&self) -> Option<u16> {
        self.high_frequency_container.speed
    }

    fn heading(&self) -> Option<u16> {
        self.high_frequency_container.heading
    }
}

impl Typed for CooperativeAwarenessMessage {
    fn get_type() -> String {
        "cam".to_string()
    }
}
