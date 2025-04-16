/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
*/
package com.orange.iot3core.clients.lwm2m.model;

import io.reactivex.annotations.Nullable;

public class ConnectivityStatisticsUpdate {
    private final Builder builder;

    private ConnectivityStatisticsUpdate(Builder builder) {
        this.builder = builder;
    }

    @Nullable
    public Long getSmsTxCounter() { return builder.smsTxCounter; }

    @Nullable
    public Long getSmsRxCounter() { return builder.smsRxCounter; }

    @Nullable
    public Long getTxData() { return builder.txData; }

    @Nullable
    public Long getRxData() { return builder.rxData; }

    @Nullable
    public Long getMaxMessageSize() { return builder.maxMessageSize; }

    @Nullable
    public Long getAvgMessageSize() { return builder.avgMessageSize; }

    @Nullable
    public Long getCollectionPeriod() { return builder.collectionPeriod; }

    public static class Builder {
        @Nullable
        private Long smsTxCounter;
        @Nullable
        private Long smsRxCounter;
        @Nullable
        private Long txData;
        @Nullable
        private Long rxData;
        @Nullable
        private Long maxMessageSize;
        @Nullable
        private Long avgMessageSize;
        @Nullable
        private Long collectionPeriod;

        public Builder() {}

        public Builder smsTxCounter(Long smsTxCounter) {
            this.smsTxCounter = smsTxCounter;
            return this;
        }

        public Builder smsRxCounter(Long smsRxCounter) {
            this.smsRxCounter = smsRxCounter;
            return this;
        }

        public Builder smsCounter(Long tx, Long rx) {
            return this
                    .smsTxCounter(tx)
                    .smsRxCounter(rx);
        }

        public Builder txData(Long txData) {
            this.txData = txData;
            return this;
        }

        public Builder rxData(Long rxData) {
            this.rxData = rxData;
            return this;
        }

        public Builder data(Long tx, Long rx) {
            return this
                    .txData(tx)
                    .rxData(rx);
        }

        public Builder maxMessageSize(Long maxMessageSize) {
            this.maxMessageSize = maxMessageSize;
            return this;
        }

        public Builder avgMessageSize(Long avgMessageSize) {
            this.avgMessageSize = avgMessageSize;
            return this;
        }

        public Builder messageSize(Long max, Long avg) {
            return this
                    .maxMessageSize(max)
                    .avgMessageSize(avg);
        }

        public Builder collectionPeriod(Long collectionPeriod) {
            this.collectionPeriod = collectionPeriod;
            return this;
        }

        public ConnectivityStatisticsUpdate build() {
            return new ConnectivityStatisticsUpdate(this);
        }

    }

}