/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.ReferencePosition;

/**
 * Management container
 *
 * @param referenceTime Reference time for all time related information in the CPM. Unit: ms (0..4398046511103).
 * @param referencePosition {@link ReferencePosition}
 * @param segmentationInfo Optional {@link SegmentationInfo}
 * @param messageRateRange Optional {@link MessageRateRange} planned or expected range of CPM generation rate.
 */
public record ManagementContainer(
        long referenceTime,
        ReferencePosition referencePosition,
        SegmentationInfo segmentationInfo,
        MessageRateRange messageRateRange) {

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long referenceTime;
        private ReferencePosition referencePosition;
        private SegmentationInfo segmentationInfo;
        private MessageRateRange messageRateRange;

        public Builder referenceTime(long referenceTime) {
            this.referenceTime = referenceTime;
            return this;
        }

        public Builder referencePosition(ReferencePosition referencePosition) {
            this.referencePosition = referencePosition;
            return this;
        }

        public Builder segmentationInfo(SegmentationInfo segmentationInfo) {
            this.segmentationInfo = segmentationInfo;
            return this;
        }

        public Builder messageRateRange(MessageRateRange messageRateRange) {
            this.messageRateRange = messageRateRange;
            return this;
        }

        public ManagementContainer build() {
            return new ManagementContainer(
                    requireNonNull(referenceTime, "reference_time"),
                    requireNonNull(referencePosition, "reference_position"),
                    segmentationInfo,
                    messageRateRange);
        }

        private static <T> T requireNonNull(T value, String field) {
            if (value == null) throw new IllegalStateException("Missing field: " + field);
            return value;
        }
    }
}
