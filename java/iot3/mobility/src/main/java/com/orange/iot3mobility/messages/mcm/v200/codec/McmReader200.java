/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.mcm.v200.codec;

import com.fasterxml.jackson.core.*;
import com.orange.iot3mobility.messages.mcm.v200.model.*;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.*;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.enums.ManoeuvreStrategy;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.AdvisedSubmanoeuvre;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.AdvisedTrrContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.ManoeuvreAdvice;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.*;
import com.orange.iot3mobility.messages.mcm.v200.validation.McmValidationException;
import com.orange.iot3mobility.messages.mcm.v200.validation.McmValidator200;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Jackson streaming reader for MCM v2.0.0 envelopes (json/raw format).
 */
public final class McmReader200 {

    private final JsonFactory jsonFactory;

    public McmReader200(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public McmEnvelope200 read(InputStream in) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(in)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);

            String messageType = null;
            String messageFormat = null;
            String sourceUuid = null;
            Long timestamp = null;
            String version = null;
            McmMessage200 message = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                switch (field) {
                    case "message_type" -> messageType = parser.getValueAsString();
                    case "message_format" -> messageFormat = parser.getValueAsString();
                    case "source_uuid" -> sourceUuid = parser.getValueAsString();
                    case "timestamp" -> timestamp = parser.getLongValue();
                    case "version" -> version = parser.getValueAsString();
                    case "message" -> message = readMessage(parser);
                    default -> parser.skipChildren();
                }
            }

            McmEnvelope200 envelope = new McmEnvelope200(
                    requireField(messageType, "message_type"),
                    requireField(messageFormat, "message_format"),
                    requireField(sourceUuid, "source_uuid"),
                    requireField(timestamp, "timestamp"),
                    requireField(version, "version"),
                    requireField(message, "message"));

            McmValidator200.validateEnvelope(envelope);
            return envelope;
        }
    }

    // ---- message ----

    private McmMessage200 readMessage(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        Integer protocolVersion = null;
        Long stationId = null;
        Integer generationDeltaTime = null;
        Integer stationType = null;
        Integer itssRole = null;
        ReferencePosition position = null;
        McmData mcmData = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "protocol_version" -> protocolVersion = parser.getIntValue();
                case "station_id" -> stationId = parser.getLongValue();
                case "generation_delta_time" -> generationDeltaTime = parser.getIntValue();
                case "station_type" -> stationType = parser.getIntValue();
                case "itss_role" -> itssRole = parser.getIntValue();
                case "position" -> position = readReferencePosition(parser);
                case "mcm_data" -> mcmData = readMcmData(parser);
                default -> parser.skipChildren();
            }
        }

        return new McmMessage200(
                requireField(protocolVersion, "protocol_version"),
                requireField(stationId, "station_id"),
                requireField(generationDeltaTime, "generation_delta_time"),
                requireField(stationType, "station_type"),
                itssRole != null ? itssRole : 0,
                requireField(position, "position"),
                requireField(mcmData, "mcm_data"));
    }

    // ---- position ----

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
                altitude);
    }

    private PositionConfidenceEllipse readPositionConfidenceEllipse(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer semiMajor = null, semiMinor = null, orientation = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "semi_major_confidence" -> semiMajor = parser.getIntValue();
                case "semi_minor_confidence" -> semiMinor = parser.getIntValue();
                case "semi_major_orientation" -> orientation = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new PositionConfidenceEllipse(
                requireField(semiMajor, "semi_major_confidence"),
                requireField(semiMinor, "semi_minor_confidence"),
                requireField(orientation, "semi_major_orientation"));
    }

    private Altitude readAltitude(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer altitudeValue = null, altitudeConfidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "altitude_value" -> altitudeValue = parser.getIntValue();
                case "altitude_confidence" -> altitudeConfidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new Altitude(
                requireField(altitudeValue, "altitude_value"),
                requireField(altitudeConfidence, "altitude_confidence"));
    }

    // ---- mcm_data ----

    private McmData readMcmData(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        VehicleManoeuvreContainer vehicleManoeuvreContainer = null;
        List<ManoeuvreAdvice> advisedManoeuvreContainer = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "vehicle_manoeuvre_container" -> vehicleManoeuvreContainer = readVehicleManoeuvreContainer(parser);
                case "advised_manoeuvre_container" -> advisedManoeuvreContainer = readManoeuvreAdviceList(parser);
                default -> parser.skipChildren();
            }
        }

        if (vehicleManoeuvreContainer != null) return McmData.ofVehicle(vehicleManoeuvreContainer);
        if (advisedManoeuvreContainer != null) return McmData.ofAdvised(advisedManoeuvreContainer);
        throw new McmValidationException("mcm_data must contain vehicle_manoeuvre_container or advised_manoeuvre_container");
    }

    // ---- vehicle_manoeuvre_container ----

    private VehicleManoeuvreContainer readVehicleManoeuvreContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        McmGenericCurrentStateContainer genericState = null;
        VehicleCurrentStateContainer vehicleState = null;
        List<Submanoeuvre> submaneuvres = null;
        List<ManoeuvreAdvice> manoeuvreAdvice = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "mcm_generic_current_state_container" -> genericState = readGenericCurrentState(parser);
                case "vehicle_current_state_container" -> vehicleState = readVehicleCurrentState(parser);
                case "submaneuvres" -> submaneuvres = readSubmanoeuvreList(parser);
                case "manoeuvre_advice" -> manoeuvreAdvice = readManoeuvreAdviceList(parser);
                default -> parser.skipChildren();
            }
        }

        return new VehicleManoeuvreContainer(
                requireField(genericState, "mcm_generic_current_state_container"),
                requireField(vehicleState, "vehicle_current_state_container"),
                requireField(submaneuvres, "submaneuvres"),
                manoeuvreAdvice);
    }

    private McmGenericCurrentStateContainer readGenericCurrentState(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer mcmType = null, manoeuvreId = null, concept = null, executionStatus = null;
        Rational rational = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "mcm_type" -> mcmType = parser.getIntValue();
                case "manoeuvre_id" -> manoeuvreId = parser.getIntValue();
                case "concept" -> concept = parser.getIntValue();
                case "rational" -> rational = readRational(parser);
                case "execution_status" -> executionStatus = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new McmGenericCurrentStateContainer(
                requireField(mcmType, "mcm_type"),
                requireField(manoeuvreId, "manoeuvre_id"),
                requireField(concept, "concept"),
                rational,
                executionStatus);
    }

    private Rational readRational(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer goal = null, cost = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "manoeuvre_cooperation_goal" -> goal = parser.getIntValue();
                case "manoeuvre_cooperation_cost" -> cost = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new Rational(goal, cost);
    }

    private VehicleCurrentStateContainer readVehicleCurrentState(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        ManoeuvreStrategy strategy = null;
        Speed vehicleSpeed = null;
        Wgs84Angle vehicleHeading = null;
        VehicleSize vehicleSize = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "manoeuvre_overall_strategy" -> strategy = ManoeuvreStrategy.fromValue(parser.getValueAsString());
                case "vehicle_speed" -> vehicleSpeed = readSpeed(parser);
                case "vehicle_heading" -> vehicleHeading = readWgs84Angle(parser);
                case "vehicle_size" -> vehicleSize = readVehicleSize(parser);
                default -> parser.skipChildren();
            }
        }

        return new VehicleCurrentStateContainer(
                requireField(strategy, "manoeuvre_overall_strategy"),
                requireField(vehicleSpeed, "vehicle_speed"),
                requireField(vehicleHeading, "vehicle_heading"),
                requireField(vehicleSize, "vehicle_size"));
    }

    private Speed readSpeed(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer speedValue = null, speedConfidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "speed_value" -> speedValue = parser.getIntValue();
                case "speed_confidence" -> speedConfidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new Speed(requireField(speedValue, "speed_value"), requireField(speedConfidence, "speed_confidence"));
    }

    private Wgs84Angle readWgs84Angle(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null, confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "value" -> value = parser.getIntValue();
                case "confidence" -> confidence = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new Wgs84Angle(requireField(value, "value"), requireField(confidence, "confidence"));
    }

    private VehicleSize readVehicleSize(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer vehicleType = null, vehicleWidth = null, vehicleHeight = null;
        VehicleTransportedGoods vehicleTransportedGoods = null;
        VehicleLength vehicleLength = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "vehicle_type" -> vehicleType = parser.getIntValue();
                case "vehicle_transported_goods" -> vehicleTransportedGoods = readVehicleTransportedGoods(parser);
                case "vehicle_lenth" -> vehicleLength = readVehicleLength(parser);
                case "vehicle_width" -> vehicleWidth = parser.getIntValue();
                case "vehicle_height" -> vehicleHeight = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new VehicleSize(
                requireField(vehicleType, "vehicle_type"),
                vehicleTransportedGoods,
                requireField(vehicleLength, "vehicle_lenth"),
                requireField(vehicleWidth, "vehicle_width"),
                requireField(vehicleHeight, "vehicle_height"));
    }

    private VehicleTransportedGoods readVehicleTransportedGoods(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Boolean heavyLoad = null, excessWidth = null, excessLength = null, excessHeight = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "heavy_load" -> heavyLoad = parser.getBooleanValue();
                case "excess_width" -> excessWidth = parser.getBooleanValue();
                case "excess_length" -> excessLength = parser.getBooleanValue();
                case "excess_height" -> excessHeight = parser.getBooleanValue();
                default -> parser.skipChildren();
            }
        }

        return new VehicleTransportedGoods(
                requireField(heavyLoad, "heavy_load"),
                requireField(excessWidth, "excess_width"),
                requireField(excessLength, "excess_length"),
                requireField(excessHeight, "excess_height"));
    }

    private VehicleLength readVehicleLength(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer lengthValue = null, confidenceIndication = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "vehicle_length_value" -> lengthValue = parser.getIntValue();
                case "vehicle_length_confidence_indication" -> confidenceIndication = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new VehicleLength(
                requireField(lengthValue, "vehicle_length_value"),
                requireField(confidenceIndication, "vehicle_length_confidence_indication"));
    }

    // ---- submaneuvres ----

    private List<Submanoeuvre> readSubmanoeuvreList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<Submanoeuvre> submanoeuvres = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            submanoeuvres.add(readSubmanoeuvre(parser));
        }
        return submanoeuvres;
    }

    private Submanoeuvre readSubmanoeuvre(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer submanoeuvreId = null;
        ManoeuvreStrategy submanoeuvreStrategy = null;
        List<WayPoint> referenceTrajectory = null;
        TrrDescription targetRoadResourceIContainer = null;
        TemporalCharacteristics temporalCharacteristics = null;
        KinematicsCharacteristics kinematicsCharacteristics = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "submanoeuvre_id" -> submanoeuvreId = parser.getIntValue();
                case "submanoeuvre_strategy" -> submanoeuvreStrategy = ManoeuvreStrategy.fromValue(parser.getValueAsString());
                case "reference_trajectory" -> referenceTrajectory = readWayPointList(parser);
                case "target_road_resource_i_container" -> targetRoadResourceIContainer = readTrrDescription(parser);
                case "temporal_charateristics" -> temporalCharacteristics = readTemporalCharacteristics(parser);
                case "kinematics_characteristics" -> kinematicsCharacteristics = readKinematicsCharacteristics(parser);
                default -> parser.skipChildren();
            }
        }

        return new Submanoeuvre(
                requireField(submanoeuvreId, "submanoeuvre_id"),
                submanoeuvreStrategy,
                referenceTrajectory,
                targetRoadResourceIContainer,
                requireField(temporalCharacteristics, "temporal_charateristics"),
                requireField(kinematicsCharacteristics, "kinematics_characteristics"));
    }

    // ---- manoeuvre_advice ----

    private List<ManoeuvreAdvice> readManoeuvreAdviceList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<ManoeuvreAdvice> adviceList = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            adviceList.add(readManoeuvreAdvice(parser));
        }
        return adviceList;
    }

    private ManoeuvreAdvice readManoeuvreAdvice(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Long executantId = null;
        String currentStateAdvisedChange = null;
        List<AdvisedSubmanoeuvre> submaneuvres = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "executant_id" -> executantId = parser.getLongValue();
                case "current_state_advised_change" -> currentStateAdvisedChange = parser.getValueAsString();
                case "submaneuvres" -> submaneuvres = readAdvisedSubmanoeuvreList(parser);
                default -> parser.skipChildren();
            }
        }

        return new ManoeuvreAdvice(
                requireField(executantId, "executant_id"),
                requireField(currentStateAdvisedChange, "current_state_advised_change"),
                requireField(submaneuvres, "submaneuvres"));
    }

    private List<AdvisedSubmanoeuvre> readAdvisedSubmanoeuvreList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<AdvisedSubmanoeuvre> advisedSubmanoeuvres = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            advisedSubmanoeuvres.add(readAdvisedSubmanoeuvre(parser));
        }
        return advisedSubmanoeuvres;
    }

    private AdvisedSubmanoeuvre readAdvisedSubmanoeuvre(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer submanoeuvreId = null;
        List<WayPoint> advisedTrajectory = null;
        AdvisedTrrContainer advisedTargetRoadResource = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "submanoeuvre_id" -> submanoeuvreId = parser.getIntValue();
                case "advised_trajectory" -> advisedTrajectory = readWayPointList(parser);
                case "advised_target_road_resource" -> advisedTargetRoadResource = readAdvisedTrrContainer(parser);
                default -> parser.skipChildren();
            }
        }

        return new AdvisedSubmanoeuvre(
                requireField(submanoeuvreId, "submanoeuvre_id"),
                advisedTrajectory,
                advisedTargetRoadResource);
    }

    // ---- shared helpers ----

    private List<WayPoint> readWayPointList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<WayPoint> wayPoints = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            wayPoints.add(readWayPoint(parser));
        }
        return wayPoints;
    }

    private WayPoint readWayPoint(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer wayPointType = null, longitude = null, latitude = null, altitude = null, speed = null;
        Wgs84Angle heading = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "way_point_type" -> wayPointType = parser.getIntValue();
                case "longitude" -> longitude = parser.getIntValue();
                case "latitude" -> latitude = parser.getIntValue();
                case "altitude" -> altitude = parser.getIntValue();
                case "heading" -> heading = readWgs84Angle(parser);
                case "speed" -> speed = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new WayPoint(
                requireField(wayPointType, "way_point_type"),
                requireField(longitude, "longitude"),
                requireField(latitude, "latitude"),
                altitude,
                heading,
                requireField(speed, "speed"));
    }

    private TrrDescription readTrrDescription(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer trrType = null, laneCount = null, startingLane = null, endingLane = null,
                trrWidth = null, trrLength = null;
        List<WayPoint> waypoints = null;
        List<Wgs84Angle> heading = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "trr_type" -> trrType = parser.getIntValue();
                case "lane_count" -> laneCount = parser.getIntValue();
                case "starting_lane_number" -> startingLane = parser.getIntValue();
                case "ending_lane_number" -> endingLane = parser.getIntValue();
                case "waypoints" -> waypoints = readWayPointList(parser);
                case "heading" -> heading = readWgs84AngleList(parser);
                case "trr_width" -> trrWidth = parser.getIntValue();
                case "trr_length" -> trrLength = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new TrrDescription(
                requireField(trrType, "trr_type"),
                requireField(laneCount, "lane_count"),
                startingLane,
                endingLane,
                waypoints,
                heading,
                requireField(trrWidth, "trr_width"),
                requireField(trrLength, "trr_length"));
    }

    private List<Wgs84Angle> readWgs84AngleList(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<Wgs84Angle> angles = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            angles.add(readWgs84Angle(parser));
        }
        return angles;
    }

    private AdvisedTrrContainer readAdvisedTrrContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        TrrDescription trrDescription = null;
        TemporalCharacteristics temporalCharacteristics = null;
        KinematicsCharacteristics kinematicsCharacteristics = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "trr_description" -> trrDescription = readTrrDescription(parser);
                case "temporal_characteristics" -> temporalCharacteristics = readTemporalCharacteristics(parser);
                case "kinematics_characteristics" -> kinematicsCharacteristics = readKinematicsCharacteristics(parser);
                default -> parser.skipChildren();
            }
        }

        return new AdvisedTrrContainer(
                requireField(trrDescription, "trr_description"),
                requireField(temporalCharacteristics, "temporal_characteristics"),
                requireField(kinematicsCharacteristics, "kinematics_characteristics"));
    }

    private TemporalCharacteristics readTemporalCharacteristics(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer startTime = null, endTime = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "trr_occupancy_start_time" -> startTime = parser.getIntValue();
                case "trr_occupancy_end_time" -> endTime = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new TemporalCharacteristics(
                requireField(startTime, "trr_occupancy_start_time"),
                requireField(endTime, "trr_occupancy_end_time"));
    }

    private KinematicsCharacteristics readKinematicsCharacteristics(JsonParser parser) throws IOException {
        // The ASN.1 spec defines this as NULL → empty JSON object {}
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            parser.nextToken();
            parser.skipChildren();
        }
        return KinematicsCharacteristics.INSTANCE;
    }

    // ---- utilities ----

    private static <T> T requireField(T value, String field) {
        if (value == null) {
            throw new McmValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void expect(JsonToken actual, JsonToken expected) throws JsonParseException {
        if (actual != expected) {
            throw new JsonParseException(null, "Expected " + expected + " but got " + actual);
        }
    }
}

