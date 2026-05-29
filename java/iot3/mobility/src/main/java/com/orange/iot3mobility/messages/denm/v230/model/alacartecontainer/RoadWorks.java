/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer;

import com.orange.iot3mobility.messages.denm.v230.model.defs.DeltaReferencePosition;
import com.orange.iot3mobility.messages.denm.v230.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.CauseCode;

import java.util.List;

/**
 * RoadWorks - container for road works information.
 * Corresponds to RoadWorksContainerExtended in ETSI EN 302 637-3.
 *
 * @param lightBarSirenInUse    Optional. Bit mask: lightBarActivated(0), sirenActivated(1). Range: 0..3.
 * @param closedLanes           Optional. Lanes closed due to road works.
 * @param restriction           Optional. List of restricted vehicle station types (1..3 items, each 0..255).
 * @param speedLimit            Optional. Speed limit in the road works area. Unit: km/h. Range: 0..255.
 * @param incidentIndication    Optional. Cause code of the incident related to road works.
 * @param recommendedPath       Optional. Recommended itinerary expressed as delta positions.
 * @param startingPointSpeedLimit Optional. Position from which the speed limit applies.
 * @param trafficFlowRule       Optional. noPassing(0), noPassingForTrucks(1), passToRight(2), passToLeft(3).
 * @param referenceDenms        Optional. Action IDs of related DENM events.
 */
public record RoadWorks(
        Integer lightBarSirenInUse,
        ClosedLanes closedLanes,
        List<Integer> restriction,
        Integer speedLimit,
        CauseCode incidentIndication,
        List<DeltaReferencePosition> recommendedPath,
        DeltaReferencePosition startingPointSpeedLimit,
        Integer trafficFlowRule,
        List<ActionId> referenceDenms) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer lightBarSirenInUse;
        private ClosedLanes closedLanes;
        private List<Integer> restriction;
        private Integer speedLimit;
        private CauseCode incidentIndication;
        private List<DeltaReferencePosition> recommendedPath;
        private DeltaReferencePosition startingPointSpeedLimit;
        private Integer trafficFlowRule;
        private List<ActionId> referenceDenms;

        public Builder lightBarSirenInUse(Integer lightBarSirenInUse) {
            this.lightBarSirenInUse = lightBarSirenInUse;
            return this;
        }

        public Builder closedLanes(ClosedLanes closedLanes) {
            this.closedLanes = closedLanes;
            return this;
        }

        public Builder restriction(List<Integer> restriction) {
            this.restriction = restriction;
            return this;
        }

        public Builder speedLimit(Integer speedLimit) {
            this.speedLimit = speedLimit;
            return this;
        }

        public Builder incidentIndication(CauseCode incidentIndication) {
            this.incidentIndication = incidentIndication;
            return this;
        }

        public Builder recommendedPath(List<DeltaReferencePosition> recommendedPath) {
            this.recommendedPath = recommendedPath;
            return this;
        }

        public Builder startingPointSpeedLimit(DeltaReferencePosition startingPointSpeedLimit) {
            this.startingPointSpeedLimit = startingPointSpeedLimit;
            return this;
        }

        public Builder trafficFlowRule(Integer trafficFlowRule) {
            this.trafficFlowRule = trafficFlowRule;
            return this;
        }

        public Builder referenceDenms(List<ActionId> referenceDenms) {
            this.referenceDenms = referenceDenms;
            return this;
        }

        public RoadWorks build() {
            return new RoadWorks(
                    lightBarSirenInUse,
                    closedLanes,
                    restriction,
                    speedLimit,
                    incidentIndication,
                    recommendedPath,
                    startingPointSpeedLimit,
                    trafficFlowRule,
                    referenceDenms);
        }
    }
}

