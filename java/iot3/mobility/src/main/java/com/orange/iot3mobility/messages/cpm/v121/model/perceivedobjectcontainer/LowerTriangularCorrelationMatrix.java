package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

import java.util.List;

/**
 * Lower triangular correlation matrix components.
 *
 * @param columns List of columns. Size: [1..17]. Each value is scaled by 100. Value: full-negative-correlation(-100), no-correlation(0), full-positive-correlation(100).
 */
public record LowerTriangularCorrelationMatrix(List<List<Integer>> columns) {}
