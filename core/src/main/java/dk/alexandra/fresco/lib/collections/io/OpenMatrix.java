/*
 * Copyright (c) 2015, 2016, 2107 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/

package dk.alexandra.fresco.lib.collections.io;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.Collections;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;

/**
 * Implements an open operation on a matrix of DRes<SInt>.
 */
public class OpenMatrix
    implements ComputationParallel<Matrix<DRes<BigInteger>>, ProtocolBuilderNumeric> {

  private final DRes<Matrix<DRes<SInt>>> closedMatrix;

  /**
   * Makes a new OpenMatrix
   *
   * @param closedMatrix the matrix to open.
   */
  public OpenMatrix(DRes<Matrix<DRes<SInt>>> closedMatrix) {
    super();
    this.closedMatrix = closedMatrix;
  }

  @Override
  public DRes<Matrix<DRes<BigInteger>>> buildComputation(ProtocolBuilderNumeric builder) {
    Collections collections = builder.collections();
    List<DRes<List<DRes<BigInteger>>>> closedRows = new ArrayList<>();
    for (List<DRes<SInt>> row : closedMatrix.out().getRows()) {
      // still sort of hacky: need to artificially wrap row in computation
      DRes<List<DRes<BigInteger>>> closedRow = collections.openList(() -> row);
      closedRows.add(closedRow);
    }
    return () -> new MatrixUtils().unwrapRows(closedRows);
  }
}
