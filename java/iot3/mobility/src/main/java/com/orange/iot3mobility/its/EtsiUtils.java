/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its;

import java.util.Calendar;

public class EtsiUtils {

    // previously 1072915200, but ETSI time is based on TAI instead of UTC,
    // and there was a 5s shift in 2004 between the two
    public static final int DELTA_1970_2004_SEC = 1072915195;
    public static final long DELTA_1970_2004_MILLISEC = 1072915195000l;
    public static final int ETSI_COORDINATES_FACTOR = 10000000;
    public static final int ETSI_ALTITUDE_FACTOR = 100;
    public static final int ETSI_SPEED_FACTOR = 100;
    public static final int ETSI_ACCELERATION_FACTOR = 10;
    public static final int ETSI_HEADING_FACTOR = 10;
    public static final int ETSI_YAW_RATE_FACTOR = 10;
    public static final int MAX_SEQUENCE_NUMBER = 65535;

    public static int localSequenceNumber = initLocalSequenceNumber();

    private static int initLocalSequenceNumber() {
        Calendar calendar = Calendar.getInstance();
        int minutes = calendar.get(Calendar.MINUTE);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int seconds = calendar.get(Calendar.SECOND);

        int sequenceNumber = hours*3600 + minutes*60 + seconds;

        if(sequenceNumber > MAX_SEQUENCE_NUMBER)
            sequenceNumber = sequenceNumber - MAX_SEQUENCE_NUMBER;

        return sequenceNumber;
    }

    public static int getNextSequenceNumber() {
        localSequenceNumber++;
        if(localSequenceNumber > MAX_SEQUENCE_NUMBER) localSequenceNumber = 0;
        return localSequenceNumber;
    }

    public static long etsiTimestampMsToUnix(long etsiTimestamp) {
        return etsiTimestamp + DELTA_1970_2004_MILLISEC;
    }

    public static long etsiTimestampSecToUnix(long etsiTimestamp) {
        return (etsiTimestamp + DELTA_1970_2004_SEC)*1000;
    }

    public static long unixTimestampToEtsiMs(long unixTimestamp) {
        return unixTimestamp - DELTA_1970_2004_MILLISEC;
    }

    public static long unixTimestampToEtsiSec(long unixTimestamp) {
        return (unixTimestamp - DELTA_1970_2004_MILLISEC)/1000;
    }

    public static long getLatitudeETSI(double latitude) {
        return (long)(latitude * ETSI_COORDINATES_FACTOR);
    }

    public static long getLongitudeETSI(double longitude) {
        return (long)(longitude * ETSI_COORDINATES_FACTOR);
    }

}
