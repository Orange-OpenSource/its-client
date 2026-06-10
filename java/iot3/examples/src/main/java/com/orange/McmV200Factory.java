/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.mcm.v200.model.McmData;
import com.orange.iot3mobility.messages.mcm.v200.model.McmEnvelope200;
import com.orange.iot3mobility.messages.mcm.v200.model.McmMessage200;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.Altitude;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.KinematicsCharacteristics;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.ManoeuvreStrategy;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.ReferencePosition;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.Speed;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.TemporalCharacteristics;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.TrrDescription;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.WayPoint;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.Wgs84Angle;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.ItssRole;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.StationType;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.TrrType;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.WayPointType;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.AdvisedSubmanoeuvre;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.AdvisedTrrContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.CurrentStateAdvisedChange;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.ManoeuvreAdvice;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.McmGenericCurrentStateContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.Rational;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.Submanoeuvre;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleCurrentStateContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleLength;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleManoeuvreContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleSize;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums.Concept;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums.ManoeuvreCooperationGoal;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums.McmType;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.enums.VehicleLengthConfidenceIndication;
import com.orange.iot3mobility.quadkey.LatLng;

import java.util.List;

/**
 * Factory producing realistic MCM (Manoeuvre Coordination Message) v2.0.0 test envelopes.
 * <p>
 * Two scenarios are modelled, both situated on the UTAC TEQMO test track:
 * <ol>
 *   <li><b>Vehicle Intent</b> — an autonomous passenger car broadcasts an
 *       {@code INTENT} (agreement-seeking) to perform a right lane change at ~50 km/h.
 *       It includes a three-waypoint reference trajectory and the rational
 *       {@code ROAD_SAFETY}.</li>
 *   <li><b>Coordinating Offer</b> — a second vehicle acting as coordinator broadcasts a
 *       prescriptive {@code OFFER} instructing a target vehicle to stay in its lane so it
 *       does not interfere with the ongoing lane change.
 *       It carries both a vehicle container (coordinator's own state) and a
 *       {@code ManoeuvreAdvice} entry directed at the target.</li>
 * </ol>
 *
 * <h3>ETSI encoding used</h3>
 * <ul>
 *   <li>Position: {@link EtsiConverter#latitudeEtsi}/{@link EtsiConverter#longitudeEtsi} (×10⁷ deg)</li>
 *   <li>Speed (MCM container): 0.02 m/s steps — value = m/s × 50</li>
 *   <li>Speed (waypoint): integer m/s (not ETSI)</li>
 *   <li>Heading (Wgs84Angle): 0.1° steps — value = degrees × 10</li>
 *   <li>Altitude: {@link EtsiConverter#altitudeEtsi} (0.01 m steps)</li>
 *   <li>GenerationDeltaTime: {@code TrueTime.getAccurateETSITime() % 65536}</li>
 * </ul>
 */
final class McmV200Factory {

    // ── Protocol constants ────────────────────────────────────────────────────
    private static final int PROTOCOL_VERSION = 2;

    // ── Station IDs ───────────────────────────────────────────────────────────
    /** Station ID of the autonomous passenger car broadcasting its intent. */
    static final long INTENT_VEHICLE_STATION_ID = 111111L;
    /** Station ID of the coordinating vehicle broadcasting the prescriptive offer. */
    static final long COORDINATOR_VEHICLE_STATION_ID = 222222L;
    /** Station ID of the target vehicle that receives the slowdown advice. */
    static final long TARGET_VEHICLE_STATION_ID = 333333L;

    // ── Manoeuvre session IDs ─────────────────────────────────────────────────
    private static final int LANE_CHANGE_MANOEUVRE_ID = 1;
    private static final int SLOWDOWN_MANOEUVRE_ID = 2;

    // ── Vehicle physical dimensions (berline / sedan) ─────────────────────────
    /** Vehicle length: 4.5 m → 45 in 0.1 m steps. */
    private static final int SEDAN_LENGTH_DECIMETRES = 45;
    /** Vehicle width: 1.8 m → 18 in 0.1 m steps (mirrors excluded). */
    private static final int SEDAN_WIDTH_DECIMETRES = 18;
    /** Vehicle height: 1.5 m → 30 in 0.05 m steps. */
    private static final int SEDAN_HEIGHT_FIFTIETHS = 30;
    /** ISO 3833 vehicle type: passenger car (1). */
    private static final int ISO_3833_PASSENGER_CAR = 1;

