/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.denm;

import com.orange.iot3mobility.its.json.JsonKey;
import com.orange.iot3mobility.its.json.JsonValue;
import com.orange.iot3mobility.its.json.MessageBase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Decentralized Environmental Notification Message (DENM) is a message that is mainly used by ITS applications
 * in order to alert road users of a detected event.
 * <p>
 * DENM is used to describe a variety of events that can be detected by ITS stations (ITS-S).
 */
public class DENM extends MessageBase {

    private static final Logger LOGGER = Logger.getLogger(DENM.class.getName());

    private JSONObject jsonDENM = new JSONObject();

    /**
     * Version of the ITS message and/or communication protocol.
     */
    private final int protocolVersion;

    /**
     * ITS-station identifier
     */
    private long stationId;

    /**
     * contains information related to the DENM management and the DENM protocol.
     */
    private final ManagementContainer managementContainer;

    /**
     * Contains information related to the type of the detected event.
     */
    private final SituationContainer situationContainer;

    /**
     * Contains information of the event location, and the location referencing.
     */
    private final LocationContainer locationContainer;

    /**
     * Contains information specific to the use case which requires the transmission of
     * additional information that is not included in the three previous containers.
     */
    private final AlacarteContainer alacarteContainer;

    private DENM(final String type,
                final String origin,
                final String version,
                final String sourceUuid,
                final long timestamp,
                final int protocolVersion,
                final long stationId,
                final ManagementContainer managementContainer,
                final SituationContainer situationContainer,
                final LocationContainer locationContainer,
                final AlacarteContainer alacarteContainer)
    {
        super(type, origin, version, sourceUuid, timestamp);
        if(protocolVersion > 255 || protocolVersion < 0) {
            throw new IllegalArgumentException("DENM ProtocolVersion should be in the range of [0 - 255]."
                    + " Value: " + protocolVersion);
        }
        this.protocolVersion = protocolVersion;
        if(stationId > 4294967295L || stationId < 0) {
            throw new IllegalArgumentException("DENM StationID should be in the range of [0 - 4294967295]."
                    + " Value: " + stationId);
        }
        this.stationId = stationId;
        if(managementContainer == null) {
            throw new IllegalArgumentException("DENM ManagementContainer missing.");
        }
        this.managementContainer = managementContainer;
        this.situationContainer = situationContainer;
        this.locationContainer = locationContainer;
        this.alacarteContainer = alacarteContainer;

        createJson();
    }

