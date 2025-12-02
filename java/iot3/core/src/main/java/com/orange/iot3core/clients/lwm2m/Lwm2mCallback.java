/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3core.clients.lwm2m;

public interface Lwm2mCallback {

    void onBootstrapStart();

    void onBootstrap(Throwable bootstrapFailure);

    void onRegistration(Throwable registrationFailure);

    void onUpdate(Throwable updateFailure);

    void onDeregistration(Throwable deregistrationFailure);

    void onUnexpectedError(Throwable error);

}
