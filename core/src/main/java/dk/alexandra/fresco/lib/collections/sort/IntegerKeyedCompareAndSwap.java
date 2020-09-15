package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IntegerKeyedCompareAndSwap implements
    ComputationParallel<List<Pair<DRes<SInt>, List<DRes<SInt>>>>, ProtocolBuilderNumeric> {

  private DRes<SInt> leftKey, rightKey;
  private List<DRes<SInt>> leftValue, rightValue;
  private DRes<SInt> additiveKey;
  private List<DRes<SInt>> additiveValue;

  /**
   * Constructs a protocol producer for the keyed compare and swap protocol. This protocol will
   * compare the keys of two key-value pairs and produce a list of pairs so that the first pair has
   * the largest key.
   *
   * @param leftKeyAndValue the key-value of the left pair
   * @param rightKeyAndValue the key-value of the right pair
   */
  public IntegerKeyedCompareAndSwap(
      Pair<DRes<SInt>, List<DRes<SInt>>> leftKeyAndValue,
      Pair<DRes<SInt>, List<DRes<SInt>>> rightKeyAndValue) {
    this.leftKey = leftKeyAndValue.getFirst();
    this.leftValue = leftKeyAndValue.getSecond();
    this.rightKey = rightKeyAndValue.getFirst();
    this.rightValue = rightKeyAndValue.getSecond();
  }

  @Override
  public DRes<List<Pair<DRes<SInt>, List<DRes<SInt>>>>> buildComputation(
      ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      // Left and right key are switched since we want the largest value in the first position of the result
      DRes<SInt> comparison = par.comparison().compareLT(rightKey, leftKey);

      additiveKey = par.numeric().add(leftKey, rightKey);
      additiveValue = leftValue.stream().map(e -> par.numeric().add(e, rightValue.get(
          leftValue.indexOf(e)))).collect(Collectors.toList());
      return () -> comparison;
    }).par((par, data) -> {
      DRes<SInt> firstKey = par.advancedNumeric().condSelect(data, leftKey, rightKey);
      List<DRes<SInt>> firstValue = leftValue.stream().map(e ->
          par.advancedNumeric().condSelect(data, e, rightValue.get(
          leftValue.indexOf(e)))).collect(Collectors.toList());

      return () -> new Pair<>(firstKey, firstValue);
    }).par((par, data) -> {
      DRes<SInt> lastKey = par.numeric().sub(additiveKey, data.getFirst());
      List<DRes<SInt>> lastValue = additiveValue.stream().map(e ->
          par.numeric().sub(e, data.getSecond().get(additiveValue.indexOf(e))))
          .collect(Collectors.toList());

      List<Pair<DRes<SInt>, List<DRes<SInt>>>> result = new ArrayList<>();
      result.add(data);
      result.add(new Pair<>(lastKey, lastValue));

      return () -> result;
    });
  }
}
