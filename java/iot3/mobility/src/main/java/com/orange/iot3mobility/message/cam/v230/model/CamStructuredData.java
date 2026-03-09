/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model;

import com.orange.iot3mobility.message.cam.v230.model.basiccontainer.BasicContainer;
import com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer.HighFrequencyContainer;
import com.orange.iot3mobility.message.cam.v230.model.lowfrequencycontainer.LowFrequencyContainer;
import com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer.SpecialVehicleContainer;

/**
 * CAM v2.3.0
 * <p>
 * Structured JSON CAM payload.
 *
 * @param protocolVersion Version of the ITS message
 * @param stationId Identifier for an ITS-S
 * @param generationDeltaTime Time of the reference position in the CAM, considered as time of the CAM generation.
 *                            TimestampIts mod 65 536. TimestampIts represents an integer value in milliseconds since
 *                            2004-01-01T00:00:00:000Z. oneMilliSec(1)
 * @param basicContainer {@link BasicContainer}
 * @param highFrequencyContainer {@link HighFrequencyContainer}
 * @param lowFrequencyContainer Optional {@link LowFrequencyContainer}
 * @param specialVehicleContainer Optional {@link SpecialVehicleContainer}
 */
public record CamStructuredData(
        int protocolVersion,
        long stationId,
        int generationDeltaTime,
        BasicContainer basicContainer,
        HighFrequencyContainer highFrequencyContainer,
        LowFrequencyContainer lowFrequencyContainer,
        SpecialVehicleContainer specialVehicleContainer) implements CamMessage230 {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for CamStructuredData.
     * <p>
     * Mandatory fields:
     * <ul>
     * <li>protocolVersion</li>
     * <li>stationId</li>
     * <li>generationDeltaTime</li>
     * <li>basicContainer</li>
     * <li>highFrequencyContainer</li>
     * </ul>
     */
    public static final class Builder {
        private Integer protocolVersion;
        private Long stationId;
        private Integer generationDeltaTime;
        private BasicContainer basicContainer;
        private HighFrequencyContainer highFrequencyContainer;
        private LowFrequencyContainer lowFrequencyContainer;
        private SpecialVehicleContainer specialVehicleContainer;

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

        public Builder basicContainer(BasicContainer basicContainer) {
            this.basicContainer = basicContainer;
            return this;
        }

        public Builder highFrequencyContainer(HighFrequencyContainer highFrequencyContainer) {
            this.highFrequencyContainer = highFrequencyContainer;
            return this;
        }

        public Builder lowFrequencyContainer(LowFrequencyContainer lowFrequencyContainer) {
            this.lowFrequencyContainer = lowFrequencyContainer;
            return this;
        }

        public Builder specialVehicleContainer(SpecialVehicleContainer specialVehicleContainer) {
            this.specialVehicleContainer = specialVehicleContainer;
            return this;
        }

        public CamStructuredData build() {
            return new CamStructuredData(
                    requireNonNull(protocolVersion, "protocol_version"),
                    requireNonNull(stationId, "station_id"),
                    requireNonNull(generationDeltaTime, "generation_delta_time"),
                    requireNonNull(basicContainer, "basic_container"),
                    requireNonNull(highFrequencyContainer, "high_frequency_container"),
                    lowFrequencyContainer,
                    specialVehicleContainer);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
