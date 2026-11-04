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

use criterion::{Criterion, criterion_group, criterion_main};
use libits::exchange::{Exchange, cause::Cause};

use serde_json::Value;

const JSON_FULL_CAM: &'static str = r#"{
    "message_type": "denm",
    "source_uuid": "com_application_12345",
    "timestamp": 1574778515424,
    "version": "2.2.0",
    "message": {
        "protocol_version": 255,
        "station_id": 4294967295,
        "generation_delta_time": 65535,
        "basic_container": {
            "station_type": 255,
            "reference_position": {
                "latitude": 900000001,
                "longitude": 1800000001,
                "altitude": {
                    "value": 800001,
                    "confidence": 15
                },
                "position_confidence_ellipse": {
                    "semi_major": 4095,
                    "semi_minor": 0,
                    "semi_major_orientation": 3601
                }
            }
        },
        "high_frequency_container": {
            "basic_vehicle_container_high_frequency": {
                "heading": {
                    "value": 3601,
                    "confidence": 127
                },
                "speed": {
                    "value": 16383,
                    "confidence": 127
                },
                "drive_direction": 2,
                "vehicle_length": {
                    "value": 1023,
                    "confidence": 4
                },
                "vehicle_width": 62,
                "longitudinal_acceleration": {
                    "value": 161,
                    "confidence": 102
                },
                "curvature": {
                    "value": 1023,
                    "confidence": 7
                },
                "curvature_calculation_mode": 2,
                "yaw_rate": {
                    "value": 32767,
                    "confidence": 8
                },
                "acceleration_control": {
                    "brake_pedal_engaged": true,
                    "gas_pedal_engaged": true,
                    "emergency_brake_engaged": true,
                    "collision_warning_engaged": true,
                    "acc_engaged": true,
                    "cruise_control_engaged": true,
                    "speed_limiter_engaged": true
                },
                "lane_position": 14,
                "steering_wheel_angle": {
                    "value": 512,
                    "confidence": 127
                },
                "lateral_acceleration": {
                    "value": -160,
                    "confidence": 0
                },
                "vertical_acceleration": {
                    "value": 0,
                    "confidence": 63
                },
                "performance_class": 2,
                "cen_dsrc_tolling_zone": {
                    "protected_zone_latitude": 900000001,
                    "protected_zone_longitude": 1800000001,
                    "cen_dsrc_tolling_zone_id": 134217727
                }
            }
       },
        "low_frequency_container": {
            "basic_vehicle_container_low_frequency": {
                "vehicle_role": 15,
                "exterior_lights": {
                    "low_beam_headlights_on": true,
                    "high_beam_headlights_on": false,
                    "left_turn_signal_on": true,
                    "right_turn_signal_on": false,
                    "daytime_running_lights_on": true,
                    "reverse_light_on": false,
                    "fog_light_on": false,
                    "parking_lights_on": true
                },
                "path_history": [
                    {
                        "path_position": {
                            "delta_latitude": 131072,
                            "delta_longitude": -131071,
                            "delta_altitude": 12800
                        },
                        "path_delta_time": 65535
                    },
                    {
                        "path_position": {
                            "delta_latitude": -131071,
                            "delta_longitude": 131072,
                            "delta_altitude": -12700
                        },
                        "path_delta_time": 1
                    }
                ]
            }
        },
        "special_vehicle_container": {
            "public_transport_container": {
                "embarkation_status": true,
                "pt_activation": {
                    "pt_activation_type": 255,
                    "pt_activation_data": "abcdefghijklmnopqrst"
                }
            }
        }
    }
}"#;

fn bench_from_json_value(criterion: &mut Criterion) {
    let value = serde_json::from_str::<Value>(JSON_FULL_CAM).unwrap();
    criterion.bench_function("Cause from full CAM JSON exchange", |b| {
        b.iter(|| Cause::from_json_value(&value))
    });
}

fn bench_from_exchange(criterion: &mut Criterion) {
    let exchange = serde_json::from_str::<Exchange>(JSON_FULL_CAM).unwrap();
    criterion.bench_function("Cause from full CAM Exchange", |b| {
        b.iter(|| Cause::from_exchange(&exchange))
    });
}

criterion_group!(benches, bench_from_json_value, bench_from_exchange);
criterion_main!(benches);
