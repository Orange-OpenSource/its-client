/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import java.util.List;

/**
 * Perceived object container.
 *
 * @param perceivedObjects List of {@link PerceivedObject} entries.
 */
public record PerceivedObjectContainer(List<PerceivedObject> perceivedObjects) {

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private List<PerceivedObject> perceivedObjects;

        public Builder perceivedObjects(List<PerceivedObject> perceivedObjects) {
            this.perceivedObjects = perceivedObjects;
            return this;
        }

        public PerceivedObjectContainer build() {
            return new PerceivedObjectContainer(requireNonNull(perceivedObjects, "perceived_object_container"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
