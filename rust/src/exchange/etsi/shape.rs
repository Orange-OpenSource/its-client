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
use serde::{Deserialize, Serialize};

/// Represents a Shape according to an ETSI standard.
///
/// This message is used to describe a shape.
/// It implements the schema defined in the [CPM version 2.1.0][1].
///
/// [1]: https://github.com/Orange-OpenSource/its-client/blob/master/schema/cpm/cpm_schema_2-1-0.json#L1104
#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Shape {
    // optional fields
    /// Rectangular shape.
    pub rectangular: Option<Rectangular>,
    /// Circular shape.
    pub circular: Option<Circular>,
    /// Polygonal shape.
    pub polygonal: Option<Polygonal>,
    /// Elliptical shape.
    pub elliptical: Option<Elliptical>,
    /// Radial shape.
    pub radial: Option<Radial>,
    /// Radial shapes.
    pub radial_shapes: Option<RadialShapes>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Rectangular {
    /// Semi-length and semi-breadth of the rectangular shape.
    pub semi_length: u8,
    /// Semi-breadth of the rectangular shape.
    pub semi_breadth: u8,

    // optional fields
    /// Center point of the rectangular shape.
    pub center_point: Option<CartesianPosition3D>,
    /// Orientation of the rectangular shape in centidegrees.
    pub orientation: Option<u16>,
    /// Height of the rectangular shape.
    pub height: Option<u16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Circular {
    /// Radius of the circular shape.
    pub radius: u16,

    // optional fields
    /// Reference point of the circular shape.
    pub shape_reference_point: Option<CartesianPosition3D>,
    /// Height of the circular shape.
    pub height: Option<u16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Polygonal {
    /// Polygon represented as a list of Cartesian positions.
    pub polygon: Vec<CartesianPosition3D>,

    // optional fields
    /// Reference point of the polygonal shape.
    pub shape_reference_point: Option<CartesianPosition3D>,
    /// Height of the polygonal shape.
    pub height: Option<u16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Elliptical {
    /// Semi-major axis length of the elliptical shape.
    pub semi_major_axis_length: u16,
    /// Semi-minor axis length of the elliptical shape.
    pub semi_minor_axis_length: u16,

    // optional fields
    /// Reference point of the elliptical shape.
    pub shape_reference_point: Option<CartesianPosition3D>,
    /// Orientation of the elliptical shape in centidegrees.
    pub orientation: Option<u16>,
    /// Height of the elliptical shape.
    pub height: Option<u16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct RadialShapes {
    /// Reference point ID for the radial shapes.
    pub ref_point_id: u8,
    /// X coordinate of the reference point.
    pub x_coordinate: i16,
    /// Y coordinate of the reference point.
    pub y_coordinate: i16,
    /// Z coordinate of the reference point, if available.
    pub radial_shapes_list: Vec<Radial>,

    // optional fields
    /// Z coordinate of the reference point.
    pub z_coordinate: Option<i16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct Radial {
    /// Range of the radial shape.
    pub range: u16,
    /// Start angle of the stationary horizontal opening in centidegrees.
    pub stationary_horizontal_opening_angle_start: u16,
    /// End angle of the stationary horizontal opening in centidegrees.
    pub stationary_horizontal_opening_angle_end: u16,

    // optional fields
    /// Reference point of the radial shape.
    pub shape_reference_point: Option<CartesianPosition3D>,
    /// Start angle of the vertical opening in centidegrees.
    pub vertical_opening_angle_start: Option<u16>,
    /// End angle of the vertical opening in centidegrees.
    pub vertical_opening_angle_end: Option<u16>,
}

#[serde_with::skip_serializing_none]
#[derive(Default, Debug, Clone, Hash, PartialEq, Eq, Serialize, Deserialize)]
pub struct CartesianPosition3D {
    /// X coordinate in the Cartesian position.
    pub x_coordinate: i16,
    /// Y coordinate in the Cartesian position.
    pub y_coordinate: i16,

    // optional field
    /// Z coordinate in the Cartesian position, if available.
    pub z_coordinate: Option<i16>,
}

#[cfg(test)]
mod tests {
    use crate::exchange::etsi::shape::{CartesianPosition3D, Radial, Shape};

    #[test]
    fn test_deserialize_cartesian_position_3d() {
        let data = r#"{
            "x_coordinate": 32767,
            "y_coordinate": -32768,
            "z_coordinate": 0
        }"#;

        match serde_json::from_str::<CartesianPosition3D>(data) {
            Ok(object) => {
                assert_eq!(object.x_coordinate, 32767);
                assert_eq!(object.y_coordinate, -32768);
                assert_eq!(object.z_coordinate, Some(0));
            }
            Err(e) => panic!("Failed to deserialize a CartesianPosition3D: '{e}'"),
        }
    }

    #[test]
    fn test_deserialize_radial() {
        let data = r#"{
            "range": 32767,
            "stationary_horizontal_opening_angle_start": 3601,
            "stationary_horizontal_opening_angle_end": 0,
            "shape_reference_point": {
                "x_coordinate": 32767,
                "y_coordinate": -32768,
                "z_coordinate": 0
            },
            "vertical_opening_angle_start": 1300,
            "vertical_opening_angle_end": 650
        }"#;

        match serde_json::from_str::<Radial>(data) {
            Ok(object) => {
                assert_eq!(object.range, 32767);
                assert_eq!(object.stationary_horizontal_opening_angle_start, 3601);
                assert_eq!(object.stationary_horizontal_opening_angle_end, 0);
                assert_eq!(
                    object.shape_reference_point,
                    Some(CartesianPosition3D {
                        x_coordinate: 32767,
                        y_coordinate: -32768,
                        z_coordinate: Some(0),
                    })
                );
                assert_eq!(object.vertical_opening_angle_start, Some(1300));
                assert_eq!(object.vertical_opening_angle_end, Some(650));
            }
            Err(e) => panic!("Failed to deserialize a Radial: '{e}'"),
        }
    }

    #[test]
    fn test_deserialize_rectangular_shape() {
        let data = r#"{
        "rectangular": {
            "center_point": {
                "x_coordinate": 32767,
                "y_coordinate": -32768,
                "z_coordinate": 0
            },
            "semi_length": 102,
            "semi_breadth": 0,
            "orientation": 3601,
            "height": 4095
        }
    }"#;

        match serde_json::from_str::<Shape>(data) {
            Ok(object) => {
                let rectangular = object.rectangular.unwrap();
                assert_eq!(rectangular.semi_length, 102);
                assert_eq!(rectangular.semi_breadth, 0);
                assert_eq!(
                    rectangular.center_point,
                    Some(CartesianPosition3D {
                        x_coordinate: 32767,
                        y_coordinate: -32768,
                        z_coordinate: Some(0),
                    })
                );
                assert_eq!(rectangular.orientation, Some(3601));
                assert_eq!(rectangular.height, Some(4095));
            }
            Err(e) => panic!("Failed to deserialize a rectangular Shape: '{e}'"),
        }
    }

    #[test]
    fn test_deserialize_circular_shape() {
        let data = r#"{
        "circular": {
            "radius": 4095,
            "shape_reference_point": {
                "x_coordinate": 32767,
                "y_coordinate": -32768,
                "z_coordinate": 0
            },
            "height": 2048
        }
    }"#;

        match serde_json::from_str::<Shape>(data) {
            Ok(object) => {
                let circular = object.circular.unwrap();
                assert_eq!(circular.radius, 4095);
                assert_eq!(
                    circular.shape_reference_point,
                    Some(CartesianPosition3D {
                        x_coordinate: 32767,
                        y_coordinate: -32768,
                        z_coordinate: Some(0),
                    })
                );
                assert_eq!(circular.height, Some(2048));
            }
            Err(e) => panic!("Failed to deserialize a circular Shape: '{e}'"),
        }
    }

    #[test]
    fn test_deserialize_polygonal_shape() {
        let data = r#"{
        "polygonal": {
            "polygon": [
                {
                    "x_coordinate": 32767,
                    "y_coordinate": -32768,
                    "z_coordinate": 0
                },
                {
                    "x_coordinate": 16384,
                    "y_coordinate": -16384,
                    "z_coordinate": 0
                }
            ],
            "shape_reference_point": {
                "x_coordinate": 32767,
                "y_coordinate": -32768,
                "z_coordinate": 0
            },
            "height": 2048
        }
    }"#;

        match serde_json::from_str::<Shape>(data) {
            Ok(object) => {
                let polygonal = object.polygonal.unwrap();
                assert_eq!(polygonal.polygon.len(), 2);
                assert_eq!(
                    polygonal.polygon[0],
                    CartesianPosition3D {
                        x_coordinate: 32767,
                        y_coordinate: -32768,
                        z_coordinate: Some(0),
                    }
                );
                assert_eq!(
                    polygonal.polygon[1],
                    CartesianPosition3D {
                        x_coordinate: 16384,
                        y_coordinate: -16384,
                        z_coordinate: Some(0),
                    }
                );
                assert_eq!(
                    polygonal.shape_reference_point,
                    Some(CartesianPosition3D {
                        x_coordinate: 32767,
                        y_coordinate: -32768,
                        z_coordinate: Some(0),
                    })
                );
                assert_eq!(polygonal.height, Some(2048));
            }
            Err(e) => panic!("Failed to deserialize a polygonal Shape: '{e}'"),
        }
    }

    #[test]
    fn test_deserialize_elliptical_shape() {
        let data = r#"{
        "elliptical": {
            "semi_major_axis_length": 4095,
            "semi_minor_axis_length": 2048,
            "shape_reference_point": {
                "x_coordinate": 32767,
                "y_coordinate": -32768,
                "z_coordinate": 0
            },
            "orientation": 3601,
            "height": 1024
        }
    }"#;

        match serde_json::from_str::<Shape>(data) {
            Ok(object) => {
                let elliptical = object.elliptical.unwrap();
                assert_eq!(elliptical.semi_major_axis_length, 4095);
                assert_eq!(elliptical.semi_minor_axis_length, 2048);
                assert_eq!(
                    elliptical.shape_reference_point,
                    Some(CartesianPosition3D {
                        x_coordinate: 32767,
                        y_coordinate: -32768,
                        z_coordinate: Some(0),
                    })
                );
                assert_eq!(elliptical.orientation, Some(3601));
                assert_eq!(elliptical.height, Some(1024));
            }
            Err(e) => panic!("Failed to deserialize an elliptical Shape: '{e}'"),
        }
    }

    #[test]
    fn test_deserialize_radial_shape() {
        let data = r#"{
        "radial": {
            "range": 4095,
            "stationary_horizontal_opening_angle_start": 900,
            "stationary_horizontal_opening_angle_end": 2700,
            "shape_reference_point": {
                "x_coordinate": 32767,
                "y_coordinate": -32768,
                "z_coordinate": 0
            },
            "vertical_opening_angle_start": 1300,
            "vertical_opening_angle_end": 650
        }
    }"#;

        match serde_json::from_str::<Shape>(data) {
            Ok(object) => {
                let radial = object.radial.unwrap();
                assert_eq!(radial.range, 4095);
                assert_eq!(radial.stationary_horizontal_opening_angle_start, 900);
                assert_eq!(radial.stationary_horizontal_opening_angle_end, 2700);
                assert_eq!(
                    radial.shape_reference_point,
                    Some(CartesianPosition3D {
                        x_coordinate: 32767,
                        y_coordinate: -32768,
                        z_coordinate: Some(0),
                    })
                );
                assert_eq!(radial.vertical_opening_angle_start, Some(1300));
                assert_eq!(radial.vertical_opening_angle_end, Some(650));
            }
            Err(e) => panic!("Failed to deserialize a radial Shape: '{e}'"),
        }
    }

    #[test]
    fn test_deserialize_radial_shapes_shape() {
        let data = r#"{
        "radial_shapes": {
            "ref_point_id": 255,
            "x_coordinate": 1001,
            "y_coordinate": -3094,
            "z_coordinate": 1047,
            "radial_shapes_list": [
                {
                    "range": 4095,
                    "stationary_horizontal_opening_angle_start": 900,
                    "stationary_horizontal_opening_angle_end": 2700
                },
                {
                    "range": 2048,
                    "stationary_horizontal_opening_angle_start": 0,
                    "stationary_horizontal_opening_angle_end": 1800
                }
            ]
        }
    }"#;

        match serde_json::from_str::<Shape>(data) {
            Ok(object) => {
                let radial_shapes = object.radial_shapes.unwrap();
                assert_eq!(radial_shapes.ref_point_id, 255);
                assert_eq!(radial_shapes.x_coordinate, 1001);
                assert_eq!(radial_shapes.y_coordinate, -3094);
                assert_eq!(radial_shapes.z_coordinate, Some(1047));
                assert_eq!(radial_shapes.radial_shapes_list.len(), 2);
                assert_eq!(radial_shapes.radial_shapes_list[0].range, 4095);
                assert_eq!(
                    radial_shapes.radial_shapes_list[0].stationary_horizontal_opening_angle_start,
                    900
                );
                assert_eq!(
                    radial_shapes.radial_shapes_list[0].stationary_horizontal_opening_angle_end,
                    2700
                );
                assert_eq!(radial_shapes.radial_shapes_list[1].range, 2048);
                assert_eq!(
                    radial_shapes.radial_shapes_list[1].stationary_horizontal_opening_angle_start,
                    0
                );
                assert_eq!(
                    radial_shapes.radial_shapes_list[1].stationary_horizontal_opening_angle_end,
                    1800
                );
            }
            Err(e) => panic!("Failed to deserialize a radial shapes Shape: '{e}'"),
        }
    }
}
