/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.denm.v230.model.alacartecontainer;

/**
 * CarryingDangerousGoods - information about dangerous goods carried by a stationary vehicle.
 *
 * @param dangerousGoodsType   Optional. Type of dangerous goods (0..19).
 *                             explosives1(0)..miscellaneousDangerousSubstances(19).
 * @param unNumber             Optional. UN number of the dangerous goods. Range: 0..9999.
 * @param elevatedTemperature  Optional. Whether goods are transported at elevated temperature.
 * @param tunnelsRestricted    Optional. Whether transport is restricted in tunnels.
 * @param limitedQuantity      Optional. Whether goods are transported as a limited quantity.
 * @param emergencyActionCode  Optional. Emergency action code (1..4 characters).
 * @param phoneNumber          Optional. Emergency telephone number.
 * @param companyName          Optional. Company responsible for transport (1..24 characters).
 */
public record CarryingDangerousGoods(
        Integer dangerousGoodsType,
        Integer unNumber,
        Boolean elevatedTemperature,
        Boolean tunnelsRestricted,
        Boolean limitedQuantity,
        String emergencyActionCode,
        String phoneNumber,
        String companyName) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Integer dangerousGoodsType;
        private Integer unNumber;
        private Boolean elevatedTemperature;
        private Boolean tunnelsRestricted;
        private Boolean limitedQuantity;
        private String emergencyActionCode;
        private String phoneNumber;
        private String companyName;

        public Builder dangerousGoodsType(Integer dangerousGoodsType) {
            this.dangerousGoodsType = dangerousGoodsType;
            return this;
        }

        public Builder unNumber(Integer unNumber) {
            this.unNumber = unNumber;
            return this;
        }

        public Builder elevatedTemperature(Boolean elevatedTemperature) {
            this.elevatedTemperature = elevatedTemperature;
            return this;
        }

        public Builder tunnelsRestricted(Boolean tunnelsRestricted) {
            this.tunnelsRestricted = tunnelsRestricted;
            return this;
        }

        public Builder limitedQuantity(Boolean limitedQuantity) {
            this.limitedQuantity = limitedQuantity;
            return this;
        }

        public Builder emergencyActionCode(String emergencyActionCode) {
            this.emergencyActionCode = emergencyActionCode;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public CarryingDangerousGoods build() {
            return new CarryingDangerousGoods(
                    dangerousGoodsType,
                    unNumber,
                    elevatedTemperature,
                    tunnelsRestricted,
                    limitedQuantity,
                    emergencyActionCode,
                    phoneNumber,
                    companyName);
        }
    }
}

