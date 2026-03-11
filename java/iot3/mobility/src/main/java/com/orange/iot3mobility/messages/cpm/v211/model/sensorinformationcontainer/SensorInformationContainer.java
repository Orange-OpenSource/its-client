package com.orange.iot3mobility.messages.cpm.v211.model.sensorinformationcontainer;

import java.util.List;

/**
 * Sensor information container.
 */
public record SensorInformationContainer(List<SensorInformation> sensorInformation) {

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private List<SensorInformation> sensorInformation;

        public Builder sensorInformation(List<SensorInformation> sensorInformation) {
            this.sensorInformation = sensorInformation;
            return this;
        }

        public SensorInformationContainer build() {
            return new SensorInformationContainer(requireNonNull(sensorInformation, "sensor_information_container"));
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) {
                throw new IllegalStateException("Missing field: " + field);
            }
            return value;
        }
    }
}

