package com.orange.iot3mobility.messages.cpm.v121.model.freespacecontainer;

import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaCircular;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaEllipse;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaPolygon;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaRectangle;

/**
 * Free space area definition.
 * <p>
 * Exactly one of the free space area options should be provided.
 *
 * @param freeSpacePolygon {@link AreaPolygon}
 * @param freeSpaceCircular {@link AreaCircular}
 * @param freeSpaceEllipse {@link AreaEllipse}
 * @param freeSpaceRectangle {@link AreaRectangle}
 */
public record FreeSpaceArea(
        AreaPolygon freeSpacePolygon,
        AreaCircular freeSpaceCircular,
        AreaEllipse freeSpaceEllipse,
        AreaRectangle freeSpaceRectangle) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private AreaPolygon freeSpacePolygon;
        private AreaCircular freeSpaceCircular;
        private AreaEllipse freeSpaceEllipse;
        private AreaRectangle freeSpaceRectangle;

        public Builder freeSpacePolygon(AreaPolygon freeSpacePolygon) {
            this.freeSpacePolygon = freeSpacePolygon;
            return this;
        }

        public Builder freeSpaceCircular(AreaCircular freeSpaceCircular) {
            this.freeSpaceCircular = freeSpaceCircular;
            return this;
        }

        public Builder freeSpaceEllipse(AreaEllipse freeSpaceEllipse) {
            this.freeSpaceEllipse = freeSpaceEllipse;
            return this;
        }

        public Builder freeSpaceRectangle(AreaRectangle freeSpaceRectangle) {
            this.freeSpaceRectangle = freeSpaceRectangle;
            return this;
        }

        public FreeSpaceArea build() {
            return new FreeSpaceArea(
                    freeSpacePolygon,
                    freeSpaceCircular,
                    freeSpaceEllipse,
                    freeSpaceRectangle);
        }
    }
}
