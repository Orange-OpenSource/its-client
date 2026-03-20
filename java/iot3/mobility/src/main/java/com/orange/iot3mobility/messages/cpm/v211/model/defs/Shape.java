/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Definition of a geographical area or volume, based on different options.
 * <p>
 * Exactly one of {@link Rectangular}, {@link Circular}, {@link Polygonal}, {@link Elliptical}, {@link Radial} or
 * {@link RadialShapes} shall be provided.
 *
 * @param rectangular Rectangular shape definition.
 * @param circular Circular shape definition.
 * @param polygonal Polygonal shape definition.
 * @param elliptical Elliptical shape definition.
 * @param radial Radial shape definition.
 * @param radialShapes Set of radial shapes with an offset reference point.
 */
public record Shape(
        Rectangular rectangular,
        Circular circular,
        Polygonal polygonal,
        Elliptical elliptical,
        Radial radial,
        RadialShapes radialShapes) {

    public static Shape rectangular(Rectangular rectangular) {
        return new Shape(rectangular, null, null, null, null, null);
    }

    public static Shape circular(Circular circular) {
        return new Shape(null, circular, null, null, null, null);
    }

    public static Shape polygonal(Polygonal polygonal) {
        return new Shape(null, null, polygonal, null, null, null);
    }

    public static Shape elliptical(Elliptical elliptical) {
        return new Shape(null, null, null, elliptical, null, null);
    }

    public static Shape radial(Radial radial) {
        return new Shape(null, null, null, null, radial, null);
    }

    public static Shape radialShapes(RadialShapes radialShapes) {
        return new Shape(null, null, null, null, null, radialShapes);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Rectangular rectangular;
        private Circular circular;
        private Polygonal polygonal;
        private Elliptical elliptical;
        private Radial radial;
        private RadialShapes radialShapes;

        public Builder rectangular(Rectangular value) {
            this.rectangular = value;
            return this;
        }

        public Builder circular(Circular value) {
            this.circular = value;
            return this;
        }

        public Builder polygonal(Polygonal value) {
            this.polygonal = value;
            return this;
        }

        public Builder elliptical(Elliptical value) {
            this.elliptical = value;
            return this;
        }

        public Builder radial(Radial value) {
            this.radial = value;
            return this;
        }

        public Builder radialShapes(RadialShapes value) {
            this.radialShapes = value;
            return this;
        }

        public Shape build() {
            int count = 0;
            if (rectangular != null) count++;
            if (circular != null) count++;
            if (polygonal != null) count++;
            if (elliptical != null) count++;
            if (radial != null) count++;
            if (radialShapes != null) count++;

            if (count != 1) {
                throw new IllegalStateException("Shape must contain exactly one non-null field");
            }

            return new Shape(rectangular, circular, polygonal, elliptical, radial, radialShapes);
        }
    }
}
