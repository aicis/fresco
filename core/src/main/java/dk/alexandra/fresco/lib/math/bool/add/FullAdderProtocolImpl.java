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
package dk.alexandra.fresco.lib.math.bool.add;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;



/**
 * This class implements a Full Adder protocol for Binary protocols.
 * It takes the naive approach of linking 1-Bit-Full Adders together to implement
 * a generic length adder.
 */
public class FullAdderProtocolImpl implements
    dk.alexandra.fresco.framework.builder.ComputationBuilder<List<Computation<SBool>>, SequentialBinaryBuilder> {

  private List<Computation<SBool>> lefts, rights;
  private Computation<SBool> inCarry;

  public FullAdderProtocolImpl(List<Computation<SBool>> lefts, 
      List<Computation<SBool>> rights, 
      Computation<SBool> inCarry) {
    
    if (lefts.size() != rights.size()) {
      throw new IllegalArgumentException("input lists for Full Adder must be of same length.");
    }
    this.lefts = lefts;
    this.rights = rights;
    this.inCarry = inCarry;
  }
  
  
  @Override
  public Computation<List<Computation<SBool>>> build(SequentialBinaryBuilder builder) {

    List<Computation<SBool>> result = new ArrayList<Computation<SBool>>(); 
    
    return builder.seq(seq -> {
      int idx = this.lefts.size() -1;
      IterationState is = new IterationState(idx, seq.advancedBinary().oneBitFullAdder(lefts.get(idx), rights.get(idx), inCarry));
      return is;
    }).whileLoop(
        (state) -> state.round >= 1,
        (state, seq) -> {
          int idx = state.round -1;
          
          result.add(0, state.value.out().getFirst());
          IterationState is = new IterationState(idx, seq.advancedBinary().oneBitFullAdder(lefts.get(idx), rights.get(idx), state.value.out().getSecond()));
          return is;
        }
    ).seq((state, seq) -> {
      result.add(0, state.value.out().getFirst());
      result.add(0, state.value.out().getSecond());
      return () -> result;
      }
    );
  }
  
  private static final class IterationState implements Computation<IterationState> {

    private int round;
    private final Computation<Pair<SBool, SBool>> value;

    private IterationState(int round,
        Computation<Pair<SBool, SBool>> value) {
      this.round = round;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }
}
