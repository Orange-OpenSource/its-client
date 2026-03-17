package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

import java.util.List;

/**
 * Perceived object container.
 *
 * @param perceivedObjects List of {@link PerceivedObject}. Size: [1..128].
 */
public record PerceivedObjectContainer(List<PerceivedObject> perceivedObjects) {}

