/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.model;

import com.orange.iot3mobility.messages.denm.v113.model.alacartecontainer.AlacarteContainer;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.LocationContainer;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.SituationContainer;

/**
 * DenmMessage113 - message DENM v1.1.3 (PDU).
 *
 * @param protocolVersion protocol version
 * @param stationId station identifier
 * @param managementContainer {@link com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ManagementContainer}
 * @param situationContainer Optional. {@link com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.SituationContainer}
 * @param locationContainer Optional. {@link com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.LocationContainer}
 * @param alacarteContainer Optional. {@link com.orange.iot3mobility.messages.denm.v113.model.alacartecontainer.AlacarteContainer}
 */
public record DenmMessage113(
        int protocolVersion,
        long stationId,
        ManagementContainer managementContainer,
        SituationContainer situationContainer,
        LocationContainer locationContainer,
        AlacarteContainer alacarteContainer) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer protocolVersion;
        private Long stationId;
        private ManagementContainer managementContainer;
        private SituationContainer situationContainer;
        private LocationContainer locationContainer;
        private AlacarteContainer alacarteContainer;

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

        public Builder situationContainer(SituationContainer situationContainer) {
            this.situationContainer = situationContainer;
            return this;
        }

        public Builder locationContainer(LocationContainer locationContainer) {
            this.locationContainer = locationContainer;
            return this;
        }

        public Builder alacarteContainer(AlacarteContainer alacarteContainer) {
            this.alacarteContainer = alacarteContainer;
            return this;
        }

        public DenmMessage113 build() {
            return new DenmMessage113(
                    requireNonNull(protocolVersion, "protocol_version"),
                    requireNonNull(stationId, "station_id"),
                    requireNonNull(managementContainer, "management_container"),
                    situationContainer,
                    locationContainer,
                    alacarteContainer);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
