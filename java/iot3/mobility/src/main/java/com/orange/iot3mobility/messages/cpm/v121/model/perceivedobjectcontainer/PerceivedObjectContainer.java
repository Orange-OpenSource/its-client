/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

import java.util.List;

/**
 * Perceived object container.
 *
 * @param perceivedObjects List of {@link PerceivedObject}. Size: [1..128].
 */
public record PerceivedObjectContainer(List<PerceivedObject> perceivedObjects) {}