    // ── Speed constants ───────────────────────────────────────────────────────
    /** 50 km/h ≈ 13.89 m/s → 694 in 0.02 m/s ETSI steps (rounded). */
    private static final int SPEED_50_KMH_ETSI = 694;
    /** 30 km/h ≈ 8.33 m/s → 416 in 0.02 m/s ETSI steps (rounded). */
    private static final int SPEED_30_KMH_ETSI = 416;
    /** Speed confidence: within 1 m/s (50 in 0.02 m/s steps). */
    private static final int SPEED_CONFIDENCE_1_MS = 50;

    /** 50 km/h ≈ 14 m/s (integer, for WayPoint speed field). */
    private static final int WAYPOINT_SPEED_50_KMH_MS = 14;
    /** 30 km/h ≈ 8 m/s (integer, for WayPoint speed field). */
    private static final int WAYPOINT_SPEED_30_KMH_MS = 8;

    // ── Heading constants ─────────────────────────────────────────────────────
    /** Heading east (90°) → 900 in 0.1° WGS84 steps. */
    private static final int HEADING_EAST_ETSI = 900;
    /** Heading confidence: within 2° → 20 in 0.1° steps. */
    private static final int HEADING_CONFIDENCE_2_DEG = 20;

    // ── Altitude (UTAC TEQMO ≈ 60 m) ─────────────────────────────────────────
    /** UTAC TEQMO altitude ≈ 60 m → 6 000 in 0.01 m steps. */
    private static final int UTAC_ALTITUDE_ETSI = 6_000;
    /** Altitude confidence: within 10 cm, level 3. */
    private static final int ALTITUDE_CONFIDENCE = 3;

    // ── Position confidence ellipse (unavailable) ─────────────────────────────
    private static final int ELLIPSE_SEMI_AXIS_UNAVAILABLE = 4095;
    private static final int ELLIPSE_ORIENTATION_UNAVAILABLE = 3601;

    // ── Temporal characteristics ──────────────────────────────────────────────
    /** The sub-manoeuvre starts immediately (0 ms after generationDeltaTime). */
    private static final int SUBMANOEUVRE_START_TIME_MS = 0;
    /** Right lane change: 5 s horizon. */
    private static final int LANE_CHANGE_END_TIME_MS = 5_000;
    /** Slowdown advice: 3 s horizon. */
    private static final int SLOWDOWN_END_TIME_MS = 3_000;

    // ── TRR description ───────────────────────────────────────────────────────
    /** Number of lanes involved in the lane change (1 lane). */
    private static final int LANE_CHANGE_LANE_COUNT = 1;
    /** Width of the TRR in lane units: 1 lane. */
    private static final int LANE_CHANGE_TRR_WIDTH = 1;
    /**
     * TRR length between start and end waypoints of the lane change trajectory ≈ 200 m.
     * Expressed in metres [0..4095].
     */
    private static final int LANE_CHANGE_TRR_LENGTH_M = 200;

    private McmV200Factory() {
        // Factory class — not instantiable
    }

    // =========================================================================
    // Public factory methods
    // =========================================================================

