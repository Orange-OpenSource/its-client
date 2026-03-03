package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * EmergencyContainer v2.3.0
 * <p>
 * If the vehicleRole component is set to emergency(6) this container shall be present.
 *
 * @param lightBarSirenInUse {@link LightBarSiren}
 * @param incidentIndication Optional {@link IncidentIndication}
 * @param emergencyPriority Optional {@link EmergencyPriority}
 */
public record EmergencyContainer(
        LightBarSiren lightBarSirenInUse,
        IncidentIndication incidentIndication,
        EmergencyPriority emergencyPriority) implements SpecialVehiclePayload {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for EmergencyContainer
     * <p>
     * Mandatory field: lightBarSirenInUse
     */
    public static final class Builder {
        private LightBarSiren lightBarSirenInUse;
        private IncidentIndication incidentIndication;
        private EmergencyPriority emergencyPriority;

        private Builder() {}

        public Builder lightBarSirenInUse(LightBarSiren lightBarSirenInUse) {
            this.lightBarSirenInUse = lightBarSirenInUse;
            return this;
        }

        public Builder incidentIndication(IncidentIndication incidentIndication) {
            this.incidentIndication = incidentIndication;
            return this;
        }

        public Builder emergencyPriority(EmergencyPriority emergencyPriority) {
            this.emergencyPriority = emergencyPriority;
            return this;
        }

        public EmergencyContainer build() {
            return new EmergencyContainer(
                    requireNonNull(lightBarSirenInUse, "light_bar_siren_in_use"),
                    incidentIndication,
                    emergencyPriority);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}
