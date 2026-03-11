package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

/**
 * Object class, one of vehicle, vru, group or other.
 */
public record ObjectClass(
        Integer vehicle,
        ObjectClassVru vru,
        ObjectClassGroup group,
        Integer other) {

    public static ObjectClass vehicle(int vehicle) {
        return new ObjectClass(vehicle, null, null, null);
    }

    public static ObjectClass vru(ObjectClassVru vru) {
        return new ObjectClass(null, vru, null, null);
    }

    public static ObjectClass group(ObjectClassGroup group) {
        return new ObjectClass(null, null, group, null);
    }

    public static ObjectClass other(int other) {
        return new ObjectClass(null, null, null, other);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Integer vehicle;
        private ObjectClassVru vru;
        private ObjectClassGroup group;
        private Integer other;

        public Builder vehicle(Integer vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        public Builder vru(ObjectClassVru vru) {
            this.vru = vru;
            return this;
        }

        public Builder group(ObjectClassGroup group) {
            this.group = group;
            return this;
        }

        public Builder other(Integer other) {
            this.other = other;
            return this;
        }

        public ObjectClass build() {
            int count = 0;
            if (vehicle != null) count++;
            if (vru != null) count++;
            if (group != null) count++;
            if (other != null) count++;
            if (count != 1) {
                throw new IllegalStateException("ObjectClass must contain exactly one non-null field");
            }
            return new ObjectClass(vehicle, vru, group, other);
        }
    }
}

