/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtil {

    public static final int UNKNOWN = Integer.MIN_VALUE;

    public static boolean isNullOrEmpty(JSONObject jsonObject) {
        // use the zero length test instead of the isEmpty() method
        // to make the SDK compatible with Android apps
        return jsonObject == null || jsonObject.length() == 0;
    }

    public static boolean isNullOrEmpty(JSONArray jsonArray) {
        // use the zero length test instead of the isEmpty() method
        // to make the SDK compatible with Android apps
        return jsonArray == null || jsonArray.length() == 0;
    }

}
