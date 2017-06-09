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
package dk.alexandra.fresco.suite.spdz.utils;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.SIntFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPPrefix;
import dk.alexandra.fresco.lib.lp.LPTableau;
import dk.alexandra.fresco.lib.lp.Matrix;
import java.io.IOException;
import java.math.BigInteger;

/**
 * This LPPrefix simply reads plaintext inputs from an LPInputReader and creates an input protocols
 * to put the corresponding plaintext values into the SInts needed for the LPSolver.
 *
 * @author psn
 */
public class PlainSpdzLPPrefix implements LPPrefix {

  private final Matrix<SInt> updateMatrix;
  private final LPTableau tableau;
  private final SInt pivot;
  private final SInt[] basis;
  private ProtocolProducer prefix;

  public PlainSpdzLPPrefix(LPInputReader inputReader, BasicNumericFactory factory)
      throws IOException {
    if (!inputReader.isRead()) {
      inputReader.readInput();
    }
    int noVariables = inputReader.getCostValues().length;
    int noConstraints = inputReader.getConstraintValues().length;
    SInt[][] C = new SInt[noConstraints][noVariables];
    SInt[] B = new SInt[noConstraints];
    SInt[] F = new SInt[noVariables];
    // fill public values
    for (int i = 0; i < C.length; i++) {
      for (int j = 0; j < C[i].length; j++) {
        if (inputReader.getCPattern()[i][j] == 0) {
          C[i][j] = factory.getSInt(inputReader.getCValues()[i][j]);
        } else {
          C[i][j] = null;
        }
      }
    }
    for (int i = 0; i < F.length; i++) {
      if (inputReader.getFPattern()[i] == 0) {
        F[i] = factory.getSInt(inputReader.getFValues()[i]);
      } else {
        F[i] = null;
      }
    }
    for (int i = 0; i < B.length; i++) {
      if (inputReader.getBPattern()[i] == 0) {
        B[i] = factory.getSInt(inputReader.getBValues()[i]);
      } else {
        B[i] = null;
      }
    }
    SInt[][] update = new SInt[noConstraints + 1][noConstraints + 1];
    for (int i = 0; i < noConstraints + 1; i++) {
      for (int j = 0; j < noConstraints + 1; j++) {
        if (i == j) {
          update[i][j] = factory.getSInt(1);
        } else {
          update[i][j] = factory.getSInt(0);
        }
      }
    }
    SInt z = factory.getSInt(0);

    C = sIntFillRemaining(C, factory);
    B = sIntFill(B, factory);
    F = sIntFill(F, factory);

    ProtocolProducer cInputProducer = makeInputProtocols(inputReader.getCValues(),
        inputReader.getCPattern(), C, factory);
    ProtocolProducer bInputProducer = makeInputProtocols(inputReader.getBValues(),
        inputReader.getBPattern(), B, factory);
    ProtocolProducer fInputProducer = makeInputProtocols(inputReader.getFValues(),
        inputReader.getFPattern(), F, factory);
    ProtocolProducer input = new ParallelProtocolProducer(cInputProducer, bInputProducer,
        fInputProducer);
    this.updateMatrix = new Matrix<SInt>(update);
    this.pivot = factory.getSInt(1);
    this.basis = new SInt[noConstraints];
    this.tableau = new LPTableau(new Matrix<SInt>(C), B, F, z);
    this.prefix = input;
  }

  private SInt[][] sIntFillRemaining(SInt[][] matrix, BasicNumericFactory factory) {
    for (SInt[] vector : matrix) {
      sIntFill(vector, factory);
    }
    return matrix;
  }


  private SInt[] sIntFill(SInt[] vector, SIntFactory factory) {
    for (int i = 0; i < vector.length; i++) {
      vector[i] = factory.getSInt();
    }
    return vector;
  }

  private ProtocolProducer makeInputProtocols(BigInteger[] values, int[] pattern,
      SInt[] vector, BasicNumericFactory factory) {
    if (vector.length != values.length || vector.length != pattern.length) {
      throw new RuntimeException("Inputs are not equal length");
    }
    ParallelProtocolProducer input = new ParallelProtocolProducer();
    for (int i = 0; i < vector.length; i++) {
      if (pattern[i] != 0) {
        input.append(factory.getCloseProtocol(values[i], vector[i], pattern[i]));
      }
    }
    return input;
  }

  private ProtocolProducer makeInputProtocols(BigInteger[][] values, int[][] pattern,
      SInt[][] matrix, BasicNumericFactory factory) {
    if (matrix.length != values.length || values.length != pattern.length ||
        values[0].length != matrix[0].length || values[0].length != pattern[0].length) {
      throw new RuntimeException("Input Dimensions are not equal");
    }
    ParallelProtocolProducer par = new ParallelProtocolProducer();
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
        if (pattern[i][j] != 0) {
          par.append(factory.getCloseProtocol(values[i][j], matrix[i][j], pattern[i][j]));
        }
      }
    }
    return par;
  }

  @Override
  public ProtocolProducer getPrefix() {
    return prefix;
  }

  @Override
  public LPTableau getTableau() {
    return this.tableau;
  }

  @Override
  public Matrix<SInt> getUpdateMatrix() {
    return this.updateMatrix;
  }

  @Override
  public SInt getPivot() {
    return this.pivot;
  }

  @Override
  public SInt[] getBasis() {
    return this.basis;
  }
}
