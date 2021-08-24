use serde::{Deserialize, Serialize};

use crate::reception::exchange::mobile::Mobile;
use crate::reception::exchange::{PositionConfidence, ReferencePosition};
use crate::reception::typed::Typed;

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct CollectivePerceptionMessage {
    pub protocol_version: u8,
    pub station_id: u32,
    pub generation_delta_time: u16,
    pub management_container: ManagementContainer,
    pub originating_vehicle_container: OriginatingVehicleContainer,
    pub sensor_information_container: Vec<SensorInformation>,
    pub perceived_object_container: Option<PerceivedObjectContainer>,
    pub number_of_perceived_objects: u8,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct ManagementContainer {
    pub station_type: Option<u8>,
    pub reference_position: ReferencePosition,
    pub confidence: Option<PositionConfidence>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct OriginatingVehicleContainer {
    pub heading: Option<u16>,
    pub speed: Option<u16>,
    pub confidence: Option<OriginatingVehicleContainerConfidence>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct PerceivedObjectContainer {
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
pub struct OriginatingVehicleContainerConfidence {
    pub heading: Option<u8>,
    pub speed: Option<u8>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct SensorInformation {
    pub sensor_id: u8,
    #[serde(rename = "type")]
    pub sensor_type: u8,
    pub vehicle_sensor: VehicleSensor,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct VehicleSensor {
    pub ref_point_id: u8,
    pub x_sensor_offset: i16,
    pub y_sensor_offset: i16,
    pub z_sensor_offset: Option<u16>,
    pub vehicle_sensor_property_list: VehicleSensorPropertyList,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct VehicleSensorPropertyList {
    pub range: u16,
    pub horizontal_opening_angle_start: u16,
    pub horizontal_opening_angle_end: u16,
    pub vertical_opening_angle_start: Option<u16>,
    pub vertical_opening_angle_end: Option<u16>,
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

impl Mobile for CollectivePerceptionMessage {
    fn mobile_id(&self) -> u32 {
        self.station_id
    }

    fn position(&self) -> &ReferencePosition {
        &self.management_container.reference_position
    }

    fn speed(&self) -> Option<u16> {
        self.originating_vehicle_container.speed
    }

    fn heading(&self) -> Option<u16> {
        self.originating_vehicle_container.heading
    }
}

impl Typed for CollectivePerceptionMessage {
    fn get_type() -> String {
        "cpm".to_string()
    }
}
