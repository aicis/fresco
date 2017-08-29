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
package dk.alexandra.fresco.lib.math.bool.mult;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a Binary Multiplication protocol by doing the school method.
 * This means that we connect O(n^2) 1-Bit-FullAdders in order to get the result.
 * As one would imagine, this is not the most efficient method, but it works as a basic case.
 *
 * @author Kasper Damgaard
 */
public class BinaryMultProtocol implements
    ComputationBuilder<List<Computation<SBool>>, ProtocolBuilderBinary> {

  private List<Computation<SBool>> lefts, rights;

  public BinaryMultProtocol(List<Computation<SBool>> lefts,
      List<Computation<SBool>> rights) {
    this.lefts = lefts;
    this.rights = rights;
  }

  @Override
  public Computation<List<Computation<SBool>>> buildComputation(ProtocolBuilderBinary builder) {
    return builder.seq(seq -> {
      int idx = this.lefts.size() - 1;
      List<Computation<SBool>> res = new ArrayList<>();
      for (Computation<SBool> right : rights) {
        res.add(seq.binary().and(lefts.get(idx), right));
      }
      IterationState is = new IterationState(idx, () -> res);
      return is;
    }).whileLoop(
        (state) -> state.round >= 1,
        (seq, state) -> {
          int idx = state.round - 1;

          List<Computation<SBool>> res = new ArrayList<>();
          for (Computation<SBool> right : rights) {
            res.add(seq.binary().and(lefts.get(idx), right));
          }
          for (int i = state.round; i < this.lefts.size(); i++) {
            res.add(seq.binary().known(false));
          }
          List<Computation<SBool>> tmp = state.value.out();
          while (tmp.size() < res.size()) {
            tmp.add(0, seq.binary().known(false));
          }
          IterationState is = new IterationState(idx,
              seq.advancedBinary().fullAdder(state.value.out(), res, seq.binary().known(false)));
          return is;
        }
    ).seq((seq, state) -> state.value
    );
  }

  private static final class IterationState implements Computation<IterationState> {

    private int round;
    private final Computation<List<Computation<SBool>>> value;

    private IterationState(int round,
        Computation<List<Computation<SBool>>> value) {
      this.round = round;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }

}
