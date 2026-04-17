/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.spatem.v200.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemMessage200;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.*;
import com.orange.iot3mobility.messages.spatem.v200.validation.SpatemValidationException;
import com.orange.iot3mobility.messages.spatem.v200.validation.SpatemValidator200;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Streaming JSON reader for SPATEM v2.0.0 payloads.
 */
public final class SpatemReader200 {

    private final JsonFactory jsonFactory;

    public SpatemReader200(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public SpatemEnvelope200 read(InputStream in) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(in)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);

            String messageType = null, origin = null, version = null, sourceUuid = null;
            Long timestamp = null;
            SpatemMessage200 message = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                switch (field) {
                    case "message_type" -> messageType = parser.getValueAsString();
                    case "origin"       -> origin = parser.getValueAsString();
                    case "version"      -> version = parser.getValueAsString();
                    case "source_uuid"  -> sourceUuid = parser.getValueAsString();
                    case "timestamp"    -> timestamp = parser.getLongValue();
                    case "message"      -> message = readMessage(parser);
                    default             -> parser.skipChildren();
                }
            }

            SpatemEnvelope200 envelope = new SpatemEnvelope200(
                    requireField(messageType, "message_type"),
                    requireField(origin, "origin"),
                    requireField(version, "version"),
                    requireField(sourceUuid, "source_uuid"),
                    requireField(timestamp, "timestamp"),
                    requireField(message, "message"));

            SpatemValidator200.validateEnvelope(envelope);
            return envelope;
        }
    }

    /* --------------------------------------------------------------------- */
    /* Message                                                               */
    /* --------------------------------------------------------------------- */

    private SpatemMessage200 readMessage(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer protocolVersion = null;
        Long stationId = null;
        List<IntersectionState> intersections = null;
        Integer timestamp = null;
        String name = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "protocol_version" -> protocolVersion = parser.getIntValue();
                case "station_id"       -> stationId = parser.getLongValue();
                case "intersections"    -> intersections = readIntersections(parser);
                case "timestamp"        -> timestamp = parser.getIntValue();
                case "name"             -> name = parser.getValueAsString();
                default                 -> parser.skipChildren();
            }
        }
        return new SpatemMessage200(
                requireField(protocolVersion, "protocol_version"),
                requireField(stationId, "station_id"),
                requireField(intersections, "intersections"),
                timestamp, name);
    }

    /* --------------------------------------------------------------------- */
    /* IntersectionState                                                     */
    /* --------------------------------------------------------------------- */

    private List<IntersectionState> readIntersections(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<IntersectionState> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readIntersectionState(parser));
        }
        return list;
    }

    private IntersectionState readIntersectionState(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        IntersectionReferenceId id = null;
        Integer revision = null;
        List<String> status = null;
        List<MovementState> states = null;
        String name = null;
        Integer moy = null;
        Integer timestamp = null;
        List<Integer> enabledLanes = null;
        List<ManeuverAssist> maneuverAssistList = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "id"                  -> id = readIntersectionReferenceId(parser);
                case "revision"            -> revision = parser.getIntValue();
                case "status"              -> status = readStringArray(parser);
                case "states"              -> states = readMovementStates(parser);
                case "name"                -> name = parser.getValueAsString();
                case "moy"                 -> moy = parser.getIntValue();
                case "timestamp"           -> timestamp = parser.getIntValue();
                case "enabled_lanes"       -> enabledLanes = readIntArray(parser);
                case "maneuver_assist_list"-> maneuverAssistList = readManeuverAssistList(parser);
                default                    -> parser.skipChildren();
            }
        }
        return new IntersectionState(
                requireField(id, "id"),
                requireField(revision, "revision"),
                requireField(status, "status"),
                requireField(states, "states"),
                name, moy, timestamp, enabledLanes, maneuverAssistList);
    }

    private IntersectionReferenceId readIntersectionReferenceId(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer region = null;
        Integer id = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "region" -> region = parser.getIntValue();
                case "id"     -> id = parser.getIntValue();
                default       -> parser.skipChildren();
            }
        }
        return new IntersectionReferenceId(region, requireField(id, "intersection_reference_id.id"));
    }

    /* --------------------------------------------------------------------- */
    /* MovementState                                                         */
    /* --------------------------------------------------------------------- */

    private List<MovementState> readMovementStates(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<MovementState> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readMovementState(parser));
        }
        return list;
    }

    private MovementState readMovementState(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer signalGroup = null;
        List<MovementEvent> stateTimeSpeed = null;
        String movementName = null;
        List<ManeuverAssist> maneuverAssistList = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "signal_group"        -> signalGroup = parser.getIntValue();
                case "state_time_speed"    -> stateTimeSpeed = readMovementEvents(parser);
                case "movement_name"       -> movementName = parser.getValueAsString();
                case "maneuver_assist_list"-> maneuverAssistList = readManeuverAssistList(parser);
                default                    -> parser.skipChildren();
            }
        }
        return new MovementState(
                requireField(signalGroup, "signal_group"),
                requireField(stateTimeSpeed, "state_time_speed"),
                movementName, maneuverAssistList);
    }

    /* --------------------------------------------------------------------- */
    /* MovementEvent                                                         */
    /* --------------------------------------------------------------------- */

    private List<MovementEvent> readMovementEvents(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<MovementEvent> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readMovementEvent(parser));
        }
        return list;
    }

    private MovementEvent readMovementEvent(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer eventState = null;
        TimeChangeDetail timing = null;
        List<AdvisorySpeed> speeds = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "event_state" -> eventState = parser.getIntValue();
                case "timing"      -> timing = readTimeChangeDetail(parser);
                case "speeds"      -> speeds = readAdvisorySpeeds(parser);
                default            -> parser.skipChildren();
            }
        }
        return new MovementEvent(requireField(eventState, "event_state"), timing, speeds);
    }

    /* --------------------------------------------------------------------- */
    /* TimeChangeDetail                                                      */
    /* --------------------------------------------------------------------- */

    private TimeChangeDetail readTimeChangeDetail(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer minEndTime = null;
        Integer startTime = null;
        Integer maxEndTime = null;
        Integer likelyTime = null;
        Integer confidence = null;
        Integer nextTime = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "min_end_time" -> minEndTime = parser.getIntValue();
                case "start_time"   -> startTime = parser.getIntValue();
                case "max_end_time" -> maxEndTime = parser.getIntValue();
                case "likely_time"  -> likelyTime = parser.getIntValue();
                case "confidence"   -> confidence = parser.getIntValue();
                case "next_time"    -> nextTime = parser.getIntValue();
                default             -> parser.skipChildren();
            }
        }
        return new TimeChangeDetail(
                requireField(minEndTime, "timing.min_end_time"),
                startTime, maxEndTime, likelyTime, confidence, nextTime);
    }

    /* --------------------------------------------------------------------- */
    /* AdvisorySpeed                                                         */
    /* --------------------------------------------------------------------- */

    private List<AdvisorySpeed> readAdvisorySpeeds(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<AdvisorySpeed> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readAdvisorySpeed(parser));
        }
        return list;
    }

    private AdvisorySpeed readAdvisorySpeed(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer type = null;
        Integer speed = null;
        Integer confidence = null;
        Integer distance = null;
        Integer classId = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "type"       -> type = parser.getIntValue();
                case "speed"      -> speed = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                case "distance"   -> distance = parser.getIntValue();
                case "class"      -> classId = parser.getIntValue();
                default           -> parser.skipChildren();
            }
        }
        return new AdvisorySpeed(requireField(type, "advisory_speed.type"), speed, confidence, distance, classId);
    }

    /* --------------------------------------------------------------------- */
    /* ManeuverAssist                                                        */
    /* --------------------------------------------------------------------- */

    private List<ManeuverAssist> readManeuverAssistList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<ManeuverAssist> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readManeuverAssist(parser));
        }
        return list;
    }

    private ManeuverAssist readManeuverAssist(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer connectionId = null;
        Integer queueLength = null;
        Integer availableStorageLength = null;
        Boolean waitOnStop = null;
        Boolean pedBicycleDetect = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "connection_id"           -> connectionId = parser.getIntValue();
                case "queue_length"            -> queueLength = parser.getIntValue();
                case "available_storage_length"-> availableStorageLength = parser.getIntValue();
                case "wait_on_stop"            -> waitOnStop = parser.getBooleanValue();
                case "ped_bicycle_detect"      -> pedBicycleDetect = parser.getBooleanValue();
                default                        -> parser.skipChildren();
            }
        }
        return new ManeuverAssist(
                requireField(connectionId, "maneuver_assist.connection_id"),
                queueLength, availableStorageLength, waitOnStop, pedBicycleDetect);
    }

    /* --------------------------------------------------------------------- */
    /* Helpers                                                               */
    /* --------------------------------------------------------------------- */

    private List<String> readStringArray(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<String> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(parser.getValueAsString());
        }
        return list;
    }

    private List<Integer> readIntArray(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<Integer> list = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(parser.getIntValue());
        }
        return list;
    }

    private void expect(JsonToken actual, JsonToken expected) throws IOException {
        if (actual != expected) {
            throw new IOException("Expected " + expected + " but got " + actual);
        }
    }

    private static <T> T requireField(T value, String field) {
        if (value == null) throw new SpatemValidationException("Missing mandatory field: " + field);
        return value;
    }
}

