/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model.locationcontainer;

import java.util.List;

/**
 * LocationContainer - DENM v2.2.0 location container.
 *
 * @param eventSpeed Optional. {@link EventSpeed}
 * @param eventPositionHeading Optional. {@link EventPositionHeading}
 * @param detectionZonesToEventPosition Optional. {@link DetectionZone} list (1 to 7)
 * @param roadType Optional. Type of road segment. Range: 0-3
 */
public record LocationContainer(
        EventSpeed eventSpeed,
        EventPositionHeading eventPositionHeading,
        List<DetectionZone> detectionZonesToEventPosition,
        Integer roadType) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private EventSpeed eventSpeed;
        private EventPositionHeading eventPositionHeading;
        private List<DetectionZone> detectionZonesToEventPosition;
        private Integer roadType;

        public Builder eventSpeed(EventSpeed eventSpeed) {
            this.eventSpeed = eventSpeed;
            return this;
        }

        public Builder eventPositionHeading(EventPositionHeading eventPositionHeading) {
            this.eventPositionHeading = eventPositionHeading;
            return this;
        }

        public Builder detectionZonesToEventPosition(List<DetectionZone> detectionZonesToEventPosition) {
            this.detectionZonesToEventPosition = detectionZonesToEventPosition;
            return this;
        }

        public Builder roadType(Integer roadType) {
            this.roadType = roadType;
            return this;
        }

        public LocationContainer build() {
            return new LocationContainer(
                    eventSpeed,
                    eventPositionHeading,
                    detectionZonesToEventPosition,
                    roadType);
        }
    }
}
