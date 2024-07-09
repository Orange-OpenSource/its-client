/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.json.JsonKey;
import com.orange.iot3mobility.its.json.JsonValue;
import com.orange.iot3mobility.its.json.MessageBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CPM extends MessageBase {

    private static final Logger LOGGER = Logger.getLogger(CPM.class.getName());

    private static boolean strictMode = true;

    private final JSONObject json = new JSONObject();

    /**
     * Version of the ITS message and/or communication protocol
     */
    private final int protocolVersion;

    /**
     * ITS-station identifier
     */
    private final long stationId;

    /**
     * Time of the reference position in the CPM, considered as time of the CPM generation.
     *
     * TimestampIts mod 65 536. TimestampIts represents an integer value in milliseconds since
     * 2004-01-01T00:00:00:000Z.
     *
     * oneMilliSec(1).
     */
    private final int generationDeltaTime;

    /**
     * Contains the type and reference position of the emitting ITS-station
     */
    private final ManagementContainer managementContainer;

    /**
     * Contains a sub-container describing the emitting ITS-station
     */
    private final StationDataContainer stationDataContainer;

    /**
     * Contains all the sensor information of the emitting ITS-S
     */
    private final SensorInformationContainer sensorInformationContainer;

    /**
     * Contains all the objects that have been perceived by the sensors of the emitting ITS-S
     */
    private final PerceivedObjectContainer perceivedObjectContainer;

    public CPM(
            final String type,
            final String origin,
            final String version,
            final String sourceUuid,
            final String destinationUuid,
            final long timestamp,
            final int protocolVersion,
            final long stationId,
            final int generationDeltaTime,
            final ManagementContainer managementContainer,
            final StationDataContainer stationDataContainer,
            final SensorInformationContainer sensorInformationContainer,
            final PerceivedObjectContainer perceivedObjectContainer
            ) throws IllegalArgumentException {
        super(type, origin, version, sourceUuid, destinationUuid, timestamp);
        if(protocolVersion == UNKNOWN && isStrictMode()) {
            throw new IllegalArgumentException("CPM ProtocolVersion is missing");
        } else if(isStrictMode() && (protocolVersion > 255 || protocolVersion < 0)) {
            throw new IllegalArgumentException("CPM ProtocolVersion should be in the range of [0 - 255]."
                    + " Value: " + protocolVersion);
        }
        this.protocolVersion = protocolVersion;
        if(stationId == UNKNOWN && isStrictMode()) {
            throw new IllegalArgumentException("CPM StationID is missing");
        } else if(isStrictMode() && (stationId > 4294967295L || stationId < 0)) {
            throw new IllegalArgumentException("CPM StationID should be in the range of [0 - 4294967295]."
                    + " Value: " + stationId);
        }
        this.stationId = stationId;
        if(generationDeltaTime == UNKNOWN && isStrictMode()) {
            throw new IllegalArgumentException("CPM GenerationDeltaTime is missing");
        } else if(isStrictMode() && (generationDeltaTime > 65535 || generationDeltaTime < 0)) {
            throw new IllegalArgumentException("CPM GenerationDeltaTime should be in the range of [0 - 65535]."
                    + " Value: " + generationDeltaTime);
        }
        this.generationDeltaTime = generationDeltaTime;
        if(managementContainer == null) {
            throw new IllegalArgumentException("CPM ManagementContainer missing.");
        }
        this.managementContainer = managementContainer;
        this.stationDataContainer = stationDataContainer;
        this.sensorInformationContainer = sensorInformationContainer;
        this.perceivedObjectContainer = perceivedObjectContainer;

        createJson();
    }

    private void createJson() {
        try {
            JSONObject message = new JSONObject();
            message.put(JsonCpmKey.Cpm.PROTOCOL_VERSION.key(), protocolVersion);
            message.put(JsonCpmKey.Cpm.STATION_ID.key(), stationId);
            message.put(JsonCpmKey.Cpm.GENERATION_DELTA_TIME.key(), generationDeltaTime);
            message.put(JsonCpmKey.Cpm.MANAGEMENT_CONTAINER.key(), managementContainer.getJson());
            if(stationDataContainer != null)
                message.put(JsonCpmKey.Cpm.STATION_DATA_CONTAINER.key(), stationDataContainer.getJson());
            if(sensorInformationContainer != null)
                message.put(JsonCpmKey.Cpm.SENSOR_INFORMATION_CONTAINER.key(), sensorInformationContainer.getJson());
            if(perceivedObjectContainer != null)
                message.put(JsonCpmKey.Cpm.PERCEIVED_OBJECT_CONTAINER.key(), perceivedObjectContainer.getJson());

            json.put(JsonKey.Header.TYPE.key(), getType());
            json.put(JsonKey.Header.ORIGIN.key(), getOrigin());
            json.put(JsonKey.Header.VERSION.key(), getVersion());
            json.put(JsonKey.Header.SOURCE_UUID.key(), getSourceUuid());
            if(!getDestinationUuid().equals(""))
                json.put(JsonKey.Header.DESTINATION_UUID.key(), getDestinationUuid());
            json.put(JsonKey.Header.TIMESTAMP.key(), getTimestamp());
            json.put(JsonKey.Header.MESSAGE.key(), message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public long getStationId() {
        return stationId;
    }

    public int getGenerationDeltaTime() {
        return generationDeltaTime;
    }

    public ManagementContainer getManagementContainer() {
        return managementContainer;
    }

    public StationDataContainer getStationDataContainer() {
        return stationDataContainer;
    }

    public SensorInformationContainer getSensorInformationContainer() {
        return sensorInformationContainer;
    }

    public PerceivedObjectContainer getPerceivedObjectContainer() {
        return perceivedObjectContainer;
    }
    
    public static class CPMBuilder {
        private String type;
        private String origin;
        private String version;
        private String sourceUuid;
        private String destinationUuid;
        private long timestamp;
        private int protocolVersion;
        private long stationId;
        private int generationDeltaTime;
        private ManagementContainer managementContainer;
        private StationDataContainer stationDataContainer;
        private SensorInformationContainer sensorInformationContainer;
        private PerceivedObjectContainer perceivedObjectContainer;
        
        public CPMBuilder() {
            this.type = JsonValue.Type.CPM.value();
        }
        
        public CPMBuilder header(String origin,
                                 String version,
                                 String sourceUuid,
                                 String destinationUuid,
                                 long timestamp) {
            this.origin = origin;
            this.version = version;
            this.sourceUuid = sourceUuid;
            this.destinationUuid = destinationUuid;
            this.timestamp = timestamp;
            return this;
        }

        public CPMBuilder header(String origin,
                                     String version,
                                     String sourceUuid,
                                     long timestamp) {
            this.origin = origin;
            this.version = version;
            this.sourceUuid = sourceUuid;
            this.destinationUuid = "";
            this.timestamp = timestamp;
            return this;
        }

        public CPMBuilder pduHeader(int protocolVersion,
                                        long stationId,
                                        int generationDeltaTime) {
            this.protocolVersion = protocolVersion;
            this.stationId = stationId;
            this.generationDeltaTime = generationDeltaTime;
            return this;
        }
        
        public CPMBuilder managementContainer(ManagementContainer managementContainer) {
            this.managementContainer = managementContainer;
            return this;
        }

        public CPMBuilder stationDataContainer(StationDataContainer stationDataContainer) {
            this.stationDataContainer = stationDataContainer;
            return this;
        }

        public CPMBuilder sensorInformationContainer(SensorInformationContainer sensorInformationContainer) {
            this.sensorInformationContainer = sensorInformationContainer;
            return this;
        }
        
        public CPMBuilder perceivedObjectContainer(PerceivedObjectContainer perceivedObjectContainer) {
            this.perceivedObjectContainer = perceivedObjectContainer;
            return this;
        }
        
        public CPM build() {
            return new CPM(
                    type, 
                    origin, 
                    version,
                    sourceUuid,
                    destinationUuid,
                    timestamp,
                    protocolVersion,
                    stationId,
                    generationDeltaTime,
                    managementContainer,
                    stationDataContainer,
                    sensorInformationContainer,
                    perceivedObjectContainer
            );
        }
    }

    public static boolean isStrictMode() {
        return strictMode;
    }

    public static void setStrictMode(boolean strictMode) {
        CPM.strictMode = strictMode;
    }
    
    public static CPM jsonParser(JSONObject json) {
        if(json == null || json.length() == 0) return null;
        try {
            String type = json.getString(JsonKey.Header.TYPE.key());

            if(type.equals(JsonValue.Type.CPM.value())) {
                JSONObject message = json.getJSONObject(JsonKey.Header.MESSAGE.key());

                String origin = json.getString(JsonKey.Header.ORIGIN.key());
                String version = json.getString(JsonKey.Header.VERSION.key());
                String sourceUuid = json.getString(JsonKey.Header.SOURCE_UUID.key());
                String destinationUuid = json.optString(JsonKey.Header.DESTINATION_UUID.key());
                long timestamp = json.getLong(JsonKey.Header.TIMESTAMP.key());

                int protocolVersion = message.getInt(JsonCpmKey.Cpm.PROTOCOL_VERSION.key());
                long stationId = message.getLong(JsonCpmKey.Cpm.STATION_ID.key());
                int generationDeltaTime = message.getInt(JsonCpmKey.Cpm.GENERATION_DELTA_TIME.key());

                JSONObject jsonManagementContainer = message.getJSONObject(JsonCpmKey.Cpm.MANAGEMENT_CONTAINER.key());
                ManagementContainer managementContainer = ManagementContainer.jsonParser(jsonManagementContainer);

                JSONObject jsonStationDataContainer = message.optJSONObject(JsonCpmKey.Cpm.STATION_DATA_CONTAINER.key());
                StationDataContainer stationDataContainer = StationDataContainer.jsonParser(jsonStationDataContainer);

                JSONArray jsonSensorInformationContainer = message.optJSONArray(JsonCpmKey.Cpm.SENSOR_INFORMATION_CONTAINER.key());
                SensorInformationContainer sensorInformationContainer = SensorInformationContainer.jsonParser(jsonSensorInformationContainer);

                JSONArray jsonPerceivedObjectContainer = message.optJSONArray(JsonCpmKey.Cpm.PERCEIVED_OBJECT_CONTAINER.key());
                PerceivedObjectContainer perceivedObjectContainer = PerceivedObjectContainer.jsonParser(jsonPerceivedObjectContainer);

                return new CPMBuilder()
                        .header(origin,
                                version,
                                sourceUuid,
                                destinationUuid,
                                timestamp)
                        .pduHeader(protocolVersion,
                                stationId,
                                generationDeltaTime)
                        .managementContainer(managementContainer)
                        .stationDataContainer(stationDataContainer)
                        .sensorInformationContainer(sensorInformationContainer)
                        .perceivedObjectContainer(perceivedObjectContainer)
                        .build();
            }
        } catch (JSONException | IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "CPM", "Error parsing CPM: " + e);
        }
        return null;
    }

}
