/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

/**
 * Velocity vector of an object.
 * <p>
 * Exactly one of {@link PolarVelocity} or {@link CartesianVelocity} shall be provided.
 *
 * @param polarVelocity Velocity in a polar or cylindrical coordinate system.
 * @param cartesianVelocity Velocity in a cartesian coordinate system.
 */
public record Velocity(PolarVelocity polarVelocity, CartesianVelocity cartesianVelocity) {

    public static Velocity polar(PolarVelocity polarVelocity) {
        return new Velocity(polarVelocity, null);
    }

    public static Velocity cartesian(CartesianVelocity cartesianVelocity) {
        return new Velocity(null, cartesianVelocity);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private PolarVelocity polarVelocity;
        private CartesianVelocity cartesianVelocity;

        public Builder polarVelocity(PolarVelocity polarVelocity) {
            this.polarVelocity = polarVelocity;
            return this;
        }

        public Builder cartesianVelocity(CartesianVelocity cartesianVelocity) {
            this.cartesianVelocity = cartesianVelocity;
            return this;
        }

        public Velocity build() {
            int count = 0;
            if (polarVelocity != null) count++;
            if (cartesianVelocity != null) count++;
            if (count != 1) {
                throw new IllegalStateException("Velocity must contain exactly one non-null field");
            }
            return new Velocity(polarVelocity, cartesianVelocity);
        }
    }
}
