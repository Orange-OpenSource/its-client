package com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer;

import com.orange.iot3mobility.messages.cpm.v121.model.defs.Offset;

/**
 * Stationary radial sensor detection area.
 *
 * @param range Radial range of the sensor from the reference point. Unit: 0.1 meter. Value: zeroPointOneMeter(1),
 *              oneMeter(10).
 * @param horizontalOpeningAngleStart Start of the stationary sensor's horizontal opening angle in WGS84.
 *                                    Unit: 0.1 degree. Value: wgs84North(0), wgs84East(900), wgs84South(1800),
 *                                    wgs84West(2700), unavailable(3601).
 * @param horizontalOpeningAngleEnd End of the stationary sensor's horizontal opening angle in WGS84.
 *                                  Unit: 0.1 degree. Value: wgs84North(0), wgs84East(900), wgs84South(1800),
 *                                  wgs84West(2700), unavailable(3601).
 * @param verticalOpeningAngleStart Optional. Start of the stationary sensor's vertical opening angle. Unit: 0.1 degree.
 *                                  Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param verticalOpeningAngleEnd Optional. End of the stationary sensor's vertical opening angle. Unit: 0.1 degree.
 *                                Value: zeroPointOneDegree(1), oneDegree(10), unavailable(3601).
 * @param sensorPositionOffset Optional. {@link Offset} of the mounting point from the station's reference position.
 */
public record StationarySensorRadial(
        int range,
        int horizontalOpeningAngleStart,
        int horizontalOpeningAngleEnd,
        Integer verticalOpeningAngleStart,
        Integer verticalOpeningAngleEnd,
        Offset sensorPositionOffset) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer range;
        private Integer horizontalOpeningAngleStart;
        private Integer horizontalOpeningAngleEnd;
        private Integer verticalOpeningAngleStart;
        private Integer verticalOpeningAngleEnd;
        private Offset sensorPositionOffset;

        public Builder range(Integer range) {
            this.range = range;
            return this;
        }

        public Builder horizontalOpeningAngleStart(Integer horizontalOpeningAngleStart) {
            this.horizontalOpeningAngleStart = horizontalOpeningAngleStart;
            return this;
        }

        public Builder horizontalOpeningAngleEnd(Integer horizontalOpeningAngleEnd) {
            this.horizontalOpeningAngleEnd = horizontalOpeningAngleEnd;
            return this;
        }

        public Builder verticalOpeningAngleStart(Integer verticalOpeningAngleStart) {
            this.verticalOpeningAngleStart = verticalOpeningAngleStart;
            return this;
        }

        public Builder verticalOpeningAngleEnd(Integer verticalOpeningAngleEnd) {
            this.verticalOpeningAngleEnd = verticalOpeningAngleEnd;
            return this;
        }

        public Builder sensorPositionOffset(Offset sensorPositionOffset) {
            this.sensorPositionOffset = sensorPositionOffset;
            return this;
        }

        public StationarySensorRadial build() {
            return new StationarySensorRadial(
                    requireNonNull(range, "range"),
                    requireNonNull(horizontalOpeningAngleStart, "horizontal_opening_angle_start"),
                    requireNonNull(horizontalOpeningAngleEnd, "horizontal_opening_angle_end"),
                    verticalOpeningAngleStart,
                    verticalOpeningAngleEnd,
                    sensorPositionOffset);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
