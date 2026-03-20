package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Identifies the MAPEM containing the topology information reference.
 * <p>
 * Exactly one of {@link RoadSegment} or {@link Intersection} shall be provided.
 *
 * @param roadSegment Road segment reference.
 * @param intersection Intersection reference.
 */
public record MapReference(RoadSegment roadSegment, Intersection intersection) {

    public static MapReference roadSegment(RoadSegment roadSegment) {
        return new MapReference(roadSegment, null);
    }

    public static MapReference intersection(Intersection intersection) {
        return new MapReference(null, intersection);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private RoadSegment roadSegment;
        private Intersection intersection;

        public Builder roadSegment(RoadSegment roadSegment) {
            this.roadSegment = roadSegment;
            return this;
        }

        public Builder intersection(Intersection intersection) {
            this.intersection = intersection;
            return this;
        }

        public MapReference build() {
            int count = 0;
            if (roadSegment != null) count++;
            if (intersection != null) count++;
            if (count != 1) {
                throw new IllegalStateException("MapReference must contain exactly one non-null field");
            }
            return new MapReference(roadSegment, intersection);
        }
    }
}
