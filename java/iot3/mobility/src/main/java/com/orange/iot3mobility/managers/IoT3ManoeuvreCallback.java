/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.mcm.core.McmCodec;
import com.orange.iot3mobility.roadobjects.ManoeuvreSession;

/**
 * Callback interface for {@link ManoeuvreSession} lifecycle events.
 * <p>
 * Register an implementation via
 * {@link com.orange.iot3mobility.IoT3Mobility#setManoeuvreCallback(IoT3ManoeuvreCallback)}.
 */
public interface IoT3ManoeuvreCallback {

    /**
     * Invoked for every valid MCM envelope received in the subscribed RoI, before any
     * road-object processing. Use this for raw MCM inspection.
     *
     * @param mcmFrame the decoded MCM frame
     */
    void mcmArrived(McmCodec.McmFrame<?> mcmFrame);

    /**
     * Invoked when a {@link ManoeuvreSession} is seen for the first time
     * (i.e. no existing session with the same key was known).
     *
     * @param manoeuvreSession the newly created session
     */
    void newManoeuvreSession(ManoeuvreSession manoeuvreSession);

    /**
     * Invoked when a known {@link ManoeuvreSession} receives an update
     * (position, phase, trajectory or advice changed).
     *
     * @param manoeuvreSession the updated session
     */
    void manoeuvreSessionUpdated(ManoeuvreSession manoeuvreSession);

    /**
     * Invoked when a {@link ManoeuvreSession} is removed, either because the rolling
     * timeout elapsed without a new MCM, or because a
     * {@link com.orange.iot3mobility.roadobjects.ManoeuvrePhase#causesImmediateExpiry()
     * terminating phase} (TERMINATION or CANCELLATION) was received.
     *
     * @param manoeuvreSession the expired session (last known state)
     */
    void manoeuvreSessionExpired(ManoeuvreSession manoeuvreSession);
}


