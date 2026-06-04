/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.denm.v230.validation;

import com.orange.iot3mobility.messages.denm.v230.model.DenmEnvelope230;
import com.orange.iot3mobility.messages.denm.v230.model.DenmMessage230;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.AlacarteContainer;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.CarryingDangerousGoods;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.ClosedLanes;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.RoadWorks;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.StationaryVehicle;
import com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer.VehicleIdentification;
import com.orange.iot3mobility.messages.denm.v230.model.defs.Altitude;
import com.orange.iot3mobility.messages.denm.v230.model.defs.DeltaReferencePosition;
import com.orange.iot3mobility.messages.denm.v230.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.DetectionZone;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.EventPositionHeading;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.EventSpeed;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.LocationContainer;
import com.orange.iot3mobility.messages.denm.v230.model.locationcontainer.PathPoint;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v230.model.path.PathElement;
import com.orange.iot3mobility.messages.denm.v230.model.path.PathPosition;
import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.CauseCode;
import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.EventZone;
import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.SituationContainer;

import java.util.List;
import java.util.Objects;

public final class DenmValidator230 {

    private DenmValidator230() {}

    public static void validateEnvelope(DenmEnvelope230 env) {
        requireNonNull("envelope", env);
        requireEquals("message_type", env.messageType(), "denm");
        requireEquals("version", env.version(), "2.3.0");
        requireNotBlank("source_uuid", env.sourceUuid());
        checkRange("timestamp", env.timestamp(), 1514764800000L, 1830297600000L);
        if (env.path() != null) {
            validatePath(env.path());
        }
        validateMessage(env.message());
    }

    public static void validateMessage(DenmMessage230 msg) {
        requireNonNull("message", msg);
        checkRange("protocol_version", msg.protocolVersion(), 0, 255);
        checkRange("station_id", msg.stationId(), 0, 4294967295L);
        validateManagement(msg.managementContainer());
        if (msg.situationContainer() != null) {
            validateSituation(msg.situationContainer());
        }
        if (msg.locationContainer() != null) {
            validateLocation(msg.locationContainer());
        }
        if (msg.alacarteContainer() != null) {
            validateAlacarte(msg.alacarteContainer());
        }
    }

    private static void validateManagement(ManagementContainer container) {
        requireNonNull("management_container", container);
        validateActionId(container.actionId());
        checkRange("detection_time", container.detectionTime(), 0L, 4398046511103L);
        checkRange("reference_time", container.referenceTime(), 0L, 4398046511103L);
        checkRange("termination", container.termination(), 0, 1);
        validateReferencePosition(container.eventPosition());
        checkRange("awareness_distance", container.awarenessDistance(), 0, 7);
        checkRange("traffic_direction", container.trafficDirection(), 0, 3);
        checkRange("validity_duration", container.validityDuration(), 0, 86400);
        checkRange("transmission_interval", container.transmissionInterval(), 1, 10000);
        requireNonNull("station_type", container.stationType());
        checkRange("station_type", container.stationType(), 0, 255);
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
        validatePositionConfidenceEllipse(position.positionConfidenceEllipse());
        validateAltitude(position.altitude());
    }

    private static void validatePositionConfidenceEllipse(PositionConfidenceEllipse ellipse) {
        checkRange("position_confidence_ellipse.semi_major", ellipse.semiMajor(), 0, 4095);
        checkRange("position_confidence_ellipse.semi_minor", ellipse.semiMinor(), 0, 4095);
        checkRange("position_confidence_ellipse.semi_major_orientation", ellipse.semiMajorOrientation(), 0, 3601);
    }

    private static void validateAltitude(Altitude altitude) {
        checkRange("altitude.value", altitude.value(), -100000, 800001);
        checkRange("altitude.confidence", altitude.confidence(), 0, 15);
    }

    private static void validateSituation(SituationContainer situation) {
        requireNonNull("situation_container", situation);
        requireNonNull("information_quality", situation.informationQuality());
        checkRange("information_quality", situation.informationQuality(), 0, 7);
        requireNonNull("event_type", situation.eventType());
        validateCauseCode("event_type", situation.eventType());
        if (situation.linkedCause() != null) {
            validateCauseCode("linked_cause", situation.linkedCause());
        }
        if (situation.eventZone() != null) {
            validateEventZones(situation.eventZone());
        }
        if (situation.linkedDenms() != null) {
            validateLinkedDenms(situation.linkedDenms());
        }
        if (situation.eventEnd() != null) {
            checkRange("event_end", situation.eventEnd(), -8190, 8191);
        }
    }

