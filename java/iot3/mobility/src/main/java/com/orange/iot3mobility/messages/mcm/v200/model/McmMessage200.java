/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model;

import com.orange.iot3mobility.messages.mcm.v200.model.defs.ReferencePosition;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.ItssRole;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.StationType;

/**
 * Structured JSON payload of an MCM 2.0.0 message (json/raw message_format).
 *
 * @param protocolVersion Protocol version [0..255].
 * @param stationId       ITS-S station identifier [0..4294967295].
 * @param generationDeltaTime TimestampIts modulo 65536 [0..65535].
 * @param stationType     0=VRU, 1=vehicle, 2=RSU, 3=central [0..3].
 * @param itssRole        0=notAvailable, 1=coordinatingItss, 2=notCoordinatingSubjectVehicle,
 *                        3=targetVehicle [0..3].
 * @param position        Position of the originating vehicle (only for vehicle ITS-S).
 * @param mcmData         MCM payload data (vehicle or advised container).
 */
public record McmMessage200(
        int protocolVersion,
        long stationId,
        int generationDeltaTime,
        int stationType,
        int itssRole,
        ReferencePosition position,
        McmData mcmData) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer protocolVersion;
        private Long stationId;
        private Integer generationDeltaTime;
        private Integer stationType;
        private Integer itssRole;
        private ReferencePosition position;
        private McmData mcmData;

        private Builder() {}

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

        public Builder stationType(int stationType) {
            this.stationType = stationType;
            return this;
        }

        /**
         * Sets the station type using the typed enum constant.
         *
         * @param stationType {@link StationType} value
         * @return this builder
         */
        public Builder stationType(StationType stationType) {
            this.stationType = stationType.value;
            return this;
        }

        public Builder itssRole(int itssRole) {
            this.itssRole = itssRole;
            return this;
        }

        /**
         * Sets the ITS-S role using the typed enum constant.
         *
         * @param itssRole {@link ItssRole} value
         * @return this builder
         */
        public Builder itssRole(ItssRole itssRole) {
            this.itssRole = itssRole.value;
            return this;
        }

        public Builder position(ReferencePosition position) {
            this.position = position;
            return this;
        }

        public Builder mcmData(McmData mcmData) {
            this.mcmData = mcmData;
            return this;
        }

        public McmMessage200 build() {
            return new McmMessage200(
                    requireNonNull(protocolVersion, "protocol_version"),
                    requireNonNull(stationId, "station_id"),
                    requireNonNull(generationDeltaTime, "generation_delta_time"),
                    requireNonNull(stationType, "station_type"),
                    itssRole != null ? itssRole : 0,
                    requireNonNull(position, "position"),
                    requireNonNull(mcmData, "mcm_data"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

