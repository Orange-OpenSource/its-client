package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

/**
 * VRU class.
 *
 * Exactly one of the VRU options should be provided.
 *
 * @param pedestrian Pedestrian type. Value: unavailable(0), ordinary-pedestrian(1), road-worker(2), first-responder(3), max(15).
 * @param bicyclist Bicyclist type. Value: unavailable(0), bicyclist(1), wheelchair-user(2), horse-and-rider(3), rollerskater(4), e-scooter(5), personal-transporter(6), pedelec(7), speed-pedelec(8), max(15).
 * @param motorcylist Motorcyclist type. Value: unavailable(0), moped(1), motorcycle(2), motorcycle-and-sidecar-right(3), motorcycle-and-sidecar-left(4), max(15).
 * @param animal Animal type. Value: unavailable(0), wild-animal(1), farm-animal(2), service-animal(3), max(15).
 */
public record ObjectClassVru(
        Integer pedestrian,
        Integer bicyclist,
        Integer motorcylist,
        Integer animal) {}
