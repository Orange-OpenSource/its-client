/*
 * Software Name : libits-client
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Authors: see CONTRIBUTORS.md
 */

use serde::{Deserialize, Deserializer, Serialize, Serializer};

pub(super) trait ConfidenceValue {
    fn confidence_value(&self) -> Option<u16>;
}

impl ConfidenceValue for u8 {
    fn confidence_value(&self) -> Option<u16> {
        Some(u16::from(*self))
    }
}

impl ConfidenceValue for u16 {
    fn confidence_value(&self) -> Option<u16> {
        Some(*self)
    }
}

impl<T: ConfidenceValue> ConfidenceValue for Option<T> {
    fn confidence_value(&self) -> Option<u16> {
        self.as_ref().and_then(ConfidenceValue::confidence_value)
    }
}

fn validate(value: &impl ConfidenceValue, minimum: u16, maximum: u16) -> Result<(), String> {
    if let Some(value) = value.confidence_value()
        && !(minimum..=maximum).contains(&value)
    {
        return Err(format!(
            "confidence out of range [{minimum}, {maximum}] (actual={value})"
        ));
    }
    Ok(())
}

pub(super) fn serialize<
    T: Serialize + ConfidenceValue,
    S: Serializer,
    const MIN: u16,
    const MAX: u16,
>(
    value: &T,
    serializer: S,
) -> Result<S::Ok, S::Error> {
    validate(value, MIN, MAX).map_err(serde::ser::Error::custom)?;
    value.serialize(serializer)
}

pub(super) fn deserialize<
    'de,
    T: Deserialize<'de> + ConfidenceValue,
    D: Deserializer<'de>,
    const MIN: u16,
    const MAX: u16,
