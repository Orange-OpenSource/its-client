/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.model;

import com.orange.iot3mobility.messages.denm.v220.model.alacartecontainer.AlacarteContainer;
import com.orange.iot3mobility.messages.denm.v220.model.locationcontainer.LocationContainer;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.SituationContainer;

/**
 * DenmMessage220 - message DENM v2.2.0 (PDU).
 *
 * @param protocolVersion protocol version
 * @param stationId station identifier
 * @param management {@link ManagementContainer}
 * @param situation Optional. {@link SituationContainer}
 * @param location Optional. {@link LocationContainer}
 * @param alacarte Optional. {@link AlacarteContainer}
 */
public record DenmMessage220(
        int protocolVersion,
        long stationId,
        ManagementContainer management,
        SituationContainer situation,
        LocationContainer location,
        AlacarteContainer alacarte) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer protocolVersion;
        private Long stationId;
        private ManagementContainer management;
        private SituationContainer situation;
        private LocationContainer location;
        private AlacarteContainer alacarte;

        public Builder protocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder stationId(long stationId) {
            this.stationId = stationId;
            return this;
        }

        public Builder management(ManagementContainer management) {
            this.management = management;
            return this;
        }

        public Builder situation(SituationContainer situation) {
            this.situation = situation;
            return this;
        }

        public Builder location(LocationContainer location) {
            this.location = location;
            return this;
        }

        public Builder alacarte(AlacarteContainer alacarte) {
            this.alacarte = alacarte;
            return this;
        }

        public DenmMessage220 build() {
            return new DenmMessage220(
                    requireNonNull(protocolVersion, "protocol_version"),
                    requireNonNull(stationId, "station_id"),
                    requireNonNull(management, "management"),
                    situation,
                    location,
                    alacarte);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
