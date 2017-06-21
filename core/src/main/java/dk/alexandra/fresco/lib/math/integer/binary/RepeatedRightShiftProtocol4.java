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
import dk.alexandra.fresco.framework.RightShiftBuilder.RightShiftResult;
import dk.alexandra.fresco.framework.builder.DelayedComputation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RepeatedRightShiftProtocol4
    implements Function<SequentialProtocolBuilder, Computation<RightShiftResult>> {

  private final int shifts;
  private final boolean calculateRemainders;
  // Input
  private final Computation<SInt> input;
  private final DelayedComputation<RightShiftResult> result = new DelayedComputation<>();


  /**
   * @param input The input.
   * @param calculateRemainders true to also calculate remainder. If false remainders in result will
   * be null.
   */
  public RepeatedRightShiftProtocol4(
      Computation<SInt> input,
      int shifts,
      boolean calculateRemainders) {
    if (shifts < 0) {
      throw new IllegalArgumentException("Number of shifts must be positive");
    }
    this.input = input;
    this.shifts = shifts;
    this.calculateRemainders = calculateRemainders;
  }

  @Override
  public Computation<RightShiftResult> apply(SequentialProtocolBuilder sequential) {
    if (calculateRemainders) {
      doIterationWithRemainder(sequential, input, shifts, new ArrayList<>(shifts));
    } else {
      doIteration(sequential, input, shifts);
    }
    return result;
  }

  private void doIteration(SequentialProtocolBuilder iterationBuilder,
      Computation<SInt> input, int shifts) {
    if (shifts > 0) {
      Computation<SInt> iteration =
          iterationBuilder.createSequentialSubFactoryReturning(
              (builder) -> builder.createRightShiftBuilder().rightShift(input)
          );
      iterationBuilder
          .createSequentialSubFactory((builder) -> doIteration(builder, iteration, shifts - 1));
    } else {
      result.setComputation(() -> new RightShiftResult(input.out(), null));
    }
  }

  private void doIterationWithRemainder(SequentialProtocolBuilder iterationBuilder,
      Computation<SInt> input, int shifts, List<SInt> remainders) {
    if (shifts > 0) {
      Computation<RightShiftResult> iteration =
          iterationBuilder.createSequentialSubFactoryReturning(
              (builder) -> builder.createRightShiftBuilder().rightShiftWithRemainder(input)
          );
      iterationBuilder
          .createSequentialSubFactory((builder) -> {
            RightShiftResult out = iteration.out();
            remainders.add(out.getRemainder().get(0));
            doIterationWithRemainder(builder, out::getResult, shifts - 1, remainders);
          });
    } else {
      result.setComputation(() -> new RightShiftResult(input.out(), remainders));
    }
  }

}