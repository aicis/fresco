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
 *******************************************************************************/
package dk.alexandra.fresco.lib.collections;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.conditional.ConditionalSelect;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a lookup protocol using a linear number of equality protocols. A
 * lookup protocol is essentially a combination of a search and a conditional
 * select protocol. This does the search by simply comparing the lookup key to
 * all the keys in the list.
 * <p>
 * Guaranteed return value is the last value where the corresponding key matches
 * </p>
 */
public class LinearLookUp implements ComputationBuilder<SInt> {

  private final Computation<SInt> lookUpKey;
  private final ArrayList<Computation<SInt>> keys;
  private final ArrayList<Computation<SInt>> values;
  private final int notFoundValue;

  /**
   * Makes a new LinearLookUp
   *
   * @param lookUpKey the key to look up.
   * @param keys the list of keys to search among.
   * @param values the values corresponding to each key.
   * @param notFoundValue The value to return if not present.
   */
  public LinearLookUp(Computation<SInt> lookUpKey,
      ArrayList<Computation<SInt>> keys,
      ArrayList<Computation<SInt>> values,
      int notFoundValue) {
    this.notFoundValue = notFoundValue;
    this.lookUpKey = lookUpKey;
    this.keys = keys;
    this.values = values;
  }

  @Override
  public Computation<SInt> build(SequentialNumericBuilder builder) {
    return builder.par((par) -> {
      int n = keys.size();
      List<Computation<SInt>> index = new ArrayList<>(n);
      for (Computation<SInt> key : keys) {
        index.add(par.comparison().equals(lookUpKey, key));
      }
      return () -> index;
    }).seq((index, seq) -> {
      Computation<SInt> outputValue = seq.numeric().known(BigInteger.valueOf(notFoundValue));
      for (int i = 0, valuesLength = values.size(); i < valuesLength; i++) {
        Computation<SInt> value = values.get(i);
        outputValue = seq.seq(new ConditionalSelect(index.get(i), value, outputValue));
      }
      return outputValue;
    });
  }
}