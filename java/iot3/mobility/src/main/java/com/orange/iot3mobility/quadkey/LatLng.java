/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.quadkey;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public final class LatLng {

    public final double latitude;
    public final double longitude;

    public LatLng(double var1, double var3) {
        if (-180.0D <= var3 && var3 < 180.0D) {
            this.longitude = var3;
        } else {
            this.longitude = ((var3 - 180.0D) % 360.0D + 360.0D) % 360.0D - 180.0D;
        }

        this.latitude = Math.max(-90.0D, Math.min(90.0D, var1));
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public final int hashCode() {
        long var2 = Double.doubleToLongBits(this.latitude);
        int var1 = 31 + (int)(var2 ^ var2 >>> 32);
        var2 = Double.doubleToLongBits(this.longitude);
        return var1 * 31 + (int)(var2 ^ var2 >>> 32);
    }

    public final boolean equals(Object var1) {
        if (this == var1) {
            return true;
        } else if (!(var1 instanceof LatLng)) {
            return false;
        } else {
            LatLng var2 = (LatLng)var1;
            return Double.doubleToLongBits(this.latitude) == Double.doubleToLongBits(var2.latitude) && Double.doubleToLongBits(this.longitude) == Double.doubleToLongBits(var2.longitude);
        }
    }

    public final String toString() {
        double var1 = this.latitude;
        double var3 = this.longitude;
        return (new StringBuilder(60)).append("lat/lng: (").append(var1).append(",").append(var3).append(")").toString();
    }
}

