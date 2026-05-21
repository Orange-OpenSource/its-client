/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

/**
 * A single node point in a lane path.
 *
 * @param delta The position of this node, either as an XY offset or absolute lat/lon.
 * @param attributes Optional. Node-level attribute data.
 */
public record NodeXY(NodeDelta delta, NodeAttributes attributes) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private NodeDelta delta;
        private NodeAttributes attributes;

        private Builder() {}

        public Builder delta(NodeDelta delta) {
            this.delta = delta;
            return this;
        }

        public Builder attributes(NodeAttributes attributes) {
            this.attributes = attributes;
            return this;
        }

        public NodeXY build() {
            return new NodeXY(requireNonNull(delta, "delta"), attributes);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}

