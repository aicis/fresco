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
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.util.RowPairC;
import dk.alexandra.fresco.framework.value.SInt;

public class ConditionalSwapRows implements ComputationBuilder<RowPairC<SInt, SInt>> {

  final private Computation<SInt> swapper;
  final private ArrayList<Computation<SInt>> row, otherRow;

  /**
   * Swaps rows in matrix based on swapper. Swapper must be 0 or 1.
   * 
   * If swapper is 1 the rows are swapped. Otherwise, original order.
   * 
   * @param swapper
   * @param mat
   * @param rowIdx
   * @param otherRowIdx
   */
  public ConditionalSwapRows(Computation<SInt> swapper, ArrayList<Computation<SInt>> row,
      ArrayList<Computation<SInt>> otherRow) {
    this.swapper = swapper;
    this.row = row;
    this.otherRow = otherRow;
  }

  @Override
  public Computation<RowPairC<SInt, SInt>> build(SequentialNumericBuilder builder) {
    Computation<SInt> flipped = builder.numeric().sub(BigInteger.ONE, swapper);
    Computation<ArrayList<Computation<SInt>>> updatedRow =
        builder.createParallelSub(new ConditionalSelectRow(flipped, row, otherRow));
    Computation<ArrayList<Computation<SInt>>> updatedOtherRow =
        builder.createParallelSub(new ConditionalSelectRow(swapper, row, otherRow));
    return () -> {
      return new RowPairC<>(updatedRow.out(), updatedOtherRow.out());
    };
  }
}
