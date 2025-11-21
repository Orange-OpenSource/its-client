/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
*/
package com.orange.iot3core.clients.lwm2m.model;

import io.reactivex.annotations.Nullable;

/**
 * LwM2M Object: IoT3 Identity (Object ID: 36050)
 * <p>
 * Description:
 * This object describes the IoT3 Identity of a User Equipment or a Low‑Latency Application
 * after it has been bootstrapped.
 * <p>
 * Object definition:
 * - Object Version: 1.0 (LwM2M 1.1)
 * - Object URN: urn:oma:lwm2m:x:36050
 * - Instances: Single (presence: Optional)
 * <p>
 * Resources (all [R]):
 * - 0 IoT3 ID: String — Mandatory. Uniquely identifies the UE/LLA in the IoT3 system.
 * - 1 PSK Identity: String — Mandatory. Public part of the PSK used for client authentication.
 * - 2 PSK Secret Key: Opaque (byte[]) — Mandatory. Secret part of the PSK used for client authentication.
 * <p>
 * Class role:
 * - Extends Lwm2mInstance and exposes read‑only resources to the LwM2M server.
 * - Sensitive byte[] values are handled with defensive copies.
 */
public class Lwm2mIoT3Identity extends Lwm2mInstance {

    private static final int OBJECT_ID = 36050;

    private static final int IOT3_ID_RES_ID = 0;
    private static final int PSK_IDENTITY_RES_ID = 1;
    private static final int PSK_SECRET_KEY_RES_ID = 2;

    // All mandatory
    private String iot3Id;
    private String pskIdentity;
    private byte[] pskSecretKey;

    public Lwm2mIoT3Identity() {
        super(OBJECT_ID);
        // Initialize mandatory resources with non-null defaults
        this.iot3Id = "";
        this.pskIdentity = "";
        this.pskSecretKey = new byte[0];
    }

    @Override
    @Nullable
    public ResponseValue read(int resourceId) {
        return switch (resourceId) {
            case IOT3_ID_RES_ID -> getResponseValue(iot3Id, true);
            case PSK_IDENTITY_RES_ID -> getResponseValue(pskIdentity, true);
            case PSK_SECRET_KEY_RES_ID -> getResponseValue(pskSecretKey.clone(), true);
            default -> null;
        };
    }

    @Override
    @Nullable
    protected ResponseValue write(int resourceId, @Nullable Object value) {
        switch (resourceId) {
            case IOT3_ID_RES_ID: // String
                if (!(value instanceof String)) return new ResponseValue(ResponseType.NOT_FOUND);
                this.iot3Id = (String) value;
                onResourcesChange(IOT3_ID_RES_ID);
                return new ResponseValue(ResponseType.SUCCESS);

            case PSK_IDENTITY_RES_ID: // String
                if (!(value instanceof String)) return new ResponseValue(ResponseType.NOT_FOUND);
                this.pskIdentity = (String) value;
                onResourcesChange(PSK_IDENTITY_RES_ID);
                return new ResponseValue(ResponseType.SUCCESS);

            case PSK_SECRET_KEY_RES_ID: // Opaque
                if (!(value instanceof byte[] secret)) return new ResponseValue(ResponseType.NOT_FOUND);
                this.pskSecretKey = secret.clone();
                onResourcesChange(PSK_SECRET_KEY_RES_ID);
                return new ResponseValue(ResponseType.SUCCESS);

            default:
                return new ResponseValue(ResponseType.NOT_FOUND);
        }
    }

    public IoT3Identity toModel() {
        return new IoT3Identity.Builder(iot3Id, pskIdentity, pskSecretKey.clone()).build();
    }

    public void fromModel(IoT3Identity model) {
        this.iot3Id = model.getIot3Id();
        this.pskIdentity = model.getPskIdentity();
        this.pskSecretKey = model.getPskSecretKey(); // returns clone already
        onResourcesChange(0,1,2);
    }

}