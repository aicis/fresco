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
package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.function.Function;

class BitLengthProtocol4 implements Function<SequentialProtocolBuilder, Computation<SInt>> {

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
  BitLengthProtocol4(Computation<SInt> input, int maxBitLength) {
    this.input = input;
    this.maxBitLength = maxBitLength;

  }

  @Override
  public Computation<SInt> apply(SequentialProtocolBuilder seq) {
    return seq.seq((builder) -> {
    /*
     * Find the bit representation of the input.
		 */
      return seq.createRightShiftBuilder()
          .rightShiftWithRemainder(input, maxBitLength);
    }).seq((rightShiftResult, builder) -> {
      Computation<SInt> mostSignificantBitIndex = seq.getSIntFactory().getSInt(0);
      for (int n = 0; n < maxBitLength; n++) {
        SInt currentIndex = seq.getSIntFactory().getSInt(n);
      /*
       * If bits[n] == 1 we let mostSignificantIndex be current index.
			 * Otherwise we leave it be.
			 */
        mostSignificantBitIndex =
            seq.numeric().add(
                seq.numeric().mult(
                    rightShiftResult.getRemainder().get(n),
                    seq.numeric().sub(currentIndex, mostSignificantBitIndex)
                ),
                mostSignificantBitIndex);
      }
    /*
     * We are interested in the bit length of the input, so we add one to
		 * the index of the most significant bit since the indices are counted
		 * from 0.
		 */
      return builder.numeric()
          .add(seq.getOIntFactory().getOInt(BigInteger.ONE), mostSignificantBitIndex);
    });
  }
}
