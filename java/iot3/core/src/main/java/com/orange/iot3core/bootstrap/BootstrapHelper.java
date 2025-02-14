/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core.bootstrap;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * This class handles all the bootstrap-related logic.
 * <p>
 * Call the {@link #bootstrap(String, String, String, Role, String, BootstrapCallback)} method to start the bootstrap
 * sequence.
 */
public class BootstrapHelper {

    /**
     * Start the bootstrap sequence and set the callback to retrieve the necessary configuration parameters
     * for the {@link com.orange.iot3core.IoT3Core} components.
     *
     * @param id the identifier of your application or user equipment
     * @param login the bootstrap login
     * @param password the bootstrap password
     * @param role the role of your application or user equipment
     * @param bootstrapUri the bootstrap URI
     * @param bootstrapCallback the bootstrap callback to retrieve the necessary configuration parameters
     */
    public static void bootstrap(String id,
                                 String login,
                                 String password,
                                 Role role,
                                 String bootstrapUri,
                                 BootstrapCallback bootstrapCallback) {
        // Create the JSON request
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("ue_id", id);
        jsonRequest.put("psk_login", login);
        jsonRequest.put("psk_password", password);
        jsonRequest.put("role", role.getJsonValue());

        // Encode credentials in Base64 for Basic Authentication
        String credentials = Base64.getEncoder().encodeToString((login + ":" + password).getBytes(StandardCharsets.UTF_8));

        // Define the MediaType for JSON
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        // Create the request body with the JSON payload
        RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);

        try {
            // Build the HTTP request using OkHttp
            Request request = new Request.Builder()
                    .url(bootstrapUri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + credentials)
                    .post(body)
                    .build();

            // Create the OkHttpClient
            OkHttpClient client = new OkHttpClient();

            // Send the request and retrieve the response (synchronously)
            try (Response response = client.newCall(request).execute()) {
                int statusCode = response.code();
                String responseBody = response.body() != null ? response.body().string() : "";

                if (statusCode >= 400 && statusCode < 600) {
                    bootstrapCallback.boostrapError(new Throwable("Error: " + statusCode + " - " + responseBody));
                } else {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    BootstrapConfig bootstrapConfig = new BootstrapConfig(jsonResponse);
                    bootstrapCallback.boostrapSuccess(bootstrapConfig);
                }
            }
        } catch (IllegalArgumentException | IOException exception) {
            bootstrapCallback.boostrapError(exception);
        }
    }

    public enum Role {
        EXTERNAL_APP("external-app"),
        INTERNAL_APP("internal-app"),
        NEIGHBOUR("neighbour"),
        USER_EQUIPMENT("user-equipment");

        private final String jsonValue;

        Role(String jsonValue) {
            this.jsonValue = jsonValue;
        }

        private String getJsonValue() {
            return jsonValue;
        }
    }

}
