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

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;

public class RepeatedRightShiftProtocolImpl implements RepeatedRightShiftProtocol {

  // Input
  private SInt input;
  private int shifts;
  private SInt result;

  // Factories
  private final BasicNumericFactory basicNumericFactory;
  private final RightShiftFactory rightShiftFactory;

  // Variables used for calculation
  private int round = 0;
  private SInt in, out;
  private ProtocolProducer protocolProducer;
  private SInt[] remainders;

  /**
   * @param input The input
   * @param shifts The number fo shifts to do
   * @param result input >> shifts
   */
  public RepeatedRightShiftProtocolImpl(SInt input, int shifts, SInt result,
      BasicNumericFactory basicNumericFactory, RightShiftFactory rightShiftFactory) {

    if (shifts < 1) {
      throw new IllegalArgumentException("n must be positive");
    }

    this.input = input;
    this.shifts = shifts;
    this.result = result;

    this.basicNumericFactory = basicNumericFactory;
    this.rightShiftFactory = rightShiftFactory;
  }

  /**
   * @param input The input
   * @param shifts The number of shifts to do
   * @param result input >> shifts
   * @param remainders The <code>shifts</code> least significant bits of the input, the least
   * significant having index 0.
   */
  public RepeatedRightShiftProtocolImpl(SInt input, int shifts, SInt result, SInt[] remainders,
      BasicNumericFactory basicNumericFactory, RightShiftFactory rightShiftFactory) {

    this(input, shifts, result, basicNumericFactory, rightShiftFactory);
    if (remainders.length != shifts) {
      throw new IllegalArgumentException(
          "Length of array for remainders must match number of performed shifts");
    }
    this.remainders = remainders;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (protocolProducer == null) {
      if (round == 0) {
        /*
         * First round
				 */
        out = basicNumericFactory.getSInt();

        ProtocolProducer rightShift;
        if (remainders != null) {
          rightShift = rightShiftFactory.getRightShiftProtocol(input, out,
              remainders[round]);
        } else {
          rightShift = rightShiftFactory.getRightShiftProtocol(input, out);
        }
        protocolProducer = rightShift;
      } else if (round < shifts - 1) {
        /*
         * Intermediate rounds
				 */
        in = out;
        out = basicNumericFactory.getSInt();
        ProtocolProducer rightShift;
        if (remainders != null) {
          rightShift = rightShiftFactory
              .getRightShiftProtocol(in, out, remainders[round]);
        } else {
          rightShift = rightShiftFactory.getRightShiftProtocol(in, out);
        }
        protocolProducer = rightShift;
      } else {
        /*
         * Last round
				 */
        in = out;
        out = null;
        ProtocolProducer rightShift;
        if (remainders != null) {
          rightShift = rightShiftFactory.getRightShiftProtocol(in, result,
              remainders[round]);
        } else {
          rightShift = rightShiftFactory.getRightShiftProtocol(in, result);
        }
        protocolProducer = rightShift;
      }
    }

    if (protocolProducer.hasNextProtocols()) {
      protocolProducer.getNextProtocols(protocolCollection);
    } else {
      round++;
      protocolProducer = null;
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return round < shifts;
  }

}
