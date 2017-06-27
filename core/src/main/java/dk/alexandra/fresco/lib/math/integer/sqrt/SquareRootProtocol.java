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
package dk.alexandra.fresco.lib.math.integer.sqrt;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.FrescoFunction;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * This class implements a protocol for approximating the square root of a
 * secret shared integer using the <a href=
 * "https://en.wikipedia.org/wiki/Methods_of_computing_square_roots#Babylonian_method"
 * >Babylonian Method</a>.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class SquareRootProtocol implements FrescoFunction<SInt> {

  // Input
  private final Computation<SInt> input;
  private final int maxInputLength;

  public SquareRootProtocol(Computation<SInt> input, int maxInputLength) {
    this.input = input;
    this.maxInputLength = maxInputLength;
  }


  @Override
  public Computation<SInt> apply(SequentialProtocolBuilder builder) {
    /*
     * Convergence is quadratic (the number of correct digits rougly doubles
		 * on each iteration) so assuming we have at least one digit correct
		 * after first iteration, we need at about log2(maxInputLength)
		 * iterations in total.
		 */
    int iterations = log2(maxInputLength) + 1;

		/*
     * First guess is x << maxInputLength / 2 + 1. We add 1 to avoid the
		 * this to be equal to zero since we divide by it later.
		 */
    return builder.seq((seq) -> {
      Computation<SInt> shifted = seq.createRightShiftBuilder()
          .rightShift(input, maxInputLength / 2);
      Computation<SInt> addedOne = seq.numeric()
          .add(seq.getOIntFactory().getOInt(BigInteger.ONE), shifted);
      return new IterationState(1, addedOne);
      /*
      * We iterate y[n+1] = (y[n] + x / y[n]) / 2.
		  */
    }).whileLoop(
        (iterationState) ->
            iterationState.iteration < iterations,
        (iterationState, seq) -> {
          Computation<SInt> value = iterationState.value;
          AdvancedNumericBuilder advancedNumeric = seq.createAdvancedNumericBuilder();

          Computation<SInt> quotient = advancedNumeric.div(
              input, value
          );
          Computation<SInt> sum = seq.numeric().add(value, quotient);
          Computation<SInt> updatedValue = seq.createRightShiftBuilder().rightShift(sum);
          return new IterationState(iterationState.iteration + 1, updatedValue);
        }
    ).seq((iterationState, seq) ->
        iterationState.value
    );
  }

  /**
   * Calculate the base-2 logarithm of <i>x</i>, <i>log<sub>2</sub>(x)</i>.
   */
  private static int log2(int x) {
    return (int) (Math.log(x) / Math.log(2));
  }

  private static final class IterationState implements Computation<IterationState> {

    private final int iteration;
    private final Computation<SInt> value;

    private IterationState(int iteration,
        Computation<SInt> value) {
      this.iteration = iteration;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }
}
