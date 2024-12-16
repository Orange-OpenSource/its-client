/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Mathieu LEFEBVRE   <mathieu1.lefebvre@orange.com>
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
    Maciej Ćmiel       <maciej.cmiel@orange.com>
 */
package com.orange.iot3core.clients.lwm2m;

import com.orange.iot3core.clients.lwm2m.model.Lwm2mConfig;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mDevice;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.util.Hex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Lwm2mClient {

    private final LeshanClient client;
    private final Lwm2mConfig lwm2mConfig;

    public Lwm2mClient(
            Lwm2mConfig lwm2mConfig,
            Lwm2mDevice lwm2mDevice,
            boolean autoConnect
    ) {
        this.lwm2mConfig = lwm2mConfig;

        // create objects
        ObjectsInitializer initializer = getObjectsInitializer(
                lwm2mConfig,
                lwm2mDevice
        );

        LeshanClientBuilder builder = new LeshanClientBuilder(lwm2mConfig.getEndpointName());
        builder.setObjects(initializer.createAll());

        client = builder.build();

        if (autoConnect) connect();
    }

    public Lwm2mClient(
            Lwm2mConfig lwm2mConfig,
            Lwm2mDevice lwm2mDevice
    ) {
        this(lwm2mConfig, lwm2mDevice, true);
    }

    public void disconnect() {
        disconnect(true);
    }

    /**
     * Stops the client. This can either be a graceful shutdown with de-registration or an immediate stop without notifying the server.
     * The client can be restarted later using {@link #connect()}.
     *
     * @param deregister If {true}, the client sends a DEREGISTER request to the LwM2M server before stopping,
     *         informing the server that it is intentionally disconnecting.
     */
    public void disconnect(boolean deregister) {
        client.stop(deregister);
    }

    public void connect() {
        System.out.println("LwM2M connecting with: " + lwm2mConfig.getUri());
        client.start();
    }

    @NotNull
    private ObjectsInitializer getObjectsInitializer(
            Lwm2mConfig lwm2mConfig,
            Lwm2mDevice lwm2mDevice
    ) {
        ObjectsInitializer initializer = new ObjectsInitializer();

        Security security = getSecurity(lwm2mConfig);
        if (security != null) {
            initializer.setInstancesForObject(LwM2mId.SECURITY, security);
        }
        initializer.setInstancesForObject(LwM2mId.SECURITY, getSecurity(lwm2mConfig));
        initializer.setInstancesForObject(LwM2mId.SERVER, getServer(lwm2mConfig));
        initializer.setInstancesForObject(LwM2mId.DEVICE, lwm2mDevice.getDevice());

        return initializer;
    }

    @Nullable
    private Security getSecurity(Lwm2mConfig lwm2mConfig) {
        if (lwm2mConfig instanceof Lwm2mConfig.Lwm2mBootstrapConfig lwm2mBootstrapConfig) {
            return Security.pskBootstrap(
                    lwm2mBootstrapConfig.getUri(),
                    lwm2mBootstrapConfig.getPskIdentity().getBytes(),
                    Hex.decodeHex(lwm2mBootstrapConfig.getPrivateKey().toCharArray())
            );
        } else if (lwm2mConfig instanceof Lwm2mConfig.Lwm2mClassicConfig lwm2mClassicConfig) {
            return Security.psk(
                    lwm2mClassicConfig.getUri(),
                    lwm2mClassicConfig.getShortServerId(),
                    lwm2mClassicConfig.getPskIdentity().getBytes(),
                    Hex.decodeHex(lwm2mClassicConfig.getPrivateKey().toCharArray())
            );
        } else {
            return null;
        }
    }

    @NotNull
    private static Server getServer(Lwm2mConfig lwm2mConfig) {
        return new Server(
                lwm2mConfig.getServerConfig().getShortServerId(),
                lwm2mConfig.getServerConfig().getLifetime(),
                BindingMode.valueOf(lwm2mConfig.getServerConfig().getBindingMode()),
                lwm2mConfig.getServerConfig().isNotifyWhenDisable()
        );
    }

}