    private static void validateCauseCode(String field, CauseCode causeCode) {
        requireNonNull(field, causeCode);
        checkRange(field + ".cause", causeCode.cause(), 0, 255);
        checkRange(field + ".subcause", causeCode.subcause(), 0, 255);
    }

    private static void validateEventZones(List<EventZone> zones) {
        for (int i = 0; i < zones.size(); i++) {
            EventZone zone = requireNonNull("event_zone[" + i + "]", zones.get(i));
            validateDeltaReferencePosition("event_zone[" + i + "].event_position", zone.eventPosition());
            checkRange("event_zone[" + i + "].event_delta_time", zone.eventDeltaTime(), 0, 65535);
            checkRange("event_zone[" + i + "].information_quality", zone.informationQuality(), 0, 7);
        }
    }

    private static void validateLinkedDenms(List<ActionId> linkedDenms) {
        checkSize("linked_denms", linkedDenms.size(), 1, 8);
        for (int i = 0; i < linkedDenms.size(); i++) {
            validateActionId(linkedDenms.get(i));
        }
    }

    private static void validateLocation(LocationContainer location) {
        requireNonNull("location_container", location);
        if (location.eventSpeed() != null) {
            validateEventSpeed(location.eventSpeed());
        }
        if (location.eventPositionHeading() != null) {
            validateEventPositionHeading(location.eventPositionHeading());
        }
        if (location.detectionZonesToEventPosition() != null) {
            validateDetectionZones(location.detectionZonesToEventPosition());
        }
        checkRange("road_type", location.roadType(), 0, 3);
    }

    private static void validateEventSpeed(EventSpeed speed) {
        checkRange("event_speed.value", speed.value(), 0, 16383);
        checkRange("event_speed.confidence", speed.confidence(), 1, 127);
    }

    private static void validateEventPositionHeading(EventPositionHeading heading) {
        checkRange("event_position_heading.value", heading.value(), 0, 3601);
        checkRange("event_position_heading.confidence", heading.confidence(), 1, 127);
    }

    private static void validateDetectionZones(List<DetectionZone> zones) {
        checkSize("detection_zones_to_event_position", zones.size(), 1, 7);
        for (int i = 0; i < zones.size(); i++) {
            DetectionZone zone = requireNonNull("detection_zones_to_event_position[" + i + "]", zones.get(i));
            validatePathPoints("detection_zones_to_event_position[" + i + "]", zone.path());
        }
    }

    private static void validatePathPoints(String prefix, List<PathPoint> points) {
        if (points.size() > 40) {
            throw new DenmValidationException(prefix + ".path size exceeds 40");
        }
        for (int i = 0; i < points.size(); i++) {
            PathPoint point = requireNonNull(prefix + ".path[" + i + "]", points.get(i));
            validateDeltaReferencePosition(prefix + ".path[" + i + "].path_position", point.pathPosition());
            checkRange(prefix + ".path[" + i + "].path_delta_time", point.pathDeltaTime(), 1, 65535);
        }
    }

    private static void validateDeltaReferencePosition(String field, DeltaReferencePosition position) {
        checkRange(field + ".delta_latitude", position.deltaLatitude(), -131071, 131072);
        checkRange(field + ".delta_longitude", position.deltaLongitude(), -131071, 131072);
        checkRange(field + ".delta_altitude", position.deltaAltitude(), -12700, 12800);
    }

    private static void validateAlacarte(AlacarteContainer alacarte) {
        checkRange("alacarte_container.lane_position", alacarte.lanePosition(), -1, 14);
        if (alacarte.roadWorks() != null) {
            validateRoadWorks(alacarte.roadWorks());
        }
        checkRange("alacarte_container.positioning_solution", alacarte.positioningSolution(), 0, 6);
        if (alacarte.stationaryVehicle() != null) {
            validateStationaryVehicle(alacarte.stationaryVehicle());
        }
    }

