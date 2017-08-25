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
package dk.alexandra.fresco.lib.compare.bool;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.List;

/**
 * Represents a comparison protocol between two bitstrings. Concretely, the protocol computes the
 * 'greater than' relation of strings A and B, i.e., it computes C := A > B.
 * 
 * @author psn
 * 
 */
public class BinaryGreaterThanProtocolImpl implements
    dk.alexandra.fresco.framework.builder.ComputationBuilder<SBool, SequentialBinaryBuilder> {

  private List<Computation<SBool>> inA, inB;
  private int length;

  /**
   * Construct a protocol to compare strings A and B. The bitstrings A and B are assumed to be even
   * length and to be ordered from most- to least significant bit.
   * 
   * @param inA input string A
   * @param inB input string B
   * @param outC a bit to hold the output C := A > B.
   * @param factory a protocol provider
   */
  public BinaryGreaterThanProtocolImpl(List<Computation<SBool>> inA, List<Computation<SBool>> inB) {
    if (inA.size() == inB.size()) {
      this.inA = inA;
      this.inB = inB;
      this.length = inA.size();
    } else {
      throw new IllegalArgumentException("Comparison failed: bitsize differs");
    }
  }

  @Override
  public Computation<SBool> build(SequentialBinaryBuilder builder) {
    return builder.seq(seq -> {
      int round = 0;
      Computation<SBool> xor = seq.binary().xor(inA.get(length - 1), inB.get(length - 1));
      round++;
      Computation<SBool> outC = seq.binary().and(inA.get(length - 1), xor);
      round++;
      for (; round <= length; round++) {
        int i = length - round;
        xor = seq.binary().xor(inA.get(i), inB.get(i));
        Computation<SBool> tmp = seq.binary().xor(inA.get(i), outC);
        tmp = seq.binary().and(tmp, xor);
        outC = seq.binary().xor(outC, tmp);
      }
      return outC;
    });
  }

}
