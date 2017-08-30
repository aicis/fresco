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

import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilderParallel;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.ParallelNumericBuilder;
import dk.alexandra.fresco.framework.util.RowPairC;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;

public class ConditionalSwapNeighbors
    implements ComputationBuilderParallel<Matrix<Computation<SInt>>> {

  final private List<Computation<SInt>> swappers;
  final private Matrix<Computation<SInt>> rows;

  /**
   * Swaps rows in matrix according to swappers bits.
   * 
   * @param swappers
   * @param rows
   */
  public ConditionalSwapNeighbors(List<Computation<SInt>> swappers,
      Matrix<Computation<SInt>> rows) {
    super();
    this.swappers = swappers;
    this.rows = rows;
  }

  @Override
  public Computation<Matrix<Computation<SInt>>> build(ParallelNumericBuilder par) {
    List<Computation<RowPairC<SInt, SInt>>> pairs = new ArrayList<>();
    int swapperIdx = 0;
    for (int i = 0; i < rows.getHeight() - 1; i += 2) {
      Computation<RowPairC<SInt, SInt>> pair = par.createSequentialSub(
          new ConditionalSwapRows(swappers.get(swapperIdx), rows.getRow(i), rows.getRow(i + 1)));
      swapperIdx++;
      pairs.add(pair);
    }
    return () -> {
      ArrayList<ArrayList<Computation<SInt>>> temp = new ArrayList<>();
      for (Computation<RowPairC<SInt, SInt>> computation : pairs) {
        temp.add(computation.out().getFirst());
        temp.add(computation.out().getSecond());
      }
      return new Matrix<>(rows.getHeight(), rows.getWidth(), temp);
    };
  }
}
