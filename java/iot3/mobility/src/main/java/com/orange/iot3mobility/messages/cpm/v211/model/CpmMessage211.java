/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model;

import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.originatingvehiclecontainer.OriginatingVehicleContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.originatingrsucontainer.OriginatingRsuContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObjectContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.perceptionregioncontainer.PerceptionRegionContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.sensorinformationcontainer.SensorInformationContainer;

/**
 * CPM v2.1.1
 *
 * @param protocolVersion Version of the ITS message (0..255).
 * @param stationId Identifier for an ITS-S (0..4294967295).
 * @param managementContainer {@link ManagementContainer}
 * @param originatingVehicleContainer Optional {@link OriginatingVehicleContainer}
 * @param originatingRsuContainer Optional {@link OriginatingRsuContainer}
 * @param sensorInformationContainer Optional {@link SensorInformationContainer}
 * @param perceptionRegionContainer Optional {@link PerceptionRegionContainer}
 * @param perceivedObjectContainer Optional {@link PerceivedObjectContainer}
 */
public record CpmMessage211(
        int protocolVersion,
        long stationId,
        ManagementContainer managementContainer,
        OriginatingVehicleContainer originatingVehicleContainer,
        OriginatingRsuContainer originatingRsuContainer,
        SensorInformationContainer sensorInformationContainer,
        PerceptionRegionContainer perceptionRegionContainer,
        PerceivedObjectContainer perceivedObjectContainer) {

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Integer protocolVersion;
        private Long stationId;
        private ManagementContainer managementContainer;
        private OriginatingVehicleContainer originatingVehicleContainer;
        private OriginatingRsuContainer originatingRsuContainer;
        private SensorInformationContainer sensorInformationContainer;
        private PerceptionRegionContainer perceptionRegionContainer;
        private PerceivedObjectContainer perceivedObjectContainer;

        public Builder protocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder stationId(long stationId) {
            this.stationId = stationId;
            return this;
        }

        public Builder managementContainer(ManagementContainer managementContainer) {
            this.managementContainer = managementContainer;
            return this;
        }

        public Builder originatingVehicleContainer(OriginatingVehicleContainer originatingVehicleContainer) {
            this.originatingVehicleContainer = originatingVehicleContainer;
            return this;
        }

        public Builder originatingRsuContainer(OriginatingRsuContainer originatingRsuContainer) {
            this.originatingRsuContainer = originatingRsuContainer;
            return this;
        }

        public Builder sensorInformationContainer(SensorInformationContainer sensorInformationContainer) {
            this.sensorInformationContainer = sensorInformationContainer;
            return this;
        }

        public Builder perceptionRegionContainer(PerceptionRegionContainer perceptionRegionContainer) {
            this.perceptionRegionContainer = perceptionRegionContainer;
            return this;
        }

        public Builder perceivedObjectContainer(PerceivedObjectContainer perceivedObjectContainer) {
            this.perceivedObjectContainer = perceivedObjectContainer;
            return this;
        }

        public CpmMessage211 build() {
            return new CpmMessage211(
                    requireNonNull(protocolVersion, "protocol_version"),
                    requireNonNull(stationId, "station_id"),
                    requireNonNull(managementContainer, "management_container"),
                    originatingVehicleContainer,
                    originatingRsuContainer,
                    sensorInformationContainer,
                    perceptionRegionContainer,
                    perceivedObjectContainer);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}
