/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.shared.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * User types that may be bound to a restriction class (ETSI TS 103 301 – {@code restriction_user_type}).
 * <p>
 * Used in {@code RestrictionClassAssignment.users}.
 */
public enum RestrictionUserType {

    /** No specific user type restriction. */
    NONE("none"),

    /** Equipped transit vehicles. */
    EQUIPPED_TRANSIT("equippedTransit"),

    /** Equipped taxi vehicles. */
    EQUIPPED_TAXIS("equippedTaxis"),

    /** Other equipped vehicles. */
    EQUIPPED_OTHER("equippedOther"),

    /** Emission-compliant vehicles. */
    EMISSION_COMPLIANT("emissionCompliant"),

    /** Equipped bicycles. */
    EQUIPPED_BICYCLE("equippedBicycle"),

    /** Weight-compliant vehicles. */
    WEIGHT_COMPLIANT("weightCompliant"),

    /** Height-compliant vehicles. */
    HEIGHT_COMPLIANT("heightCompliant"),

    /** Pedestrians. */
    PEDESTRIANS("pedestrians"),

    /** Slow-moving persons (e.g. seniors, persons with reduced mobility). */
    SLOW_MOVING_PERSONS("slowMovingPersons"),

    /** Wheelchair users. */
    WHEELCHAIR_USERS("wheelchairUsers"),

    /** Persons with visual disabilities. */
    VISUAL_DISABILITIES("visualDisabilities"),

    /** Persons with audio disabilities. */
    AUDIO_DISABILITIES("audioDisabilities"),

    /** Other unknown disability types. */
    OTHER_UNKNOWN_DISABILITIES("otherUnknownDisabilities");

    private final String jsonValue;

    private static final Map<String, RestrictionUserType> BY_VALUE = new HashMap<>();

    static {
        for (RestrictionUserType restrictionUserType : values()) {
            BY_VALUE.put(restrictionUserType.jsonValue, restrictionUserType);
        }
    }

    RestrictionUserType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    /**
     * Returns the JSON string value as defined in the ETSI TS 103 301 schema.
     *
     * @return the exact string used in MAPEM JSON payloads
     */
    public String value() {
        return jsonValue;
    }

    /**
     * Resolve a JSON string to the corresponding {@link RestrictionUserType}, or {@code null}
     * if the value is not recognised.
     *
     * @param jsonValue the raw string from a MAPEM JSON payload
     * @return the matching constant, or {@code null}
     */
    public static RestrictionUserType fromValue(String jsonValue) {
        return BY_VALUE.get(jsonValue);
    }
}

