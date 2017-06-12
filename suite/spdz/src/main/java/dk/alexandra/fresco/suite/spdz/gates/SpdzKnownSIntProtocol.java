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

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;

public class SpdzKnownSIntProtocol extends SpdzNativeProtocol<SInt> {

  BigInteger value;
  private SpdzSInt sValue;

  /**
   * Creates a gate loading a given value into a given SInt
   *
   * @param value the value
   * @param sValue the SInt
   */
  public SpdzKnownSIntProtocol(BigInteger value, SInt sValue) {
    this.value = value;
    this.sValue = (SpdzSInt) sValue;
  }

  /**
   * Creates a gate loading a given value into a given SInt
   *
   * @param value the value
   * @param sValue the SInt
   */
  public SpdzKnownSIntProtocol(int value, SInt sValue) {
    this(BigInteger.valueOf(value), sValue);
  }

  @Override
  public SInt out() {
    return sValue;
  }

  @Override
  public EvaluationStatus evaluate(
      int round,
      SpdzResourcePool spdzResourcePool,
      SCENetwork network) {
    BigInteger modulus = spdzResourcePool.getModulus();
    value = value.mod(modulus);
    SpdzElement elm;
    BigInteger globalKeyShare = spdzResourcePool.getStore().getSSK();
    if (spdzResourcePool.getMyId() == 1) {
      elm = new SpdzElement(value, value.multiply(globalKeyShare).mod(modulus));
    } else {
      elm = new SpdzElement(BigInteger.ZERO, value.multiply(globalKeyShare).mod(modulus));
    }
    sValue.value = elm;
    return EvaluationStatus.IS_DONE;
  }
}
