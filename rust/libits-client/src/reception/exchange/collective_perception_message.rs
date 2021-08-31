use serde::{Deserialize, Serialize};

use crate::reception::exchange::mobile::Mobile;
use crate::reception::exchange::{PositionConfidence, ReferencePosition};
use crate::reception::typed::Typed;

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct CollectivePerceptionMessage {
    pub protocol_version: u8,
    pub station_id: u32,
    pub message_id: u8,
    pub generation_delta_time: u16,
    pub management_container: ManagementContainer,
    pub station_data_container: Option<StationDataContainer>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub sensor_information_container: Vec<SensorInformation>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub perceived_object_container: Vec<PerceivedObject>,
    pub number_of_perceived_objects: u8,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct ManagementContainer {
    pub station_type: u8,
    pub reference_position: ReferencePosition,
    pub confidence: PositionConfidence,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct StationDataContainer {
    pub originating_vehicle_container: Option<OriginatingVehicleContainer>,
    pub originating_rsu_container: Option<OriginatingRSUContainer>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct OriginatingVehicleContainer {
    pub heading: u16,
    pub speed: u16,
    pub drive_direction: Option<u8>,
    pub vehicle_length: Option<u16>,
    pub vehicle_width: Option<u8>,
    pub longitudinal_acceleration: Option<i16>,
    pub yaw_rate: Option<i16>,
    pub lateral_acceleration: Option<i16>,
    pub vertical_acceleration: Option<i16>,
    pub confidence: OriginatingVehicleContainerConfidence,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct OriginatingRSUContainer {
    pub intersection_reference_id: Option<IntersectionReferenceId>,
    pub road_segment_reference_id: Option<u32>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct IntersectionReferenceId {
    pub road_regulator_id: Option<u32>,
    pub intersection_id: u32,
}

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
pub struct OriginatingVehicleContainerConfidence {
    pub heading: u8,
    pub speed: u8,
    pub vehicle_length: Option<u8>,
    pub yaw_rate: Option<u8>,
    pub longitudinal_acceleration: Option<u8>,
    pub lateral_acceleration: Option<u8>,
    pub vertical_acceleration: Option<u8>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct SensorInformation {
    pub sensor_id: u8,
    #[serde(rename = "type")]
    pub sensor_type: u8,
    pub detection_area: DetectionArea,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct DetectionArea {
    pub vehicle_sensor: Option<VehicleSensor>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct VehicleSensor {
    pub ref_point_id: u8,
    pub x_sensor_offset: i16,
    pub y_sensor_offset: i16,
    pub z_sensor_offset: Option<u16>,
    #[serde(skip_serializing_if = "Vec::is_empty", default)]
    pub vehicle_sensor_property_list: Vec<VehicleSensorProperty>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Serialize, Deserialize)]
pub struct VehicleSensorProperty {
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
        if let Some(station_data_container) = &self.station_data_container {
            if let Some(originating_vehicle_container) =
                &station_data_container.originating_vehicle_container
            {
                return Some(originating_vehicle_container.speed);
            }
        }
        None
    }

    fn heading(&self) -> Option<u16> {
        if let Some(station_data_container) = &self.station_data_container {
            if let Some(originating_vehicle_container) =
                &station_data_container.originating_vehicle_container
            {
                return Some(originating_vehicle_container.heading);
            }
        }
        None
    }
}

impl Typed for CollectivePerceptionMessage {
    fn get_type() -> String {
        "cpm".to_string()
    }
}

#[cfg(test)]
mod tests {}
