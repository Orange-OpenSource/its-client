/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

/**
 * VRU class.
 * <p>
 * Exactly one of the VRU options should be provided.
 *
 * @param pedestrian Pedestrian type. Value: unavailable(0), ordinary-pedestrian(1), road-worker(2), first-responder(3), 
 *                   max(15).
 * @param bicyclist Bicyclist type. Value: unavailable(0), bicyclist(1), wheelchair-user(2), horse-and-rider(3), 
 *                  rollerskater(4), e-scooter(5), personal-transporter(6), pedelec(7), speed-pedelec(8), max(15).
 * @param motorcylist Motorcyclist type. Value: unavailable(0), moped(1), motorcycle(2), 
 *                    motorcycle-and-sidecar-right(3), motorcycle-and-sidecar-left(4), max(15).
 * @param animal Animal type. Value: unavailable(0), wild-animal(1), farm-animal(2), service-animal(3), max(15).
 */
public record ObjectClassVru(
        Integer pedestrian,
        Integer bicyclist,
        Integer motorcylist,
        Integer animal) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer pedestrian;
        private Integer bicyclist;
        private Integer motorcylist;
        private Integer animal;

        public Builder pedestrian(Integer pedestrian) {
            this.pedestrian = pedestrian;
            return this;
        }

        public Builder bicyclist(Integer bicyclist) {
            this.bicyclist = bicyclist;
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
            return new ObjectClassVru(pedestrian, bicyclist, motorcylist, animal);
        }
    }
}
