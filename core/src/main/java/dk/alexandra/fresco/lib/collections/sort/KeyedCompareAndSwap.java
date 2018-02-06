package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KeyedCompareAndSwap implements
    Computation<List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>>, ProtocolBuilderBinary> {

  private List<DRes<SBool>> leftKey, leftValue, rightKey, rightValue;
  private List<DRes<SBool>> xorKey, xorValue;

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
      Pair<List<DRes<SBool>>, List<DRes<SBool>>> leftKeyAndValue,
      Pair<List<DRes<SBool>>, List<DRes<SBool>>> rightKeyAndValue) {
    this.leftKey = leftKeyAndValue.getFirst();
    this.leftValue = leftKeyAndValue.getSecond();
    this.rightKey = rightKeyAndValue.getFirst();
    this.rightValue = rightKeyAndValue.getSecond();
  }

  @Override
  public DRes<List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>>> buildComputation(
      ProtocolBuilderBinary builder) {
    return builder.par(seq -> {

      DRes<SBool> comparison = seq.comparison().greaterThan(leftKey, rightKey);
      xorKey = leftKey.stream().map(e -> seq.binary().xor(e, rightKey.get(leftKey.indexOf(e))))
          .collect(Collectors.toList());

      xorValue =
          leftValue.stream().map(e -> seq.binary().xor(e, rightValue.get(leftValue.indexOf(e))))
              .collect(Collectors.toList());
      return () -> comparison;
    }).par((par, data) -> {

      List<DRes<SBool>> firstValue = leftValue.stream()
          .map(e -> par.advancedBinary().condSelect(data, e, rightValue.get(leftValue.indexOf(e))))
          .collect(Collectors.toList());

      List<DRes<SBool>> firstKey = leftKey.stream()
          .map(e -> par.advancedBinary().condSelect(data, e, rightKey.get(leftKey.indexOf(e))))
          .collect(Collectors.toList());

      return () -> new Pair<>(firstKey, firstValue);
    }).par((par, data) -> {
      List<DRes<SBool>> lastValue =
          xorValue.stream().map(e -> par.binary().xor(e, data.getSecond().get(xorValue.indexOf(e))))
              .collect(Collectors.toList());

      List<DRes<SBool>> lastKey =
          xorKey.stream().map(e -> par.binary().xor(e, data.getFirst().get(xorKey.indexOf(e))))
              .collect(Collectors.toList());

      List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> result = new ArrayList<>();
      result.add(data);
      result.add(new Pair<>(lastKey, lastValue));

      return () -> result;
    });
  }
}
