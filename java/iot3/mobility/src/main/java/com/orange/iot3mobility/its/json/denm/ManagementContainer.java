/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.denm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import com.orange.iot3mobility.its.json.JsonKey;
import com.orange.iot3mobility.its.json.JsonUtil;
import com.orange.iot3mobility.its.json.Position;
import com.orange.iot3mobility.its.json.PositionConfidence;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ManagementContainer {

    private static final Logger LOGGER = Logger.getLogger(ManagementContainer.class.getName());

    private JSONObject jsonManagementContainer = new JSONObject();

    /**
     * Contains the originating ITS-S station ID and the sequence number.
     */
    private final ActionId actionId;

    /**
     * Unit: millisecond since ETSI epoch (2004/01/01, so 1072915200000).
     * Time at which the event is detected by the originating ITS-S.
     * <p>
     * For the DENM repetition, this shall remain unchanged.
     * <p>
     * utcStartOf2004(0), oneMillisecAfterUTCStartOf2004(1)
     */
    private final long detectionTime;

    /**
     * Unit: millisecond since ETSI epoch (2004/01/01, so 1072915200000).
     * Time at which a new DENM, an update DENM or a cancellation DENM is generated.
     * <p>
     * utcStartOf2004(0), oneMillisecAfterUTCStartOf2004(1)
     */
    private final long referenceTime;

    /**
     * Set to terminate an existing DENM.
     * <p>
     * isCancellation(0), isNegation (1)
     */
    private int termination;

    /**
     * Position of the event.
     * <p>
     * See {@link Position}
     */
    private final Position eventPosition;

    /**
     * Distance at which the event is relevant.
     * <p>
     * lessThan50m(0), lessThan100m(1), lessThan200m(2), lessThan500m(3), lessThan1000m(4), lessThan5km(5),
     * lessThan10km(6), over10km(7)
     */
    private final int relevanceDistance;

    /**
     * Traffic direction for which the event is relevant.
     * <p>
     * allTrafficDirections(0), upstreamTraffic(1), downstreamTraffic(2), oppositeTraffic(3)
     */
    private final int relevanceTrafficDirection;

    /**
     * Unit: second. Validity duration of the event since detection.
     * <p>
     * timeOfDetection(0), oneSecondAfterDetection(1)
     */
    private int validityDuration;

    /**
     * Unit: millisecond. Transmission interval of the DENM.
     * <p>
     * Set if the event is sent periodically.
     * <p>
     * oneMilliSecond(1), tenSeconds(10000)
     */
    private final int transmissionInterval;

    /**
     * Station type of the originating ITS-S.
     * <p>
     * unknown(0), pedestrian(1), cyclist(2), moped(3), motorcycle(4), passengerCar(5), bus(6), lightTruck(7),
     * heavyTruck(8), trailer(9), specialVehicles(10), tram(11), roadSideUnit(15)
     */
    private final int stationType;

    /**
     * Confidence of the event position.
     * <p>
     * {@link PositionConfidence}
     */
    private final PositionConfidence positionConfidence;

    public ManagementContainer(
            final ActionId actionId,
            final long detectionTime,
            final long referenceTime,
            final Position eventPosition,
            final int stationType)
    {
        this(
                actionId,
                detectionTime,
                referenceTime,
                UNKNOWN,
                eventPosition,
                UNKNOWN,
                UNKNOWN,
                600,
                UNKNOWN,
                stationType,
                null);
    }

    public ManagementContainer(
            final ActionId actionId,
            final long detectionTime,
            final long referenceTime,
            final Position eventPosition,
            final int validityDuration,
            final int stationType)
    {
        this(
                actionId,
                detectionTime,
                referenceTime,
                UNKNOWN,
                eventPosition,
                UNKNOWN,
                UNKNOWN,
                validityDuration,
                UNKNOWN,
                stationType,
                null);
    }

    public ManagementContainer(
            final ActionId actionId,
            final long detectionTime,
            final long referenceTime,
            final int termination,
            final Position eventPosition,
            final int relevanceDistance,
            final int relevanceTrafficDirection,
            final int validityDuration,
            final int transmissionInterval,
            final int stationType,
            final PositionConfidence positionConfidence)
    {
        if(actionId == null) {
            throw new IllegalArgumentException("DENM ManagementContainer ActionID missing.");
        }
        this.actionId = actionId;
        if(detectionTime > 789048000000L || detectionTime < 473428800000L) {
            throw new IllegalArgumentException("DENM ManagementContainer DetectionTime should be in the range of [473428800000 - 789048000000]."
                    + " Value: " + detectionTime);
        }
        this.detectionTime = detectionTime;
        if(referenceTime > 789048000000L || referenceTime < 473428800000L) {
            throw new IllegalArgumentException("DENM ManagementContainer ReferenceTime should be in the range of [473428800000 - 789048000000]."
                    + " Value: " + referenceTime);
        }
        this.referenceTime = referenceTime;
        if(termination != UNKNOWN && (termination > 1 || termination < 0)) {
            throw new IllegalArgumentException("DENM ManagementContainer Termination should be in the range of [0 - 1]."
                    + " Value: " + termination);
        }
        this.termination = termination;
        if(eventPosition == null) {
            throw new IllegalArgumentException("DENM ManagementContainer EventPosition missing.");
        }
        this.eventPosition = eventPosition;
        if(relevanceDistance != UNKNOWN && (relevanceDistance > 7 || relevanceDistance < 0)) {
            throw new IllegalArgumentException("DENM ManagementContainer RelevanceDistance should be in the range of [0 - 7]."
                    + " Value: " + relevanceDistance);
        }
        this.relevanceDistance = relevanceDistance;
        if(relevanceTrafficDirection != UNKNOWN && (relevanceTrafficDirection > 3 || relevanceTrafficDirection < 0)) {
            throw new IllegalArgumentException("DENM ManagementContainer RelevanceTrafficDirection should be in the range of [0 - 3]."
                    + " Value: " + relevanceTrafficDirection);
        }
        this.relevanceTrafficDirection = relevanceTrafficDirection;
        if(validityDuration != UNKNOWN && (validityDuration > 86400 || validityDuration < 0)) {
            throw new IllegalArgumentException("DENM ManagementContainer ValidityDuration should be in the range of [0 - 86400]."
                    + " Value: " + validityDuration);
        }
        this.validityDuration = validityDuration;
        if(transmissionInterval != UNKNOWN && (transmissionInterval > 10000 || transmissionInterval < 0)) {
            throw new IllegalArgumentException("DENM ManagementContainer TransmissionInterval should be in the range of [0 - 10000]."
                    + " Value: " + transmissionInterval);
        }
        this.transmissionInterval = transmissionInterval;
        if(stationType > 255 || stationType < 0) {
            throw new IllegalArgumentException("DENM ManagementContainer StationType should be in the range of [0 - 255]."
                    + " Value: " + stationType);
        }
        this.stationType = stationType;
        this.positionConfidence = positionConfidence;

        createJson();
    }

    private void createJson() {
        JSONObject jsonActionId = actionId.getJsonActionId();
        JSONObject jsonEventPosition = eventPosition.getJson();

        try {
            jsonManagementContainer.put(JsonKey.ManagementContainer.ACTION_ID.key(), jsonActionId);
            jsonManagementContainer.put(JsonKey.ManagementContainer.DETECTION_TIME.key(), detectionTime);
            jsonManagementContainer.put(JsonKey.ManagementContainer.REFERENCE_TIME.key(), referenceTime);
            if(termination != UNKNOWN)
                jsonManagementContainer.put(JsonKey.ManagementContainer.TERMINATION.key(), termination);
            jsonManagementContainer.put(JsonKey.ManagementContainer.EVENT_POSITION.key(), jsonEventPosition);
            if(relevanceDistance != UNKNOWN)
                jsonManagementContainer.put(JsonKey.ManagementContainer.RELEVANCE_DISTANCE.key(), relevanceDistance);
            if(relevanceTrafficDirection != UNKNOWN)
                jsonManagementContainer.put(JsonKey.ManagementContainer.RELEVANCE_TRAFFIC_DIRECTION.key(), relevanceTrafficDirection);
            if(validityDuration != UNKNOWN)
                jsonManagementContainer.put(JsonKey.ManagementContainer.VALIDITY_DURATION.key(), validityDuration);
            if(transmissionInterval != UNKNOWN)
                jsonManagementContainer.put(JsonKey.ManagementContainer.TRANSMISSION_INTERVAL.key(), transmissionInterval);
            jsonManagementContainer.put(JsonKey.ManagementContainer.STATION_TYPE.key(), stationType);
            if(positionConfidence != null)
                jsonManagementContainer.put(JsonKey.Position.CONFIDENCE.key(), positionConfidence.getJson());
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "ManagementContainer JSON build error", "Error: " + e);
        }
    }

    private void updateJson() {
        jsonManagementContainer = new JSONObject();
        createJson();
    }

    public JSONObject getJsonManagementContainer() {
        return jsonManagementContainer;
    }

    public ActionId getActionId() {
        return actionId;
    }

    public long getDetectionTime() {
        return detectionTime;
    }

    public long getReferenceTime() {
        return referenceTime;
    }

    public int getTermination() {
        return termination;
    }

    public Position getEventPosition() {
        return eventPosition;
    }

    public int getRelevanceDistance() {
        return relevanceDistance;
    }

    public int getRelevanceTrafficDirection() {
        return relevanceTrafficDirection;
    }

    public int getValidityDuration() {
        return validityDuration;
    }

    public void terminate(boolean isCancellation) {
        validityDuration = 0;
        if(isCancellation) termination = 0; // same ITS-S -> cancellation
        else termination = 1; // different ITS-S -> negation
        updateJson();
    }

    public int getTransmissionInterval() {
        return transmissionInterval;
    }

    public int getStationType() {
        return stationType;
    }

    public PositionConfidence getPositionConfidence() {
        return positionConfidence;
    }

    public static ManagementContainer jsonParser(JSONObject jsonManagementContainer) {
        if(JsonUtil.isNullOrEmpty(jsonManagementContainer)) return null;
        try {
            JSONObject jsonActionId = jsonManagementContainer.getJSONObject(JsonKey.ManagementContainer.ACTION_ID.key());
            ActionId actionId = ActionId.jsonParser(jsonActionId);
            long detectionTime = jsonManagementContainer.getLong(JsonKey.ManagementContainer.DETECTION_TIME.key());
            long referenceTime = jsonManagementContainer.getLong(JsonKey.ManagementContainer.REFERENCE_TIME.key());
            int termination = jsonManagementContainer.optInt(JsonKey.ManagementContainer.TERMINATION.key(), UNKNOWN);
            JSONObject jsonEventPosition = jsonManagementContainer.getJSONObject(JsonKey.ManagementContainer.EVENT_POSITION.key());
            Position eventPosition = Position.jsonParser(jsonEventPosition);
            int relevanceDistance = jsonManagementContainer.optInt(JsonKey.ManagementContainer.RELEVANCE_DISTANCE.key(), UNKNOWN);
            int relevanceTrafficDirection = jsonManagementContainer.optInt(JsonKey.ManagementContainer.RELEVANCE_TRAFFIC_DIRECTION.key(), UNKNOWN);
            int validityDuration = jsonManagementContainer.optInt(JsonKey.ManagementContainer.VALIDITY_DURATION.key(), 600);
            int transmissionInterval = jsonManagementContainer.optInt(JsonKey.ManagementContainer.TRANSMISSION_INTERVAL.key(), UNKNOWN);
            int stationType = jsonManagementContainer.getInt(JsonKey.ManagementContainer.STATION_TYPE.key());
            JSONObject jsonPositionConfidence = jsonManagementContainer.optJSONObject(JsonKey.Position.CONFIDENCE.key());
            PositionConfidence positionConfidence = PositionConfidence.jsonParser(jsonPositionConfidence);

            return new ManagementContainer(
                    actionId,
                    detectionTime,
                    referenceTime,
                    termination,
                    eventPosition,
                    relevanceDistance,
                    relevanceTrafficDirection,
                    validityDuration,
                    transmissionInterval,
                    stationType,
                    positionConfidence);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "ManagementContainer JSON parsing error", "Error: " + e);
        }
        return null;
    }
}
