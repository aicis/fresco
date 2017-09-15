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

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.Collections;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CloseMatrix
    implements ComputationParallel<Matrix<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final Matrix<BigInteger> openMatrix;
  private final int inputParty;
  private final int height;
  private final int width;
  private final boolean isInputProvider;

  public CloseMatrix(Matrix<BigInteger> openMatrix, int inputParty) {
    super();
    this.openMatrix = openMatrix;
    this.height = openMatrix.getHeight();
    this.width = openMatrix.getWidth();
    this.inputParty = inputParty;
    this.isInputProvider = true;
  }

  public CloseMatrix(int h, int w, int inputParty) {
    super();
    this.openMatrix = null;
    this.height = h;
    this.width = w;
    this.inputParty = inputParty;
    this.isInputProvider = false;
  }

  private List<DRes<List<DRes<SInt>>>> buildAsProvider(Collections collections) {
    List<DRes<List<DRes<SInt>>>> closedRows = new ArrayList<>();
    for (List<BigInteger> row : openMatrix.getRows()) {
      DRes<List<DRes<SInt>>> closedRow = collections.closeList(row, inputParty);
      closedRows.add(closedRow);
    }
    return closedRows;
  }

  private List<DRes<List<DRes<SInt>>>> buildAsReceiver(Collections collections) {
    List<DRes<List<DRes<SInt>>>> closedRows = new ArrayList<>();
    for (int r = 0; r < height; r++) {
      DRes<List<DRes<SInt>>> closedRow = collections.closeList(width, inputParty);
      closedRows.add(closedRow);
    }
    return closedRows;
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    Collections collections = builder.collections();
    List<DRes<List<DRes<SInt>>>> closed =
        isInputProvider ? buildAsProvider(collections) : buildAsReceiver(collections);
    return () -> new MatrixUtils().unwrapRows(closed);
  }
}
