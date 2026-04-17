/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.model;

import com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionState;

import java.util.List;

/**
 * SPAT message payload, carrying signal phase and timing for one or more intersections.
 *
 * @param protocolVersion Required. Protocol version [0..255].
 * @param stationId       Required. Originating station ID [0..4294967295].
 * @param intersections   Required. List of IntersectionState entries [1..32].
 * @param timestamp       Optional. Minute of current UTC year [0..527040].
 * @param name            Optional. Human-readable name for this collection. For debug use only.
 */
public record SpatemMessage200(
        int protocolVersion,
        long stationId,
        List<IntersectionState> intersections,
        Integer timestamp,
        String name) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer protocolVersion;
        private Long stationId;
        private List<IntersectionState> intersections;
        private Integer timestamp;
        private String name;

        private Builder() {}

        public Builder protocolVersion(int protocolVersion) { this.protocolVersion = protocolVersion; return this; }
        public Builder stationId(long stationId) { this.stationId = stationId; return this; }
        public Builder intersections(List<IntersectionState> intersections) { this.intersections = intersections; return this; }
        public Builder timestamp(Integer timestamp) { this.timestamp = timestamp; return this; }
        public Builder name(String name) { this.name = name; return this; }

        public SpatemMessage200 build() {
            return new SpatemMessage200(
                    requireNonNull(protocolVersion, "protocolVersion"),
                    requireNonNull(stationId, "stationId"),
                    requireNonNull(intersections, "intersections"),
                    timestamp, name);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

