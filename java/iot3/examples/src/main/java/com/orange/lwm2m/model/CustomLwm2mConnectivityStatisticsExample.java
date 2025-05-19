/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
*/
package com.orange.lwm2m.model;

import com.orange.iot3core.clients.lwm2m.model.ConnectivityStatisticsUpdate;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mConnectivityStatistics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * When reporting the Tx Data or Rx Data,
 * the LwM2M Client reports the total KB transmitted/received over IP bearer(s),
 * including all protocol header bytes up to and including the IP header.
 * This does not include lower level retransmissions/optimizations (e.g. RAN, header compression) or SMS messages.
 */
public class CustomLwm2mConnectivityStatisticsExample extends Lwm2mConnectivityStatistics {

    private ScheduledExecutorService scheduler = null;
    private ScheduledFuture<?> scheduledTask = null;
    private boolean isDataCollecting = false;
    private long txData = 0L;
    private long rxData = 0L;
    private long smsTxCounter = 0L;
    private long smsRxCounter = 0L;
    private long maxMessageSize = 0L;
    private long avgMessageSize = 0L;

    @Override
    public synchronized void actionStart() {
        actionStop();
        isDataCollecting = true;

        // reset resources 0-5
        reset();

        // Stop collecting information after specified period ended if determined
        Long collectionPeriod = getCollectionPeriod();
        if (collectionPeriod != null && collectionPeriod > 0) {
            if (isSchedulerShutdown()) {
                scheduler = Executors.newSingleThreadScheduledExecutor();
            }
            scheduledTask = scheduler.schedule(this::actionStop, collectionPeriod, TimeUnit.SECONDS);
        }
    }

    @Override
    public synchronized void actionStop() {
        if (scheduledTask != null && !scheduledTask.isDone()) {
            scheduledTask.cancel(true);
        }
        if (!isSchedulerShutdown()) {
            scheduler.shutdown();
        }
        isDataCollecting = false;
    }

    public synchronized void addTx(Long data, Boolean isSuccess) {
        if (!isDataCollecting) return;
        txData += data;
        if (isSuccess) {
            smsTxCounter++;
        }
    }

    public synchronized void addRx(Long data, Boolean isSuccess) {
        if (!isDataCollecting) return;
        rxData += data;
        if (isSuccess) {
            smsRxCounter++;
        }
    }

    public synchronized void update() {
        if (!isDataCollecting) return;
        updateNow();
    }

    private void updateNow() {
        ConnectivityStatisticsUpdate update = new ConnectivityStatisticsUpdate.Builder()
                .data(txData, rxData)
                .smsCounter(smsTxCounter, smsRxCounter)
                // TODO: .messageSize(maxMessageSize, avgMessageSize)
                .build();
        update(update);
    }

    private void reset() {
        txData = 0L;
        rxData = 0L;
        smsTxCounter = 0L;
        smsRxCounter = 0L;
        maxMessageSize = 0L;
        avgMessageSize = 0L;
        updateNow();
    }

    private boolean isSchedulerShutdown() {
        return scheduler == null || scheduler.isShutdown();
    }

}