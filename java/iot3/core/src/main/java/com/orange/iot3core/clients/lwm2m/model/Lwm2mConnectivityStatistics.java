/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
*/
package com.orange.iot3core.clients.lwm2m.model;

import io.reactivex.annotations.Nullable;

public abstract class Lwm2mConnectivityStatistics extends Lwm2mInstance {
    @Nullable // 0 [R] optional
    private Long smsTxCounter;
    @Nullable // 1 [R] optional
    private Long smsRxCounter;
    @Nullable // 2 [R] optional, in kilobytes
    private Long txData;
    @Nullable // 3 [R] optional, in kilobytes
    private Long rxData;
    @Nullable // 4 [R] optional, in bytes
    private Long maxMessageSize;
    @Nullable // 5 [R] optional, in bytes
    private Long avgMessageSize;
    @Nullable // 8 [RW] optional, in seconds
    private Long collectionPeriod;

    private static final int SMS_TX_COUNTER_RES_ID = 0;
    private static final int SMS_RX_COUNTER_RES_ID = 1;
    private static final int TX_DATA_RES_ID = 2;
    private static final int RX_DATA_RES_ID = 3;
    private static final int MAX_MESSAGE_SIZE_RES_ID = 4;
    private static final int AVG_MESSAGE_SIZE_RES_ID = 5;
    private static final int START_RES_ID = 6;
    private static final int STOP_RES_ID = 7;
    private static final int COLLECTION_PERIOD_RES_ID = 8;

    public Lwm2mConnectivityStatistics() {
        super(ObjectId.CONNECTIVITY_STATISTICS);
        // Initialize with default values
        this.smsTxCounter = null;
        this.smsRxCounter = null;
        this.txData = null;
        this.rxData = null;
        this.maxMessageSize = null;
        this.avgMessageSize = null;
        this.collectionPeriod = null;
    }

    @Override
    @Nullable
    public ResponseValue read(int resourceId) {
        return switch (resourceId) {
            case SMS_TX_COUNTER_RES_ID -> getResponseValue(smsTxCounter);
            case SMS_RX_COUNTER_RES_ID -> getResponseValue(smsRxCounter);
            case TX_DATA_RES_ID -> getResponseValue(txData);
            case RX_DATA_RES_ID -> getResponseValue(rxData);
            case MAX_MESSAGE_SIZE_RES_ID -> getResponseValue(maxMessageSize);
            case AVG_MESSAGE_SIZE_RES_ID -> getResponseValue(avgMessageSize);
            case COLLECTION_PERIOD_RES_ID -> getResponseValue(collectionPeriod);
            default -> null;
        };
    }

    @Override
    @Nullable
    protected ResponseValue write(
            int resourceId,
            @Nullable
            Object value
    ) {
        switch (resourceId) {
            case COLLECTION_PERIOD_RES_ID -> {
                this.collectionPeriod = (Long) value;
                onResourcesChange(COLLECTION_PERIOD_RES_ID);
            }
            default -> {
                return null;
            }
        }
        return new ResponseValue(ResponseType.SUCCESS);
    }

    @Override
    @Nullable
    protected ResponseValue execute(
            int resourceId,
            @Nullable
            String params
    ) {
        switch (resourceId) {
            case START_RES_ID -> actionStart();
            case STOP_RES_ID -> actionStop();
            default -> {
                return null;
            }
        }
        return new ResponseValue(ResponseType.SUCCESS);
    }

    public abstract void actionStart();

    public abstract void actionStop();

    @Nullable
    public Long getCollectionPeriod() {
        return collectionPeriod;
    }

    public void update(ConnectivityStatisticsUpdate update) {
        if (update.getSmsTxCounter() != null) {
            this.smsTxCounter = update.getSmsTxCounter();
            onResourcesChange(SMS_TX_COUNTER_RES_ID);
        }
        if (update.getSmsRxCounter() != null) {
            this.smsRxCounter = update.getSmsRxCounter();
            onResourcesChange(SMS_RX_COUNTER_RES_ID);
        }
        if (update.getTxData() != null) {
            this.txData = update.getTxData();
            onResourcesChange(TX_DATA_RES_ID);
        }
        if (update.getRxData() != null) {
            this.rxData = update.getRxData();
            onResourcesChange(RX_DATA_RES_ID);
        }
        if (update.getMaxMessageSize() != null) {
            this.maxMessageSize = update.getMaxMessageSize();
            onResourcesChange(MAX_MESSAGE_SIZE_RES_ID);
        }
        if (update.getAvgMessageSize() != null) {
            this.avgMessageSize = update.getAvgMessageSize();
            onResourcesChange(AVG_MESSAGE_SIZE_RES_ID);
        }
        if (update.getCollectionPeriod() != null) {
            this.collectionPeriod = update.getCollectionPeriod();
            onResourcesChange(COLLECTION_PERIOD_RES_ID);
        }
    }

}