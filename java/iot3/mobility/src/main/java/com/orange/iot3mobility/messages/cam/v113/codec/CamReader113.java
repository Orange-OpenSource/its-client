/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v113.codec;

import com.fasterxml.jackson.core.*;
import com.orange.iot3mobility.messages.cam.v113.model.*;
import com.orange.iot3mobility.messages.cam.v113.validation.CamValidationException;
import com.orange.iot3mobility.messages.cam.v113.validation.CamValidator113;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class CamReader113 {

    private final JsonFactory jsonFactory;

    public CamReader113(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public CamEnvelope113 read(InputStream in) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(in)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);

            String type = null;
            String origin = null;
            String version = null;
            String sourceUuid = null;
            Long timestamp = null;
            CamMessage113 message = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.currentName();
                parser.nextToken();
                switch (field) {
                    case "type" -> type = parser.getValueAsString();
                    case "origin" -> origin = parser.getValueAsString();
                    case "version" -> version = parser.getValueAsString();
                    case "source_uuid" -> sourceUuid = parser.getValueAsString();
                    case "timestamp" -> timestamp = parser.getLongValue();
                    case "message" -> message = readMessage(parser);
                    default -> parser.skipChildren();
                }
            }

            CamEnvelope113 envelope = new CamEnvelope113(
                    requireField(type, "type"),
                    requireField(origin, "origin"),
                    requireField(version, "version"),
                    requireField(sourceUuid, "source_uuid"),
                    requireField(timestamp, "timestamp"),
                    requireField(message, "message"));

            CamValidator113.validateEnvelope(envelope);
            return envelope;
        }
    }

    private CamMessage113 readMessage(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        Integer protocolVersion = null;
        Long stationId = null;
        Integer generationDelta = null;
        BasicContainer basic = null;
        HighFrequencyContainer high = null;
        LowFrequencyContainer low = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "protocol_version" -> protocolVersion = parser.getIntValue();
                case "station_id" -> stationId = parser.getLongValue();
                case "generation_delta_time" -> generationDelta = parser.getIntValue();
                case "basic_container" -> basic = readBasic(parser);
                case "high_frequency_container" -> high = readHighFrequency(parser);
                case "low_frequency_container" -> low = readLowFrequency(parser);
                default -> parser.skipChildren();
            }
        }

        return new CamMessage113(
                requireField(protocolVersion, "protocol_version"),
                requireField(stationId, "station_id"),
                requireField(generationDelta, "generation_delta_time"),
                requireField(basic, "basic_container"),
                requireField(high, "high_frequency_container"),
                low);
    }

    private BasicContainer readBasic(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer stationType = null;
        ReferencePosition reference = null;
        PositionConfidence confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "station_type" -> stationType = parser.getIntValue();
                case "reference_position" -> reference = readReferencePosition(parser);
                case "confidence" -> confidence = readConfidence(parser);
                default -> parser.skipChildren();
            }
        }
        return new BasicContainer(
                requireField(stationType, "basic_container.station_type"),
                requireField(reference, "basic_container.reference_position"),
                confidence);
    }

    private ReferencePosition readReferencePosition(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer lat = null, lon = null, alt = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "latitude" -> lat = parser.getIntValue();
                case "longitude" -> lon = parser.getIntValue();
                case "altitude" -> alt = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new ReferencePosition(
                requireField(lat, "latitude"),
                requireField(lon, "longitude"),
                requireField(alt, "altitude"));
    }

    private PositionConfidence readConfidence(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        PositionConfidenceEllipse ellipse = null;
        Integer altitude = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "position_confidence_ellipse" -> ellipse = readEllipse(parser);
                case "altitude" -> altitude = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new PositionConfidence(
                requireField(ellipse, "position_confidence_ellipse"),
                requireField(altitude, "altitude_confidence"));
    }

    private PositionConfidenceEllipse readEllipse(JsonParser parser) throws IOException {
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

    private HighFrequencyContainer readHighFrequency(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        Integer heading = null;
        Integer speed = null;
        Integer direction = null;
        Integer vehicleLength = null;
        Integer vehicleWidth = null;
        Integer curvature = null;
        Integer curvatureMode = null;
        Integer longitudinal = null;
        Integer yawRate = null;
        String accControl = null;
        Integer lane = null;
        Integer lateral = null;
        Integer vertical = null;
        HighFrequencyConfidence confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "heading" -> heading = parser.getIntValue();
                case "speed" -> speed = parser.getIntValue();
                case "drive_direction" -> direction = parser.getIntValue();
                case "vehicle_length" -> vehicleLength = parser.getIntValue();
                case "vehicle_width" -> vehicleWidth = parser.getIntValue();
                case "curvature" -> curvature = parser.getIntValue();
                case "curvature_calculation_mode" -> curvatureMode = parser.getIntValue();
                case "longitudinal_acceleration" -> longitudinal = parser.getIntValue();
                case "yaw_rate" -> yawRate = parser.getIntValue();
                case "acceleration_control" -> accControl = parser.getValueAsString();
                case "lane_position" -> lane = parser.getIntValue();
                case "lateral_acceleration" -> lateral = parser.getIntValue();
                case "vertical_acceleration" -> vertical = parser.getIntValue();
                case "confidence" -> confidence = readHighFrequencyConfidence(parser);
                default -> parser.skipChildren();
            }
        }

        return new HighFrequencyContainer(
                heading,
                speed,
                direction,
                vehicleLength,
                vehicleWidth,
                curvature,
                curvatureMode,
                longitudinal,
                yawRate,
                accControl,
                lane,
                lateral,
                vertical,
                confidence);
    }

    private HighFrequencyConfidence readHighFrequencyConfidence(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer heading = null, speed = null, vehicleLength = null, yawRate = null;
        Integer longitudinal = null, curvature = null, lateral = null, vertical = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "heading" -> heading = parser.getIntValue();
                case "speed" -> speed = parser.getIntValue();
                case "vehicle_length" -> vehicleLength = parser.getIntValue();
                case "yaw_rate" -> yawRate = parser.getIntValue();
                case "longitudinal_acceleration" -> longitudinal = parser.getIntValue();
                case "curvature" -> curvature = parser.getIntValue();
                case "lateral_acceleration" -> lateral = parser.getIntValue();
                case "vertical_acceleration" -> vertical = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new HighFrequencyConfidence(
                heading,
                speed,
                vehicleLength,
                yawRate,
                longitudinal,
                curvature,
                lateral,
                vertical);
    }

    private LowFrequencyContainer readLowFrequency(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer vehicleRole = null;
        String lights = null;
        List<PathPoint> history = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "vehicle_role" -> vehicleRole = parser.getIntValue();
                case "exterior_lights" -> lights = parser.getValueAsString();
                case "path_history" -> history = readPathHistory(parser);
                default -> parser.skipChildren();
            }
        }
        return new LowFrequencyContainer(
                requireField(vehicleRole, "vehicle_role"),
                requireField(lights, "exterior_lights"),
                requireField(history, "path_history"));
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
        DeltaReferencePosition delta = null;
        Integer deltaTime = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "path_position" -> delta = readDeltaPosition(parser);
                case "path_delta_time" -> deltaTime = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new PathPoint(requireField(delta, "path_position"), deltaTime);
    }

    private DeltaReferencePosition readDeltaPosition(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer lat = null, lon = null, alt = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.currentName();
            parser.nextToken();
            switch (field) {
                case "delta_latitude" -> lat = parser.getIntValue();
                case "delta_longitude" -> lon = parser.getIntValue();
                case "delta_altitude" -> alt = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new DeltaReferencePosition(
                requireField(lat, "delta_latitude"),
                requireField(lon, "delta_longitude"),
                requireField(alt, "delta_altitude"));
    }

    private static <T> T requireField(T value, String field) {
        if (value == null) {
            throw new CamValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void expect(JsonToken actual, JsonToken expected) throws JsonParseException {
        if (actual != expected) {
            throw new JsonParseException(null, "Expected " + expected + " but got " + actual);
        }
    }
}
