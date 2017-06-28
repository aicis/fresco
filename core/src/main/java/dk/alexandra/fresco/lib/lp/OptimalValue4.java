/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 */
package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;

/**
 * NativeProtocol extracting the optimal value from a {@link LPTableau} and an update
 * matrix representing a terminated Simplex method.
 */
public class OptimalValue4 implements ComputationBuilder<SInt> {

  private final Matrix4<Computation<SInt>> updateMatrix;
  private final Computation<SInt> pivot;
  private final LPTableau4 tableau;

  /**
   * An general version of the protocol working any (valid) initial tableau.
   *
   * @param updateMatrix the final update matrix
   * @param tableau the initial tableau
   * @param pivot the final pivot
   */
  public OptimalValue4(
      Matrix4<Computation<SInt>> updateMatrix,
      LPTableau4 tableau,
      Computation<SInt> pivot) {
    this.updateMatrix = updateMatrix;
    this.tableau = tableau;
    this.pivot = pivot;
  }


  @Override
  public Computation<SInt> build(SequentialProtocolBuilder builder) {
    ArrayList<Computation<SInt>> row = updateMatrix.getRow(updateMatrix.getHeight() - 1);
    ArrayList<Computation<SInt>> column = new ArrayList<>(row.size());
    column.addAll(tableau.getB());
    column.add(tableau.getZ());
    AdvancedNumericBuilder advanced = builder.createAdvancedNumericBuilder();
    Computation<SInt> numerator = advanced.dot(row, column);
    Computation<SInt> invDenominator = advanced.invert(pivot);
    return builder.numeric().mult(numerator, invDenominator);
  }
}
