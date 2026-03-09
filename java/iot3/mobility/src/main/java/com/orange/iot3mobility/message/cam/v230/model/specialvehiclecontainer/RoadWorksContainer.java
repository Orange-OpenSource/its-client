/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * RoadWorksContainer v2.3.0
 * <p>
 * If the vehicleRole component is set to roadWork(4) this container shall be present.
 *
 * @param roadWorksSubCauseCode Optional component, in case the originating ITS-S is mounted to a vehicle ITS-S
 *                              participating to roadwork. unavailable (0), majorRoadworks (1), roadMarkingWork (2),
 *                              slowMovingRoadMaintenance (3), shortTermStationaryRoadworks (4), streetCleaning (5),
 *                              winterService (6), setupPhase (7), remodellingPhase (8), dismantlingPhase (9)
 * @param lightBarSirenInUse {@link LightBarSiren}
 * @param closedLanes Optional {@link ClosedLanes}
 */
public record RoadWorksContainer(
        Integer roadWorksSubCauseCode,
        LightBarSiren lightBarSirenInUse,
        ClosedLanes closedLanes) implements SpecialVehiclePayload {}
