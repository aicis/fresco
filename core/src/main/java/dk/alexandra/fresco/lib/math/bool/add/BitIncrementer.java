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
package dk.alexandra.fresco.lib.math.bool.add;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;

/**
 * Increment a binary vector with a secret boolean. The class uses the naive approach of linking
 * 1-Bit-Half Adders together to implement a generic length adder.
 *
 */
public class BitIncrementer
    implements Computation<List<DRes<SBool>>, ProtocolBuilderBinary> {

  private List<DRes<SBool>> base;
  private DRes<SBool> increment;

  public BitIncrementer(List<DRes<SBool>> base, DRes<SBool> increment) {
    this.base = base;
    this.increment = increment;
  }


  @Override
  public DRes<List<DRes<SBool>>> buildComputation(ProtocolBuilderBinary builder) {

    List<DRes<SBool>> result = new ArrayList<DRes<SBool>>();

    return builder.seq(seq -> {
      int idx = base.size() - 1;
      IterationState is =
          new IterationState(idx, seq.advancedBinary().oneBitHalfAdder(base.get(idx), increment));
      return is;
    }).whileLoop((state) -> state.round >= 1, (seq, state) -> {
      int idx = state.round - 1;

      result.add(0, state.value.out().getFirst());
      IterationState is = new IterationState(idx,
          seq.advancedBinary().oneBitHalfAdder(base.get(idx), state.value.out().getSecond()));
      return is;
    }).seq((seq, state) -> {
      result.add(0, state.value.out().getFirst());
      result.add(0, state.value.out().getSecond());
      return () -> result;
    });
  }

  private static final class IterationState implements DRes<IterationState> {

    private int round;
    private final DRes<Pair<SBool, SBool>> value;

    private IterationState(int round, DRes<Pair<SBool, SBool>> value) {
      this.round = round;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }
}