>(
    deserializer: D,
) -> Result<T, D::Error> {
    let value = T::deserialize(deserializer)?;
    validate(&value, MIN, MAX).map_err(serde::de::Error::custom)?;
    Ok(value)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::exchange::etsi::acceleration::{Acceleration, AccelerationMagnitude};
    use crate::exchange::etsi::angle::Angle;
    use crate::exchange::etsi::collective_perception_message::{
        PerceptionRegion, SensorInformation,
    };
    use crate::exchange::etsi::cooperative_awareness_message::CooperativeAwarenessMessage;
    use crate::exchange::etsi::cooperative_awareness_message_113::HighFrequencyConfidence;
    use crate::exchange::etsi::coordinate::CartesianCoordinate;
    use crate::exchange::etsi::curvature::Curvature;
    use crate::exchange::etsi::decentralized_environmental_notification_message::{
        AltitudeValue, EventPoint, SituationContainer,
    };
    use crate::exchange::etsi::heading::Heading;
    use crate::exchange::etsi::longitudinal_lane_position::LongitudinalLanePosition;
    use crate::exchange::etsi::object_dimension::ObjectDimension;
    use crate::exchange::etsi::perceived_object::{
        CartesianAngularVelocityComponent, ObjectClass, ObjectClassification, PerceivedObject,
    };
    use crate::exchange::etsi::reference_position::{Altitude, PositionConfidenceEllipse};
    use crate::exchange::etsi::reference_position113::{
        PositionConfidence, PositionConfidenceEllipse as LegacyEllipse,
    };
    use crate::exchange::etsi::speed::Speed;
    use crate::exchange::etsi::steering_wheel_angle::SteeringWheelAngle;
    use crate::exchange::etsi::vehicle_length::VehicleLength;
    use crate::exchange::etsi::velocity::Velocity;
    use crate::exchange::etsi::yaw_rate::YawRate;
    use serde::de::DeserializeOwned;
    use serde_json::{Value, json};

    fn check_field<T: Serialize + DeserializeOwned>(fixture: T, field: &str, bounds: &Value) {
        let minimum = bounds["minimum"].as_u64().expect("schema minimum");
        let maximum = bounds["maximum"].as_u64().expect("schema maximum");
        let fixture = serde_json::to_value(fixture).unwrap();
        assert_eq!(
            fixture[field],
            bounds.get("default").cloned().unwrap_or(json!(maximum))
        );
        for value in [
            0,
            minimum.saturating_sub(1),
            minimum,
            maximum,
            maximum + 1,
            255,
            256,
            4095,
            4096,
            65535,
        ] {
            let mut candidate = fixture.clone();
            candidate[field] = json!(value);
            let decoded = serde_json::from_value::<T>(candidate);
            assert_eq!(
                decoded.is_ok(),
                (minimum..=maximum).contains(&value),
                "{field}={value}, bounds={bounds}"
            );
            if let Ok(decoded) = decoded {
                assert_eq!(serde_json::to_value(decoded).unwrap()[field], json!(value));
            }
        }
        for value in [json!(-1), json!(1.5), json!("1")] {
            let mut candidate = fixture.clone();
            candidate[field] = value;
            assert!(serde_json::from_value::<T>(candidate).is_err());
        }
    }

    macro_rules! scalar {
        ($ty:ident, $bounds:expr, $invalid:expr) => {
            check_field($ty::default(), "confidence", $bounds);
            assert!(
                serde_json::to_value($ty {
                    confidence: $invalid,
                    ..Default::default()
                })
                .is_err()
            );
        };
    }

    #[test]
    fn cam_confidences_match_each_supported_modern_schema() {
        for schema in [
            include_str!("../../../../schema/cam/cam_schema_2-2-0.json"),
            include_str!("../../../../schema/cam/cam_schema_2-3-0.json"),
            include_str!("../../../../schema/cam/cam_schema_2-4-0.json"),
        ] {
            let schema: Value = serde_json::from_str(schema).unwrap();
            let bounds = |name: &str| &schema["$defs"][name]["properties"]["confidence"];
            scalar!(Heading, bounds("heading"), 0);
            scalar!(Speed, bounds("speed"), 0);
            scalar!(SteeringWheelAngle, bounds("steering_wheel_angle"), 0);
            scalar!(Acceleration, bounds("acceleration_component"), 103);
            scalar!(YawRate, bounds("yaw_rate"), 9);
            scalar!(Altitude, bounds("altitude"), 16);
            check_field(
                VehicleLength::default(),
                "confidence",
                bounds("vehicle_length"),
            );
            check_field(Curvature::default(), "confidence", bounds("curvature"));
            for field in ["semi_major", "semi_minor", "semi_major_orientation"] {
                let bounds = &schema["$defs"]["position_confidence_ellipse"]["properties"][field];
                let bounds = if let Some(reference) = bounds["$ref"].as_str() {
                    schema
                        .pointer(reference.strip_prefix('#').unwrap())
                        .unwrap()
                } else {
                    bounds
                };
                check_field(PositionConfidenceEllipse::default(), field, bounds);
            }
        }
    }

    #[test]
    fn denm_confidences_match_220_and_230_schemas() {
        for schema in [
            include_str!("../../../../schema/denm/denm_schema_2-2-0.json"),
            include_str!("../../../../schema/denm/denm_schema_2-3-0.json"),
        ] {
            let schema: Value = serde_json::from_str(schema).unwrap();
            let location = &schema["properties"]["message"]["properties"]["location"]["properties"];
            scalar!(
                Speed,
                &location["event_speed"]["properties"]["confidence"],
                0
            );
            scalar!(
                Heading,
                &location["event_position_heading"]["properties"]["confidence"],
                0
            );
            scalar!(
                AltitudeValue,
                &schema["$defs"]["altitude"]["properties"]["confidence"],
                16
            );
            let quality = &schema["$defs"]["information_quality"];
            check_field(
                SituationContainer::default(),
                "information_quality",
                quality,
            );
            check_field(EventPoint::default(), "information_quality", quality);
            assert!(
                serde_json::to_value(SituationContainer {
                    information_quality: 8,
                    ..Default::default()
                })
                .is_err()
            );
            assert!(
                serde_json::to_value(EventPoint {
                    information_quality: 8,
                    ..Default::default()
                })
                .is_err()
            );
        }
    }

    #[test]
    fn cpm_confidences_match_210_and_211_schemas() {
        for schema in [
            include_str!("../../../../schema/cpm/cpm_schema_2-1-0.json"),
            include_str!("../../../../schema/cpm/cpm_schema_2-1-1.json"),
        ] {
            let schema: Value = serde_json::from_str(schema).unwrap();
            let bounds = |name: &str| &schema["$defs"][name]["properties"]["confidence"];
            scalar!(Angle, bounds("angle"), 0);
            scalar!(Speed, bounds("speed"), 0);
            scalar!(Velocity, bounds("velocity_component"), 0);
            scalar!(
                CartesianCoordinate,
                bounds("cartesian_coordinate_with_confidence"),
                0
            );
            scalar!(ObjectDimension, bounds("object_dimension"), 0);
            scalar!(Acceleration, bounds("acceleration_component"), 103);
            scalar!(
                CartesianAngularVelocityComponent,
                bounds("cartesian_angular_velocity_component"),
                8
            );
            scalar!(
                LongitudinalLanePosition,
                bounds("longitudinal_lane_position"),
                1024
            );
            let objects = &schema["properties"]["message"]["properties"]["perceived_object_container"]
                ["items"]["properties"];
            check_field(
                PerceivedObject {
                    object_perception_quality: Some(15),
                    ..Default::default()
                },
                "object_perception_quality",
                &objects["object_perception_quality"],
            );
            assert!(
                serde_json::to_value(PerceivedObject {
                    object_perception_quality: Some(16),
                    ..Default::default()
                })
                .is_err()
            );
            scalar!(
                AccelerationMagnitude,
                &objects["acceleration"]["properties"]["polar_acceleration"]["properties"]["acceleration_magnitude"]
                    ["properties"]["confidence"],
                103
            );
            let classification = ObjectClassification {
                object_class: ObjectClass::Vehicle(5),
                confidence: 101,
            };
            check_field(
                classification.clone(),
                "confidence",
                &objects["classification"]["items"]["properties"]["confidence"],
            );
            assert!(
                serde_json::to_value(ObjectClassification {
                    confidence: 0,
                    ..classification
                })
                .is_err()
            );
            let properties = &schema["properties"]["message"]["properties"];
            let region = &properties["perception_region_container"]["items"]["properties"]["perception_region_confidence"];
            check_field(
                PerceptionRegion::default(),
                "perception_region_confidence",
                region,
            );
            assert!(
                serde_json::to_value(PerceptionRegion {
                    perception_region_confidence: 0,
                    ..Default::default()
                })
                .is_err()
            );
            let sensor = SensorInformation {
                perception_region_confidence: Some(101),
                ..Default::default()
            };
            check_field(
                sensor,
                "perception_region_confidence",
                &properties["sensor_information_container"]["items"]["properties"]["perception_region_confidence"],
            );
            assert!(
                serde_json::to_value(SensorInformation {
                    perception_region_confidence: Some(0),
                    ..Default::default()
                })
                .is_err()
            );
        }
    }

    #[test]
    fn legacy_optional_confidences_keep_absence_and_validate_present_values() {
        let schema: Value =
            serde_json::from_str(include_str!("../../../../schema/cam/cam_schema_1-1-3.json"))
                .unwrap();
        let properties = &schema["properties"]["message"]["properties"];
        let high =
            &properties["high_frequency_container"]["properties"]["confidence"]["properties"];
        for field in [
            "heading",
            "speed",
            "vehicle_length",
            "yaw_rate",
            "longitudinal_acceleration",
            "curvature",
            "lateral_acceleration",
            "vertical_acceleration",
        ] {
            let mut fixture = json!({});
            fixture[field] = high[field]
                .get("default")
                .cloned()
                .unwrap_or(high[field]["maximum"].clone());
            check_field(
                serde_json::from_value::<HighFrequencyConfidence>(fixture).unwrap(),
                field,
                &high[field],
            );
        }
        let basic = &properties["basic_container"]["properties"]["confidence"]["properties"];
        check_field(
            PositionConfidence {
                altitude: Some(15),
                ..Default::default()
            },
            "altitude",
            &basic["altitude"],
        );
        for field in [
            "semi_major_confidence",
            "semi_minor_confidence",
            "semi_major_orientation",
        ] {
            let bounds = &basic["position_confidence_ellipse"]["properties"][field];
            let mut fixture = json!({});
            fixture[field] = bounds["default"].clone();
            check_field(
                serde_json::from_value::<LegacyEllipse>(fixture).unwrap(),
                field,
                bounds,
            );
        }
        let absent: HighFrequencyConfidence = serde_json::from_str("{}").unwrap();
        assert_eq!(serde_json::to_value(absent).unwrap(), json!({}));
        let sensor: SensorInformation =
            serde_json::from_str(r#"{"sensor_id":1,"sensor_type":0,"shadowing_applies":false}"#)
                .unwrap();
        assert!(sensor.perception_region_confidence.is_none());
    }

    #[test]
    fn constructors_emit_unavailable_confidence_not_false_precision() {
        use crate::client::configuration::Configuration;
        use crate::exchange::{Exchange, message::Message, sequence_number::SequenceNumber};
        use crate::mobility::position::Position;

        let cam = CooperativeAwarenessMessage::new(42, 5, Position::default(), 3.0, 0.1);
        let vehicle = cam
            .high_frequency_container
            .basic_vehicle_container_high_frequency
            .as_ref()
            .unwrap();
        assert_eq!(vehicle.heading.confidence, 127);
        assert_eq!(vehicle.speed.confidence, 127);
        assert_eq!(
            cam.basic_container.reference_position.altitude.confidence,
            15
        );
        assert_eq!(
            cam.basic_container
                .reference_position
                .position_confidence_ellipse
                .semi_major,
            4095
        );
        let timestamp = crate::now();
        let denm = Exchange::create_denm(
            timestamp,
            &Configuration::default(),
            97,
            Some(0),
            &mut SequenceNumber::new(u16::MAX.into()),
            &cam,
            Vec::new(),
        );
        let id = denm.management.action_id.clone();
        let mut updated = denm.update(timestamp + 1, &cam);
        assert_eq!(updated.management.action_id, id);
        let location = updated.location.as_ref().unwrap();
        assert_eq!(location.event_speed.as_ref().unwrap().confidence, 127);
        assert_eq!(
            location.event_position_heading.as_ref().unwrap().confidence,
            127
        );
        let location = updated.location.as_mut().unwrap();
        location.event_speed.as_mut().unwrap().confidence = 100;
        location.event_position_heading.as_mut().unwrap().confidence = 1;
        let updated = updated.update(timestamp + 2, &cam);
        assert_eq!(updated.management.action_id, id);
        let location = updated.location.as_ref().unwrap();
        assert_eq!(location.event_speed.as_ref().unwrap().confidence, 100);
        assert_eq!(
            location.event_position_heading.as_ref().unwrap().confidence,
            1
        );
        let envelope = Exchange::new(
            "confidence-test".into(),
            timestamp,
            "2.3.0".into(),
            Vec::new(),
            None,
            Message::DENM(updated),
        );
        let json = serde_json::to_value(envelope).unwrap();
        assert!(json.get("path").is_none());
    }
}
