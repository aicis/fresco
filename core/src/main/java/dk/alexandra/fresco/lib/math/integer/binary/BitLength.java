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
package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import java.math.BigInteger;

public class BitLength implements ComputationBuilder<SInt, ProtocolBuilderNumeric> {

  private Computation<SInt> input;
  private int maxBitLength;

  /**
   * Create a protocol for finding the bit length of an integer. This is done
   * by finding the bit representation of the integer and then returning the
   * index of the highest set bit.
   *
   * @param input An integer.
   * @param maxBitLength An upper bound for the bit length.
   */
  public BitLength(Computation<SInt> input, int maxBitLength) {
    this.input = input;
    this.maxBitLength = maxBitLength;

  }

  @Override
  public Computation<SInt> build(ProtocolBuilderNumeric builder) {
    return builder.seq((seq) -> {
    /*
     * Find the bit representation of the input.
		 */
      return seq.advancedNumeric()
          .rightShiftWithRemainder(input, maxBitLength);
    }).seq((rightShiftResult, seq) -> {
      Computation<SInt> mostSignificantBitIndex = null;
      NumericBuilder numeric = seq.numeric();
      for (int n = 0; n < maxBitLength; n++) {
      /*
       * If bits[n] == 1 we let mostSignificantIndex be current index.
			 * Otherwise we leave it be.
			 */
        SInt remainderResult = rightShiftResult.getRemainder().get(n);
        if (mostSignificantBitIndex == null) {
          mostSignificantBitIndex = numeric.mult(BigInteger.valueOf(n), () -> remainderResult);
        } else {
          Computation<SInt> sub = numeric.sub(BigInteger.valueOf(n), mostSignificantBitIndex);
          Computation<SInt> mult = numeric.mult(() -> remainderResult, sub);
          mostSignificantBitIndex = numeric.add(mult, mostSignificantBitIndex);
        }
      }
    /*
     * We are interested in the bit length of the input, so we add one to
		 * the index of the most significant bit since the indices are counted
		 * from 0.
		 */
      return numeric.add(BigInteger.ONE, mostSignificantBitIndex);
    });
  }
}
