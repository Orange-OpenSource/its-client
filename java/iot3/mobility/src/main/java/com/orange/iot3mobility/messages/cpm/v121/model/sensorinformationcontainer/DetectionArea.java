package com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer;

import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaCircular;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaEllipse;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaPolygon;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaRectangle;

/**
 * Detection area for a sensor.
 *
 * Exactly one of the detection area options should be provided.
 *
 * @param vehicleSensor {@link VehicleSensor}
 * @param stationarySensorRadial {@link StationarySensorRadial}
 * @param stationarySensorPolygon {@link AreaPolygon}
 * @param stationarySensorCircular {@link AreaCircular}
 * @param stationarySensorEllipse {@link AreaEllipse}
 * @param stationarySensorRectangle {@link AreaRectangle}
 */
public record DetectionArea(
        VehicleSensor vehicleSensor,
        StationarySensorRadial stationarySensorRadial,
        AreaPolygon stationarySensorPolygon,
        AreaCircular stationarySensorCircular,
        AreaEllipse stationarySensorEllipse,
        AreaRectangle stationarySensorRectangle) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private VehicleSensor vehicleSensor;
        private StationarySensorRadial stationarySensorRadial;
        private AreaPolygon stationarySensorPolygon;
        private AreaCircular stationarySensorCircular;
        private AreaEllipse stationarySensorEllipse;
        private AreaRectangle stationarySensorRectangle;

        public Builder vehicleSensor(VehicleSensor vehicleSensor) {
            this.vehicleSensor = vehicleSensor;
            return this;
        }

        public Builder stationarySensorRadial(StationarySensorRadial stationarySensorRadial) {
            this.stationarySensorRadial = stationarySensorRadial;
            return this;
        }

        public Builder stationarySensorPolygon(AreaPolygon stationarySensorPolygon) {
            this.stationarySensorPolygon = stationarySensorPolygon;
            return this;
        }

        public Builder stationarySensorCircular(AreaCircular stationarySensorCircular) {
            this.stationarySensorCircular = stationarySensorCircular;
            return this;
        }

        public Builder stationarySensorEllipse(AreaEllipse stationarySensorEllipse) {
            this.stationarySensorEllipse = stationarySensorEllipse;
            return this;
        }

        public Builder stationarySensorRectangle(AreaRectangle stationarySensorRectangle) {
            this.stationarySensorRectangle = stationarySensorRectangle;
            return this;
        }

        public DetectionArea build() {
            return new DetectionArea(
                    vehicleSensor,
                    stationarySensorRadial,
                    stationarySensorPolygon,
                    stationarySensorCircular,
                    stationarySensorEllipse,
                    stationarySensorRectangle);
        }
    }
}
