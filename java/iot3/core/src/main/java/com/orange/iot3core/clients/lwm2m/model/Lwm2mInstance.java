/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
*/
package com.orange.iot3core.clients.lwm2m.model;

import io.reactivex.annotations.Beta;
import io.reactivex.annotations.Nullable;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.resource.BaseInstanceEnablerFactory;
import org.eclipse.leshan.client.resource.LwM2mInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;

import java.util.Date;

public abstract class Lwm2mInstance {

    private final int objectId;

    @Nullable
    private BaseInstanceEnabler instanceEnabler = null;

    @Beta
    private final BaseInstanceEnablerFactory instanceEnablerFactory = new BaseInstanceEnablerFactory() {

        @Override
        public LwM2mInstanceEnabler create() {
            instanceEnabler = new InternalInstanceEnabler();
            return instanceEnabler;
        }

    };

    public Lwm2mInstance(int objectId) {
        this.objectId = objectId;
    }

    public int getObjectId() {
        return objectId;
    }

    public BaseInstanceEnabler getInstanceEnabler() {
        return instanceEnabler;
    }

    public BaseInstanceEnablerFactory getInstanceEnablerFactory() {
        return instanceEnablerFactory;
    }

    protected ResponseValue getResponseValue(
            @Nullable
            Object value,
            Boolean isMandatory
    ) {
        return value != null || isMandatory
                ? new ResponseValue(ResponseType.SUCCESS, value)
                : new ResponseValue(ResponseType.NOT_FOUND);
    }

    protected ResponseValue getResponseValue(
            @Nullable
            Object value
    ) {
        return getResponseValue(value, false);
    }

    @Nullable
    protected ResponseValue read(int resourceId) {
        return null;
    }

    @Nullable
    protected ResponseValue write(
            int resourceId,
            @Nullable
            Object value
    ) {
        return null;
    }

    @Nullable
    protected ResponseValue execute(
            int resourceId,
            @Nullable
            String params
    ) {
        return null;
    }

    protected boolean onResourcesChange(int... resourceIds) {
        if (instanceEnabler != null) {
            instanceEnabler.fireResourcesChange(resourceIds);
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    private LwM2mSingleResource convertToResourceValue(int resourceId, Object content) {
        if (content instanceof Long) {
            return LwM2mSingleResource.newIntegerResource(resourceId, (Long) content);
        } else if (content instanceof Double) {
            return LwM2mSingleResource.newFloatResource(resourceId, (Double) content);
        } else if (content instanceof Boolean) {
            return LwM2mSingleResource.newBooleanResource(resourceId, (Boolean) content);
        } else if (content instanceof byte[]) {
            return LwM2mSingleResource.newBinaryResource(resourceId, (byte[]) content);
        } else if (content instanceof String) {
            return LwM2mSingleResource.newStringResource(resourceId, (String) content);
        } else if (content instanceof Date) {
            return LwM2mSingleResource.newDateResource(resourceId, (Date) content);
//        } else if (content instanceof ObjectLink) {
//            return LwM2mSingleResource.newObjectLinkResource(resourceId, (ObjectLink) content);
        } else if (content instanceof Integer) {
            return LwM2mSingleResource.newIntegerResource(resourceId, ((Integer) content).longValue());
        } else if (content instanceof Float) {
            return LwM2mSingleResource.newFloatResource(resourceId, ((Float) content).doubleValue());
        } else {
            return null;
        }
    }

    public interface ObjectId {
        //int SECURITY = LwM2mId.SECURITY;
        //int SERVER = LwM2mId.SERVER;
        //int ACCESS_CONTROL = LwM2mId.ACCESS_CONTROL;
        //int DEVICE = LwM2mId.DEVICE;
        //int CONNECTIVITY_MONITORING = LwM2mId.CONNECTIVITY_MONITORING;
        //int FIRMWARE = LwM2mId.FIRMWARE;
        int LOCATION = LwM2mId.LOCATION;
        int CONNECTIVITY_STATISTICS = LwM2mId.CONNECTIVITY_STATISTICS;
        //int SOFTWARE_MANAGEMENT = LwM2mId.SOFTWARE_MANAGEMENT;
        //int SEC_SERVER_URI = LwM2mId.SEC_SERVER_URI;
        //int SEC_BOOTSTRAP = LwM2mId.SEC_BOOTSTRAP;
        //int SEC_SECURITY_MODE = LwM2mId.SEC_SECURITY_MODE;
        //int SEC_PUBKEY_IDENTITY = LwM2mId.SEC_PUBKEY_IDENTITY;
        //int SEC_SERVER_PUBKEY = LwM2mId.SEC_SERVER_PUBKEY;
        //int SEC_SECRET_KEY = LwM2mId.SEC_SECRET_KEY;
        //int SEC_SERVER_ID = LwM2mId.SEC_SERVER_ID;
        //int SRV_SERVER_ID = LwM2mId.SRV_SERVER_ID;
        //int SRV_LIFETIME = LwM2mId.SRV_LIFETIME;
        //int SRV_BINDING = LwM2mId.SRV_BINDING;
    }

    public static class ResponseValue {

        private final ResponseType type;
        @Nullable
        private final Object value;

        public ResponseValue(
                ResponseType type,
                @Nullable
                Object value
        ) {
            this.type = type;
            this.value = value;
        }

        public ResponseValue(ResponseType type) {
            this(type, null);
        }

        public ResponseType getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }

    }

    public enum ResponseType {
        SUCCESS,
        NOT_FOUND;
    }

    private class InternalInstanceEnabler extends BaseInstanceEnabler {

        @Override
        public ReadResponse read(ServerIdentity identity, int resourceId) {
            ResponseValue responseValue = Lwm2mInstance.this.read(resourceId);
            if (responseValue != null) {
                ResponseType type = responseValue.getType();
                Object content = responseValue.getValue();
                return switch (type) {
                    case SUCCESS -> ReadResponse.success(convertToResourceValue(resourceId, content));
                    case NOT_FOUND -> ReadResponse.notFound();
                };
            } else {
                return super.read(identity, resourceId);
            }
        }

        @Override
        public WriteResponse write(ServerIdentity identity, int resourceId, LwM2mResource value) {
            ResponseValue responseValue = Lwm2mInstance.this.write(resourceId, value.getValue());
            if (responseValue != null) {
                ResponseType type = responseValue.getType();
                return switch (type) {
                    case SUCCESS -> WriteResponse.success();
                    case NOT_FOUND -> WriteResponse.notFound();
                };
            } else {
                return super.write(identity, resourceId, value);
            }
        }

        @Override
        public ExecuteResponse execute(ServerIdentity identity, int resourceId, String params) {
            ResponseValue responseValue = Lwm2mInstance.this.execute(resourceId, params);
            if (responseValue != null) {
                ResponseType type = responseValue.getType();
                return switch (type) {
                    case SUCCESS -> ExecuteResponse.success();
                    case NOT_FOUND -> ExecuteResponse.notFound();
                };
            } else {
                return super.execute(identity, resourceId, params);
            }
        }

        @Override
        public void onDelete(ServerIdentity identity) {
            super.onDelete(identity);
        }

    }

}