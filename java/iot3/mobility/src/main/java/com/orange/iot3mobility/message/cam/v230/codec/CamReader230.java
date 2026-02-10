package com.orange.iot3mobility.message.cam.v230.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orange.iot3mobility.message.cam.v230.model.*;
import com.orange.iot3mobility.message.cam.v230.model.basiccontainer.*;
import com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer.*;
import com.orange.iot3mobility.message.cam.v230.model.lowfrequencycontainer.*;
import com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer.*;
import com.orange.iot3mobility.message.cam.v230.validation.CamValidationException;
import com.orange.iot3mobility.message.cam.v230.validation.CamValidator230;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Streaming JSON reader for CAM 2.3.0 payloads (structured JSON or ASN.1).
 */
public final class CamReader230 {

    private final JsonFactory jsonFactory;

    public CamReader230(JsonFactory jsonFactory) {
        this.jsonFactory = Objects.requireNonNull(jsonFactory, "jsonFactory");
    }

    public CamEnvelope230 read(InputStream in) throws IOException {
        try (JsonParser parser = jsonFactory.createParser(in)) {
            expect(parser.nextToken(), JsonToken.START_OBJECT);

            String messageType = null;
            String messageFormat = null;
            String sourceUuid = null;
            Long timestamp = null;
            String version = null;
            CamMessage230 payload = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.getCurrentName();
                parser.nextToken();
                switch (field) {
                    case "message_type" -> messageType = parser.getValueAsString();
                    case "message_format" -> messageFormat = parser.getValueAsString();
                    case "source_uuid" -> sourceUuid = parser.getValueAsString();
                    case "timestamp" -> timestamp = parser.getLongValue();
                    case "version" -> version = parser.getValueAsString();
                    case "message" -> payload = readPayload(parser);
                    default -> parser.skipChildren();
                }
            }

            CamEnvelope230 envelope = new CamEnvelope230(
                    messageType,
                    messageFormat,
                    sourceUuid,
                    requireField(timestamp, "timestamp"),
                    requireField(version, "version"),
                    requireField(payload, "message"));

            CamValidator230.validateEnvelope(envelope);
            return envelope;
        }
    }

    private CamMessage230 readPayload(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        boolean structuredDetected = false;
        boolean asn1Detected = false;

        Integer protocolVersion = null;
        Long stationId = null;
        Integer generationDeltaTime = null;
        BasicContainer basic = null;
        HighFrequencyContainer high = null;
        LowFrequencyContainer low = null;
        SpecialVehicleContainer special = null;

        String asn1Version = null;
        String asn1Payload = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "protocol_version" -> {
                    structuredDetected = true;
                    protocolVersion = parser.getIntValue();
                }
                case "station_id" -> {
                    structuredDetected = true;
                    stationId = parser.getLongValue();
                }
                case "generation_delta_time" -> {
                    structuredDetected = true;
                    generationDeltaTime = parser.getIntValue();
                }
                case "basic_container" -> {
                    structuredDetected = true;
                    basic = readBasicContainer(parser);
                }
                case "high_frequency_container" -> {
                    structuredDetected = true;
                    high = readHighFrequencyContainer(parser);
                }
                case "low_frequency_container" -> {
                    structuredDetected = true;
                    low = readLowFrequencyContainer(parser);
                }
                case "special_vehicle_container" -> {
                    structuredDetected = true;
                    special = readSpecialVehicleContainer(parser);
                }
                case "version" -> {
                    asn1Detected = true;
                    asn1Version = parser.getValueAsString();
                }
                case "payload" -> {
                    asn1Detected = true;
                    asn1Payload = parser.getValueAsString();
                }
                default -> parser.skipChildren();
            }

            if (structuredDetected && asn1Detected) {
                throw new CamValidationException("message cannot mix structured CAM fields with ASN.1 payload");
            }
        }

        if (structuredDetected) {
            return new CamStructuredData(
                    requireField(protocolVersion, "protocol_version"),
                    requireField(stationId, "station_id"),
                    requireField(generationDeltaTime, "generation_delta_time"),
                    requireField(basic, "basic_container"),
                    requireField(high, "high_frequency_container"),
                    low,
                    special);
        } else if (asn1Detected) {
            return new CamAsn1Payload(
                    requireField(asn1Version, "version"),
                    requireField(asn1Payload, "payload"));
        } else {
            throw new CamValidationException("message payload is empty");
        }
    }

    /* --------------------------------------------------------------------- */
    /* Basic container                                                       */
    /* --------------------------------------------------------------------- */

    private BasicContainer readBasicContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer stationType = null;
        ReferencePosition reference = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "station_type" -> stationType = parser.getIntValue();
                case "reference_position" -> reference = readReferencePosition(parser);
                default -> parser.skipChildren();
            }
        }
        return new BasicContainer(
                requireField(stationType, "basic_container.station_type"),
                requireField(reference, "basic_container.reference_position"));
    }

    private ReferencePosition readReferencePosition(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer lat = null;
        Integer lon = null;
        PositionConfidenceEllipse ellipse = null;
        Altitude altitude = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "latitude" -> lat = parser.getIntValue();
                case "longitude" -> lon = parser.getIntValue();
                case "position_confidence_ellipse" -> ellipse = readEllipse(parser);
                case "altitude" -> altitude = readAltitude(parser);
                default -> parser.skipChildren();
            }
        }
        return new ReferencePosition(
                requireField(lat, "reference_position.latitude"),
                requireField(lon, "reference_position.longitude"),
                requireField(ellipse, "reference_position.position_confidence_ellipse"),
                requireField(altitude, "reference_position.altitude"));
    }

    private PositionConfidenceEllipse readEllipse(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer semiMajor = null;
        Integer semiMinor = null;
        Integer orientation = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "semi_major" -> semiMajor = parser.getIntValue();
                case "semi_minor" -> semiMinor = parser.getIntValue();
                case "semi_major_orientation" -> orientation = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new PositionConfidenceEllipse(
                requireField(semiMajor, "position_confidence_ellipse.semi_major"),
                requireField(semiMinor, "position_confidence_ellipse.semi_minor"),
                requireField(orientation, "position_confidence_ellipse.semi_major_orientation"));
    }

    private Altitude readAltitude(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
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

    /* --------------------------------------------------------------------- */
    /* High-frequency containers                                             */
    /* --------------------------------------------------------------------- */

    private HighFrequencyContainer readHighFrequencyContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        BasicVehicleContainerHighFrequency basicVehicle = null;
        RsuContainerHighFrequency rsu = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "basic_vehicle_container_high_frequency" ->
                        basicVehicle = readBasicVehicleHF(parser);
                case "rsu_container_high_frequency" ->
                        rsu = readRsuHF(parser);
                default -> parser.skipChildren();
            }
        }

        if (basicVehicle != null && rsu != null) {
            throw new CamValidationException("high_frequency_container cannot contain both vehicle and RSU sub-containers");
        } else if (basicVehicle != null) {
            return basicVehicle;
        } else if (rsu != null) {
            return rsu;
        }
        throw new CamValidationException("high_frequency_container is missing required sub-container");
    }

    private BasicVehicleContainerHighFrequency readBasicVehicleHF(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);

        Heading heading = null;
        Speed speed = null;
        Integer driveDirection = null;
        VehicleLength vehicleLength = null;
        Integer vehicleWidth = null;
        AccelerationComponent longitudinal = null;
        Curvature curvature = null;
        Integer curvatureMode = null;
        YawRate yawRate = null;
        AccelerationControl accControl = null;
        Integer lanePosition = null;
        SteeringWheelAngle steering = null;
        AccelerationComponent lateral = null;
        AccelerationComponent vertical = null;
        Integer performanceClass = null;
        CenDsrcTollingZone cenZone = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "heading" -> heading = readHeading(parser);
                case "speed" -> speed = readSpeed(parser);
                case "drive_direction" -> driveDirection = parser.getIntValue();
                case "vehicle_length" -> vehicleLength = readVehicleLength(parser);
                case "vehicle_width" -> vehicleWidth = parser.getIntValue();
                case "longitudinal_acceleration" -> longitudinal = readAccelerationComponent(parser);
                case "curvature" -> curvature = readCurvature(parser);
                case "curvature_calculation_mode" -> curvatureMode = parser.getIntValue();
                case "yaw_rate" -> yawRate = readYawRate(parser);
                case "acceleration_control" -> accControl = readAccelerationControl(parser);
                case "lane_position" -> lanePosition = parser.getIntValue();
                case "steering_wheel_angle" -> steering = readSteeringWheelAngle(parser);
                case "lateral_acceleration" -> lateral = readAccelerationComponent(parser);
                case "vertical_acceleration" -> vertical = readAccelerationComponent(parser);
                case "performance_class" -> performanceClass = parser.getIntValue();
                case "cen_dsrc_tolling_zone" -> cenZone = readCenDsrcTollingZone(parser);
                default -> parser.skipChildren();
            }
        }

        return new BasicVehicleContainerHighFrequency(
                requireField(heading, "high_frequency_container.heading"),
                requireField(speed, "high_frequency_container.speed"),
                requireField(driveDirection, "high_frequency_container.drive_direction"),
                requireField(vehicleLength, "high_frequency_container.vehicle_length"),
                requireField(vehicleWidth, "high_frequency_container.vehicle_width"),
                requireField(longitudinal, "high_frequency_container.longitudinal_acceleration"),
                requireField(curvature, "high_frequency_container.curvature"),
                requireField(curvatureMode, "high_frequency_container.curvature_calculation_mode"),
                requireField(yawRate, "high_frequency_container.yaw_rate"),
                accControl,
                lanePosition,
                steering,
                lateral,
                vertical,
                performanceClass,
                cenZone);
    }

    private RsuContainerHighFrequency readRsuHF(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        List<ProtectedCommunicationZone> zones = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("protected_communication_zones_rsu".equals(field)) {
                zones = readProtectedZones(parser);
            } else {
                parser.skipChildren();
            }
        }
        return new RsuContainerHighFrequency(
                requireField(zones, "rsu_container_high_frequency.protected_communication_zones_rsu"));
    }

    private List<ProtectedCommunicationZone> readProtectedZones(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_ARRAY);
        List<ProtectedCommunicationZone> zones = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            zones.add(readProtectedZone(parser));
        }
        return zones;
    }

    private ProtectedCommunicationZone readProtectedZone(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer type = null;
        Long expiry = null;
        Integer latitude = null;
        Integer longitude = null;
        Integer radius = null;
        Integer zoneId = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "protected_zone_type" -> type = parser.getIntValue();
                case "expiry_time" -> expiry = parser.getLongValue();
                case "protected_zone_latitude" -> latitude = parser.getIntValue();
                case "protected_zone_longitude" -> longitude = parser.getIntValue();
                case "protected_zone_radius" -> radius = parser.getIntValue();
                case "protected_zone_id" -> zoneId = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }

        return new ProtectedCommunicationZone(
                requireField(type, "protected_zone_type"),
                expiry,
                requireField(latitude, "protected_zone_latitude"),
                requireField(longitude, "protected_zone_longitude"),
                radius,
                zoneId);
    }

    /* --------------------------------------------------------------------- */
    /* Low-frequency container                                               */
    /* --------------------------------------------------------------------- */

    private LowFrequencyContainer readLowFrequencyContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        BasicVehicleContainerLowFrequency basic = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("basic_vehicle_container_low_frequency".equals(field)
                    || "basic_vehicle_container_low_frequency  ".equals(field)) { // schema typo
                basic = readBasicVehicleLF(parser);
            } else {
                parser.skipChildren();
            }
        }
        return new LowFrequencyContainer(requireField(basic, "basic_vehicle_container_low_frequency"));
    }

    private BasicVehicleContainerLowFrequency readBasicVehicleLF(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer vehicleRole = null;
        ExteriorLights lights = null;
        List<PathPoint> history = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "vehicle_role" -> vehicleRole = parser.getIntValue();
                case "exterior_lights" -> lights = readExteriorLights(parser);
                case "path_history" -> history = readPathHistory(parser);
                default -> parser.skipChildren();
            }
        }
        return new BasicVehicleContainerLowFrequency(
                requireField(vehicleRole, "vehicle_role"),
                requireField(lights, "exterior_lights"),
                requireField(history, "path_history"));
    }

    private ExteriorLights readExteriorLights(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Boolean lowBeam = null;
        Boolean highBeam = null;
        Boolean leftTurn = null;
        Boolean rightTurn = null;
        Boolean daytime = null;
        Boolean reverse = null;
        Boolean fog = null;
        Boolean parking = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "low_beam_headlights_on" -> lowBeam = parser.getBooleanValue();
                case "high_beam_headlights_on" -> highBeam = parser.getBooleanValue();
                case "left_turn_signal_on" -> leftTurn = parser.getBooleanValue();
                case "right_turn_signal_on" -> rightTurn = parser.getBooleanValue();
                case "daytime_running_lights_on" -> daytime = parser.getBooleanValue();
                case "reverse_light_on" -> reverse = parser.getBooleanValue();
                case "fog_light_on" -> fog = parser.getBooleanValue();
                case "parking_lights_on" -> parking = parser.getBooleanValue();
                default -> parser.skipChildren();
            }
        }
        return new ExteriorLights(
                requireField(lowBeam, "exterior_lights.low_beam_headlights_on"),
                requireField(highBeam, "exterior_lights.high_beam_headlights_on"),
                requireField(leftTurn, "exterior_lights.left_turn_signal_on"),
                requireField(rightTurn, "exterior_lights.right_turn_signal_on"),
                requireField(daytime, "exterior_lights.daytime_running_lights_on"),
                requireField(reverse, "exterior_lights.reverse_light_on"),
                requireField(fog, "exterior_lights.fog_light_on"),
                requireField(parking, "exterior_lights.parking_lights_on"));
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
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "path_position" -> delta = readDeltaReferencePosition(parser);
                case "path_delta_time" -> deltaTime = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new PathPoint(requireField(delta, "path_position"), deltaTime);
    }

    private DeltaReferencePosition readDeltaReferencePosition(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer lat = null;
        Integer lon = null;
        Integer alt = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
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

    /* --------------------------------------------------------------------- */
    /* Special vehicle container                                             */
    /* --------------------------------------------------------------------- */

    private SpecialVehicleContainer readSpecialVehicleContainer(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        SpecialVehiclePayload payload = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            payload = switch (field) {
                case "public_transport_container" -> readPublicTransport(parser);
                case "special_transport_container" -> readSpecialTransport(parser);
                case "dangerous_goods_container" -> readDangerousGoods(parser);
                case "road_works_container_basic" -> readRoadWorks(parser);
                case "rescue_container" -> readRescue(parser);
                case "emergency_container" -> readEmergency(parser);
                case "safety_car_container" -> readSafetyCar(parser);
                default -> {
                    parser.skipChildren();
                    yield payload;
                }
            };
        }
        return new SpecialVehicleContainer(requireField(payload, "special_vehicle_container payload"));
    }

    private PublicTransportContainer readPublicTransport(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Boolean embarkation = null;
        PtActivation activation = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("embarkation_status".equals(field)) {
                embarkation = parser.getBooleanValue();
            } else if ("pt_activation".equals(field)) {
                activation = readPtActivation(parser);
            } else {
                parser.skipChildren();
            }
        }
        return new PublicTransportContainer(
                requireField(embarkation, "public_transport_container.embarkation_status"),
                activation);
    }

    private PtActivation readPtActivation(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer type = null;
        String data = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("pt_activation_type".equals(field)) {
                type = parser.getIntValue();
            } else if ("pt_activation_data".equals(field)) {
                data = parser.getValueAsString();
            } else {
                parser.skipChildren();
            }
        }
        return new PtActivation(
                requireField(type, "pt_activation.pt_activation_type"),
                requireField(data, "pt_activation.pt_activation_data"));
    }

    private SpecialTransportContainer readSpecialTransport(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        SpecialTransportType type = null;
        LightBarSiren lightBar = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("special_transport_type".equals(field)) {
                type = readSpecialTransportType(parser);
            } else if ("light_bar_siren_in_use".equals(field)) {
                lightBar = readLightBar(parser);
            } else {
                parser.skipChildren();
            }
        }
        return new SpecialTransportContainer(
                requireField(type, "special_transport_container.special_transport_type"),
                requireField(lightBar, "special_transport_container.light_bar_siren_in_use"));
    }

    private SpecialTransportType readSpecialTransportType(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Boolean heavy = null, width = null, length = null, height = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "heavy_load" -> heavy = parser.getBooleanValue();
                case "excess_width" -> width = parser.getBooleanValue();
                case "excess_length" -> length = parser.getBooleanValue();
                case "excess_height" -> height = parser.getBooleanValue();
                default -> parser.skipChildren();
            }
        }
        return new SpecialTransportType(
                requireField(heavy, "special_transport_type.heavy_load"),
                requireField(width, "special_transport_type.excess_width"),
                requireField(length, "special_transport_type.excess_length"),
                requireField(height, "special_transport_type.excess_height"));
    }

    private DangerousGoodsContainer readDangerousGoods(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer goods = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("dangerous_goods_basic".equals(field)) {
                goods = parser.getIntValue();
            } else {
                parser.skipChildren();
            }
        }
        return new DangerousGoodsContainer(
                requireField(goods, "dangerous_goods_basic"));
    }

    private RoadWorksContainer readRoadWorks(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer subCause = null;
        LightBarSiren lightBar = null;
        ClosedLanes closedLanes = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "road_works_sub_cause_code" -> subCause = parser.getIntValue();
                case "light_bar_siren_in_use" -> lightBar = readLightBar(parser);
                case "closed_lanes" -> closedLanes = readClosedLanes(parser);
                default -> parser.skipChildren();
            }
        }
        return new RoadWorksContainer(subCause, lightBar, closedLanes);
    }

    private ClosedLanes readClosedLanes(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer inner = null;
        Integer outer = null;
        DrivingLaneStatus status = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("inner_hard_shoulder_status".equals(field)) {
                inner = parser.getIntValue();
            } else if ("outer_hard_shoulder_status".equals(field)) {
                outer = parser.getIntValue();
            } else if ("driving_lane_status".equals(field)) {
                status = readDrivingLaneStatus(parser);
            } else {
                parser.skipChildren();
            }
        }
        return new ClosedLanes(inner, outer, status);
    }

    private DrivingLaneStatus readDrivingLaneStatus(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Boolean[] lanes = new Boolean[13];

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if (field.startsWith("lane_") && field.endsWith("_closed")) {
                int index = Integer.parseInt(field.substring(5, field.length() - 7)) - 1;
                lanes[index] = parser.getBooleanValue();
            } else {
                parser.skipChildren();
            }
        }
        for (int i = 0; i < lanes.length; i++) {
            if (lanes[i] == null) {
                throw new CamValidationException("driving_lane_status is missing lane_" + (i + 1) + "_closed");
            }
        }
        return new DrivingLaneStatus(
                lanes[0], lanes[1], lanes[2], lanes[3], lanes[4], lanes[5], lanes[6],
                lanes[7], lanes[8], lanes[9], lanes[10], lanes[11], lanes[12]);
    }

    private RescueContainer readRescue(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        LightBarSiren lightBar = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if ("light_bar_siren_in_use".equals(parser.getCurrentName())) {
                parser.nextToken();
                lightBar = readLightBar(parser);
            } else {
                parser.skipChildren();
            }
        }
        return new RescueContainer(requireField(lightBar, "rescue_container.light_bar_siren_in_use"));
    }

    private EmergencyContainer readEmergency(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        LightBarSiren lightBar = null;
        IncidentIndication indication = null;
        EmergencyPriority priority = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "light_bar_siren_in_use" -> lightBar = readLightBar(parser);
                case "incident_indication" -> indication = readIncidentIndication(parser);
                case "emergency_priority" -> priority = readEmergencyPriority(parser);
                default -> parser.skipChildren();
            }
        }
        return new EmergencyContainer(lightBar, indication, priority);
    }

    private SafetyCarContainer readSafetyCar(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        LightBarSiren lightBar = null;
        IncidentIndication indication = null;
        Integer trafficRule = null;
        Integer speedLimit = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "light_bar_siren_in_use" -> lightBar = readLightBar(parser);
                case "incident_indication" -> indication = readIncidentIndication(parser);
                case "traffic_rule" -> trafficRule = parser.getIntValue();
                case "speed_limit" -> speedLimit = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new SafetyCarContainer(lightBar, indication, trafficRule, speedLimit);
    }

    private IncidentIndication readIncidentIndication(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        CauseCode causeCode = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if ("cc_and_scc".equals(parser.getCurrentName())) {
                parser.nextToken();
                causeCode = readCauseCode(parser);
            } else {
                parser.skipChildren();
            }
        }
        return new IncidentIndication(requireField(causeCode, "incident_indication.cc_and_scc"));
    }

    private CauseCode readCauseCode(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer cause = null;
        Integer subcause = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("cause".equals(field)) {
                cause = parser.getIntValue();
            } else if ("subcause".equals(field)) {
                subcause = parser.getIntValue();
            } else {
                parser.skipChildren();
            }
        }
        return new CauseCode(requireField(cause, "cause_code.cause"), subcause);
    }

    private EmergencyPriority readEmergencyPriority(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Boolean rightOfWay = null;
        Boolean freeCrossing = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("request_for_right_of_way".equals(field)) {
                rightOfWay = parser.getBooleanValue();
            } else if ("request_for_free_crossing_at_a_traffic_light".equals(field)) {
                freeCrossing = parser.getBooleanValue();
            } else {
                parser.skipChildren();
            }
        }
        return new EmergencyPriority(
                requireField(rightOfWay, "emergency_priority.request_for_right_of_way"),
                requireField(freeCrossing, "emergency_priority.request_for_free_crossing_at_a_traffic_light"));
    }

    private LightBarSiren readLightBar(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Boolean lightBar = null;
        Boolean siren = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("light_bar_activated".equals(field)) {
                lightBar = parser.getBooleanValue();
            } else if ("siren_activated".equals(field)) {
                siren = parser.getBooleanValue();
            } else {
                parser.skipChildren();
            }
        }
        return new LightBarSiren(
                requireField(lightBar, "light_bar_siren_in_use.light_bar_activated"),
                requireField(siren, "light_bar_siren_in_use.siren_activated"));
    }

    /* --------------------------------------------------------------------- */
    /* Shared primitive readers                                              */
    /* --------------------------------------------------------------------- */

    private Heading readHeading(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("value".equals(field)) {
                value = parser.getIntValue();
            } else if ("confidence".equals(field)) {
                confidence = parser.getIntValue();
            } else {
                parser.skipChildren();
            }
        }
        return new Heading(
                requireField(value, "heading.value"),
                requireField(confidence, "heading.confidence"));
    }

    private Speed readSpeed(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("value".equals(field)) {
                value = parser.getIntValue();
            } else if ("confidence".equals(field)) {
                confidence = parser.getIntValue();
            } else {
                parser.skipChildren();
            }
        }
        return new Speed(
                requireField(value, "speed.value"),
                requireField(confidence, "speed.confidence"));
    }

    private VehicleLength readVehicleLength(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("value".equals(field)) {
                value = parser.getIntValue();
            } else if ("confidence".equals(field)) {
                confidence = parser.getIntValue();
            } else {
                parser.skipChildren();
            }
        }
        return new VehicleLength(
                requireField(value, "vehicle_length.value"),
                requireField(confidence, "vehicle_length.confidence"));
    }

    private AccelerationComponent readAccelerationComponent(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("value".equals(field)) {
                value = parser.getIntValue();
            } else if ("confidence".equals(field)) {
                confidence = parser.getIntValue();
            } else {
                parser.skipChildren();
            }
        }
        return new AccelerationComponent(
                requireField(value, "acceleration_component.value"),
                requireField(confidence, "acceleration_component.confidence"));
    }

    private Curvature readCurvature(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("value".equals(field)) {
                value = parser.getIntValue();
            } else if ("confidence".equals(field)) {
                confidence = parser.getIntValue();
            } else {
                parser.skipChildren();
            }
        }
        return new Curvature(
                requireField(value, "curvature.value"),
                requireField(confidence, "curvature.confidence"));
    }

    private YawRate readYawRate(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("value".equals(field)) {
                value = parser.getIntValue();
            } else if ("confidence".equals(field)) {
                confidence = parser.getIntValue();
            } else {
                parser.skipChildren();
            }
        }
        return new YawRate(
                requireField(value, "yaw_rate.value"),
                requireField(confidence, "yaw_rate.confidence"));
    }

    private SteeringWheelAngle readSteeringWheelAngle(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer value = null;
        Integer confidence = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("value".equals(field)) {
                value = parser.getIntValue();
            } else if ("confidence".equals(field)) {
                confidence = parser.getIntValue();
            } else {
                parser.skipChildren();
            }
        }
        return new SteeringWheelAngle(
                requireField(value, "steering_wheel_angle.value"),
                requireField(confidence, "steering_wheel_angle.confidence"));
    }

    private AccelerationControl readAccelerationControl(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Boolean brake = null, gas = null, emergency = null, collision = null, acc = null, cruise = null, limiter = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "brake_pedal_engaged" -> brake = parser.getBooleanValue();
                case "gas_pedal_engaged" -> gas = parser.getBooleanValue();
                case "emergency_brake_engaged" -> emergency = parser.getBooleanValue();
                case "collision_warning_engaged" -> collision = parser.getBooleanValue();
                case "acc_engaged" -> acc = parser.getBooleanValue();
                case "cruise_control_engaged" -> cruise = parser.getBooleanValue();
                case "speed_limiter_engaged" -> limiter = parser.getBooleanValue();
                default -> parser.skipChildren();
            }
        }
        return new AccelerationControl(
                requireField(brake, "acceleration_control.brake_pedal_engaged"),
                requireField(gas, "acceleration_control.gas_pedal_engaged"),
                requireField(emergency, "acceleration_control.emergency_brake_engaged"),
                requireField(collision, "acceleration_control.collision_warning_engaged"),
                requireField(acc, "acceleration_control.acc_engaged"),
                requireField(cruise, "acceleration_control.cruise_control_engaged"),
                requireField(limiter, "acceleration_control.speed_limiter_engaged"));
    }

    private CenDsrcTollingZone readCenDsrcTollingZone(JsonParser parser) throws IOException {
        expect(parser.getCurrentToken(), JsonToken.START_OBJECT);
        Integer latitude = null;
        Integer longitude = null;
        Integer zoneId = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String field = parser.getCurrentName();
            parser.nextToken();
            if ("protected_zone_latitude".equals(field)) {
                latitude = parser.getIntValue();
            } else if ("protected_zone_longitude".equals(field)) {
                longitude = parser.getIntValue();
            } else if ("cen_dsrc_tolling_zone_id".equals(field)) {
                zoneId = parser.getIntValue();
            } else {
                parser.skipChildren();
            }
        }
        return new CenDsrcTollingZone(
                requireField(latitude, "cen_dsrc_tolling_zone.protected_zone_latitude"),
                requireField(longitude, "cen_dsrc_tolling_zone.protected_zone_longitude"),
                zoneId);
    }

    /* --------------------------------------------------------------------- */

    private static <T> T requireField(T value, String field) {
        if (value == null) {
            throw new CamValidationException("Missing mandatory field: " + field);
        }
        return value;
    }

    private static void expect(JsonToken actual, JsonToken expected) throws JsonParseException {
        if (actual != expected) {
            throw new JsonParseException(null, "Expected token " + expected + " but got " + actual);
        }
    }
}