/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer;

/**
 * Vehicle sensor property item.
 *
 * @param range Range of sensor within the indicated azimuth angle. Unit: 0.1 meter. Value: zeroPointOneMeter(1),
 *              oneMeter(10).
 * @param horizontalOpeningAngleStart Start of the sensor's horizontal opening angle extension. Unit: 0.1 degree.
 *                                    Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param horizontalOpeningAngleEnd End of the sensor's horizontal opening angle extension. Unit: 0.1 degree.
 *                                  Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param verticalOpeningAngleStart Optional. Start of the sensor's vertical opening angle extension. Unit: 0.1 degree.
 *                                  Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param verticalOpeningAngleEnd Optional. End of the sensor's vertical opening angle extension. Unit: 0.1 degree.
 *                                Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 */
public record VehicleSensorProperty(
        int range,
        int horizontalOpeningAngleStart,
        int horizontalOpeningAngleEnd,
        Integer verticalOpeningAngleStart,
        Integer verticalOpeningAngleEnd) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer range;
        private Integer horizontalOpeningAngleStart;
        private Integer horizontalOpeningAngleEnd;
        private Integer verticalOpeningAngleStart;
        private Integer verticalOpeningAngleEnd;

        public Builder range(int range) {
            this.range = range;
            return this;
        }

        public Builder horizontalOpeningAngle(int horizontalOpeningAngleStart, int horizontalOpeningAngleEnd) {
            this.horizontalOpeningAngleStart = horizontalOpeningAngleStart;
            this.horizontalOpeningAngleEnd = horizontalOpeningAngleEnd;
            return this;
        }

        public Builder verticalOpeningAngle(Integer verticalOpeningAngleStart, Integer verticalOpeningAngleEnd) {
            this.verticalOpeningAngleStart = verticalOpeningAngleStart;
            this.verticalOpeningAngleEnd = verticalOpeningAngleEnd;
            return this;
        }

        public VehicleSensorProperty build() {
            return new VehicleSensorProperty(
                    requireNonNull(range, "range"),
                    requireNonNull(horizontalOpeningAngleStart, "horizontal_opening_angle_start"),
                    requireNonNull(horizontalOpeningAngleEnd, "horizontal_opening_angle_end"),
                    verticalOpeningAngleStart,
                    verticalOpeningAngleEnd);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
