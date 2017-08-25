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
package dk.alexandra.fresco.lib.math.integer.exp;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public class ExponentiationOpenExponent implements
    ComputationBuilder<SInt, ProtocolBuilderNumeric> {

  private Computation<SInt> base;
  private BigInteger exponent;

  public ExponentiationOpenExponent(Computation<SInt> x, BigInteger e) {
    this.base = x;
    this.exponent = e;
  }

  @Override
  public Computation<SInt> build(ProtocolBuilderNumeric builder) {
    if (exponent.equals(BigInteger.ZERO)) {
      return builder.numeric().known(BigInteger.valueOf(1));
    }
    return builder.seq((seq) -> {
      Computation<SInt> accOdd = seq.numeric().known(BigInteger.valueOf(1));
      Computation<SInt> accEven = base;
      return new IterationState(exponent, accEven, accOdd);
    }).whileLoop(
        iterationState -> !iterationState.exponent.equals(BigInteger.ONE),
        (iterationState, seq) -> {
          BigInteger exponent = iterationState.exponent;
          Computation<SInt> accEven = iterationState.accEven;
          Computation<SInt> accOdd = iterationState.accOdd;
          NumericBuilder numeric = seq.numeric();
          if (exponent.getLowestSetBit() == 0) {
            accOdd = numeric.mult(accOdd, accEven);
            accEven = numeric.mult(accEven, accEven);
            exponent = exponent.subtract(BigInteger.ONE).shiftRight(1);
          } else {
            exponent = exponent.shiftRight(1);
            accEven = numeric.mult(accEven, accEven);
          }
          return new IterationState(exponent, accEven, accOdd);
        }
    ).seq((iterationState, seq) ->
        seq.numeric().mult(iterationState.accEven, iterationState.accOdd)
    );
  }

  private static class IterationState implements Computation<IterationState> {

    final BigInteger exponent;
    final Computation<SInt> accEven;
    final Computation<SInt> accOdd;

    private IterationState(BigInteger exponent,
        Computation<SInt> accEven,
        Computation<SInt> accOdd) {
      this.exponent = exponent;
      this.accEven = accEven;
      this.accOdd = accOdd;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }

}
