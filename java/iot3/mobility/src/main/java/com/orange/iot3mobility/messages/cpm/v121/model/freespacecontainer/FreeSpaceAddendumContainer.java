package com.orange.iot3mobility.messages.cpm.v121.model.freespacecontainer;

import java.util.List;

/**
 * Free space addendum container.
 *
 * @param freeSpaceAddenda List of addenda. Size: [1..128].
 */
public record FreeSpaceAddendumContainer(List<FreeSpaceAddendum> freeSpaceAddenda) {}
