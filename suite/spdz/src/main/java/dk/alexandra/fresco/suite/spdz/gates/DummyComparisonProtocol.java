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
package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocol;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzOInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;

public class DummyComparisonProtocol implements ComparisonProtocol {

  private final SpdzSInt a, b, result;
  private final BasicNumericFactory factory;
  private ProtocolProducer currPP;
  private boolean done = false;

  public DummyComparisonProtocol(SInt a, SInt b, SInt result,
      BasicNumericFactory factory) {
    this.a = (SpdzSInt) a;
    this.b = (SpdzSInt) b;
    this.result = (SpdzSInt) result;
    this.factory = factory;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (currPP == null) {
      OInt a_open = factory.getOInt();
      OInt b_open = factory.getOInt();
      Computation<? extends OInt> openA = factory.getOpenProtocol(a, a_open);
      Computation<? extends OInt> openB = factory.getOpenProtocol(b, b_open);
      SpdzOInt a_open_ = (SpdzOInt) a_open;
      SpdzOInt b_open_ = (SpdzOInt) b_open;

      DummyInternalChooseGate chooseGate = new DummyInternalChooseGate(
          a_open_, b_open_, result);
      ParallelProtocolProducer parrGP = new ParallelProtocolProducer(openA, openB);
      currPP = new SequentialProtocolProducer(parrGP, chooseGate);
    }
    if (currPP.hasNextProtocols()) {
      currPP.getNextProtocols(protocolCollection);
    } else {
      currPP = null;
      done = true;
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return !done;
  }

  private class DummyInternalChooseGate extends SpdzNativeProtocol<SpdzSInt> {

    private SpdzOInt a, b;
    private SpdzSInt result;

    DummyInternalChooseGate(SpdzOInt a, SpdzOInt b, SInt result) {
      this.a = a;
      this.b = b;
      this.result = (SpdzSInt) result;
    }

    @Override
    public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
        SCENetwork network) {
      SpdzOInt min;
      if (compareModP(a.getValue(), b.getValue(), spdzResourcePool.getModulus()) <= 0) {
        min = new SpdzOInt(BigInteger.ONE);
      } else {
        min = new SpdzOInt(BigInteger.ZERO);
      }
      SpdzElement elm;
      if (min.getValue().equals(BigInteger.ONE)) {
        if (spdzResourcePool.getMyId() == 1) {
          elm = new SpdzElement(BigInteger.ONE, min.getValue()
              .multiply(spdzResourcePool.getStore().getSSK()));
        } else {
          elm = new SpdzElement(BigInteger.ZERO, min.getValue()
              .multiply(spdzResourcePool.getStore().getSSK()));
        }
      } else {
        elm = new SpdzElement(BigInteger.ZERO, BigInteger.ZERO);
      }
      this.result.value = elm;
      return EvaluationStatus.IS_DONE;
    }

    /**
     * @return a comparison where numbers (P - a) that are larger than ((P - 1) / 2) are interpreted
     * as the negative number (- a)
     */
    private int compareModP(BigInteger a, BigInteger b, BigInteger modulus) {
      BigInteger realA = a;
      BigInteger realB = b;
      BigInteger halfPoint = modulus.subtract(BigInteger.ONE).divide((BigInteger.valueOf(2)));
      if (a.compareTo(halfPoint) > 0) {
        realA = a.subtract(modulus);
      }
      if (b.compareTo(halfPoint) > 0) {
        realB = b.subtract(modulus);
      }
      return realA.compareTo(realB);
    }

    @Override
    public SpdzSInt out() {
      return result;
    }

  }
}
