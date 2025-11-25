package com.orange;

import com.orange.iot3core.clients.lwm2m.Lwm2mCallback;
import com.orange.iot3core.clients.lwm2m.Lwm2mClient;
import com.orange.iot3core.clients.lwm2m.model.*;

import java.util.Arrays;

public class Lwm2mExample {

    private static final Lwm2mDevice EXAMPLE_LWM2M_DEVICE = new Lwm2mDevice(
            "orange",
            "123",
            "456",
            "U"
    );
    private static final Lwm2mServer EXAMPLE_LWM2M_SERVER = new Lwm2mServer(
            123456,
            5 * 60,
            "U"
    );
    private static final Lwm2mConfig EXAMPLE_LWM2M_BOOTSTRAP_CONFIG = new Lwm2mConfig.Lwm2mBootstrapConfig(
            "your_endpoint_name",
            "your_url",
            "your_psk_id",
            "your_private_key_in_hex",
            EXAMPLE_LWM2M_SERVER,
            true
    );

    public static void main(String[] args) {
        bootstrapExample();
    }

    private static void bootstrapExample() {
        Lwm2mInstance[] lwm2mInstances = new Lwm2mInstance[] {
                new Lwm2mIoT3Identity(),
                new Lwm2mIoT3ServiceEndpoint()
        };

        // instantiate LwM2M client
        Lwm2mClient lwm2mClient = new Lwm2mClient(
                EXAMPLE_LWM2M_BOOTSTRAP_CONFIG,
                EXAMPLE_LWM2M_DEVICE,
                lwm2mInstances,
                new Lwm2mCallback() {
                    @Override
                    public void onBootstrapStart() {
                        // allow to write values during the bootstrap sequence
                        for(Lwm2mInstance lwm2mInstance: lwm2mInstances) {
                            lwm2mInstance.enableBootstrapWrites();
                        }
                    }

                    @Override
                    public void onBootstrap(Throwable bootstrapFailure) {
                        // bootstrap sequence has ended, disable write permission
                        for(Lwm2mInstance lwm2mInstance: lwm2mInstances) {
                            lwm2mInstance.disableBootstrapWrites();
                        }
                        if(bootstrapFailure == null) {
                            System.out.println("LwM2M bootstrap success!");
                            for(Lwm2mInstance lwm2mInstance: lwm2mInstances) {
                                if(lwm2mInstance instanceof Lwm2mIoT3Identity identity) {
                                    IoT3Identity ioT3Identity = identity.toModel();
                                    System.out.println("IoT3Identity:"
                                            + "\nID: " + ioT3Identity.getIot3Id()
                                            + "\nPSK identity: " + ioT3Identity.getPskIdentity()
                                            + "\nPSK secret key: " + Arrays.toString(ioT3Identity.getPskSecretKey()));
                                }
                                if(lwm2mInstance instanceof Lwm2mIoT3ServiceEndpoint endpoint) {
                                    IoT3ServiceEndpoint ioT3ServiceEndpoint = endpoint.toModel();
                                    System.out.println("IoT3ServiceEndpoint:"
                                            + "\nService name: " + ioT3ServiceEndpoint.getServiceName()
                                            + "\nPayload: " + ioT3ServiceEndpoint.getPayload()
                                            + "\nURI: " + ioT3ServiceEndpoint.getServiceUri()
                                            + "\nTopic root: " + ioT3ServiceEndpoint.getTopicRoot()
                                            + "\nServer public key: " + Arrays.toString(ioT3ServiceEndpoint.getServerPublicKey()));
                                }
                            }
                        } else {
                            System.out.println("LwM2M bootstrap failed: " + bootstrapFailure);
                        }
                    }

                    @Override
                    public void onRegistration(Throwable registrationFailure) {
                        if(registrationFailure == null) {
                            System.out.println("LwM2M registration success!");
                        } else {
                            System.out.println("LwM2M registration failed: " + registrationFailure);
                        }
                    }

                    @Override
                    public void onUpdate(Throwable updateFailure) {
                        if(updateFailure == null) {
                            System.out.println("LwM2M update success!");
                        } else {
                            System.out.println("LwM2M update failed: " + updateFailure);
                        }
                    }

                    @Override
                    public void onDeregistration(Throwable deregistrationFailure) {
                        if(deregistrationFailure == null) {
                            System.out.println("LwM2M deregistration success!");
                        } else {
                            System.out.println("LwM2M deregistration failed: " + deregistrationFailure);
                        }
                    }

                    @Override
                    public void onUnexpectedError(Throwable error) {
                        System.out.println("LwM2M unexpected error: " + error);
                    }
                },
                true);
        lwm2mClient.connect();
    }

}