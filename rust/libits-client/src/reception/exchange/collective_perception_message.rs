use serde::{Deserialize, Serialize};

use crate::reception::exchange::mobile::Mobile;
use crate::reception::exchange::mobile_perceived_object::MobilePerceivedObject;
use crate::reception::exchange::perceived_object::PerceivedObject;
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

impl CollectivePerceptionMessage {
    pub fn mobile_perceived_object_list(&self) -> Vec<MobilePerceivedObject> {
        if let Some(station_data_container) = &self.station_data_container {
            if let Some(originating_vehicle_container) =
                &station_data_container.originating_vehicle_container
            {
                return self
                    .perceived_object_container
                    .iter()
                    .map(|perceived_object| {
                        MobilePerceivedObject::new(
                            //assumed clone : we store a copy into the MobilePerceivedObject container
                            // TODO use a lifetime to propage the lifecycle betwwen PerceivedObject and MobilePerceivedObject instead of clone
                            perceived_object.clone(),
                            self.station_id,
                            &self.management_container.reference_position,
                            originating_vehicle_container.heading,
                        )
                    })
                    .collect();
            }
        }
        Vec::new()
    }
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
mod tests {
    use crate::reception::exchange::collective_perception_message::{
        CollectivePerceptionMessage, DetectionArea, ManagementContainer,
        OriginatingVehicleContainer, SensorInformation, StationDataContainer, VehicleSensor,
        VehicleSensorProperty,
    };
    use crate::reception::exchange::mobile_perceived_object::MobilePerceivedObject;
    use crate::reception::exchange::perceived_object::{
        Distance, DistanceConfidence, PerceivedObject, Speed, SpeedConfidence,
    };
    use crate::reception::exchange::reference_position::ReferencePosition;

    fn create_perceived_object_in_front() -> PerceivedObject {
        PerceivedObject {
            object_id: 45,
            time_of_measurement: 50,
            object_confidence: 10,
            distance: Distance {
                x_distance: 40,
                y_distance: 100,
            },
            distance_confidence: DistanceConfidence {
                x_distance: 102,
                y_distance: 102,
            },
            speed: Speed {
                x_speed: 1400,
                y_speed: 500,
            },
            speed_confidence: SpeedConfidence {
                x_speed: 127,
                y_speed: 127,
            },
            object_ref_point: 0,
        }
    }

    fn create_perceived_object_behind() -> PerceivedObject {
        PerceivedObject {
            object_id: 48,
            time_of_measurement: 51,
            object_confidence: 9,
            distance: Distance {
                x_distance: -40,
                y_distance: 100,
            },
            distance_confidence: DistanceConfidence {
                x_distance: 102,
                y_distance: 102,
            },
            speed: Speed {
                x_speed: 1200,
                y_speed: 400,
            },
            speed_confidence: SpeedConfidence {
                x_speed: 127,
                y_speed: 127,
            },
            object_ref_point: 0,
        }
    }

    fn create_cpm_with_two_perceived_object() -> CollectivePerceptionMessage {
        CollectivePerceptionMessage {
            protocol_version: 1,
            station_id: 31470,
            message_id: 12,
            generation_delta_time: 65535,
            management_container: ManagementContainer {
                station_type: 5,
                reference_position: ReferencePosition {
                    latitude: 426263556,
                    longitude: -82492123,
                    altitude: 800001,
                },
                confidence: Default::default(),
            },
            station_data_container: Some(StationDataContainer {
                originating_vehicle_container: Some(OriginatingVehicleContainer {
                    heading: 900,
                    speed: 1600,
                    ..Default::default()
                }),
                ..Default::default()
            }),
            sensor_information_container: vec![SensorInformation {
                sensor_id: 3,
                sensor_type: 3,
                detection_area: DetectionArea {
                    vehicle_sensor: Some(VehicleSensor {
                        ref_point_id: 0,
                        x_sensor_offset: -20,
                        y_sensor_offset: 20,
                        z_sensor_offset: Some(0),
                        vehicle_sensor_property_list: vec![VehicleSensorProperty {
                            range: 5000,
                            horizontal_opening_angle_start: 600,
                            horizontal_opening_angle_end: 600,
                            vertical_opening_angle_start: None,
                            vertical_opening_angle_end: None,
                        }],
                    }),
                },
            }],
            perceived_object_container: vec![
                create_perceived_object_in_front(),
                create_perceived_object_behind(),
            ],
            number_of_perceived_objects: 2,
        }
    }

    #[test]
    fn it_can_provide_the_mobile_perceived_object_list() {
        assert_eq!(
            create_cpm_with_two_perceived_object().mobile_perceived_object_list(),
            vec![
                MobilePerceivedObject {
                    perceived_object: create_perceived_object_in_front(),
                    mobile_id: 3147045,
                    reference_position: ReferencePosition {
                        latitude: 426263645,
                        longitude: -82492074,
                        altitude: 800001,
                    },
                    speed: 1486,
                    heading: 900
                },
                MobilePerceivedObject {
                    perceived_object: create_perceived_object_behind(),
                    mobile_id: 3147048,
                    reference_position: ReferencePosition {
                        latitude: 426263645,
                        longitude: -82492170,
                        altitude: 800001,
                    },
                    speed: 1264,
                    heading: 900
                },
            ]
        );
    }
}