    /**
     * Build an MCM v2.0.0 envelope representing an autonomous passenger car broadcasting its
     * <b>lane-change intent</b> (INTENT, AGREEMENT_SEEKING).
     * <p>
     * The vehicle is travelling eastbound at approximately 50 km/h and announces it will merge
     * into the right lane. A three-waypoint reference trajectory describes the path on the
     * UTAC TEQMO test track. The rational field indicates the goal is road safety.
     *
     * @param sourceUuid the UUID of the emitting ITS-S
     * @param position   the current geographic position of the vehicle
     * @return the constructed {@link McmEnvelope200}
     */
    static McmEnvelope200 createVehicleIntentMcm(String sourceUuid, LatLng position) {
        ReferencePosition referencePosition = buildReferencePosition(position);

        McmGenericCurrentStateContainer genericState = McmGenericCurrentStateContainer.builder()
                .mcmType(McmType.INTENT)
                .manoeuvreId(LANE_CHANGE_MANOEUVRE_ID)
                .concept(Concept.AGREEMENT_SEEKING)
                .rational(Rational.ofGoal(ManoeuvreCooperationGoal.ROAD_SAFETY))
                .build();

        VehicleCurrentStateContainer vehicleState = VehicleCurrentStateContainer.builder()
                .manoeuvreOverallStrategy(ManoeuvreStrategy.GO_TO_RIGHT_LANE)
                .vehicleSpeed(Speed.builder()
                        .speedValue(SPEED_50_KMH_ETSI)
                        .speedConfidence(SPEED_CONFIDENCE_1_MS)
                        .build())
                .vehicleHeading(Wgs84Angle.builder()
                        .value(HEADING_EAST_ETSI)
                        .confidence(HEADING_CONFIDENCE_2_DEG)
                        .build())
                .vehicleSize(buildSedanSize())
                .build();

        List<WayPoint> laneChangeTrajectory = buildLaneChangeTrajectory(position);

        TrrDescription trrDescription = TrrDescription.builder()
                .trrType(TrrType.TRR_TYPE_3)
                .laneCount(LANE_CHANGE_LANE_COUNT)
                .trrWidth(LANE_CHANGE_TRR_WIDTH)
                .trrLength(LANE_CHANGE_TRR_LENGTH_M)
                .build();

        Submanoeuvre laneChangeSubmanoeuvre = Submanoeuvre.builder()
                .submanoeuvreId(1)
                .submanoeuvreStrategy(ManoeuvreStrategy.GO_TO_RIGHT_LANE)
                .referenceTrajectory(laneChangeTrajectory)
                .targetRoadResourceIContainer(trrDescription)
                .temporalCharacteristics(TemporalCharacteristics.builder()
                        .trrOccupancyStartTime(SUBMANOEUVRE_START_TIME_MS)
                        .trrOccupancyEndTime(LANE_CHANGE_END_TIME_MS)
                        .build())
                .kinematicsCharacteristics(KinematicsCharacteristics.INSTANCE)
                .build();

        VehicleManoeuvreContainer vehicleManoeuvreContainer = VehicleManoeuvreContainer.builder()
                .mcmGenericCurrentStateContainer(genericState)
                .vehicleCurrentStateContainer(vehicleState)
                .submaneuvres(List.of(laneChangeSubmanoeuvre))
                .build();

        McmMessage200 mcmMessage = McmMessage200.builder()
                .protocolVersion(PROTOCOL_VERSION)
                .stationId(INTENT_VEHICLE_STATION_ID)
                .generationDeltaTime((int)(TrueTime.getAccurateETSITime() % 65536))
                .stationType(StationType.VEHICLE)
                .itssRole(ItssRole.COORDINATING_ITSS)
                .position(referencePosition)
                .mcmData(McmData.ofVehicle(vehicleManoeuvreContainer))
                .build();

        return McmEnvelope200.builder()
                .messageFormat("json/raw")
                .sourceUuid(sourceUuid)
                .timestamp(TrueTime.getAccurateTime())
                .message(mcmMessage)
                .build();
    }

