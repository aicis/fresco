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
 *******************************************************************************/
package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.ParallelProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.lp.LPTableau4;
import dk.alexandra.fresco.lib.lp.Matrix4;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PlainSpdzLPPrefix4 {

  private final Matrix4<Computation<SInt>> updateMatrix;
  private final LPTableau4 tableau;
  private final Computation<SInt> pivot;

  public PlainSpdzLPPrefix4(PlainLPInputReader4 inputReader, ParallelProtocolBuilder par)
      throws IOException {
    if (!inputReader.isRead()) {
      inputReader.readInput();
    }
    int noVariables = inputReader.getCostValues().length;
    int noConstraints = inputReader.getConstraintValues().length;
    Matrix4<Computation<SInt>> C;
    NumericBuilder numeric = par.numeric();
    C = new Matrix4<>(noVariables, noConstraints,
        (i) -> {
          ArrayList<Computation<SInt>> row = new ArrayList<>(noVariables);
          for (int j = 0; j < noVariables; j++) {
            row.add(numeric.known(inputReader.getCValues()[i][j]));
          }
          return row;
        });
    ArrayList<Computation<SInt>> f = new ArrayList<>(
        Arrays.stream(inputReader.getFValues()).map(numeric::known)
            .collect(Collectors.toList()));
    ArrayList<Computation<SInt>> b = new ArrayList<>(
        Arrays.stream(inputReader.getBValues()).map(numeric::known)
            .collect(Collectors.toList()));

    this.updateMatrix = new Matrix4<>(noConstraints + 1, noConstraints + 1,
        (i) -> {
          ArrayList<Computation<SInt>> row = new ArrayList<>();
          for (int j = 0; j < noConstraints + 1; j++) {
            if (i == j) {
              row.add(numeric.known(BigInteger.ONE));
            } else {
              row.add(numeric.known(BigInteger.ZERO));
            }
          }
          return row;
        });

    Computation<SInt> z = numeric.known(BigInteger.ZERO);

    this.pivot = numeric.known(BigInteger.ONE);
    this.tableau = new LPTableau4(C, b, f, z);
  }

  public LPTableau4 getTableau() {
    return this.tableau;
  }

  public Matrix4<Computation<SInt>> getUpdateMatrix() {
    return this.updateMatrix;
  }

  public Computation<SInt> getPivot() {
    return this.pivot;
  }

}
