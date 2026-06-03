/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer;

import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums.Concept;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums.ExecutionStatus;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums.McmType;

/**
 * Generic state container common to all MCM types.
 *
 * @param mcmType Type of MCM [0..8].
 *   intent(0), request(1), response(2), reservation(3), termination(4),
 *   cancellationRequest(5), emergencyManoeuvreReservation(6), executionStatus(7), offer(8).
 * @param manoeuvreId MCS session ID modulo 256 [0..255].
 * @param concept agreementSeeking(0), prescriptive(1).
 * @param rational Optional. Rational (goal or cost) associated to this MCM.
 * @param executionStatus Optional. Only in type 7 and 4. started(0), inProgress(1),
 *   completed(2), terminated(3), chained(4).
 */
public record McmGenericCurrentStateContainer(
        int mcmType,
        int manoeuvreId,
        int concept,
        Rational rational,
        Integer executionStatus) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer mcmType;
        private Integer manoeuvreId;
        private Integer concept;
        private Rational rational;
        private Integer executionStatus;

        private Builder() {}

        public Builder mcmType(int mcmType) {
            this.mcmType = mcmType;
            return this;
        }

        /**
         * Sets the MCM type using the typed enum constant.
         *
         * @param mcmType {@link McmType} value
         * @return this builder
         */
        public Builder mcmType(McmType mcmType) {
            this.mcmType = mcmType.value;
            return this;
        }

        public Builder manoeuvreId(int manoeuvreId) {
            this.manoeuvreId = manoeuvreId;
            return this;
        }

        public Builder concept(int concept) {
            this.concept = concept;
            return this;
        }

        /**
         * Sets the manoeuvre coordination concept using the typed enum constant.
         *
         * @param concept {@link Concept} value
         * @return this builder
         */
        public Builder concept(Concept concept) {
            this.concept = concept.value;
            return this;
        }

        public Builder rational(Rational rational) {
            this.rational = rational;
            return this;
        }

        public Builder executionStatus(Integer executionStatus) {
            this.executionStatus = executionStatus;
            return this;
        }

        /**
         * Sets the execution status using the typed enum constant.
         *
         * @param status {@link ExecutionStatus} value
         * @return this builder
         */
        public Builder executionStatus(ExecutionStatus status) {
            this.executionStatus = status.value;
            return this;
        }

        public McmGenericCurrentStateContainer build() {
            return new McmGenericCurrentStateContainer(
                    requireNonNull(mcmType, "mcm_type"),
                    requireNonNull(manoeuvreId, "manoeuvre_id"),
                    requireNonNull(concept, "concept"),
                    rational,
                    executionStatus);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

