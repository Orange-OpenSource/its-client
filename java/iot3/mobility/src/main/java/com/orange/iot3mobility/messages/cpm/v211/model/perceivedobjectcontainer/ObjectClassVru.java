package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

/**
 * VRU object class.
 * <p>
 * Exactly one option shall be provided.
 *
 * @param pedestrian Pedestrian subclasses. Examples: unavailable (0), ordinary-pedestrian (1), road-worker (2),
 *                   first-responder (3), max (15).
 * @param bicyclistAndLightVruVehicle Bicyclist/light VRU subclasses. Examples: unavailable (0), bicyclist (1),
 *                                   wheelchair-user (2), horse-and-rider (3), rollerskater (4), e-scooter (5),
 *                                   personal-transporter (6), pedelec (7), speed-pedelec (8), max (15).
 * @param motorcylist Motorcyclist subclasses. Examples: unavailable (0), moped (1), motorcycle (2),
 *                    motorcycle-and-sidecar-right (3), motorcycle-and-sidecar-left (4), max (15).
 * @param animal Animal subclasses. Examples: unavailable (0), wild-animal (1), farm-animal (2), service-animal (3),
 *               max (15).
 */
public record ObjectClassVru(
        Integer pedestrian,
        Integer bicyclistAndLightVruVehicle,
        Integer motorcylist,
        Integer animal) {

    public static ObjectClassVru pedestrian(int pedestrian) {
        return new ObjectClassVru(pedestrian, null, null, null);
    }

    public static ObjectClassVru bicyclistAndLightVruVehicle(int value) {
        return new ObjectClassVru(null, value, null, null);
    }

    public static ObjectClassVru motorcylist(int motorcylist) {
        return new ObjectClassVru(null, null, motorcylist, null);
    }

    public static ObjectClassVru animal(int animal) {
        return new ObjectClassVru(null, null, null, animal);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Integer pedestrian;
        private Integer bicyclistAndLightVruVehicle;
        private Integer motorcylist;
        private Integer animal;

        public Builder pedestrian(Integer pedestrian) {
            this.pedestrian = pedestrian;
            return this;
        }

        public Builder bicyclistAndLightVruVehicle(Integer bicyclistAndLightVruVehicle) {
            this.bicyclistAndLightVruVehicle = bicyclistAndLightVruVehicle;
            return this;
        }

        public Builder motorcylist(Integer motorcylist) {
            this.motorcylist = motorcylist;
            return this;
        }

        public Builder animal(Integer animal) {
            this.animal = animal;
            return this;
        }

        public ObjectClassVru build() {
            int count = 0;
            if (pedestrian != null) count++;
            if (bicyclistAndLightVruVehicle != null) count++;
            if (motorcylist != null) count++;
            if (animal != null) count++;
            if (count != 1) {
                throw new IllegalStateException("ObjectClassVru must contain exactly one non-null field");
            }
            return new ObjectClassVru(pedestrian, bicyclistAndLightVruVehicle, motorcylist, animal);
        }
    }
}
