/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v113.validation;

import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmMessage113;
import com.orange.iot3mobility.messages.denm.v113.model.path.PathElement;
import com.orange.iot3mobility.messages.denm.v113.model.path.PathPosition;
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

import java.util.List;
import java.util.Objects;

public final class DenmValidator113 {

    private DenmValidator113() {}

    public static void validateEnvelope(DenmEnvelope113 env) {
        requireNonNull("envelope", env);
        requireEquals("type", env.type(), "denm");
        requireEnum("origin", env.origin(),
                List.of("self", "global_application", "mec_application", "on_board_application"));
        requireEquals("version", env.version(), "1.1.3");
        requireNotBlank("source_uuid", env.sourceUuid());
        checkRange("timestamp", env.timestamp(), 1514764800000L, 1830297600000L);
        if (env.path() != null) {
            validatePath(env.path());
        }
        validateMessage(env.message());
    }

    public static void validateMessage(DenmMessage113 msg) {
        requireNonNull("message", msg);
        checkRange("protocol_version", msg.protocolVersion(), 0, 255);
        checkRange("station_id", msg.stationId(), 0, 4294967295L);
        validateManagementContainer(msg.managementContainer());
        if (msg.situationContainer() != null) {
            validateSituationContainer(msg.situationContainer());
        }
        if (msg.locationContainer() != null) {
            validateLocationContainer(msg.locationContainer());
        }
        if (msg.alacarteContainer() != null) {
            validateAlacarteContainer(msg.alacarteContainer());
        }
    }

    private static void validateManagementContainer(ManagementContainer container) {
        requireNonNull("management_container", container);
        validateActionId(container.actionId());
        checkRange("detection_time", container.detectionTime(), 0L, 4398046511103L);
        checkRange("reference_time", container.referenceTime(), 0L, 4398046511103L);
        checkRange("termination", container.termination(), 0, 1);
        validateReferencePosition(container.eventPosition());
        checkRange("relevance_distance", container.relevanceDistance(), 0, 7);
        checkRange("relevance_traffic_direction", container.relevanceTrafficDirection(), 0, 3);
        checkRange("validity_duration", container.validityDuration(), 0, 86400);
        checkRange("transmission_interval", container.transmissionInterval(), 1, 10000);
        checkRange("station_type", container.stationType(), 0, 255);
        if (container.confidence() != null) {
            validatePositionConfidence(container.confidence());
        }
    }

    private static void validateActionId(ActionId actionId) {
        requireNonNull("action_id", actionId);
        checkRange("originating_station_id", actionId.originatingStationId(), 0, 4294967295L);
        checkRange("sequence_number", actionId.sequenceNumber(), 0, 65535);
    }

    private static void validateReferencePosition(ReferencePosition position) {
        requireNonNull("event_position", position);
        checkRange("event_position.latitude", position.latitude(), -900000000, 900000001);
        checkRange("event_position.longitude", position.longitude(), -1800000000, 1800000001);
        checkRange("event_position.altitude", position.altitude(), -100000, 800001);
    }

    private static void validatePositionConfidence(PositionConfidence confidence) {
        if (confidence.positionConfidenceEllipse() != null) {
            validatePositionConfidenceEllipse(confidence.positionConfidenceEllipse());
        }
        checkRange("confidence.altitude", confidence.altitude(), 0, 15);
    }

    private static void validatePositionConfidenceEllipse(PositionConfidenceEllipse ellipse) {
        checkRange("confidence.position_confidence_ellipse.semi_major_confidence", ellipse.semiMajorConfidence(), 0, 4095);
        checkRange("confidence.position_confidence_ellipse.semi_minor_confidence", ellipse.semiMinorConfidence(), 0, 4095);
        checkRange("confidence.position_confidence_ellipse.semi_major_orientation", ellipse.semiMajorOrientation(), 0, 3601);
    }

    private static void validateSituationContainer(SituationContainer situation) {
        requireNonNull("situation_container", situation);
        checkRange("information_quality", situation.informationQuality(), 0, 7);
        validateEventType(situation.eventType());
        if (situation.linkedCause() != null) {
            validateLinkedCause(situation.linkedCause());
        }
    }

    private static void validateEventType(EventType eventType) {
        requireNonNull("event_type", eventType);
        checkRange("event_type.cause", eventType.cause(), 0, 255);
        checkRange("event_type.subcause", eventType.subcause(), 0, 255);
    }

    private static void validateLinkedCause(LinkedCause linkedCause) {
        checkRange("linked_cause.cause", linkedCause.cause(), 0, 255);
        checkRange("linked_cause.subcause", linkedCause.subcause(), 0, 255);
    }

