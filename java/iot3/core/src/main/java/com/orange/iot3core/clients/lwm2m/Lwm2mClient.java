/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Mathieu LEFEBVRE   <mathieu1.lefebvre@orange.com>
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
    Maciej Ćmiel       <maciej.cmiel@orange.com>
 */
package com.orange.iot3core.clients.lwm2m;

import com.orange.iot3core.clients.lwm2m.model.Lwm2mConfig;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mDevice;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mInstance;
import io.reactivex.annotations.Nullable;
import org.eclipse.leshan.client.LeshanClient;
import org.eclipse.leshan.client.LeshanClientBuilder;
import org.eclipse.leshan.client.californium.endpoint.CaliforniumClientEndpointsProvider;
import org.eclipse.leshan.client.californium.endpoint.coap.CoapClientProtocolProvider;
import org.eclipse.leshan.client.californium.endpoint.coaps.CoapsClientProtocolProvider;
import org.eclipse.leshan.client.engine.DefaultRegistrationEngineFactory;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.BaseInstanceEnablerFactory;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.client.resource.listener.ObjectsListenerAdapter;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.model.*;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.util.Hex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Lwm2mClient {

    private static final Logger logger = Logger.getLogger(Lwm2mClient.class.getName());
    private final LeshanClient client;
    private final Lwm2mConfig lwm2mConfig;

    public Lwm2mClient(
            Lwm2mConfig lwm2mConfig,
            Lwm2mDevice lwm2mDevice,
            Lwm2mInstance[] lwm2mInstances,
            boolean autoConnect
    ) {
        this.lwm2mConfig = lwm2mConfig;

        // create objects
        ObjectsInitializer initializer = getObjectsInitializer(
                lwm2mConfig,
                lwm2mDevice,
                lwm2mInstances
        );

        LeshanClientBuilder builder = new LeshanClientBuilder(lwm2mConfig.getEndpointName());
        builder.setObjects(initializer.createAll());

        CaliforniumClientEndpointsProvider.Builder endpointsBuilder =
                new CaliforniumClientEndpointsProvider.Builder(
                        new CoapClientProtocolProvider(),
                        new CoapsClientProtocolProvider()
                );
        builder.setEndpointsProviders(endpointsBuilder.build());

        DefaultRegistrationEngineFactory engineFactory = new DefaultRegistrationEngineFactory();
        engineFactory.setQueueMode(lwm2mConfig.isQueueMode());
        builder.setRegistrationEngineFactory(engineFactory);

        client = builder.build();

        setupObservationLogging();

        if (autoConnect) connect();
    }

    public Lwm2mClient(
            Lwm2mConfig lwm2mConfig,
            Lwm2mDevice lwm2mDevice,
            Lwm2mInstance[] lwm2mInstances
    ) {
        this(lwm2mConfig, lwm2mDevice, lwm2mInstances, true);
    }

    public void close() {
        close(true);
    }

    /**
     * Stops the client. This can either be a graceful shutdown with de-registration or an immediate stop without notifying the server.
     * The client can be restarted later using {@link #connect()}.
     *
     * @param deregister If {true}, the client sends a DEREGISTER request to the LwM2M server before stopping,
     *                   informing the server that it is intentionally disconnecting.
     */
    public void close(boolean deregister) {
        client.stop(deregister);
    }

    public void connect() {
        logger.info("LwM2M connecting with: " + lwm2mConfig.getUri());
        client.start();
    }

    @NotNull
    private ObjectsInitializer getObjectsInitializer(
            Lwm2mConfig lwm2mConfig,
            Lwm2mDevice lwm2mDevice,
            Lwm2mInstance[] lwm2mInstances
    ) {
        // create our custom IoT3 objects
        ObjectModel obj36050 = new ObjectModel(
                36050, "IoT3 Identity", "urn:oma:lwm2m:x:36050", "1.0",
                false,  // multiple
                false,  // mandatory
                new ResourceModel(0, "IoT3 ID", ResourceModel.Operations.R, false, true, ResourceModel.Type.STRING, null, null, null),
                new ResourceModel(1, "PSK Identity", ResourceModel.Operations.R, false, true, ResourceModel.Type.STRING, null, null, null),
                new ResourceModel(2, "PSK Secret", ResourceModel.Operations.R, false, true, ResourceModel.Type.OPAQUE, null, null, null)
        );

        ObjectModel obj36051 = new ObjectModel(
                36051, "IoT3 Service Endpoint", "urn:oma:lwm2m:x:36051", "1.0",
                true,   // multiple
                false,  // mandatory
                new ResourceModel(0, "Service Name", ResourceModel.Operations.R, false, true, ResourceModel.Type.STRING, null, null, null),
                new ResourceModel(1, "Payload", ResourceModel.Operations.R, false, false, ResourceModel.Type.STRING, null, null, null),
                new ResourceModel(2, "Service URI", ResourceModel.Operations.R, false, true, ResourceModel.Type.STRING, null, null, null),
                new ResourceModel(3, "Topic Root", ResourceModel.Operations.R, false, false, ResourceModel.Type.STRING, null, null, null),
                new ResourceModel(4, "Server Public Key", ResourceModel.Operations.R, false, false, ResourceModel.Type.OPAQUE, null, null, null)
        );

        // load standard models and add our custom IoT3 models
        List<ObjectModel> standardModels = ObjectLoader.loadDefault();
        List<ObjectModel> allModels = new ArrayList<>(standardModels);
        allModels.add(obj36050);
        allModels.add(obj36051);

        LwM2mModel model = new StaticModel(allModels);

        ObjectsInitializer initializer = new ObjectsInitializer(model);

        Security security = getSecurity(lwm2mConfig);
        if (security != null) {
            initializer.setInstancesForObject(LwM2mId.SECURITY, security);
        }
        initializer.setInstancesForObject(LwM2mId.SECURITY, getSecurity(lwm2mConfig));
        initializer.setInstancesForObject(LwM2mId.SERVER, getServer(lwm2mConfig));
        initializer.setInstancesForObject(LwM2mId.DEVICE, lwm2mDevice.getDevice());
        if (lwm2mInstances != null) {
            for (Lwm2mInstance lwm2mInstance : lwm2mInstances) {
                BaseInstanceEnablerFactory factory = lwm2mInstance.getInstanceEnablerFactory();
                initializer.setInstancesForObject(lwm2mInstance.getObjectId(), factory.create());
                initializer.setFactoryForObject(lwm2mInstance.getObjectId(), factory);
            }
        }
        return initializer;
    }

    private void setupObservationLogging() {
        client.getObjectTree().addListener(
                new ObjectsListenerAdapter() {
                    @Override
                    public void resourceChanged(LwM2mPath... paths) {
                        super.resourceChanged(paths);
                        for (LwM2mPath path : paths) {
                            logger.fine("Resource changed: " + path.getObjectId() + "/" + path.getObjectInstanceId() + "/" + path.getResourceId());
                        }
                    }
                }
        );
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
                lwm2mConfig.getServerConfig().getBindingModes(),
                lwm2mConfig.getServerConfig().isNotifyWhenDisable(),
                lwm2mConfig.getServerConfig().getPreferredTransport()
        );
    }

}
