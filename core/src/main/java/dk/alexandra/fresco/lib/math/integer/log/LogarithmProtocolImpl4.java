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
package dk.alexandra.fresco.lib.math.integer.log;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.function.Function;

/**
 * This class implements a protocol for finding the natural logarithm of a
 * secret shared integer. It is based on approximating the logarithm of base 2
 * using the bitlength of a number and then scaling it to the natural logarithm.
 *
 * Since the bitlength of a number is only an approximation of the logarithm of
 * base 2, this protocol is not nessecarily correct on the least significant
 * bit.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class LogarithmProtocolImpl4
    implements Function<SequentialProtocolBuilder, Computation<SInt>> {

  // Input
  private Computation<SInt> input;
  private int maxInputLength;


  public LogarithmProtocolImpl4(Computation<SInt> input, int maxInputLength) {
    this.input = input;
    this.maxInputLength = maxInputLength;
  }


  @Override
  public Computation<SInt> apply(SequentialProtocolBuilder builder) {
    /*
     * ln(2) = 45426 >> 16;
		 */
    OIntFactory intFactory = builder.getOIntFactory();
    OInt ln2 = intFactory.getOInt(BigInteger.valueOf(45426));
    int shifts = 16;

		/*
     * Find the bit length of the input. Note that bit length - 1 is the
		 * floor of the the logartihm with base 2 of the input.
		 */
    Computation<SInt> bitLength =
        builder.createBitLengthBuilder().bitLength(input, maxInputLength);
    Computation<SInt> log2 =
        builder.numeric().sub(bitLength, intFactory.getOInt(BigInteger.ONE));

		/*
		 * ln(x) = log_2(x) * ln(2), and we use 45426 >> 16 as an approximation of ln(2).
		 */
    Computation<SInt> scaledLog = builder.numeric().mult(ln2, log2);
    return builder.createRightShiftBuilder()
        .rightShift(scaledLog, shifts);
  }
}
