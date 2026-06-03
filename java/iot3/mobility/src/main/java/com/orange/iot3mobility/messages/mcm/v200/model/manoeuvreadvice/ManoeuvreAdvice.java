/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice;

import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.CurrentStateAdvisedChange;
import java.util.List;

/**
 * An advice entry directed to a specific ITS-S (executant).
 *
 * @param executantId              ITS-S station ID [0..4294967295].
 * @param currentStateAdvisedChange Advised state change string. One of the values in the
 *                                  {@code current_state_advised_change} enum of the schema.
 * @param submaneuvres             List of advised sub-manoeuvres.
 */
public record ManoeuvreAdvice(
        long executantId,
        String currentStateAdvisedChange,
        List<AdvisedSubmanoeuvre> submaneuvres) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long executantId;
        private String currentStateAdvisedChange;
        private List<AdvisedSubmanoeuvre> submaneuvres;

        private Builder() {}

        public Builder executantId(long executantId) {
            this.executantId = executantId;
            return this;
        }

        public Builder currentStateAdvisedChange(String currentStateAdvisedChange) {
            this.currentStateAdvisedChange = currentStateAdvisedChange;
            return this;
        }

        /**
         * Sets the advised state change using the typed enum constant.
         *
         * @param change {@link CurrentStateAdvisedChange} value
         * @return this builder
         */
        public Builder currentStateAdvisedChange(CurrentStateAdvisedChange change) {
            this.currentStateAdvisedChange = change.value;
            return this;
        }

        public Builder submaneuvres(List<AdvisedSubmanoeuvre> submaneuvres) {
            this.submaneuvres = submaneuvres;
            return this;
        }

        public ManoeuvreAdvice build() {
            return new ManoeuvreAdvice(
                    requireNonNull(executantId, "executant_id"),
                    requireNonNull(currentStateAdvisedChange, "current_state_advised_change"),
                    requireNonNull(submaneuvres, "submaneuvres"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

