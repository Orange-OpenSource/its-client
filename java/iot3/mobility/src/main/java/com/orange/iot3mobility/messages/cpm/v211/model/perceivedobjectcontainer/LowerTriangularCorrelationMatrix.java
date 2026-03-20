package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

import java.util.List;

/**
 * Lower triangular correlation matrix.
 *
 * @param componentsIncludedInTheMatrix {@link LowerTriangularMatrixComponents} indicating which components are included.
 * @param matrix List of matrix cells ordered by columns and rows. Cell values are correlation values (-100..101), where
 *               101 indicates unavailable.
 */
public record LowerTriangularCorrelationMatrix(
        LowerTriangularMatrixComponents componentsIncludedInTheMatrix,
        List<List<List<Integer>>> matrix) {}

