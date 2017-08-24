/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.conditional;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ComputationBuilderParallel;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.ParallelNumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;

public class ConditionalSwap
    implements ComputationBuilderParallel<Pair<Computation<SInt>, Computation<SInt>>> {

  final private Computation<SInt> a, b, swapper;

  /**
   * Swaps a and b based on swapper. Swapper must be 0 or 1.
   * 
   * If swapper is 1 the values are swapped (in place). Otherwise, original order.
   * 
   * @param swapper
   * @param a
   * @param b
   */
  public ConditionalSwap(Computation<SInt> swapper, Computation<SInt> a, Computation<SInt> b) {
    this.swapper = swapper;
    this.a = a;
    this.b = b;
  }

  public ConditionalSwap(Computation<SInt> swapper,
      Pair<Computation<SInt>, Computation<SInt>> pair) {
    this.swapper = swapper;
    this.a = pair.getFirst();
    this.b = pair.getSecond();
  }

  @Override
  public Computation<Pair<Computation<SInt>, Computation<SInt>>> build(
      ParallelNumericBuilder builder) {
    Computation<SInt> updatedA = builder.createSequentialSub(
        new ConditionalSelect(builder.numeric().sub(BigInteger.ONE, swapper), a, b));
    Computation<SInt> updatedB = builder.createSequentialSub(new ConditionalSelect(swapper, a, b));
    return () -> new Pair<>(updatedA, updatedB);
  }
}
