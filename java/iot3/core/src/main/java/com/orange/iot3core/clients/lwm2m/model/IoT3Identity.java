/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
*/
package com.orange.iot3core.clients.lwm2m.model;

/**
 * Model for the LwM2M IoT3 Identity object.
 * <p>
 * Object:
 * - Name: IoT3 Identity
 * - Object ID: 36050
 * - Object Version: 1.0 (LwM2M 1.1)
 * - Instances: Single
 * - Object URN: urn:oma:lwm2m:x:36050
 * <p>
 * Resources (Single, all [R] Mandatory):
 * - 0 IoT3 ID: String — Uniquely identifies the User Equipment or Low-Latency Application in the IoT3 system.
 * - 1 PSK Identity: String — Public part of a PSK used for client authentication on an IoT3 service.
 * - 2 PSK Secret Key: Opaque (byte[]) — Secret part of the PSK used for client authentication on an IoT3 service.
 * <p>
 * Immutability & Security:
 * - Any provided byte[] is defensively copied on construction and when returned by getters.
 */
public class IoT3Identity {

    private final String iot3Id;
    private final String pskIdentity;
    private final byte[] pskSecretKey;

    private IoT3Identity(Builder builder) {
        this.iot3Id = builder.iot3Id;
        this.pskIdentity = builder.pskIdentity;
        // Defensive copy to protect internal state
        this.pskSecretKey = builder.pskSecretKey.clone();
    }

    public String getIot3Id() { return iot3Id; }
    public String getPskIdentity() { return pskIdentity; }

    /**
     * Returns a defensive copy of the PSK secret key bytes.
     */
    public byte[] getPskSecretKey() {
        return pskSecretKey.clone();
    }

    /**
     * Builder for creating IoT3IdentityUpdate instances.
     * All fields are mandatory for this object.
     */
    public static class Builder {
        private final String iot3Id;
        private final String pskIdentity;
        private final byte[] pskSecretKey;

        /**
         * Creates a new Builder with all mandatory values.
         *
         * @param iot3Id       Unique IoT3 identifier of the UE/LLA
         * @param pskIdentity  Public PSK identity used for client authentication
         * @param pskSecretKey Secret PSK key bytes used for client authentication
         */
        public Builder(String iot3Id, String pskIdentity, byte[] pskSecretKey) {
            this.iot3Id = iot3Id;
            this.pskIdentity = pskIdentity;
            // Defensive copy to avoid external mutation
            this.pskSecretKey = pskSecretKey.clone();
        }

        public IoT3Identity build() {
            return new IoT3Identity(this);
        }
    }
}