    private void createJson() {
        try {
            JSONObject message = new JSONObject();
            message.put(JsonKey.Denm.PROTOCOL_VERSION.key(), protocolVersion);
            message.put(JsonKey.Denm.STATION_ID.key(), stationId);
            message.put(JsonKey.Denm.MANAGEMENT_CONTAINER.key(), managementContainer.getJsonManagementContainer());
            if(situationContainer != null)
                message.put(JsonKey.Denm.SITUATION_CONTAINER.key(), situationContainer.getJsonSituationContainer());
            if(locationContainer != null)
                message.put(JsonKey.Denm.LOCATION_CONTAINER.key(), locationContainer.getJsonLocationContainer());
            if(alacarteContainer != null)
                message.put(JsonKey.Denm.ALACARTE_CONTAINER.key(), alacarteContainer.getJsonAlacarteContainer());

            jsonDENM.put(JsonKey.Header.TYPE.key(), getType());
            jsonDENM.put(JsonKey.Header.ORIGIN.key(), getOrigin());
            jsonDENM.put(JsonKey.Header.VERSION.key(), getVersion());
            jsonDENM.put(JsonKey.Header.SOURCE_UUID.key(), getSourceUuid());
            jsonDENM.put(JsonKey.Header.TIMESTAMP.key(), getTimestamp());
            jsonDENM.put(JsonKey.Header.MESSAGE.key(), message);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "DENM JSON build error", "Error: " + e);
        }
    }

    private void updateJson() {
        jsonDENM = new JSONObject();
        createJson();
    }

    public void terminate(long stationId) {
        managementContainer.terminate(stationId == this.getManagementContainer().getActionId().getOriginatingStationId());
        this.stationId = stationId;
        updateTimestamp();
        updateJson();
    }

    public JSONObject getJsonDENM() {
        return jsonDENM;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public long getStationId() {
        return stationId;
    }

    public ManagementContainer getManagementContainer() {
        return managementContainer;
    }

    public SituationContainer getSituationContainer() {
        return situationContainer;
    }

    public LocationContainer getLocationContainer() {
        return locationContainer;
    }

    public AlacarteContainer getAlacarteContainer() {
        return alacarteContainer;
    }

    public static class DENMBuilder {
        private final String type;
        private String origin;
        private String version;
        private String sourceUuid;
        private long timestamp;
        private int protocolVersion;
        private long stationId;
        private ManagementContainer managementContainer;
        private SituationContainer situationContainer;
        private LocationContainer locationContainer;
        private AlacarteContainer alacarteContainer;

        /**
         * Start building a DENM.
         */
        public DENMBuilder() {
            this.type = JsonValue.Type.DENM.value();
        }

        /**
         * Sets the JSON header of the DENM.
         * <p>
         * These fields are mandatory.
         *
         * @param origin The entity responsible for emitting the message.
         * @param version JSON message format version.
         * @param sourceUuid The identifier of the entity responsible for emitting the message.
         * @param timestamp The timestamp when the message was generated since Unix Epoch (1970/01/01), in milliseconds.
         */
        public DENMBuilder header(String origin,
                                  String version,
                                  String sourceUuid,
                                  long timestamp) {
            this.origin = origin;
            this.version = version;
            this.sourceUuid = sourceUuid;
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Sets the PDU header of the DENM.
         * <p>
         * These fields are mandatory.
         *
         * @param protocolVersion {@link DENM#protocolVersion}
         * @param stationId {@link DENM#stationId}
         */
        public DENMBuilder pduHeader(int protocolVersion,
                                     long stationId) {
            this.protocolVersion = protocolVersion;
            this.stationId = stationId;
            return this;
        }

        /**
         * Sets the management container of the DENM.
         * <p>
         * This field is mandatory.
         *
         * @param managementContainer {@link DENM#managementContainer}
         */
        public DENMBuilder managementContainer(ManagementContainer managementContainer) {
            this.managementContainer = managementContainer;
            return this;
        }

        /**
         * Sets the situation container of the DENM.
         * <p>
         * This field is optional.
         *
         * @param situationContainer {@link DENM#situationContainer}
         */
        public DENMBuilder situationContainer(SituationContainer situationContainer) {
            this.situationContainer = situationContainer;
            return this;
        }

        /**
         * Sets the location container of the DENM.
         * <p>
         * This field is optional.
         *
         * @param locationContainer {@link DENM#locationContainer}
         */
        public DENMBuilder locationContainer(LocationContainer locationContainer) {
            this.locationContainer = locationContainer;
            return this;
        }

        /**
         * Sets the a la carte container of the DENM.
         * <p>
         * This field is optional.
         *
         * @param alacarteContainer {@link DENM#alacarteContainer}
         */
        public DENMBuilder alacarteContainer(AlacarteContainer alacarteContainer) {
            this.alacarteContainer = alacarteContainer;
            return this;
        }

        /**
         * Build the DENM.
         * <p>
         * Call after setting all the mandatory fields.
         *
         * @return {@link DENM}
         */
        public DENM build() {
            return new DENM(type,
                    origin,
                    version,
                    sourceUuid,
                    timestamp,
                    protocolVersion,
                    stationId,
                    managementContainer,
                    situationContainer,
                    locationContainer,
                    alacarteContainer);
        }
    }

    /**
     * Parse a DENM in JSON format.
     *
     * @param jsonDENM The CAM in JSON format
     * @return {@link DENM}
     */
    public static DENM jsonParser(JSONObject jsonDENM) {
        if(jsonDENM == null || jsonDENM.isEmpty()) return null;
        try {
            String type = jsonDENM.getString(JsonKey.Header.TYPE.key());

            if(type.equals(JsonValue.Type.DENM.value())){
                JSONObject message = jsonDENM.getJSONObject(JsonKey.Header.MESSAGE.key());

                String origin = jsonDENM.getString(JsonKey.Header.ORIGIN.key());
                String version = jsonDENM.getString(JsonKey.Header.VERSION.key());
                String sourceUuid = jsonDENM.getString(JsonKey.Header.SOURCE_UUID.key());
                long timestamp = jsonDENM.getLong(JsonKey.Header.TIMESTAMP.key());

                int protocolVersion = message.getInt(JsonKey.Denm.PROTOCOL_VERSION.key());
                long stationId = message.getLong(JsonKey.Denm.STATION_ID.key());

                JSONObject jsonManagementContainer = message.getJSONObject(JsonKey.Denm.MANAGEMENT_CONTAINER.key());
                ManagementContainer managementContainer = ManagementContainer.jsonParser(jsonManagementContainer);

                JSONObject jsonSituationContainer = message.optJSONObject(JsonKey.Denm.SITUATION_CONTAINER.key());
                SituationContainer situationContainer = SituationContainer.jsonParser(jsonSituationContainer);

                JSONObject jsonLocationContainer = message.optJSONObject(JsonKey.Denm.LOCATION_CONTAINER.key());
                LocationContainer locationContainer = LocationContainer.jsonParser(jsonLocationContainer);

                JSONObject jsonAlacarteContainer = message.optJSONObject(JsonKey.Denm.ALACARTE_CONTAINER.key());
                AlacarteContainer alacarteContainer = AlacarteContainer.jsonParser(jsonAlacarteContainer);

                return new DENMBuilder()
                        .header(origin,
                                version,
                                sourceUuid,
                                timestamp)
                        .pduHeader(protocolVersion,
                                stationId)
                        .managementContainer(managementContainer)
                        .situationContainer(situationContainer)
                        .locationContainer(locationContainer)
                        .alacarteContainer(alacarteContainer)
                        .build();
            }
        } catch (JSONException | IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "DENM JSON parsing error", "Error: " + e);
        }
        return null;
    }
}
