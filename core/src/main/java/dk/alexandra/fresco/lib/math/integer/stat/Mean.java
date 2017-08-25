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
package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.SumSIntList;
import java.math.BigInteger;
import java.util.List;

/**
 * This protocol calculates the arithmetic mean of a data set.
 */
public class Mean implements ComputationBuilder<SInt, ProtocolBuilderNumeric> {

  private final List<Computation<SInt>> data;
  private final int degreesOfFreedom;

  public Mean(List<Computation<SInt>> data) {
    this(data, data.size());
  }

  public Mean(List<Computation<SInt>> data, int degreesOfFreedom) {
    this.data = data;
    this.degreesOfFreedom = degreesOfFreedom;
  }

  @Override
  public Computation<SInt> build(ProtocolBuilderNumeric builder) {
    return builder.seq((seq) ->
        () -> this.data
    ).seq((list, seq) ->
        new SumSIntList(list).build(seq)
    ).seq((sum, seq) -> {
      BigInteger n = BigInteger.valueOf(this.degreesOfFreedom);
      return seq.advancedNumeric().div(() -> sum, n);
    });
  }

}
