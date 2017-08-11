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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.binary.ComputationBuilderBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.crypto.mimc.MiMCConstants;
import dk.alexandra.fresco.lib.field.bool.generic.GenericBinaryBuilderAdvanced;



/**
 * This class implements a Full Adder protocol for Binary protocols.
 * It takes the naive approach of linking 1-Bit-Full Adders together to implement
 * a generic length adder.
 */
public class FullAdderProtocolImpl implements ComputationBuilderBinary<List<Computation<SBool>>> {

  private List<Computation<SBool>> lefts, rights, outs;
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
    
    
    
    return builder.seq(seq -> {
      int idx = this.lefts.size() -1;
      
      return new IterationState(idx, seq.advancedBinary().oneBitFullAdder(lefts.get(idx), rights.get(idx), inCarry));
    }).whileLoop(
        (state) -> state.round >= 1,
        (state, seq) -> {
          int idx = state.round;
          idx--;

          //Computation<Pair<SBool, SBool>> adder = 
              //new OneBitFullAdderProtocolImpl(lefts.get(idx), rights.get(idx), state.value.out().getSecond());

//          Computation<SInt> updatedValue = seq.advancedNumeric().exp(masked, three);
          System.out.println("null:  "+state.value.out());
          return new IterationState(idx, seq.advancedBinary().oneBitFullAdder(lefts.get(idx), rights.get(idx), state.value.out().getSecond()));
        }
    ).seq((state, seq) ->
        /*
         * We're in the last round so we just mask the current
         * cipher text with the encryption key
         */
      () -> new ArrayList<Computation<SBool>>()
    
    );
  }
  
  private static final class IterationState implements Computation<IterationState> {

    private final int round;
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
  
  
  /*
  @Override
  public Computation<List<Computation<SBool>>> build(SequentialBinaryBuilder builder) {
    return builder.seq(seq -> {
      int idx = this.lefts.size() -1;
      OneBitFullAdderProtocolImpl adder = new OneBitFullAdderProtocolImpl(lefts.get(idx), rights.get(idx), inCarry);
      idx--;
      List<Computation<SBool>> res = new ArrayList<Computation<SBool>>();
      while(idx >= 0) {
        Pair<SBool, SBool> prev = adder.build(seq).out();
        System.out.println("failing . "+prev);
        adder = new OneBitFullAdderProtocolImpl(lefts.get(idx), rights.get(idx), prev.getSecond());
        res.add(0, prev.getFirst());
        idx--;
      }
      Pair<SBool, SBool> prev = adder.build(seq).out();
      res.add(0, prev.getFirst());
      res.add(0, prev.getSecond());
      System.out.println("got a build on full...  returning "+res);
      return () -> res;
    });
  }*/
  
/*
  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (round == 0) {
      if (curPP == null) {
        curPP = FAFactory
            .getOneBitFullAdderProtocol(lefts[stopRound - 1], rights[stopRound - 1], inCarry,
                outs[stopRound - 1], tmpCarry);
      }
    } else if (round < stopRound - 1) {
      if (curPP == null) {
        //TODO: Using tmpCarry both as in and out might not be good for all implementations of a 1Bit FA protocol?
        //But at least it works for OneBitFullAdderprotocolImpl.
        curPP = FAFactory
            .getOneBitFullAdderProtocol(lefts[stopRound - round - 1], rights[stopRound - round - 1],
                tmpCarry, outs[stopRound - round - 1], tmpCarry);
      }
    } else {
      if (curPP == null) {
        curPP = FAFactory
            .getOneBitFullAdderProtocol(lefts[0], rights[0], tmpCarry, outs[0], outCarry);
      }
    }
    if (curPP.hasNextProtocols()) {
      curPP.getNextProtocols(protocolCollection);
    } else {
      round++;
      curPP = null;
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return round < stopRound;
  }
*/


}
