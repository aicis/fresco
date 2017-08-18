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

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilderParallel;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.ParallelNumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;

/**
 * Implements a close operation on a matrix of BigIntegers.
 */
public class CloseMatrix implements ComputationBuilderParallel<Matrix<Computation<SInt>>> {

  private final Matrix<BigInteger> openMatrix;
  private final int inputParty;
  private final int h;
  private final int w;
  private final boolean isInputProvider;

  /**
   * Makes a new CloseMatrix.
   * 
   * Party providing input should call this.
   *
   * @param openMatrix the matrix to close.
   */
  public CloseMatrix(Matrix<BigInteger> openMatrix, int inputParty) {
    super();
    this.openMatrix = openMatrix;
    this.h = openMatrix.getHeight();
    this.w = openMatrix.getWidth();
    this.inputParty = inputParty;
    this.isInputProvider = true;
  }

  /**
   * Makes a new CloseMatrix.
   * 
   * Party providing input should call this.
   *
   * @param openMatrix the matrix to close.
   */
  public CloseMatrix(int h, int w, int inputParty) {
    super();
    this.openMatrix = null;
    this.h = h;
    this.w = w;
    this.inputParty = inputParty;
    this.isInputProvider = false;
  }

  private List<Computation<List<Computation<SInt>>>> buildAsProvider(ParallelNumericBuilder par) {
    List<Computation<List<Computation<SInt>>>> closedRows = new ArrayList<>();
    for (List<BigInteger> row : openMatrix.getRows()) {
      Computation<List<Computation<SInt>>> closedRow =
          par.createParallelSub(new CloseList(row, inputParty));
      closedRows.add(closedRow);
    }
    return closedRows;
  }

  private List<Computation<List<Computation<SInt>>>> buildAsReceiver(ParallelNumericBuilder par) {
    List<Computation<List<Computation<SInt>>>> closedRows = new ArrayList<>();
    for (int r = 0; r < h; r++) {
      Computation<List<Computation<SInt>>> closedRow =
          par.createParallelSub(new CloseList(w, inputParty));
      closedRows.add(closedRow);
    }
    return closedRows;
  }

  @Override
  public Computation<Matrix<Computation<SInt>>> build(ParallelNumericBuilder par) {
    List<Computation<List<Computation<SInt>>>> closed =
        isInputProvider ? buildAsProvider(par) : buildAsReceiver(par);
    return () -> new MatrixUtils().unwrapRows(closed);
  }
}
