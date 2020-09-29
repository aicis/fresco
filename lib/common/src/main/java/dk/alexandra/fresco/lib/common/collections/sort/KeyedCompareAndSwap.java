package dk.alexandra.fresco.lib.common.collections.sort;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderImpl;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.BinaryComparison;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedBinary;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import java.util.ArrayList;
import java.util.List;

public class KeyedCompareAndSwap<KeyT, ValueT,
    ConditionT, BuilderT extends ProtocolBuilderImpl<BuilderT>> implements
    ComputationParallel<List<Pair<KeyT, List<ValueT>>>, BuilderT> {

  private final Compare<KeyT, ConditionT, BuilderT> comparison;
  private final ConditionalSelect<ConditionT, KeyT, BuilderT> condSelectKey;
  private final ConditionalSelect<ConditionT, List<ValueT>, BuilderT> condSelectValue;
  private final Addition<KeyT, BuilderT> additionKey;
  private final Addition<List<ValueT>, BuilderT> additionValue;
  private final Addition<KeyT, BuilderT> subtractionKey;
  private final Addition<List<ValueT>, BuilderT> subtractionValue;
  private final KeyT leftKey;
  private final KeyT rightKey;
  private final List<ValueT> leftValue;
  private final List<ValueT> rightValue;
  private KeyT xorKey;
  private List<ValueT> xorValue;

  private KeyedCompareAndSwap(
      Pair<KeyT, List<ValueT>> leftKeyAndValue,
      Pair<KeyT, List<ValueT>> rightKeyAndValue,
      Compare<KeyT, ConditionT, BuilderT> comparison,
      Addition<KeyT, BuilderT> additionKey,
      Addition<KeyT, BuilderT> subtractionKey,
      Addition<ValueT, BuilderT> additionValue,
      Addition<ValueT, BuilderT> subtractionValue,
      ConditionalSelect<ConditionT, KeyT, BuilderT> condSelectKey,
      ConditionalSelect<ConditionT, ValueT, BuilderT> condSelectValue) {
    this.leftKey = leftKeyAndValue.getFirst();
    this.leftValue = leftKeyAndValue.getSecond();
    this.rightKey = rightKeyAndValue.getFirst();
    this.rightValue = rightKeyAndValue.getSecond();

    this.comparison = comparison;
    this.additionKey = additionKey;
    this.subtractionKey = subtractionKey;
    this.additionValue = Addition.forLists(additionValue);
    this.subtractionValue = Addition.forLists(subtractionValue);
    this.condSelectKey = condSelectKey;
    this.condSelectValue = ConditionalSelect.forLists(condSelectValue);
  }

  /**
   * Constructs a protocol producer for the keyed compare and swap protocol. This protocol will
   * compare the keys of two key-value pairs and produce a list of pairs so that the first pair has
   * the largest key.
   *
   * @param leftKeyAndValue  the key and value of the left pair
   * @param rightKeyAndValue the key and value of the right pair
   */
  public static KeyedCompareAndSwap<List<DRes<SBool>>,
      DRes<SBool>, DRes<SBool>, ProtocolBuilderBinary> binary(
      Pair<List<DRes<SBool>>, List<DRes<SBool>>> leftKeyAndValue,
      Pair<List<DRes<SBool>>, List<DRes<SBool>>> rightKeyAndValue) {

    return new KeyedCompareAndSwap<List<DRes<SBool>>,
        DRes<SBool>, DRes<SBool>, ProtocolBuilderBinary>(leftKeyAndValue, rightKeyAndValue,
        (a, b, builder) -> BinaryComparison
            .using(builder).greaterThan(a, b),
        Addition
            .forLists((ai, bi, builder) -> builder.binary().xor(ai, bi)),
        Addition
            .forLists((ai, bi, builder) -> builder.binary().xor(ai, bi)),
        (ai, bi, builder) -> builder.binary().xor(ai, bi),
        (ai, bi, builder) -> builder.binary().xor(ai, bi),
        ConditionalSelect.forLists(
            (c, ai, bi, builder) -> AdvancedBinary.using(builder)
                .condSelect(c, ai, bi)),
        (c, ai, bi, builder) -> AdvancedBinary.using(builder)
            .condSelect(c, ai, bi));
  }

  /**
   * Constructs a protocol producer for the keyed compare and swap protocol. This protocol will
   * compare the keys of two key-value pairs and produce a list of pairs so that the first pair has
   * the largest key.
   *
   * @param leftKeyAndValue  the key and value of the left pair
   * @param rightKeyAndValue the key and value of the right pair
   */
  public static KeyedCompareAndSwap<DRes<SInt>,
      DRes<SInt>, DRes<SInt>, ProtocolBuilderNumeric> numeric(
      Pair<DRes<SInt>, List<DRes<SInt>>> leftKeyAndValue,
      Pair<DRes<SInt>, List<DRes<SInt>>> rightKeyAndValue) {

    return new KeyedCompareAndSwap<>(leftKeyAndValue, rightKeyAndValue,
        (a, b, builder) -> Comparison
            .using(builder).compareLEQ(b, a),
        (a, b, builder) -> builder.numeric().add(a, b),
        (a, b, builder) -> builder.numeric().sub(a, b),
        (a, b, builder) -> builder.numeric().add(a, b),
        (a, b, builder) -> builder.numeric().sub(a, b),
        (c, a, b, builder) -> AdvancedNumeric.using(builder)
            .condSelect(c, a, b),
        (c, a, b, builder) -> AdvancedNumeric.using(builder)
            .condSelect(c, a, b));
  }

  @Override
  public DRes<List<Pair<KeyT, List<ValueT>>>> buildComputation(
      BuilderT builder) {
    return builder.par(seq -> {
      ConditionT c = comparison.compare(leftKey, rightKey, seq);
      xorKey = additionKey.add(leftKey, rightKey, seq);
      xorValue = additionValue.add(leftValue, rightValue, seq);
      return () -> c;
    }).par((par, c) -> {
      List<ValueT> firstValue = condSelectValue.apply(
          c, leftValue, rightValue, par);
      KeyT firstKey = condSelectKey.apply(
          c, leftKey, rightKey, par);
      return () -> new Pair<>(firstKey, firstValue);
    }).par((par, data) -> {
      List<ValueT> lastValue = subtractionValue.add(xorValue, data.getSecond(), par);
      KeyT lastKey = subtractionKey.add(xorKey, data.getFirst(), par);
      List<Pair<KeyT, List<ValueT>>> result = new ArrayList<>();
      result.add(data);
      result.add(new Pair<>(lastKey, lastValue));
      return () -> result;
    });
  }

  private interface Addition<ValueT, BuilderT> {

    static <V, B> Addition<List<V>, B> forLists(Addition<V, B> op) {
      return (a, b, builder) -> {
        assert (a.size() == b.size());
        List<V> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
          result.add(op.add(a.get(i), b.get(i), builder));
        }
        return result;
      };
    }

    ValueT add(ValueT a, ValueT b, BuilderT builder);
  }

  private interface Compare<ValueT, ResultT, BuilderT> {

    ResultT compare(ValueT a, ValueT b, BuilderT builder);
  }

  private interface ConditionalSelect<ConditionT, ValueT, BuilderT> {

    static <C, V, B extends ProtocolBuilderImpl<B>> ConditionalSelect<C, List<V>, B> forLists(
        ConditionalSelect<C, V, B> op) {
      return (c, a, b, builder) -> {
        assert (a.size() == b.size());
        List<V> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
          result.add(op.apply(c, a.get(i), b.get(i), builder));
        }
        return result;
      };
    }

    ValueT apply(ConditionT condition, ValueT a, ValueT b, BuilderT builder);
  }
}
