/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility;

import com.orange.iot3mobility.its.EtsiUtils;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by rjgd9993 on 26/01/17.
 */

public class TrueTime {
    
    private final static String LOG_TAG = "TrueTime";

    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(20);
    
    private static final ArrayList<String> ntpServerList = new ArrayList<String>();

    //NTP
    public static String defaultTimeServerAddress = "0.pool.ntp.org";
    private static String timeServerAddress = defaultTimeServerAddress;
    private static long deltaNtpTime = 0;
    private static long lastTimeCheck = System.currentTimeMillis();
    private static ArrayList<Long> deltaNtpTimesList = new ArrayList<Long>();

    private static boolean trueTimeAvailable = false;
    
    public static void initTrueTime() {
        EXECUTOR.remove(TrueTime::getTime);
        EXECUTOR.purge();
		EXECUTOR.scheduleWithFixedDelay(TrueTime::getTime, 0, 10, TimeUnit.SECONDS);
	}

    public static boolean isTrueTimeAvailable() {
        if(System.currentTimeMillis() - lastTimeCheck > 20000) trueTimeAvailable = false;
        return trueTimeAvailable;
    }

    public static long getAccurateTime(){
        return System.currentTimeMillis() - deltaNtpTime;
    }

    public static long getAccurateTimeSec(){
        return getAccurateTime() / 1000;
    }

    public static long getAccurateETSITime(){
        return getAccurateTime() - EtsiUtils.DELTA_1970_2004_MILLISEC;
    }

    public static long getAccurateETSITimeSec(){
        return (getAccurateTime() / 1000) - EtsiUtils.DELTA_1970_2004_SEC;
    }

    public static void setDefaultTimeServerAddress(String address){
        defaultTimeServerAddress = address;
        setTimeServerAddress(address);
    }

    public static void setTimeServerAddress(String address){
        timeServerAddress = address;
    }

    private static void getTime() {
        try {
            NTPUDPClient timeClient = new NTPUDPClient();
            timeClient.open();
            timeClient.setSoTimeout(5000);
            InetAddress inetAddress = InetAddress.getByName(timeServerAddress);
            TimeInfo timeInfo = timeClient.getTime(inetAddress);
            long localSendingTime = timeInfo.getMessage().getOriginateTimeStamp().getTime();    //local device time
            long localReceivingTime = timeInfo.getReturnTime();   //local device time

            long refLocalTime = localSendingTime + (localReceivingTime - localSendingTime)/2;
            long ntpTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();   //server time
            long deltaTime = refLocalTime - ntpTime;
            setDeltaNtpTime(deltaTime);

            trueTimeAvailable = true;
            lastTimeCheck = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setDeltaNtpTime(long deltaTime) {
        deltaNtpTimesList.add(deltaTime);
        if(deltaNtpTimesList.size() > 10) deltaNtpTimesList.remove(0);
        long sum = 0;
        for(Long deltaT: deltaNtpTimesList) {
            sum = sum + deltaT;
        }
        deltaNtpTime = sum / deltaNtpTimesList.size();
    }
    
    public static void setNtpServers(JSONArray ntpServers) {
        if(ntpServers != null) {
            try {
                ntpServerList.clear();
                for(int i = 0; i < ntpServers.length(); i++) {
                    ntpServerList.add(ntpServers.getString(i));
                }
                if(!ntpServerList.isEmpty()) setDefaultTimeServerAddress(ntpServerList.get(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
