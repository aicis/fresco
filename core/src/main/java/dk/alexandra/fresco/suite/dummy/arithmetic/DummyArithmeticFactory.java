/*******************************************************************************
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
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

package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import java.math.BigInteger;

/**
 * Implementation of {@link BasicNumericFactory} for the Dummy Arithmetic suite.
 */
public class DummyArithmeticFactory implements BasicNumericFactory {

  private BigInteger mod;
  private int maxBitLength;

  public DummyArithmeticFactory(BigInteger mod, int maxBitLength) {
    this.mod = mod;
    this.maxBitLength = maxBitLength;
  }

  @Override
  public SInt getSInt() {
    return new DummyArithmeticSInt();
  }

  @Override
  public SInt getSInt(int i) {
    return new DummyArithmeticSInt(i);
  }

  @Override
  public SInt getSInt(BigInteger i) {
    return new DummyArithmeticSInt(i);
  }

  @Override
  public Computation<SInt> getSInt(BigInteger i, SInt si) {
    return new DummyArithmeticNativeProtocol<SInt>() {

      @Override
      public EvaluationStatus evaluate(int round, DummyArithmeticResourcePool resourcePool,
          SCENetwork network) {
        ((DummyArithmeticSInt) si).setValue(i);
        return EvaluationStatus.IS_DONE;
      }

      @Override
      public SInt out() {
        return si;
      }
    };
  }

  @Override
  public Computation<? extends SInt> getAddProtocol(SInt a, SInt b, SInt out) {
    return new DummyArithmeticAddProtocol(a, b, out);
  }

  @Override
  public Computation<? extends SInt> getAddProtocol(SInt input, BigInteger openInput, SInt out) {
    return new DummyArithmeticAddProtocol(input, () -> new DummyArithmeticSInt(openInput), out);
  }

  @Override
  public Computation<? extends SInt> getSubtractProtocol(SInt a, SInt b, SInt out) {
    return new DummyArithmeticSubtractProtocol(a, b, out);
  }


  @Override
  public Computation<? extends SInt> getMultProtocol(SInt a, SInt b, SInt out) {
    return new DummyArithmeticMultProtocol(a, b, out);
  }

  @Override
  public Computation<? extends SInt> getCloseProtocol(int source, BigInteger open, SInt closed) {
    return new DummyArithmeticCloseProtocol(source, () -> open, closed);
  }


  @Override
  public Computation<BigInteger> getOpenProtocol(SInt closed) {
    return new DummyArithmeticOpenToAllProtocol(closed);
  }

  @Override
  public Computation<BigInteger> getOpenProtocol(int target, SInt closed) {
    return new DummyArithmeticOpenProtocol(closed, target);
  }

  @Override
  public int getMaxBitLength() {
    return maxBitLength;
  }

  @Override
  public SInt getSqrtOfMaxValue() {
    BigInteger two = BigInteger.valueOf(2);
    BigInteger max = mod.subtract(BigInteger.ONE).divide(two);
    int bitlength = max.bitLength();
    BigInteger approxMaxSqrt = two.pow(bitlength / 2);
    return getSInt(approxMaxSqrt);
  }

  @Override
  public BigInteger getModulus() {
    return mod;
  }

}
