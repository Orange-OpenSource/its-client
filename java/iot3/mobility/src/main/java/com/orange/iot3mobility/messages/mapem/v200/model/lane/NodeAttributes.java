/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane;

import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.NodeAttributeXY;
import com.orange.iot3mobility.messages.mapem.v200.model.lane.enums.SegmentAttributeXY;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Optional attribute set attached to a node point in a lane path.
 *
 * @param localNode   Optional. Attribute states pertaining to this specific node point.
 *                    Use {@link NodeAttributeXY} values.
 * @param disabled    Optional. Segment attributes disabled at this node point.
 *                    Use {@link SegmentAttributeXY} values.
 * @param enabled     Optional. Segment attributes enabled at this node point and staying enabled.
 *                    Use {@link SegmentAttributeXY} values.
 * @param data        Optional. Attributes that carry additional numeric data values.
 * @param dWidth      Optional. Width delta from this node onward, in cm (offset_b16). Value of zero must not be used.
 * @param dElevation  Optional. Elevation delta from this node onward, in 10 cm steps (offset_b16).
 */
public record NodeAttributes(
        List<String> localNode,
        List<String> disabled,
        List<String> enabled,
        List<NodeAttributeData> data,
        Integer dWidth,
        Integer dElevation) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private List<String> localNode;
        private List<String> disabled;
        private List<String> enabled;
        private List<NodeAttributeData> data;
        private Integer dWidth;
        private Integer dElevation;

        private Builder() {}

        /** Sets the local node attributes using raw JSON strings. */
        public Builder localNode(List<String> localNode) { this.localNode = localNode; return this; }

        /**
         * Sets the local node attributes using typed enum constants.
         *
         * @param nodeAttributes zero or more {@link NodeAttributeXY} values
         * @return this builder
         */
        public Builder localNode(NodeAttributeXY... nodeAttributes) {
            this.localNode = nodeAttributes.length == 0
                    ? Collections.emptyList()
                    : Arrays.stream(nodeAttributes).map(NodeAttributeXY::value).toList();
            return this;
        }

        /** Sets the disabled segment attributes using raw JSON strings. */
        public Builder disabled(List<String> disabled) { this.disabled = disabled; return this; }

        /**
         * Sets the segment attributes disabled at this node using typed enum constants.
         *
         * @param segmentAttributes zero or more {@link SegmentAttributeXY} values
         * @return this builder
         */
        public Builder disabled(SegmentAttributeXY... segmentAttributes) {
            this.disabled = segmentAttributes.length == 0
                    ? Collections.emptyList()
                    : Arrays.stream(segmentAttributes).map(SegmentAttributeXY::value).toList();
            return this;
        }

        /** Sets the enabled segment attributes using raw JSON strings. */
        public Builder enabled(List<String> enabled) { this.enabled = enabled; return this; }

        /**
         * Sets the segment attributes enabled at this node using typed enum constants.
         *
         * @param segmentAttributes zero or more {@link SegmentAttributeXY} values
         * @return this builder
         */
        public Builder enabled(SegmentAttributeXY... segmentAttributes) {
            this.enabled = segmentAttributes.length == 0
                    ? Collections.emptyList()
                    : Arrays.stream(segmentAttributes).map(SegmentAttributeXY::value).toList();
            return this;
        }

        public Builder data(List<NodeAttributeData> data) { this.data = data; return this; }
        public Builder dWidth(Integer dWidth) { this.dWidth = dWidth; return this; }
        public Builder dElevation(Integer dElevation) { this.dElevation = dElevation; return this; }

        public NodeAttributes build() {
            return new NodeAttributes(localNode, disabled, enabled, data, dWidth, dElevation);
        }
    }
}
