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
package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KeyedCompareAndSwap implements
    ComputationBuilder<List<Pair<List<Computation<SBool>>, List<Computation<SBool>>>>, ProtocolBuilderBinary> {

  private List<Computation<SBool>> leftKey, leftValue, rightKey, rightValue;
  private List<Computation<SBool>> xorKey, xorValue;

  /**
   * Constructs a protocol producer for the keyed compare and swap protocol. This protocol will
   * compare the keys of two key-value pairs and produce a list of pairs so that the first pair has
   * the largest key.
   * 
   * @param leftKey the key of the left pair
   * @param leftValue the value of the left pair
   * @param rightKey the key of the right pair
   * @param rightValue the value of the right pair
   */
  public KeyedCompareAndSwap(
      Pair<List<Computation<SBool>>, List<Computation<SBool>>> leftKeyAndValue,
      Pair<List<Computation<SBool>>, List<Computation<SBool>>> rightKeyAndValue) {
    this.leftKey = leftKeyAndValue.getFirst();
    this.leftValue = leftKeyAndValue.getSecond();
    this.rightKey = rightKeyAndValue.getFirst();
    this.rightValue = rightKeyAndValue.getSecond();
  }

  @Override
  public Computation<List<Pair<List<Computation<SBool>>, List<Computation<SBool>>>>> buildComputation(
      ProtocolBuilderBinary builder) {
    return builder.par(seq -> {

      Computation<SBool> comparison = seq.comparison().greaterThan(leftKey, rightKey);
      xorKey = leftKey.stream().map(e -> seq.binary().xor(e, rightKey.get(leftKey.indexOf(e))))
          .collect(Collectors.toList());

      xorValue =
          leftValue.stream().map(e -> seq.binary().xor(e, rightValue.get(leftValue.indexOf(e))))
              .collect(Collectors.toList());
      return () -> comparison;
    }).par((par, data) -> {

      List<Computation<SBool>> firstValue = leftValue.stream()
          .map(e -> par.advancedBinary().condSelect(data, e, rightValue.get(leftValue.indexOf(e))))
          .collect(Collectors.toList());

      List<Computation<SBool>> firstKey = leftKey.stream()
          .map(e -> par.advancedBinary().condSelect(data, e, rightKey.get(leftKey.indexOf(e))))
          .collect(Collectors.toList());

      return () -> new Pair<>(firstKey, firstValue);
    }).par((par, data) -> {
      List<Computation<SBool>> lastValue =
          xorValue.stream().map(e -> par.binary().xor(e, data.getSecond().get(xorValue.indexOf(e))))
              .collect(Collectors.toList());

      List<Computation<SBool>> lastKey =
          xorKey.stream().map(e -> par.binary().xor(e, data.getFirst().get(xorKey.indexOf(e))))
              .collect(Collectors.toList());

      List<Pair<List<Computation<SBool>>, List<Computation<SBool>>>> result = new ArrayList<>();
      result.add(data);
      result.add(new Pair<>(lastKey, lastValue));

      return () -> result;
    });
  }
}
