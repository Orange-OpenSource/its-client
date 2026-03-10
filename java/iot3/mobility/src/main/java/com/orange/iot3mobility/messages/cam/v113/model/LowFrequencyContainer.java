/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v113.model;

import java.util.List;

/**
 * LowFrequencyContainer v1.1.3
 *
 * @param vehicleRole default(0), publicTransport(1), specialTransport(2), dangerousGoods(3), roadWork(4), rescue(5),
 *                    emergency(6), safetyCar(7), agriculture(8),commercial(9),military(10),roadOperator(11),taxi(12),
 *                    reserved1(13), reserved2(14), reserved3(15)
 * @param exteriorLights Status of the exterior light switches represented as a bit string: lowBeamHeadlightsOn (0),
 *                       highBeamHeadlightsOn (1), leftTurnSignalOn (2), rightTurnSignalOn (3),
 *                       daytimeRunningLightsOn (4), reverseLightOn (5), fogLightOn (6), parkingLightsOn (7)
 * @param pathHistory the path history, a path with a set of {@link PathPoint}
 */
public record LowFrequencyContainer(
        Integer vehicleRole,
        String exteriorLights,
        List<PathPoint> pathHistory) {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for LowFrequencyContainer.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>vehicleRole</li>
     * <li>exteriorLights</li>
     * <li>pathHistory</li>
     * </ul>
     */
    public static final class Builder {
        private Integer vehicleRole;
        private String exteriorLights;
        private List<PathPoint> pathHistory;

        private Builder() {}

        public Builder vehicleRole(int vehicleRole) {
            this.vehicleRole = vehicleRole;
            return this;
        }

        public Builder exteriorLights(String exteriorLights) {
            this.exteriorLights = exteriorLights;
            return this;
        }

        public Builder pathHistory(List<PathPoint> pathHistory) {
            this.pathHistory = pathHistory;
            return this;
        }

        public LowFrequencyContainer build() {
            return new LowFrequencyContainer(
                    requireNonNull(vehicleRole, "vehicle_role"),
                    requireNonNull(exteriorLights, "exterior_lights"),
                    requireNonNull(pathHistory, "path_history"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
