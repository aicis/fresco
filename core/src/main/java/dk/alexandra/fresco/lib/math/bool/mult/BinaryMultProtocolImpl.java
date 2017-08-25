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
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary;
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
public class BinaryMultProtocolImpl implements
    dk.alexandra.fresco.framework.builder.ComputationBuilder<List<Computation<SBool>>, ProtocolBuilderBinary> {

  private List<Computation<SBool>> lefts, rights;

  public BinaryMultProtocolImpl(List<Computation<SBool>> lefts, 
      List<Computation<SBool>> rights) {
    this.lefts = lefts;
    this.rights = rights;
   }

  @Override
  public Computation<List<Computation<SBool>>> build(ProtocolBuilderBinary builder) {
    return builder.seq(seq -> {
      int idx = this.lefts.size() -1;
      List<Computation<SBool>> res = new ArrayList<Computation<SBool>>();
      for(int i = 0; i< rights.size(); i++) {
        res.add(seq.binary().and(lefts.get(idx), rights.get(i)));
      }
      IterationState is = new IterationState(idx, () -> res);// seq.advancedBinary().oneBitFullAdder(lefts.get(idx), rights.get(idx), inCarry));
      return is;
    }).whileLoop(
        (state) -> state.round >= 1,
        (state, seq) -> {
          int idx = state.round -1;
          
          List<Computation<SBool>> res = new ArrayList<Computation<SBool>>();
          for(int i = 0; i< rights.size(); i++) {
            res.add(seq.binary().and(lefts.get(idx), rights.get(i)));
          } 
          for(int i = state.round; i < this.lefts.size(); i++){
            res.add(seq.binary().known(false));
          }
          List<Computation<SBool>> tmp = state.value.out();
          while(tmp.size()< res.size()) {
            tmp.add(0, seq.binary().known(false));
          }
          IterationState is = new IterationState(idx, seq.advancedBinary().fullAdder(state.value.out(), res , seq.binary().known(false)));
          return is;
        }
    ).seq((state, seq) -> state.value
    );
  }
  
  
  /**
   * Round 1: Create a matrix that is the AND of every possible input combination. Round
   * 2-(stopRound-1): Create layers of adders that takes the last layers result as well as the
   * corresponding andMatrix layer and adds it. Round stopRound-1: Do the final layer. Same as the
   * other rounds, except the last carry is outputted.
   */ /*
  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (round == 0) {
      if (curPP == null) {
        curPP = new ParallelProtocolProducer();
        for (int i = 0; i < lefts.length; i++) {
          for (int j = 0; j < rights.length; j++) {
            if (j == rights.length - 1) { //corresponding to having least significant bit steady.
              if (i == lefts.length - 1) {
                ((ParallelProtocolProducer) curPP).append(
                    basicFactory.getAndProtocol(lefts[i], rights[j], outs[outs.length - 1]));
              } else {
                ((ParallelProtocolProducer) curPP).append(basicFactory
                    .getAndProtocol(lefts[i], rights[j],
                        intermediateResults[intermediateResults.length - 2 - i]));
              }
            } else {
              ((ParallelProtocolProducer) curPP).append(basicFactory
                  .getAndProtocol(lefts[i], rights[j],
                      andMatrix[lefts.length - 1 - i][rights.length - 2 - j]));
            }
          }
        }
      }
      getNextFromPp(protocolCollection);

    } else if (round < stopRound - 1) {
      if (curPP == null) {
        ProtocolProducer firstHA = adderFactory
            .getOneBitHalfAdderProtocol(andMatrix[0][round - 1], intermediateResults[0],
                outs[outs.length - 1 - round], carries[0]);
        ProtocolProducer[] FAs = new ProtocolProducer[lefts.length - 1];
        for (int i = 1; i < lefts.length; i++) {
          if (round == 1 && i == lefts.length - 1) {
            //special case where we need a half adder, not a full adder since we do not have a carry from first layer.
            FAs[i - 1] = adderFactory
                .getOneBitHalfAdderProtocol(andMatrix[i][round - 1], carries[i - 1],
                    intermediateResults[i - 1], intermediateResults[i]);
          } else if (i == lefts.length - 1) {
            FAs[i - 1] = adderFactory
                .getOneBitFullAdderProtocol(andMatrix[i][round - 1], intermediateResults[i],
                    carries[i - 1], intermediateResults[i - 1], intermediateResults[i]);
          } else {
            FAs[i - 1] = adderFactory
                .getOneBitFullAdderProtocol(andMatrix[i][round - 1], intermediateResults[i],
                    carries[i - 1], intermediateResults[i - 1], carries[i]);
          }
        }
        SequentialProtocolProducer tmp = new SequentialProtocolProducer(FAs);
        curPP = new SequentialProtocolProducer(firstHA, tmp);
      }
      getNextFromPp(protocolCollection);
    } else {
      if (curPP == null) {
        ProtocolProducer firstHA = adderFactory
            .getOneBitHalfAdderProtocol(andMatrix[0][round - 1], intermediateResults[0],
                outs[outs.length - 1 - round], carries[0]);
        ProtocolProducer[] FAs = new ProtocolProducer[lefts.length - 1];
        for (int i = 1; i < lefts.length; i++) {
          if (i == lefts.length - 1) {
            FAs[i - 1] = adderFactory
                .getOneBitFullAdderProtocol(andMatrix[i][round - 1], intermediateResults[i],
                    carries[i - 1], outs[1], outs[0]);
          } else {
            FAs[i - 1] = adderFactory
                .getOneBitFullAdderProtocol(andMatrix[i][round - 1], intermediateResults[i],
                    carries[i - 1], outs[outs.length - 1 - round - i], carries[i]);
          }
        }
        SequentialProtocolProducer tmp = new SequentialProtocolProducer(FAs);
        curPP = new SequentialProtocolProducer(firstHA, tmp);
      }
      getNextFromPp(protocolCollection);
    }
  }

*/

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
