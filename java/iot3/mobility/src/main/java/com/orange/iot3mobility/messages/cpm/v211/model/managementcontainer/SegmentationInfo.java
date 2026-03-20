package com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer;

/**
 * Segmentation information
 * <p>
 * Information regarding the message segmentation on the facility layer.
 *
 * @param totalMsgNo Indicates the total number of messages used to encode the information (1..8).
 * @param thisMsgNo Indicates the position of the message within the total set (1..8).
 */
public record SegmentationInfo(int totalMsgNo, int thisMsgNo) {}