    private static void validateLocationContainer(LocationContainer location) {
        requireNonNull("location_container", location);
        checkRange("event_speed", location.eventSpeed(), 0, 16383);
        checkRange("event_position_heading", location.eventPositionHeading(), 0, 3601);
        validateTraces(location.traces());
        checkRange("road_type", location.roadType(), 0, 3);
        if (location.confidence() != null) {
            validateLocationConfidence(location.confidence());
        }
    }

    private static void validateTraces(List<PathHistory> traces) {
        requireNonNull("traces", traces);
        checkSize("traces", traces.size(), 1, 7);
        for (int i = 0; i < traces.size(); i++) {
            validatePathHistory("traces[" + i + "]", traces.get(i));
        }
    }

    private static void validatePathHistory(String prefix, PathHistory history) {
        requireNonNull(prefix + ".path_history", history);
        List<PathPoint> points = requireNonNull(prefix + ".path_history", history.pathHistory());
        if (points.size() > 40) {
            throw new DenmValidationException(prefix + ".path_history size exceeds 40");
        }
        for (int i = 0; i < points.size(); i++) {
            validatePathPoint(prefix + ".path_history[" + i + "]", points.get(i));
        }
    }

    private static void validatePathPoint(String prefix, PathPoint point) {
        requireNonNull(prefix, point);
        DeltaReferencePosition pos = requireNonNull(prefix + ".path_position", point.pathPosition());
        checkRange(prefix + ".delta_latitude", pos.deltaLatitude(), -131071, 131072);
        checkRange(prefix + ".delta_longitude", pos.deltaLongitude(), -131071, 131072);
        checkRange(prefix + ".delta_altitude", pos.deltaAltitude(), -12700, 12800);
        checkRange(prefix + ".path_delta_time", point.pathDeltaTime(), 1, 65535);
    }

    private static void validateLocationConfidence(LocationConfidence confidence) {
        checkRange("confidence.event_speed", confidence.eventSpeed(), 1, 127);
        checkRange("confidence.event_position_heading", confidence.eventPositionHeading(), 1, 127);
    }

    private static void validateAlacarteContainer(AlacarteContainer alacarte) {
        checkRange("alacarte.lane_position", alacarte.lanePosition(), -1, 14);
        if (alacarte.positioningSolution() != null && alacarte.positioningSolution() < 0) {
            throw new DenmValidationException("positioning_solution must be >= 0");
        }
    }

    private static void validatePath(List<PathElement> path) {
        checkSize("path", path.size(), 1, Integer.MAX_VALUE);
        for (int i = 0; i < path.size(); i++) {
            PathElement element = requireNonNull("path[" + i + "]", path.get(i));
            validatePathPosition("path[" + i + "].position", element.position());
            requireEnum("path[" + i + "].message_type", element.messageType(),
                    List.of("denm", "cam", "cpm", "po"));
        }
    }

    private static void validatePathPosition(String prefix, PathPosition position) {
        requireNonNull(prefix, position);
        checkRange(prefix + ".latitude", position.latitude(), -900000000, 900000001);
        checkRange(prefix + ".longitude", position.longitude(), -1800000000, 1800000001);
        checkRange(prefix + ".altitude", position.altitude(), -100000, 800001);
    }

    private static <T> T requireNonNull(String field, T value) {
        if (value == null) {
            throw new DenmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void requireNotBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new DenmValidationException("Missing mandatory field: " + field);
        }
    }

    private static void requireEquals(String field, String actual, String expected) {
        if (!Objects.equals(actual, expected)) {
            throw new DenmValidationException(field + " must equal '" + expected + "'");
        }
    }

    private static void requireEnum(String field, String actual, List<String> allowed) {
        if (!allowed.contains(actual)) {
            throw new DenmValidationException(field + " must be one of " + allowed);
        }
    }

    private static void checkRange(String field, Long value, long min, long max) {
        if (value != null && (value < min || value > max)) {
            throw new DenmValidationException(
                    field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }

    private static void checkRange(String field, Integer value, long min, long max) {
        if (value != null && (value < min || value > max)) {
            throw new DenmValidationException(
                    field + " out of range [" + min + ", " + max + "] (actual=" + value + ")");
        }
    }

    private static void checkSize(String field, int size, int min, int max) {
        if (size < min || size > max) {
            throw new DenmValidationException(
                    field + " size out of range [" + min + ", " + max + "] (actual=" + size + ")");
        }
    }
}
