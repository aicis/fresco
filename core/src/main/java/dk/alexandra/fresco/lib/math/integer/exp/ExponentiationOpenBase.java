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
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public class ExponentiationOpenBase implements ComputationBuilder<SInt> {

  private final BigInteger base;
  private final Computation<SInt> exponent;
  private final int maxExponentBitLength;

  public ExponentiationOpenBase(BigInteger base, Computation<SInt> exponent,
      int maxExponentBitLength) {
    this.base = base;
    this.exponent = exponent;
    this.maxExponentBitLength = maxExponentBitLength;
  }

  @Override
  public Computation<SInt> build(SequentialNumericBuilder builder) {
    return builder.seq((seq) ->
        seq.advancedNumeric().toBits(exponent, maxExponentBitLength)
    ).seq((bits, seq) -> {
      BigInteger e = base;
      NumericBuilder numeric = seq.numeric();
      Computation<SInt> result = null;
      for (SInt bit : bits) {
        /*
         * result += bits[i] * (result * r - r) + r
				 *
				 *  aka.
				 *
				 *            result       if bits[i] = 0
				 * result = {
				 *            result * e   if bits[i] = 1
				 */
        if (result == null) {
          BigInteger sub = e.subtract(BigInteger.ONE);
          result = numeric.add(BigInteger.ONE, numeric.mult(sub, () -> bit));
        } else {
          Computation<SInt> sub = numeric.sub(numeric.mult(e, result), result);
          result = numeric.add(result, numeric.mult(sub, () -> bit));
        }
        e = e.multiply(e);
      }
      return result;
    });
  }
}
