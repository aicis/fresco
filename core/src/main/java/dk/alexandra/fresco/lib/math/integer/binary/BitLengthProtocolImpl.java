/*******************************************************************************
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

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import java.math.BigInteger;

public class BitLengthProtocolImpl extends SimpleProtocolProducer implements ProtocolProducer {

  private SInt input;
  private SInt result;
  private int maxBitLength;

  private final BasicNumericFactory basicNumericFactory;
  private final IntegerToBitsFactory integerToBitsFactory;

  /**
   * Create a protocol for finding the bit length of an integer. This is done
   * by finding the bit representation of the integer and then returning the
   * index of the highest set bit.
   *
   * @param input An integer.
   * @param result The bit length of the input.
   * @param maxBitLength An upper bound for the bit length.
   */
  public BitLengthProtocolImpl(SInt input, SInt result, int maxBitLength,
      BasicNumericFactory basicNumericFactory, IntegerToBitsFactory integerToBitsFactory) {

    this.input = input;
    this.result = result;
    this.maxBitLength = maxBitLength;

    this.basicNumericFactory = basicNumericFactory;
    this.integerToBitsFactory = integerToBitsFactory;
  }

  @Override
  protected ProtocolProducer initializeProtocolProducer() {
    NumericProtocolBuilder builder = new NumericProtocolBuilder(basicNumericFactory);

		/*
     * Find the bit representation of the inpyt.
		 */
    SInt[] bits = builder.getSIntArray(maxBitLength);
    builder.addProtocolProducer(integerToBitsFactory.getIntegerToBitsCircuit(input, maxBitLength,
        bits));

    SInt mostSignificantBitIndex = builder.getSInt(0);
    for (int n = 0; n < maxBitLength; n++) {
      SInt currentIndex = builder.getSInt(n);
      /*
       * If bits[n] == 1 we let mostSignificantIndex be current index.
			 * Otherwise we leave it be.
			 */
      mostSignificantBitIndex = builder.add(
          builder.mult(bits[n], builder.sub(currentIndex, mostSignificantBitIndex)),
          mostSignificantBitIndex);
    }

		/*
		 * We are interested in the bit length of the input, so we add one to
		 * the index of the most significant bit since the indices are counted
		 * from 0.
		 */
    SInt bitLength = builder.add(mostSignificantBitIndex, BigInteger.ONE);
    builder.copy(result, bitLength);
    return builder.getProtocol();
  }

}
