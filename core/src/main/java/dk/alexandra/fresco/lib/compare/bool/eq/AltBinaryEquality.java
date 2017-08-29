/*******************************************************************************
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
package dk.alexandra.fresco.lib.compare.bool.eq;


import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilderAdvanced;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;

/**
 * Does a simple compare like this: out = (a1 XNOR b1) AND (a2 XNOR b2) AND (a3 XNOR b3) AND ...
 *
 * The XNORs are done in parallel and the ANDs are done by a log-depth tree structured protocol.
 *
 */
public class AltBinaryEquality implements ComputationBuilder<SBool, ProtocolBuilderBinary> {


  private List<Computation<SBool>> inLeft;
  private List<Computation<SBool>> inRight;
  private final int length;

  public AltBinaryEquality(List<Computation<SBool>> inLeft,
      List<Computation<SBool>> inRight) {
    this.inLeft = inLeft;
    this.inRight = inRight;
    if (inLeft.size() != inRight.size()) {
      throw new IllegalArgumentException("Binary strings must be of equal length");
    }
    this.length = inLeft.size();
  }

  @Override
  public Computation<SBool> buildComputation(ProtocolBuilderBinary builder) {
    return builder.par(par -> {
      BinaryBuilderAdvanced bb = par.advancedBinary();
      List<Computation<SBool>> xnors = new ArrayList<>();
      for (int i = 0; i < length; i++) {
        xnors.add(bb.xnor(inLeft.get(i), inRight.get(i)));
      }

      IterationState is = new IterationState(xnors.size(), () -> xnors);
      return is;
    }).whileLoop((state) -> state.round > 1, (seq, state) -> {
      List<Computation<SBool>> input = state.value.out();
      int size = input.size() % 2 + input.size() / 2;

      IterationState is = new IterationState(size, seq.par(par -> {
        List<Computation<SBool>> ands = new ArrayList<>();
        int idx = 0;
        while (idx < input.size() - 1) {
          ands.add(par.binary().and(input.get(idx), input.get(idx + 1)));
          idx += 2;
        }
        if (idx < input.size()) {
          ands.add(input.get(idx));
        }
        return () -> ands;
      }));
      return is;
    }).seq((seq, state) -> {
      return state.value.out().get(0);
    });
  }

  private static final class IterationState implements Computation<IterationState> {

    private int round;
    private final Computation<List<Computation<SBool>>> value;

    private IterationState(int round, Computation<List<Computation<SBool>>> value) {
      this.round = round;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }

}