    private static void validateRoadWorks(RoadWorks roadWorks) {
        checkRange("road_works.light_bar_siren_in_use", roadWorks.lightBarSirenInUse(), 0, 3);
        if (roadWorks.closedLanes() != null) {
            validateClosedLanes(roadWorks.closedLanes());
        }
        if (roadWorks.restriction() != null) {
            checkSize("road_works.restriction", roadWorks.restriction().size(), 1, 3);
            for (int i = 0; i < roadWorks.restriction().size(); i++) {
                checkRange("road_works.restriction[" + i + "]", roadWorks.restriction().get(i), 0, 255);
            }
        }
        checkRange("road_works.speed_limit", roadWorks.speedLimit(), 0, 255);
        if (roadWorks.incidentIndication() != null) {
            validateCauseCode("road_works.incident_indication", roadWorks.incidentIndication());
        }
        if (roadWorks.recommendedPath() != null) {
            for (int i = 0; i < roadWorks.recommendedPath().size(); i++) {
                validateDeltaReferencePosition("road_works.recommended_path[" + i + "]", roadWorks.recommendedPath().get(i));
            }
        }
        if (roadWorks.startingPointSpeedLimit() != null) {
            validateDeltaReferencePosition("road_works.starting_point_speed_limit", roadWorks.startingPointSpeedLimit());
        }
        checkRange("road_works.traffic_flow_rule", roadWorks.trafficFlowRule(), 0, 3);
        if (roadWorks.referenceDenms() != null) {
            for (int i = 0; i < roadWorks.referenceDenms().size(); i++) {
                validateActionId(roadWorks.referenceDenms().get(i));
            }
        }
    }

    private static void validateClosedLanes(ClosedLanes closedLanes) {
        checkRange("road_works.closed_lanes.hard_shoulder_status", closedLanes.hardShoulderStatus(), 0, 2);
        checkRange("road_works.closed_lanes.driving_lane_status", closedLanes.drivingLaneStatus(), 0, 16383);
    }

    private static void validateStationaryVehicle(StationaryVehicle stationaryVehicle) {
        checkRange("stationary_vehicle.stationary_since", stationaryVehicle.stationarySince(), 0, 3);
        if (stationaryVehicle.stationaryCause() != null) {
            validateCauseCode("stationary_vehicle.stationary_cause", stationaryVehicle.stationaryCause());
        }
        if (stationaryVehicle.carryingDangerousGoods() != null) {
            validateCarryingDangerousGoods(stationaryVehicle.carryingDangerousGoods());
        }
        checkRange("stationary_vehicle.number_of_occupants", stationaryVehicle.numberOfOccupants(), 0, 127);
        if (stationaryVehicle.vehicleIdentification() != null) {
            validateVehicleIdentification(stationaryVehicle.vehicleIdentification());
        }
        checkRange("stationary_vehicle.energy_storage_type", stationaryVehicle.energyStorageType(), 0, 127);
    }

    private static void validateCarryingDangerousGoods(CarryingDangerousGoods goods) {
        checkRange("carrying_dangerous_goods.dangerous_goods_type", goods.dangerousGoodsType(), 0, 19);
        checkRange("carrying_dangerous_goods.un_number", goods.unNumber(), 0, 9999);
        if (goods.emergencyActionCode() != null) {
            int length = goods.emergencyActionCode().length();
            if (length < 1 || length > 4) {
                throw new DenmValidationException(
                        "carrying_dangerous_goods.emergency_action_code length out of range [1, 4] (actual=" + length + ")");
            }
        }
        if (goods.companyName() != null) {
            int length = goods.companyName().length();
            if (length < 1 || length > 24) {
                throw new DenmValidationException(
                        "carrying_dangerous_goods.company_name length out of range [1, 24] (actual=" + length + ")");
            }
        }
    }

    private static void validateVehicleIdentification(VehicleIdentification vehicleIdentification) {
        if (vehicleIdentification.wMINumber() != null) {
            int length = vehicleIdentification.wMINumber().length();
            if (length < 1 || length > 3) {
                throw new DenmValidationException(
                        "vehicle_identification.w_m_inumber length out of range [1, 3] (actual=" + length + ")");
            }
        }
        if (vehicleIdentification.vDS() != null && vehicleIdentification.vDS().length() != 6) {
            throw new DenmValidationException(
                    "vehicle_identification.v_d_s must be exactly 6 characters (actual=" + vehicleIdentification.vDS().length() + ")");
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

