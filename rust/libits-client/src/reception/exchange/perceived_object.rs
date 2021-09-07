use serde::{Deserialize, Serialize};

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct PerceivedObject {
    pub object_id: u8,
    pub time_of_measurement: i16,
    pub object_confidence: u8,
    pub distance: Distance,
    pub distance_confidence: DistanceConfidence,
    pub speed: Speed,
    pub speed_confidence: SpeedConfidence,
    pub object_ref_point: u8,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct Distance {
    pub x_distance: i32,
    pub y_distance: i32,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct DistanceConfidence {
    pub x_distance: u8,
    pub y_distance: u8,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct Speed {
    pub x_speed: i16,
    pub y_speed: i16,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct SpeedConfidence {
    pub x_speed: u8,
    pub y_speed: u8,
}
