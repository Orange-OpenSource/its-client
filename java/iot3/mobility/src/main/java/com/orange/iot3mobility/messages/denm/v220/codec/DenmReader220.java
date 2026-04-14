/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v220.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.messages.denm.v220.model.DenmEnvelope220;
import com.orange.iot3mobility.messages.denm.v220.model.DenmMessage220;
import com.orange.iot3mobility.messages.denm.v220.model.path.PathElement;
import com.orange.iot3mobility.messages.denm.v220.model.path.PathPosition;
import com.orange.iot3mobility.messages.denm.v220.model.alacartecontainer.AlacarteContainer;
import com.orange.iot3mobility.messages.denm.v220.model.defs.Altitude;
import com.orange.iot3mobility.messages.denm.v220.model.defs.DeltaReferencePosition;
import com.orange.iot3mobility.messages.denm.v220.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.denm.v220.model.locationcontainer.DetectionZone;
import com.orange.iot3mobility.messages.denm.v220.model.locationcontainer.EventPositionHeading;
import com.orange.iot3mobility.messages.denm.v220.model.locationcontainer.EventSpeed;
import com.orange.iot3mobility.messages.denm.v220.model.locationcontainer.LocationContainer;
import com.orange.iot3mobility.messages.denm.v220.model.locationcontainer.PathPoint;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.CauseCode;
import com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.EventZone;
import com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.SituationContainer;
import com.orange.iot3mobility.messages.denm.v220.validation.DenmValidationException;
import com.orange.iot3mobility.messages.denm.v220.validation.DenmValidator220;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class DenmReader220 {

    private final JsonFactory jsonFactory;

    public DenmReader220(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public DenmEnvelope220 read(InputStream in) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(in)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);

            String messageType = null;
            String version = null;
            String sourceUuid = null;
            Long timestamp = null;
            List<PathElement> path = null;
            DenmMessage220 message = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                switch (field) {
                    case "message_type" -> messageType = parser.getValueAsString();
                    case "version" -> version = parser.getValueAsString();
                    case "source_uuid" -> sourceUuid = parser.getValueAsString();
                    case "timestamp" -> timestamp = parser.getLongValue();
                    case "path" -> path = readPath(parser);
                    case "message" -> message = readMessage(parser);
                    default -> parser.skipChildren();
                }
            }

            DenmEnvelope220 envelope = new DenmEnvelope220(
                    requireField(messageType, "message_type"),
                    requireField(sourceUuid, "source_uuid"),
                    requireField(timestamp, "timestamp"),
                    requireField(version, "version"),
                    path,
                    requireField(message, "message"));

            DenmValidator220.validateEnvelope(envelope);
            return envelope;
        }
    }

    private List<PathElement> readPath(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<PathElement> elements = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            elements.add(readPathElement(parser));
        }
        return elements;
    }

    private PathElement readPathElement(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        PathPosition position = null;
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

        return new PathElement(
                requireField(position, "path.position"),
                requireField(messageType, "path.message_type"));
    }

    private PathPosition readPathPosition(JsonParser parser) throws IOException {
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

        return new PathPosition(
                requireField(latitude, "path.position.latitude"),
                requireField(longitude, "path.position.longitude"),
                requireField(altitude, "path.position.altitude"));
    }

    private DenmMessage220 readMessage(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        Integer protocolVersion = null;
        Long stationId = null;
        ManagementContainer management = null;
        SituationContainer situation = null;
        LocationContainer location = null;
        AlacarteContainer alacarte = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "protocol_version" -> protocolVersion = parser.getIntValue();
                case "station_id" -> stationId = parser.getLongValue();
                case "management_container" -> management = readManagement(parser);
                case "situation_container" -> situation = readSituation(parser);
                case "location_container" -> location = readLocation(parser);
                case "alacarte_container" -> alacarte = readAlacarte(parser);
                default -> parser.skipChildren();
            }
        }

        return new DenmMessage220(
                requireField(protocolVersion, "protocol_version"),
                requireField(stationId, "station_id"),
                requireField(management, "management_container"),
                situation,
                location,
                alacarte);
    }

    private ManagementContainer readManagement(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        ActionId actionId = null;
        Long detectionTime = null;
        Long referenceTime = null;
        Integer termination = null;
        ReferencePosition eventPosition = null;
        Integer awarenessDistance = null;
        Integer trafficDirection = null;
        Integer validityDuration = null;
        Integer transmissionInterval = null;
        Integer stationType = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "action_id" -> actionId = readActionId(parser);
                case "detection_time" -> detectionTime = parser.getLongValue();
                case "reference_time" -> referenceTime = parser.getLongValue();
                case "termination" -> termination = parser.getIntValue();
                case "event_position" -> eventPosition = readReferencePosition(parser);
                case "awareness_distance" -> awarenessDistance = parser.getIntValue();
                case "traffic_direction" -> trafficDirection = parser.getIntValue();
                case "validity_duration" -> validityDuration = parser.getIntValue();
                case "transmission_interval" -> transmissionInterval = parser.getIntValue();
                case "station_type" -> stationType = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ManagementContainer(
                requireField(actionId, "action_id"),
                requireField(detectionTime, "detection_time"),
                requireField(referenceTime, "reference_time"),
                termination,
                requireField(eventPosition, "event_position"),
                awarenessDistance,
                trafficDirection,
                validityDuration,
                transmissionInterval,
                requireField(stationType, "station_type"));
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
        PositionConfidenceEllipse ellipse = null;
        Altitude altitude = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "latitude" -> latitude = parser.getIntValue();
                case "longitude" -> longitude = parser.getIntValue();
                case "position_confidence_ellipse" -> ellipse = readPositionConfidenceEllipse(parser);
                case "altitude" -> altitude = readAltitude(parser);
                default -> parser.skipChildren();
            }
        }

        return new ReferencePosition(
                requireField(latitude, "latitude"),
                requireField(longitude, "longitude"),
                requireField(ellipse, "position_confidence_ellipse"),
                requireField(altitude, "altitude"));
    }

    private PositionConfidenceEllipse readPositionConfidenceEllipse(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer semiMajor = null;
        Integer semiMinor = null;
        Integer semiMajorOrientation = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "semi_major" -> semiMajor = parser.getIntValue();
                case "semi_minor" -> semiMinor = parser.getIntValue();
                case "semi_major_orientation" -> semiMajorOrientation = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new PositionConfidenceEllipse(
                requireField(semiMajor, "semi_major"),
                requireField(semiMinor, "semi_minor"),
                requireField(semiMajorOrientation, "semi_major_orientation"));
    }

    private Altitude readAltitude(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new Altitude(
                requireField(value, "altitude.value"),
                requireField(confidence, "altitude.confidence"));
    }

    private SituationContainer readSituation(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer informationQuality = null;
        CauseCode eventType = null;
        CauseCode linkedCause = null;
        List<EventZone> eventZone = null;
        List<ActionId> linkedDenms = null;
        Integer eventEnd = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "information_quality" -> informationQuality = parser.getIntValue();
                case "event_type" -> eventType = readCauseCode(parser);
                case "linked_cause" -> linkedCause = readCauseCode(parser);
                case "event_zone" -> eventZone = readEventZone(parser);
                case "linked_denms" -> linkedDenms = readLinkedDenms(parser);
                case "event_end" -> eventEnd = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new SituationContainer(
                requireField(informationQuality, "information_quality"),
                requireField(eventType, "event_type"),
                linkedCause,
                eventZone,
                linkedDenms,
                eventEnd);
    }

    private CauseCode readCauseCode(JsonParser parser) throws IOException {
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

        return new CauseCode(
                requireField(cause, "cause"),
                subcause);
    }

    private List<EventZone> readEventZone(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<EventZone> zones = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            zones.add(readEventZoneItem(parser));
        }
        return zones;
    }

    private EventZone readEventZoneItem(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        DeltaReferencePosition eventPosition = null;
        Integer eventDeltaTime = null;
        Integer informationQuality = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "event_position" -> eventPosition = readDeltaReferencePosition(parser);
                case "event_delta_time" -> eventDeltaTime = parser.getIntValue();
                case "information_quality" -> informationQuality = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new EventZone(
                requireField(eventPosition, "event_position"),
                eventDeltaTime,
                requireField(informationQuality, "information_quality"));
    }

    private List<ActionId> readLinkedDenms(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<ActionId> linked = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            linked.add(readActionId(parser));
        }
        return linked;
    }

    private LocationContainer readLocation(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        EventSpeed eventSpeed = null;
        EventPositionHeading eventPositionHeading = null;
        List<DetectionZone> detectionZones = null;
        Integer roadType = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "event_speed" -> eventSpeed = readEventSpeed(parser);
                case "event_position_heading" -> eventPositionHeading = readEventPositionHeading(parser);
                case "detection_zones_to_event_position" -> detectionZones = readDetectionZones(parser);
                case "road_type" -> roadType = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new LocationContainer(
                eventSpeed,
                eventPositionHeading,
                detectionZones,
                roadType);
    }

    private EventSpeed readEventSpeed(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new EventSpeed(
                requireField(value, "event_speed.value"),
                requireField(confidence, "event_speed.confidence"));
    }

    private EventPositionHeading readEventPositionHeading(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new EventPositionHeading(
                requireField(value, "event_position_heading.value"),
                requireField(confidence, "event_position_heading.confidence"));
    }

    private List<DetectionZone> readDetectionZones(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<DetectionZone> zones = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            zones.add(readDetectionZone(parser));
        }
        return zones;
    }

    private DetectionZone readDetectionZone(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        List<PathPoint> path = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            if ("path".equals(field)) {
                path = readPathPoints(parser);
            } else {
                parser.skipChildren();
            }
        }

        return new DetectionZone(requireField(path, "path"));
    }

    private List<PathPoint> readPathPoints(JsonParser parser) throws IOException {
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
        Integer deltaLatitude = null;
        Integer deltaLongitude = null;
        Integer deltaAltitude = null;

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

        return new DeltaReferencePosition(
                requireField(deltaLatitude, "delta_latitude"),
                requireField(deltaLongitude, "delta_longitude"),
                requireField(deltaAltitude, "delta_altitude"));
    }

    private AlacarteContainer readAlacarte(JsonParser parser) throws IOException {
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

