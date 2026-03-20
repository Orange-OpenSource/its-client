/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.originatingrsucontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.MapReference;

import java.util.List;

/**
 * Originating RSU container
 *
 * @param mapReferences List of {@link MapReference} identifying MAPEM topology references for perceived objects.
 */
public record OriginatingRsuContainer(List<MapReference> mapReferences) {

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private List<MapReference> mapReferences;

        public Builder mapReferences(List<MapReference> mapReferences) {
            this.mapReferences = mapReferences;
            return this;
        }

        public OriginatingRsuContainer build() {
            return new OriginatingRsuContainer(requireNonNull(mapReferences, "originating_rsu_container"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
