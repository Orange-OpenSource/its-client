/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

/**
 * A computed lane defined as a translated (and optionally rotated/scaled) copy of a reference lane.
 *
 * @param referenceLaneId ID of the source lane this computed lane is derived from.
 * @param offsetXAxis X-axis translation offset applied to all points of the reference lane (driven_line_offset integer).
 * @param offsetYAxis Y-axis translation offset (driven_line_offset integer).
 * @param rotateXy Optional. Rotation (in units of 0.0054932 degrees) applied about the first path point.
 * @param scaleXAxis Optional. X-axis zoom factor (scale_b12 integer).
 * @param scaleYAxis Optional. Y-axis zoom factor (scale_b12 integer).
 */
public record ComputedLane(
        int referenceLaneId,
        int offsetXAxis,
        int offsetYAxis,
        Integer rotateXy,
        Integer scaleXAxis,
        Integer scaleYAxis) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer referenceLaneId;
        private Integer offsetXAxis;
        private Integer offsetYAxis;
        private Integer rotateXy;
        private Integer scaleXAxis;
        private Integer scaleYAxis;

        private Builder() {}

        public Builder referenceLaneId(int referenceLaneId) {
            this.referenceLaneId = referenceLaneId;
            return this;
        }

        public Builder offsetXAxis(int offsetXAxis) {
            this.offsetXAxis = offsetXAxis;
            return this;
        }

        public Builder offsetYAxis(int offsetYAxis) {
            this.offsetYAxis = offsetYAxis;
            return this;
        }

        public Builder rotateXy(Integer rotateXy) {
            this.rotateXy = rotateXy;
            return this;
        }

        public Builder scaleXAxis(Integer scaleXAxis) {
            this.scaleXAxis = scaleXAxis;
            return this;
        }

        public Builder scaleYAxis(Integer scaleYAxis) {
            this.scaleYAxis = scaleYAxis;
            return this;
        }

        public ComputedLane build() {
            return new ComputedLane(
                    requireNonNull(referenceLaneId, "reference_lane_id"),
                    requireNonNull(offsetXAxis, "offset_x_axis"),
                    requireNonNull(offsetYAxis, "offset_y_axis"),
                    rotateXy,
                    scaleXAxis,
                    scaleYAxis);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

