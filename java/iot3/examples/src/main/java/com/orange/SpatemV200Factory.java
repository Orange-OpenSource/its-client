package com.orange;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemMessage200;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.*;

import java.util.List;

/**
 * Factory producing a minimal SPATEM v2.0.0 test message.
 * <p>
 * The generated message describes a fictitious intersection with two signal groups:
 * signal group 1 = protected-Movement-Allowed (green) and signal group 2 = stop-And-Remain (red).
 */
final class SpatemV200Factory {

    private static final int PROTOCOL_VERSION = 2;
    private static final long STATION_ID = 654321L;
    private static final int REGION_ID = 10;
    private static final int INTERSECTION_ID = 1001;
    private static final int REVISION = 1;

    private SpatemV200Factory() {
        // Factory class
    }

    /**
     * Build a minimal SPATEM v2.0.0 envelope.
     *
     * @param sourceUuid the source UUID of the emitting station
     * @return the constructed {@link SpatemEnvelope200}
     */
    static SpatemEnvelope200 createTestSpatemEnvelope(String sourceUuid) {
        // Signal group 1: protected green (6), min end time = 100 (10 seconds)
        MovementState sg1 = MovementState.builder()
                .signalGroup(1)
                .stateTimeSpeed(List.of(
                        MovementEvent.builder()
                                .eventState(6)
                                .timing(TimeChangeDetail.builder()
                                        .minEndTime(100)
                                        .build())
                                .build()))
                .movementName("signal-group-1")
                .build();

        // Signal group 2: red (3), min end time = 200 (20 seconds)
        MovementState sg2 = MovementState.builder()
                .signalGroup(2)
                .stateTimeSpeed(List.of(
                        MovementEvent.builder()
                                .eventState(3)
                                .timing(TimeChangeDetail.builder()
                                        .minEndTime(200)
                                        .build())
                                .build()))
                .movementName("signal-group-2")
                .build();

        IntersectionState intersection = IntersectionState.builder()
                .id(new IntersectionReferenceId(REGION_ID, INTERSECTION_ID))
                .revision(REVISION)
                .status(List.of())
                .states(List.of(sg1, sg2))
                .name("test-intersection")
                .build();

        return SpatemEnvelope200.builder()
                .origin("self")
                .sourceUuid(sourceUuid)
                .timestamp(TrueTime.getAccurateTime())
                .message(SpatemMessage200.builder()
                        .protocolVersion(PROTOCOL_VERSION)
                        .stationId(STATION_ID)
                        .intersections(List.of(intersection))
                        .build())
                .build();
    }
}
