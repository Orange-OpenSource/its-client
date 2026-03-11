package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import java.util.List;

/**
 * Lower triangular correlation matrix.
 */
public record LowerTriangularCorrelationMatrix(
        LowerTriangularMatrixComponents componentsIncludedInTheMatrix,
        List<List<List<Integer>>> matrix) {}

