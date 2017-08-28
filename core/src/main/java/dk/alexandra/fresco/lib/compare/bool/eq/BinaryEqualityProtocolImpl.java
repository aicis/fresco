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
package dk.alexandra.fresco.lib.compare.bool.eq;

import dk.alexandra.fresco.framework.Computation;
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
 * @author Kasper Damgaard
 */
public class BinaryEqualityProtocolImpl implements
    dk.alexandra.fresco.framework.builder.ComputationBuilder<SBool, ProtocolBuilderBinary> {

  private List<Computation<SBool>> inLeft;
  private List<Computation<SBool>> inRight;
  private final int length;

  public BinaryEqualityProtocolImpl(List<Computation<SBool>> inLeft,
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
      return () -> xnors;
    }).seq((seq, xnors) -> {
      // xnors are now a bitstring where a 0 represents that something differed between the inputs
      // Can now do an AND row to determine if the entire bitstring is 1's (inputs are equal) or
      // not.
      // TODO: One can obtain better results using a tree structure and doing those in parallel.
      Computation<SBool> xnorsAnd;
      xnorsAnd = seq.binary().and(xnors.get(0), xnors.get(1));
      int i = 2;
      while (i < length) {
        xnorsAnd = seq.binary().and(xnorsAnd, xnors.get(i));
        i++;
      }
      return xnorsAnd;
    });
  }
}
