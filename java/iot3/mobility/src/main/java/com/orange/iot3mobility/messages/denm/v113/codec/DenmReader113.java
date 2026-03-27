/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmMessage113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmPathElement;
import com.orange.iot3mobility.messages.denm.v113.model.DenmPathPosition;
import com.orange.iot3mobility.messages.denm.v113.model.alacartecontainer.AlacarteContainer;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.DeltaReferencePosition;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.LocationConfidence;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.LocationContainer;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.PathHistory;
import com.orange.iot3mobility.messages.denm.v113.model.locationcontainer.PathPoint;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.PositionConfidence;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.EventType;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.LinkedCause;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.SituationContainer;
import com.orange.iot3mobility.messages.denm.v113.validation.DenmValidationException;
import com.orange.iot3mobility.messages.denm.v113.validation.DenmValidator113;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class DenmReader113 {

    private final JsonFactory jsonFactory;

    public DenmReader113(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public DenmEnvelope113 read(InputStream in) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(in)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);

            String type = null;
            String origin = null;
            String version = null;
            String sourceUuid = null;
            Long timestamp = null;
            List<DenmPathElement> path = null;
            DenmMessage113 message = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                switch (field) {
                    case "type" -> type = parser.getValueAsString();
                    case "origin" -> origin = parser.getValueAsString();
                    case "version" -> version = parser.getValueAsString();
                    case "source_uuid" -> sourceUuid = parser.getValueAsString();
                    case "timestamp" -> timestamp = parser.getLongValue();
                    case "path" -> path = readPath(parser);
                    case "message" -> message = readMessage(parser);
                    default -> parser.skipChildren();
                }
            }

            DenmEnvelope113 envelope = new DenmEnvelope113(
                    requireField(type, "type"),
                    requireField(origin, "origin"),
                    requireField(version, "version"),
                    requireField(sourceUuid, "source_uuid"),
                    requireField(timestamp, "timestamp"),
                    path,
                    requireField(message, "message"));

            DenmValidator113.validateEnvelope(envelope);
            return envelope;
        }
    }

    private List<DenmPathElement> readPath(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<DenmPathElement> elements = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            elements.add(readPathElement(parser));
        }
        return elements;
    }

    private DenmPathElement readPathElement(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        DenmPathPosition position = null;
        String messageType = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "position" -> position = readPathPosition(parser);
                case "message_type" -> messageType = parser.getValueAsString();
                default -> parser.skipChildren();
            }
        }

        return new DenmPathElement(
                requireField(position, "path.position"),
                requireField(messageType, "path.message_type"));
    }

    private DenmPathPosition readPathPosition(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer latitude = null;
        Integer longitude = null;
        Integer altitude = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "latitude" -> latitude = parser.getIntValue();
                case "longitude" -> longitude = parser.getIntValue();
                case "altitude" -> altitude = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new DenmPathPosition(
                requireField(latitude, "path.position.latitude"),
                requireField(longitude, "path.position.longitude"),
                requireField(altitude, "path.position.altitude"));
    }

    private DenmMessage113 readMessage(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        Integer protocolVersion = null;
        Long stationId = null;
        ManagementContainer managementContainer = null;
        SituationContainer situationContainer = null;
        LocationContainer locationContainer = null;
        AlacarteContainer alacarteContainer = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "protocol_version" -> protocolVersion = parser.getIntValue();
                case "station_id" -> stationId = parser.getLongValue();
                case "management_container" -> managementContainer = readManagementContainer(parser);
                case "situation_container" -> situationContainer = readSituationContainer(parser);
                case "location_container" -> locationContainer = readLocationContainer(parser);
                case "alacarte_container" -> alacarteContainer = readAlacarteContainer(parser);
                default -> parser.skipChildren();
            }
        }

        return new DenmMessage113(
                requireField(protocolVersion, "protocol_version"),
                requireField(stationId, "station_id"),
                requireField(managementContainer, "management_container"),
                situationContainer,
                locationContainer,
                alacarteContainer);
    }

    private ManagementContainer readManagementContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        ActionId actionId = null;
        Long detectionTime = null;
        Long referenceTime = null;
        Integer termination = null;
        ReferencePosition eventPosition = null;
        Integer relevanceDistance = null;
        Integer relevanceTrafficDirection = null;
        Integer validityDuration = null;
        Integer transmissionInterval = null;
        Integer stationType = null;
        PositionConfidence confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "action_id" -> actionId = readActionId(parser);
                case "detection_time" -> detectionTime = parser.getLongValue();
                case "reference_time" -> referenceTime = parser.getLongValue();
                case "termination" -> termination = parser.getIntValue();
                case "event_position" -> eventPosition = readReferencePosition(parser);
                case "relevance_distance" -> relevanceDistance = parser.getIntValue();
                case "relevance_traffic_direction" -> relevanceTrafficDirection = parser.getIntValue();
                case "validity_duration" -> validityDuration = parser.getIntValue();
                case "transmission_interval" -> transmissionInterval = parser.getIntValue();
                case "station_type" -> stationType = parser.getIntValue();
                case "confidence" -> confidence = readPositionConfidence(parser);
                default -> parser.skipChildren();
            }
        }

        return new ManagementContainer(
                requireField(actionId, "action_id"),
                requireField(detectionTime, "detection_time"),
                requireField(referenceTime, "reference_time"),
                termination,
                requireField(eventPosition, "event_position"),
                relevanceDistance,
                relevanceTrafficDirection,
                validityDuration,
                transmissionInterval,
                stationType,
                confidence);
    }

    private ActionId readActionId(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Long originatingStationId = null;
        Integer sequenceNumber = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "originating_station_id" -> originatingStationId = parser.getLongValue();
                case "sequence_number" -> sequenceNumber = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ActionId(
                requireField(originatingStationId, "originating_station_id"),
                requireField(sequenceNumber, "sequence_number"));
    }

    private ReferencePosition readReferencePosition(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer latitude = null;
        Integer longitude = null;
        Integer altitude = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "latitude" -> latitude = parser.getIntValue();
                case "longitude" -> longitude = parser.getIntValue();
                case "altitude" -> altitude = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ReferencePosition(
                requireField(latitude, "latitude"),
                requireField(longitude, "longitude"),
                requireField(altitude, "altitude"));
    }

    private PositionConfidence readPositionConfidence(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        PositionConfidenceEllipse ellipse = null;
        Integer altitude = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "position_confidence_ellipse" -> ellipse = readPositionConfidenceEllipse(parser);
                case "altitude" -> altitude = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        if (ellipse == null && altitude == null) {
            return null;
        }
        return new PositionConfidence(ellipse, altitude);
    }

    private PositionConfidenceEllipse readPositionConfidenceEllipse(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        int semiMajor = 4095;
        int semiMinor = 4095;
        int semiMajorOrientation = 3601;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "semi_major_confidence" -> semiMajor = parser.getIntValue();
                case "semi_minor_confidence" -> semiMinor = parser.getIntValue();
                case "semi_major_orientation" -> semiMajorOrientation = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new PositionConfidenceEllipse(semiMajor, semiMinor, semiMajorOrientation);
    }

    private SituationContainer readSituationContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer informationQuality = null;
        EventType eventType = null;
        LinkedCause linkedCause = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "information_quality" -> informationQuality = parser.getIntValue();
                case "event_type" -> eventType = readEventType(parser);
                case "linked_cause" -> linkedCause = readLinkedCause(parser);
                default -> parser.skipChildren();
            }
        }

        return new SituationContainer(
                informationQuality,
                requireField(eventType, "event_type"),
                linkedCause);
    }

    private EventType readEventType(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer cause = null;
        Integer subcause = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "cause" -> cause = parser.getIntValue();
                case "subcause" -> subcause = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new EventType(
                requireField(cause, "cause"),
                subcause);
    }

    private LinkedCause readLinkedCause(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer cause = null;
        Integer subcause = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "cause" -> cause = parser.getIntValue();
                case "subcause" -> subcause = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new LinkedCause(
                requireField(cause, "cause"),
                subcause);
    }

    private LocationContainer readLocationContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer eventSpeed = null;
        Integer eventPositionHeading = null;
        List<PathHistory> traces = null;
        Integer roadType = null;
        LocationConfidence confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "event_speed" -> eventSpeed = parser.getIntValue();
                case "event_position_heading" -> eventPositionHeading = parser.getIntValue();
                case "traces" -> traces = readTraces(parser);
                case "road_type" -> roadType = parser.getIntValue();
                case "confidence" -> confidence = readLocationConfidence(parser);
                default -> parser.skipChildren();
            }
        }

        return new LocationContainer(
                eventSpeed,
                eventPositionHeading,
                requireField(traces, "traces"),
                roadType,
                confidence);
    }

    private List<PathHistory> readTraces(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<PathHistory> traces = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            traces.add(readTraceElement(parser));
        }
        return traces;
    }

    private PathHistory readTraceElement(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        List<PathPoint> pathHistory = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            if ("path_history".equals(field)) {
                pathHistory = readPathHistory(parser);
            } else {
                parser.skipChildren();
            }
        }

        return new PathHistory(requireField(pathHistory, "path_history"));
    }

    private List<PathPoint> readPathHistory(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<PathPoint> points = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            points.add(readPathPoint(parser));
        }
        return points;
    }

    private PathPoint readPathPoint(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        DeltaReferencePosition pathPosition = null;
        Integer pathDeltaTime = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "path_position" -> pathPosition = readDeltaReferencePosition(parser);
                case "path_delta_time" -> pathDeltaTime = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new PathPoint(
                requireField(pathPosition, "path_position"),
                pathDeltaTime);
    }

    private DeltaReferencePosition readDeltaReferencePosition(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        int deltaLatitude = 131072;
        int deltaLongitude = 131072;
        int deltaAltitude = 12800;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "delta_latitude" -> deltaLatitude = parser.getIntValue();
                case "delta_longitude" -> deltaLongitude = parser.getIntValue();
                case "delta_altitude" -> deltaAltitude = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new DeltaReferencePosition(deltaLatitude, deltaLongitude, deltaAltitude);
    }

    private LocationConfidence readLocationConfidence(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer eventSpeed = null;
        Integer eventPositionHeading = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "event_speed" -> eventSpeed = parser.getIntValue();
                case "event_position_heading" -> eventPositionHeading = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        if (eventSpeed == null && eventPositionHeading == null) {
            return null;
        }
        return new LocationConfidence(eventSpeed, eventPositionHeading);
    }

    private AlacarteContainer readAlacarteContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer lanePosition = null;
        Integer positioningSolution = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "lane_position" -> lanePosition = parser.getIntValue();
                case "positioning_solution" -> positioningSolution = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new AlacarteContainer(lanePosition, positioningSolution);
    }

    private static void expect(JsonToken actual, JsonToken expected) {
        if (actual != expected) {
            throw new DenmValidationException("Expected token " + expected + " but got " + actual);
        }
    }

    private static <T> T requireField(T value, String field) {
        if (value == null) {
            throw new DenmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }
}

