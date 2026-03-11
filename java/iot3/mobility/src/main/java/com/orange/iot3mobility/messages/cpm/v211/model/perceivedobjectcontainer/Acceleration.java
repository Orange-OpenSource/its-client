package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

/**
 * Acceleration vector of an object.
 */
public record Acceleration(PolarAcceleration polarAcceleration, CartesianAcceleration cartesianAcceleration) {

    public static Acceleration polar(PolarAcceleration polarAcceleration) {
        return new Acceleration(polarAcceleration, null);
    }

    public static Acceleration cartesian(CartesianAcceleration cartesianAcceleration) {
        return new Acceleration(null, cartesianAcceleration);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private PolarAcceleration polarAcceleration;
        private CartesianAcceleration cartesianAcceleration;

        public Builder polarAcceleration(PolarAcceleration polarAcceleration) {
            this.polarAcceleration = polarAcceleration;
            return this;
        }

        public Builder cartesianAcceleration(CartesianAcceleration cartesianAcceleration) {
            this.cartesianAcceleration = cartesianAcceleration;
            return this;
        }

        public Acceleration build() {
            int count = 0;
            if (polarAcceleration != null) count++;
            if (cartesianAcceleration != null) count++;
            if (count != 1) {
                throw new IllegalStateException("Acceleration must contain exactly one non-null field");
            }
            return new Acceleration(polarAcceleration, cartesianAcceleration);
        }
    }
}

