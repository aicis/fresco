package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

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
      additiveValue = new ArrayList<DRes<SInt>>();
      for (int i = 0; i < leftValue.size(); i++) {
        additiveValue.add(par.numeric().add(leftValue.get(i), rightValue.get(i)));
      }
      return () -> comparison;
    }).par((par, data) -> {
      DRes<SInt> firstKey = par.advancedNumeric().condSelect(data, leftKey, rightKey);
      List<DRes<SInt>> firstValue = new ArrayList<DRes<SInt>>();
      for (int i = 0; i < leftValue.size(); i++) {
        firstValue.add(par.advancedNumeric().condSelect(data, leftValue.get(i), rightValue.get(i)));
      }
      return () -> new Pair<>(firstKey, firstValue);
    }).par((par, data) -> {
      DRes<SInt> lastKey = par.numeric().sub(additiveKey, data.getFirst());
      List<DRes<SInt>> lastValue = new ArrayList<DRes<SInt>>();
      for (int i = 0; i < additiveValue.size(); i++) {
        lastValue.add(par.numeric().sub(additiveValue.get(i), data.getSecond().get(i)));
      }

      List<Pair<DRes<SInt>, List<DRes<SInt>>>> result = new ArrayList<>();
      result.add(data);
      result.add(new Pair<>(lastKey, lastValue));

      return () -> result;
    });
  }
}
