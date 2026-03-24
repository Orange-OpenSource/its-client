/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model;

import com.orange.iot3mobility.messages.cpm.v121.model.freespacecontainer.FreeSpaceAddendumContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.PerceivedObjectContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.SensorInformationContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.StationDataContainer;

/**
 * CPM v1.2.1 message body.
 *
 * @param protocolVersion Version of the message and/or communication protocol. Value: [0..255].
 * @param stationId Identifier. Value: [0..4294967295].
 * @param generationDeltaTime Generation delta time. Unit: millisecond (TimestampIts mod 65536). Value: [0..65535].
 * @param managementContainer {@link ManagementContainer}
 * @param stationDataContainer Optional {@link StationDataContainer}
 * @param sensorInformationContainer Optional {@link SensorInformationContainer}
 * @param perceivedObjectContainer Optional {@link PerceivedObjectContainer}
 * @param freeSpaceAddendumContainer Optional {@link FreeSpaceAddendumContainer}
 */
public record CpmMessage121(
        int protocolVersion,
        long stationId,
        int generationDeltaTime,
        ManagementContainer managementContainer,
        StationDataContainer stationDataContainer,
        SensorInformationContainer sensorInformationContainer,
        PerceivedObjectContainer perceivedObjectContainer,
        FreeSpaceAddendumContainer freeSpaceAddendumContainer) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer protocolVersion;
        private Long stationId;
        private Integer generationDeltaTime;
        private ManagementContainer managementContainer;
        private StationDataContainer stationDataContainer;
        private SensorInformationContainer sensorInformationContainer;
        private PerceivedObjectContainer perceivedObjectContainer;
        private FreeSpaceAddendumContainer freeSpaceAddendumContainer;

        public Builder protocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder stationId(long stationId) {
            this.stationId = stationId;
            return this;
        }

        public Builder generationDeltaTime(int generationDeltaTime) {
            this.generationDeltaTime = generationDeltaTime;
            return this;
        }

        public Builder managementContainer(ManagementContainer managementContainer) {
            this.managementContainer = managementContainer;
            return this;
        }

        public Builder stationDataContainer(StationDataContainer stationDataContainer) {
            this.stationDataContainer = stationDataContainer;
            return this;
        }

        public Builder sensorInformationContainer(SensorInformationContainer sensorInformationContainer) {
            this.sensorInformationContainer = sensorInformationContainer;
            return this;
        }

        public Builder perceivedObjectContainer(PerceivedObjectContainer perceivedObjectContainer) {
            this.perceivedObjectContainer = perceivedObjectContainer;
            return this;
        }

        public Builder freeSpaceAddendumContainer(FreeSpaceAddendumContainer freeSpaceAddendumContainer) {
            this.freeSpaceAddendumContainer = freeSpaceAddendumContainer;
            return this;
        }

        public CpmMessage121 build() {
            return new CpmMessage121(
                    requireNonNull(protocolVersion, "protocol_version"),
                    requireNonNull(stationId, "station_id"),
                    requireNonNull(generationDeltaTime, "generation_delta_time"),
                    requireNonNull(managementContainer, "management_container"),
                    stationDataContainer,
                    sensorInformationContainer,
                    perceivedObjectContainer,
                    freeSpaceAddendumContainer);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