    /**
     * Build an MCM v2.0.0 envelope representing a coordinating vehicle broadcasting a
     * <b>prescriptive offer</b> (OFFER, PRESCRIPTIVE) to instruct a target vehicle to stay in
     * its lane and not interfere with the ongoing lane change.
     * <p>
     * The coordinator is travelling eastbound at approximately 30 km/h. It includes its own
     * vehicle state (STAY_IN_LANE strategy) and a {@link ManoeuvreAdvice} entry directed at
     * {@code executantStationId}, advising it to stay in its current lane.
     *
     * @param sourceUuid          the UUID of the emitting coordinating ITS-S
     * @param position            the current geographic position of the coordinating vehicle
     * @param executantStationId  the station ID of the target vehicle that must slow down
     * @return the constructed {@link McmEnvelope200}
     */
    static McmEnvelope200 createCoordinatingMcm(
            String sourceUuid,
            LatLng position,
            long executantStationId) {
        ReferencePosition referencePosition = buildReferencePosition(position);

        McmGenericCurrentStateContainer genericState = McmGenericCurrentStateContainer.builder()
                .mcmType(McmType.OFFER)
                .manoeuvreId(SLOWDOWN_MANOEUVRE_ID)
                .concept(Concept.PRESCRIPTIVE)
                .build();

        VehicleCurrentStateContainer vehicleState = VehicleCurrentStateContainer.builder()
                .manoeuvreOverallStrategy(ManoeuvreStrategy.STAY_IN_LANE)
                .vehicleSpeed(Speed.builder()
                        .speedValue(SPEED_30_KMH_ETSI)
                        .speedConfidence(SPEED_CONFIDENCE_1_MS)
                        .build())
                .vehicleHeading(Wgs84Angle.builder()
                        .value(HEADING_EAST_ETSI)
                        .confidence(HEADING_CONFIDENCE_2_DEG)
                        .build())
                .vehicleSize(buildSedanSize())
                .build();

        List<WayPoint> coordinatorTrajectory = buildStraightTrajectory(position);

        Submanoeuvre coordinatorSubmanoeuvre = Submanoeuvre.builder()
                .submanoeuvreId(1)
                .submanoeuvreStrategy(ManoeuvreStrategy.STAY_IN_LANE)
                .referenceTrajectory(coordinatorTrajectory)
                .temporalCharacteristics(TemporalCharacteristics.builder()
                        .trrOccupancyStartTime(SUBMANOEUVRE_START_TIME_MS)
                        .trrOccupancyEndTime(SLOWDOWN_END_TIME_MS)
                        .build())
                .kinematicsCharacteristics(KinematicsCharacteristics.INSTANCE)
                .build();

        // Advisory sub-manoeuvre: advise the target to decelerate to a stop
        AdvisedSubmanoeuvre advisedSubmanoeuvre = AdvisedSubmanoeuvre.builder()
                .submanoeuvreId(1)
                .advisedTargetRoadResource(AdvisedTrrContainer.builder()
                        .trrDescription(TrrDescription.builder()
                                .trrType(TrrType.TRR_TYPE_1)
                                .laneCount(1)
                                .trrWidth(1)
                                .trrLength(50)
                                .build())
                        .temporalCharacteristics(TemporalCharacteristics.builder()
                                .trrOccupancyStartTime(SUBMANOEUVRE_START_TIME_MS)
                                .trrOccupancyEndTime(SLOWDOWN_END_TIME_MS)
                                .build())
                        .kinematicsCharacteristics(KinematicsCharacteristics.INSTANCE)
                        .build())
                .build();

        ManoeuvreAdvice slowdownAdvice = ManoeuvreAdvice.builder()
                .executantId(executantStationId)
                .currentStateAdvisedChange(CurrentStateAdvisedChange.STAY_IN_LANE)
                .submaneuvres(List.of(advisedSubmanoeuvre))
                .build();

        VehicleManoeuvreContainer vehicleManoeuvreContainer = VehicleManoeuvreContainer.builder()
                .mcmGenericCurrentStateContainer(genericState)
                .vehicleCurrentStateContainer(vehicleState)
                .submaneuvres(List.of(coordinatorSubmanoeuvre))
                .manoeuvreAdvice(List.of(slowdownAdvice))
                .build();

        McmMessage200 mcmMessage = McmMessage200.builder()
                .protocolVersion(PROTOCOL_VERSION)
                .stationId(COORDINATOR_VEHICLE_STATION_ID)
                .generationDeltaTime((int)(TrueTime.getAccurateETSITime() % 65536))
                .stationType(StationType.VEHICLE)
                .itssRole(ItssRole.COORDINATING_ITSS)
                .position(referencePosition)
                .mcmData(McmData.ofVehicle(vehicleManoeuvreContainer))
                .build();

        return McmEnvelope200.builder()
                .messageFormat("json/raw")
                .sourceUuid(sourceUuid)
                .timestamp(TrueTime.getAccurateTime())
                .message(mcmMessage)
                .build();
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Build a {@link ReferencePosition} from a {@link LatLng}, using ETSI encoding.
     * Altitude corresponds to UTAC TEQMO elevation (~60 m).
     */
    private static ReferencePosition buildReferencePosition(LatLng position) {
        return ReferencePosition.builder()
                .latitude(EtsiConverter.latitudeEtsi(position.getLatitude()))
                .longitude(EtsiConverter.longitudeEtsi(position.getLongitude()))
                .positionConfidenceEllipse(PositionConfidenceEllipse.builder()
                        .semiMajorConfidence(ELLIPSE_SEMI_AXIS_UNAVAILABLE)
                        .semiMinorConfidence(ELLIPSE_SEMI_AXIS_UNAVAILABLE)
                        .semiMajorOrientation(ELLIPSE_ORIENTATION_UNAVAILABLE)
                        .build())
                .altitude(Altitude.builder()
                        .altitudeValue(UTAC_ALTITUDE_ETSI)
                        .altitudeConfidence(ALTITUDE_CONFIDENCE)
                        .build())
                .build();
    }

    /**
     * Build a {@link VehicleSize} representing a typical sedan (passenger car).
     *
     * <ul>
     *   <li>ISO 3833 type 1 (passenger car)</li>
     *   <li>Length: 4.5 m (45 in 0.1 m steps), no trailer</li>
     *   <li>Width: 1.8 m (18 in 0.1 m steps, mirrors excluded)</li>
     *   <li>Height: 1.5 m (30 in 0.05 m steps)</li>
     * </ul>
     */
    private static VehicleSize buildSedanSize() {
        return VehicleSize.builder()
                .vehicleType(ISO_3833_PASSENGER_CAR)
                .vehicleLength(VehicleLength.builder()
                        .vehicleLengthValue(SEDAN_LENGTH_DECIMETRES)
                        .vehicleLengthConfidenceIndication(VehicleLengthConfidenceIndication.NO_TRAILER_PRESENT)
                        .build())
                .vehicleWidth(SEDAN_WIDTH_DECIMETRES)
                .vehicleHeight(SEDAN_HEIGHT_FIFTIETHS)
                .build();
    }

    /**
     * Build a three-waypoint right lane-change trajectory, eastbound on the UTAC track.
     * <ul>
     *   <li>Start: current position (already in the left lane)</li>
     *   <li>Intermediate: ~100 m east and ~3.5 m south (crossing into right lane)</li>
     *   <li>End: ~200 m east and settled in the right lane</li>
     * </ul>
     * Offsets in 10⁻⁷ degrees:
     * <ul>
     *   <li>100 m east at lat 48.6° ≈ +13 600 ETSI lon units</li>
     *   <li>3.5 m south ≈ −315 ETSI lat units (right lane shift)</li>
     * </ul>
     */
    private static List<WayPoint> buildLaneChangeTrajectory(LatLng position) {
        int startLatEtsi  = EtsiConverter.latitudeEtsi(position.getLatitude());
        int startLonEtsi  = EtsiConverter.longitudeEtsi(position.getLongitude());

        // Intermediate: 100 m east, 3.5 m south (entering right lane)
        int midLatEtsi    = startLatEtsi  - 315;
        int midLonEtsi    = startLonEtsi  + 13_600;

        // End: 200 m east, settled 3.5 m south
        int endLonEtsi    = midLonEtsi    + 13_600;

        WayPoint startWayPoint = WayPoint.builder()
                .wayPointType(WayPointType.STARTING_WAY_POINT)
                .latitude(startLatEtsi)
                .longitude(startLonEtsi)
                .heading(Wgs84Angle.builder()
                        .value(HEADING_EAST_ETSI)
                        .confidence(HEADING_CONFIDENCE_2_DEG)
                        .build())
                .speed(WAYPOINT_SPEED_50_KMH_MS)
                .build();

        WayPoint intermediateWayPoint = WayPoint.builder()
                .wayPointType(WayPointType.INTERMEDIATE_WAY_POINT)
                .latitude(midLatEtsi)
                .longitude(midLonEtsi)
                .heading(Wgs84Angle.builder()
                        .value(HEADING_EAST_ETSI + 50) // slight south-east heading during merge (95°)
                        .confidence(HEADING_CONFIDENCE_2_DEG)
                        .build())
                .speed(WAYPOINT_SPEED_50_KMH_MS)
                .build();

        WayPoint endWayPoint = WayPoint.builder()
                .wayPointType(WayPointType.ENDING_WAY_POINT)
                .latitude(midLatEtsi)
                .longitude(endLonEtsi)
                .heading(Wgs84Angle.builder()
                        .value(HEADING_EAST_ETSI)
                        .confidence(HEADING_CONFIDENCE_2_DEG)
                        .build())
                .speed(WAYPOINT_SPEED_50_KMH_MS)
                .build();

        return List.of(startWayPoint, intermediateWayPoint, endWayPoint);
    }

    /**
     * Build a two-waypoint straight-ahead trajectory for the coordinating vehicle.
     * The coordinator travels ~50 m eastbound at 30 km/h.
     */
    private static List<WayPoint> buildStraightTrajectory(LatLng position) {
        int startLatEtsi = EtsiConverter.latitudeEtsi(position.getLatitude());
        int startLonEtsi = EtsiConverter.longitudeEtsi(position.getLongitude());

        // End: ~50 m east ≈ +6 800 ETSI lon units at lat 48.6°
        int endLonEtsi   = startLonEtsi + 6_800;

        WayPoint startWayPoint = WayPoint.builder()
                .wayPointType(WayPointType.STARTING_WAY_POINT)
                .latitude(startLatEtsi)
                .longitude(startLonEtsi)
                .heading(Wgs84Angle.builder()
                        .value(HEADING_EAST_ETSI)
                        .confidence(HEADING_CONFIDENCE_2_DEG)
                        .build())
                .speed(WAYPOINT_SPEED_30_KMH_MS)
                .build();

        WayPoint endWayPoint = WayPoint.builder()
                .wayPointType(WayPointType.ENDING_WAY_POINT)
                .latitude(startLatEtsi)
                .longitude(endLonEtsi)
                .heading(Wgs84Angle.builder()
                        .value(HEADING_EAST_ETSI)
                        .confidence(HEADING_CONFIDENCE_2_DEG)
                        .build())
                .speed(WAYPOINT_SPEED_30_KMH_MS)
                .build();

        return List.of(startWayPoint, endWayPoint);
    }
}

