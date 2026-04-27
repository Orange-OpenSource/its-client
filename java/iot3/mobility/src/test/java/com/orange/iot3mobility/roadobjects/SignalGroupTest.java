/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.quadkey.LatLng;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignalGroupTest {

    // ─── SignalPhase mapping ────────────────────────────────────────────────────

    @Test
    void fromEtsiValueAllPhasesCorrect() {
        assertEquals(SignalPhase.UNAVAILABLE,                SignalPhase.fromEtsiValue(0));
        assertEquals(SignalPhase.DARK,                       SignalPhase.fromEtsiValue(1));
        assertEquals(SignalPhase.STOP_THEN_PROCEED,          SignalPhase.fromEtsiValue(2));
        assertEquals(SignalPhase.STOP_AND_REMAIN,            SignalPhase.fromEtsiValue(3));
        assertEquals(SignalPhase.PRE_MOVEMENT,               SignalPhase.fromEtsiValue(4));
        assertEquals(SignalPhase.PERMISSIVE_MOVEMENT_ALLOWED,SignalPhase.fromEtsiValue(5));
        assertEquals(SignalPhase.PROTECTED_MOVEMENT_ALLOWED, SignalPhase.fromEtsiValue(6));
        assertEquals(SignalPhase.PERMISSIVE_CLEARANCE,       SignalPhase.fromEtsiValue(7));
        assertEquals(SignalPhase.PROTECTED_CLEARANCE,        SignalPhase.fromEtsiValue(8));
        assertEquals(SignalPhase.CAUTION_CONFLICTING_TRAFFIC,SignalPhase.fromEtsiValue(9));
    }

    @Test
    void outOfRangeEventStateFallsBackToUnavailable() {
        assertEquals(SignalPhase.UNAVAILABLE, SignalPhase.fromEtsiValue(99));
        assertEquals(SignalPhase.UNAVAILABLE, SignalPhase.fromEtsiValue(-1));
    }

    @Test
    void colorAndBlinkingFlagsForRepresentativePhases() {
        // RED / non-blinking
        assertEquals(SignalColor.RED,     SignalPhase.STOP_AND_REMAIN.color());
        assertFalse(SignalPhase.STOP_AND_REMAIN.isBlinking());

        // RED / blinking
        assertEquals(SignalColor.RED,     SignalPhase.STOP_THEN_PROCEED.color());
        assertTrue(SignalPhase.STOP_THEN_PROCEED.isBlinking());

        // GREEN / non-blinking
        assertEquals(SignalColor.GREEN,   SignalPhase.PROTECTED_MOVEMENT_ALLOWED.color());
        assertFalse(SignalPhase.PROTECTED_MOVEMENT_ALLOWED.isBlinking());

        // YELLOW / blinking
        assertEquals(SignalColor.YELLOW,  SignalPhase.CAUTION_CONFLICTING_TRAFFIC.color());
        assertTrue(SignalPhase.CAUTION_CONFLICTING_TRAFFIC.isBlinking());

        // UNKNOWN
        assertEquals(SignalColor.UNKNOWN, SignalPhase.UNAVAILABLE.color());
    }

    // ─── SignalGroup accessors ──────────────────────────────────────────────────

    @Test
    void accessorsReturnConstructorValues() {
        SignalGroup signalGroup = new SignalGroup(3, 6, 100, 200);

        assertEquals(3, signalGroup.getId());
        assertEquals(SignalPhase.PROTECTED_MOVEMENT_ALLOWED, signalGroup.getPhase());
        assertEquals(SignalColor.GREEN, signalGroup.getColor());
        assertFalse(signalGroup.isBlinking());
        assertEquals(6, signalGroup.getEventState());
        assertEquals(100, signalGroup.getMinEndTime());
        assertEquals(200, signalGroup.getMaxEndTime());
    }

    @Test
    void positionIsNullUntilSet() {
        SignalGroup signalGroup = new SignalGroup(1, 6, null, null);
        assertNull(signalGroup.getPosition());
        assertTrue(signalGroup.getLaneLevelPositions().isEmpty());

        LatLng stopLine = new LatLng(48.8566, 2.3522);
        signalGroup.addLanePosition(10, stopLine);

        assertNotNull(signalGroup.getPosition());
        assertEquals(48.8566, signalGroup.getPosition().getLatitude(), 1e-9);
        assertEquals(2.3522,  signalGroup.getPosition().getLongitude(), 1e-9);
    }

    @Test
    void getLaneLevelPositionsContainsAllAddedLanes() {
        SignalGroup signalGroup = new SignalGroup(1, 6, null, null);
        signalGroup.addLanePosition(10, new LatLng(48.1, 2.1));
        signalGroup.addLanePosition(11, new LatLng(48.2, 2.2));

        Map<Integer, LatLng> positions = signalGroup.getLaneLevelPositions();
        assertEquals(2, positions.size());
        assertEquals(48.1, positions.get(10).getLatitude(), 1e-9);
        assertEquals(48.2, positions.get(11).getLatitude(), 1e-9);
    }

    @Test
    void getLaneLevelPositionsIsUnmodifiable() {
        SignalGroup signalGroup = new SignalGroup(1, 6, null, null);
        signalGroup.addLanePosition(10, new LatLng(48.1, 2.1));

        assertThrows(UnsupportedOperationException.class,
                () -> signalGroup.getLaneLevelPositions().put(99, new LatLng(0, 0)));
    }

    @Test
    void getPositionReturnsFirstLaneEntry() {
        SignalGroup signalGroup = new SignalGroup(1, 6, null, null);
        LatLng firstPosition = new LatLng(48.1, 2.1);
        signalGroup.addLanePosition(10, firstPosition);
        signalGroup.addLanePosition(11, new LatLng(48.2, 2.2));

        // getPosition() must return the first-added entry
        assertEquals(firstPosition, signalGroup.getPosition());
    }

    @Test
    void updateChangesPhaseAndTiming() {
        SignalGroup signalGroup = new SignalGroup(1, 6, 100, 200);
        assertEquals(SignalPhase.PROTECTED_MOVEMENT_ALLOWED, signalGroup.getPhase());

        signalGroup.update(3, 50, 75);

        assertEquals(SignalPhase.STOP_AND_REMAIN, signalGroup.getPhase());
        assertEquals(SignalColor.RED, signalGroup.getColor());
        assertEquals(50, signalGroup.getMinEndTime());
        assertEquals(75, signalGroup.getMaxEndTime());
    }

    @Test
    void updateWithNullTimingClearsEndTimes() {
        SignalGroup signalGroup = new SignalGroup(1, 6, 100, 200);
        signalGroup.update(3, null, null);

        assertNull(signalGroup.getMinEndTime());
        assertNull(signalGroup.getMaxEndTime());
    }

    // ─── expiry ────────────────────────────────────────────────────────────────

    @Test
    void isExpiredFalseWhenNoTimingProvided() {
        // phaseExpiryMs = Long.MAX_VALUE when both minEndTime and maxEndTime are null
        SignalGroup signalGroup = new SignalGroup(1, 6, null, null);
        assertFalse(signalGroup.isExpired());
    }

    @Test
    void isExpiredFalseWhenTimingIsInFuture() {
        // maxEndTime = 36001 (unknown) → Long.MAX_VALUE fallback
        SignalGroup signalGroup = new SignalGroup(1, 6, 36001, null);
        assertFalse(signalGroup.isExpired());
    }

    @Test
    void isExpiredTrueAfterMarkExpired() {
        SignalGroup signalGroup = new SignalGroup(1, 6, null, null);
        assertFalse(signalGroup.isExpired());

        signalGroup.markExpired();

        assertTrue(signalGroup.isExpired());
        assertEquals(SignalPhase.UNAVAILABLE, signalGroup.getPhase());
        assertEquals(SignalColor.UNKNOWN, signalGroup.getColor());
        assertNull(signalGroup.getMinEndTime());
        assertNull(signalGroup.getMaxEndTime());
    }

    @Test
    void updateResetsExpiredFlag() {
        SignalGroup signalGroup = new SignalGroup(1, 6, null, null);
        signalGroup.markExpired();
        assertTrue(signalGroup.isExpired());

        // A new SPATEM update should reset the expired flag
        signalGroup.update(6, null, null);

        assertFalse(signalGroup.isExpired());
        assertEquals(SignalPhase.PROTECTED_MOVEMENT_ALLOWED, signalGroup.getPhase());
    }
}

