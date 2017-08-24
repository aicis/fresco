/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.conditional;

import java.math.BigInteger;
import java.util.ArrayList;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilderParallel;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.ParallelNumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;

public class ConditionalSwapRows implements ComputationBuilderParallel<Matrix<Computation<SInt>>> {

  final private Computation<SInt> swapper;
  final private Matrix<Computation<SInt>> mat;
  final private int rowIdx, otherRowIdx;
  final private ArrayList<Computation<SInt>> row, otherRow;

  /**
   * Swaps rows in matrix based on swapper. Swapper must be 0 or 1.
   * 
   * If swapper is 1 the rows are swapped (in place). Otherwise, original order.
   * 
   * @param swapper
   * @param mat
   * @param rowIdx
   * @param otherRowIdx
   */
  public ConditionalSwapRows(Computation<SInt> swapper, Matrix<Computation<SInt>> mat, int rowIdx,
      int otherRowIdx) {
    this.swapper = swapper;
    this.mat = mat;
    this.rowIdx = rowIdx;
    this.otherRowIdx = otherRowIdx;
    this.row = mat.getRow(this.rowIdx);
    this.otherRow = mat.getRow(this.otherRowIdx);
  }

  @Override
  public Computation<Matrix<Computation<SInt>>> build(ParallelNumericBuilder builder) {
    Computation<ArrayList<Computation<SInt>>> updatedRow =
        new ConditionalSelectRow(builder.numeric().sub(BigInteger.ONE, swapper), row, otherRow)
            .build(builder);
    Computation<ArrayList<Computation<SInt>>> updatedOtherRow =
        new ConditionalSelectRow(swapper, row, otherRow).build(builder);
    return () -> {
      mat.setRow(rowIdx, updatedRow.out());
      mat.setRow(otherRowIdx, updatedOtherRow.out());
      return mat;
    };
  }
}
