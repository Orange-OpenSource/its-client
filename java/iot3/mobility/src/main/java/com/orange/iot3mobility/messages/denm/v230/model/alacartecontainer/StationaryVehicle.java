/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer;

import com.orange.iot3mobility.messages.denm.v230.model.situationcontainer.CauseCode;

/**
 * StationaryVehicle - information about a stationary vehicle involved in an event.
 *
 * @param stationarySince         Optional. Duration since the vehicle is stationary.
 *                                lessThan1Minute(0), lessThan2Minutes(1), lessThan15Minutes(2),
 *                                equalOrGreater15Minutes(3).
 * @param stationaryCause         Optional. Cause code of the stationary event.
 * @param carryingDangerousGoods  Optional. Dangerous goods information.
 * @param numberOfOccupants       Optional. Number of occupants. 0..127, unavailable(127).
 * @param vehicleIdentification   Optional. Identification of the vehicle.
 * @param energyStorageType       Optional. Bit mask of energy storage types.
 *                                hydrogenStorage(0), electricEnergyStorage(1), liquidPropaneGas(2),
 *                                compressedNaturalGas(3), diesel(4), gasoline(5), ammonia(6).
 *                                Range: 0..127.
 */
public record StationaryVehicle(
        Integer stationarySince,
        CauseCode stationaryCause,
        CarryingDangerousGoods carryingDangerousGoods,
        Integer numberOfOccupants,
        VehicleIdentification vehicleIdentification,
        Integer energyStorageType) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer stationarySince;
        private CauseCode stationaryCause;
        private CarryingDangerousGoods carryingDangerousGoods;
        private Integer numberOfOccupants;
        private VehicleIdentification vehicleIdentification;
        private Integer energyStorageType;

        public Builder stationarySince(Integer stationarySince) {
            this.stationarySince = stationarySince;
            return this;
        }

        public Builder stationaryCause(CauseCode stationaryCause) {
            this.stationaryCause = stationaryCause;
            return this;
        }

        public Builder carryingDangerousGoods(CarryingDangerousGoods carryingDangerousGoods) {
            this.carryingDangerousGoods = carryingDangerousGoods;
            return this;
        }

        public Builder numberOfOccupants(Integer numberOfOccupants) {
            this.numberOfOccupants = numberOfOccupants;
            return this;
        }

        public Builder vehicleIdentification(VehicleIdentification vehicleIdentification) {
            this.vehicleIdentification = vehicleIdentification;
            return this;
        }

        public Builder energyStorageType(Integer energyStorageType) {
            this.energyStorageType = energyStorageType;
            return this;
        }

        public StationaryVehicle build() {
            return new StationaryVehicle(
                    stationarySince,
                    stationaryCause,
                    carryingDangerousGoods,
                    numberOfOccupants,
                    vehicleIdentification,
                    energyStorageType);
        }
    }
}

