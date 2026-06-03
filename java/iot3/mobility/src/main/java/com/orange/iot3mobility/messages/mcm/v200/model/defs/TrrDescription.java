/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.defs;

import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.TrrType;
import java.util.List;

/**
 * Description of a Target Road Resource (TRR): the geographic area covered by a sub-manoeuvre.
 *
 * @param trrType trrType1(0): box by position, trrType2(1): box by vehicles,
 *                trrType3(2): trajectory-based. [0..2].
 * @param laneCount Number of adjacent lanes covered [0..31].
 * @param startingLaneNumber Optional. Starting lane number [0..31].
 * @param endingLaneNumber   Optional. Ending lane number [0..31].
 * @param waypoints          Optional. List of waypoints (used for type 3).
 * @param heading            Optional. List of heading angles.
 * @param trrWidth           Width computed from lane count [0..15].
 * @param trrLength          Distance between start and end waypoints [0..4095].
 */
public record TrrDescription(
        int trrType,
        int laneCount,
        Integer startingLaneNumber,
        Integer endingLaneNumber,
        List<WayPoint> waypoints,
        List<Wgs84Angle> heading,
        int trrWidth,
        int trrLength) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer trrType;
        private Integer laneCount;
        private Integer startingLaneNumber;
        private Integer endingLaneNumber;
        private List<WayPoint> waypoints;
        private List<Wgs84Angle> heading;
        private Integer trrWidth;
        private Integer trrLength;

        private Builder() {}

        public Builder trrType(int trrType) {
            this.trrType = trrType;
            return this;
        }
        
        /**
         * Sets the TRR type using the typed enum constant.
         *
         * @param trrType {@link TrrType} value
         * @return this builder
         */
        public Builder trrType(TrrType trrType) {
            this.trrType = trrType.value;
            return this;
        }
        
        public Builder laneCount(int laneCount) {
            this.laneCount = laneCount;
            return this;
        }

        public Builder startingLaneNumber(Integer startingLaneNumber) {
            this.startingLaneNumber = startingLaneNumber;
            return this;
        }

        public Builder endingLaneNumber(Integer endingLaneNumber) {
            this.endingLaneNumber = endingLaneNumber;
            return this;
        }

        public Builder waypoints(List<WayPoint> waypoints) {
            this.waypoints = waypoints;
            return this;
        }

        public Builder heading(List<Wgs84Angle> heading) {
            this.heading = heading;
            return this;
        }

        public Builder trrWidth(int trrWidth) {
            this.trrWidth = trrWidth;
            return this;
        }

        public Builder trrLength(int trrLength) {
            this.trrLength = trrLength;
            return this;
        }

        public TrrDescription build() {
            return new TrrDescription(
                    requireNonNull(trrType, "trr_type"),
                    requireNonNull(laneCount, "lane_count"),
                    startingLaneNumber,
                    endingLaneNumber,
                    waypoints,
                    heading,
                    requireNonNull(trrWidth, "trr_width"),
                    requireNonNull(trrLength, "trr_length"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

