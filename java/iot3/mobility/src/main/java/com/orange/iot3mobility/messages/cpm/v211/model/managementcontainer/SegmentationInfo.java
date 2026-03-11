package com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer;

/**
 * Information regarding the message segmentation on the facility layer.
 */
public record SegmentationInfo(int totalMsgNo, int thisMsgNo) {}

