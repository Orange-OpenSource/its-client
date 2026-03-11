package com.orange.iot3mobility.messages.cpm.v211.model.perceptionregioncontainer;

import java.util.List;

/**
 * Perception region container.
 */
public record PerceptionRegionContainer(List<PerceptionRegion> perceptionRegions) {

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private List<PerceptionRegion> perceptionRegions;

        public Builder perceptionRegions(List<PerceptionRegion> perceptionRegions) {
            this.perceptionRegions = perceptionRegions;
            return this;
        }

        public PerceptionRegionContainer build() {
            return new PerceptionRegionContainer(requireNonNull(perceptionRegions, "perception_region_container"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}

