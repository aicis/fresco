package dk.alexandra.fresco.lib.common.collections.sort;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.common.math.DefaultAdvancedBinary;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

/**
 * Test class for the sorting-related computations for binary suites.
 */
public class CollectionsSortingTests {

  public static class TestKeyedCompareAndSwap<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestKeyedCompareAndSwap() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() {

          List<Boolean> rawLeftKey = Arrays.asList(ByteAndBitConverter.toBoolean("49"));
          List<Boolean> rawLeftValue = Arrays.asList(ByteAndBitConverter.toBoolean("00"));
          List<Boolean> rawRightKey = Arrays.asList(ByteAndBitConverter.toBoolean("ff"));
          List<Boolean> rawRightValue = Arrays.asList(ByteAndBitConverter.toBoolean("ee"));

          Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary> app =
              builder -> builder.seq(seq -> {
                List<DRes<SBool>> leftKey =
                    rawLeftKey.stream().map(builder.binary()::known).collect(Collectors.toList());
                List<DRes<SBool>> rightKey =
                    rawRightKey.stream().map(builder.binary()::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> leftValue =
                    rawLeftValue.stream().map(builder.binary()::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> rightValue =
                    rawRightValue.stream().map(builder.binary()::known)
                        .collect(Collectors.toList());

                return new DefaultAdvancedBinary(seq).keyedCompareAndSwap(new Pair<>(leftKey, leftValue),
                    new Pair<>(rightKey, rightValue));
              }).seq((seq, data) -> {
                List<Pair<List<DRes<Boolean>>, List<DRes<Boolean>>>> open = new ArrayList<>();

                for (Pair<List<DRes<SBool>>, List<DRes<SBool>>> o : data) {

                  List<DRes<Boolean>> first =
                      o.getFirst().stream().map(seq.binary()::open).collect(Collectors.toList());
                  List<DRes<Boolean>> second =
                      o.getSecond().stream().map(seq.binary()::open).collect(Collectors.toList());

                  Pair<List<DRes<Boolean>>, List<DRes<Boolean>>> pair = new Pair<>(first, second);
                  open.add(pair);
                }
                return () -> open;
              }).seq((seq, data) -> {
                List<Pair<List<Boolean>, List<Boolean>>> out = new ArrayList<>();
                for (Pair<List<DRes<Boolean>>, List<DRes<Boolean>>> o : data) {
                  List<Boolean> first =
                      o.getFirst().stream().map(DRes::out).collect(Collectors.toList());
                  List<Boolean> second =
                      o.getSecond().stream().map(DRes::out).collect(Collectors.toList());

                  Pair<List<Boolean>, List<Boolean>> pair = new Pair<>(first, second);
                  out.add(pair);

                }

                return () -> out;
              });

          List<Pair<List<Boolean>, List<Boolean>>> result = runApplication(app);

          Assert.assertEquals("ff", ByteAndBitConverter.toHex(result.get(0).getFirst()));

          Assert.assertEquals("ee", ByteAndBitConverter.toHex(result.get(0).getSecond()));

          Assert.assertEquals("49", ByteAndBitConverter.toHex(result.get(1).getFirst()));

          Assert.assertEquals("00", ByteAndBitConverter.toHex(result.get(1).getSecond()));
        }
      };
    }
  }

  public static class TestOddEvenMerge<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    private final boolean presorted;

    public TestOddEvenMerge(boolean presorted) {
      this.presorted = presorted;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() {

          Boolean[] left11 = ByteAndBitConverter.toBoolean("01");
          Boolean[] left12 = ByteAndBitConverter.toBoolean("08");
          Boolean[] left21 = ByteAndBitConverter.toBoolean("03");
          Boolean[] left22 = ByteAndBitConverter.toBoolean("07");
          Boolean[] left31 = ByteAndBitConverter.toBoolean("00");
          Boolean[] left32 = ByteAndBitConverter.toBoolean("06");
          Boolean[] left41 = ByteAndBitConverter.toBoolean("02");
          Boolean[] left42 = ByteAndBitConverter.toBoolean("05");

          Boolean[] left51 = ByteAndBitConverter.toBoolean("11");
          Boolean[] left52 = ByteAndBitConverter.toBoolean("10");
          Boolean[] left61 = ByteAndBitConverter.toBoolean("13");
          Boolean[] left62 = ByteAndBitConverter.toBoolean("17");
          Boolean[] left71 = ByteAndBitConverter.toBoolean("19");
          Boolean[] left72 = ByteAndBitConverter.toBoolean("16");
          Boolean[] left81 = ByteAndBitConverter.toBoolean("12");
          Boolean[] left82 = ByteAndBitConverter.toBoolean("15");

          Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary> app =
              producer -> producer.seq(seq -> {
                Binary builder = seq.binary();
                List<DRes<SBool>> l11 =
                    Arrays.stream(left11).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l12 =
                    Arrays.stream(left12).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l21 =
                    Arrays.stream(left21).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l22 =
                    Arrays.stream(left22).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l31 =
                    Arrays.stream(left31).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l32 =
                    Arrays.stream(left32).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l41 =
                    Arrays.stream(left41).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l42 =
                    Arrays.stream(left42).map(builder::known)
                        .collect(Collectors.toList());

                List<DRes<SBool>> l51 =
                    Arrays.stream(left51).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l52 =
                    Arrays.stream(left52).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l61 =
                    Arrays.stream(left61).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l62 =
                    Arrays.stream(left62).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l71 =
                    Arrays.stream(left71).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l72 =
                    Arrays.stream(left72).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l81 =
                    Arrays.stream(left81).map(builder::known)
                        .collect(Collectors.toList());
                List<DRes<SBool>> l82 =
                    Arrays.stream(left82).map(builder::known)
                        .collect(Collectors.toList());

                List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> toSort = new ArrayList<>();

                if (presorted) {
                  // Keep in mind that the first n / 2 keys need to be sorted, as do the second n / 2 keys.
                  // With the way comparisons work currently, we need the two halves to be sorted in descending order.
                  toSort.add(new Pair<>(l21, l22));
                  toSort.add(new Pair<>(l41, l42));
                  toSort.add(new Pair<>(l11, l12));
                  toSort.add(new Pair<>(l31, l32));
                  toSort.add(new Pair<>(l71, l72));
                  toSort.add(new Pair<>(l61, l62));
                  toSort.add(new Pair<>(l81, l82));
                  toSort.add(new Pair<>(l51, l52));
                  return seq.seq(new OddEvenMerge(toSort, presorted));
                } else {
                  toSort.add(new Pair<>(l11, l12));
                  toSort.add(new Pair<>(l21, l22));
                  toSort.add(new Pair<>(l31, l32));
                  toSort.add(new Pair<>(l41, l42));
                  toSort.add(new Pair<>(l51, l52));
                  toSort.add(new Pair<>(l61, l62));
                  toSort.add(new Pair<>(l71, l72));
                  toSort.add(new Pair<>(l81, l82));
                  // make code-cov happy
                  return seq.seq(new OddEvenMerge(toSort));
                }
              }).seq((seq, sorted) -> {
                Binary builder = seq.binary();
                List<Pair<List<DRes<Boolean>>, List<DRes<Boolean>>>> opened = new ArrayList<>();
                for (Pair<List<DRes<SBool>>, List<DRes<SBool>>> p : sorted) {
                  List<DRes<Boolean>> oKeys = new ArrayList<>();
                  for (DRes<SBool> key : p.getFirst()) {
                    oKeys.add(builder.open(key));
                  }
                  List<DRes<Boolean>> oValues = new ArrayList<>();
                  for (DRes<SBool> value : p.getSecond()) {
                    oValues.add(builder.open(value));
                  }
                  opened.add(new Pair<>(oKeys, oValues));
                }
                return () -> opened;
              }).seq((seq, opened) -> () -> opened.stream().map((p) -> {
                List<Boolean> key =
                    p.getFirst().stream().map(DRes::out).collect(Collectors.toList());
                List<Boolean> value =
                    p.getSecond().stream().map(DRes::out).collect(Collectors.toList());
                return new Pair<>(key, value);
              }).collect(Collectors.toList()));

          List<Pair<List<Boolean>, List<Boolean>>> resultsMergeOnly = runApplication(app);

          Assert.assertEquals("19", ByteAndBitConverter.toHex(resultsMergeOnly.get(0).getFirst()));
          Assert.assertEquals("16", ByteAndBitConverter.toHex(resultsMergeOnly.get(0).getSecond()));

          Assert.assertEquals("13", ByteAndBitConverter.toHex(resultsMergeOnly.get(1).getFirst()));
          Assert.assertEquals("17", ByteAndBitConverter.toHex(resultsMergeOnly.get(1).getSecond()));

          Assert.assertEquals("12", ByteAndBitConverter.toHex(resultsMergeOnly.get(2).getFirst()));
          Assert.assertEquals("15", ByteAndBitConverter.toHex(resultsMergeOnly.get(2).getSecond()));

          Assert.assertEquals("11", ByteAndBitConverter.toHex(resultsMergeOnly.get(3).getFirst()));
          Assert.assertEquals("10", ByteAndBitConverter.toHex(resultsMergeOnly.get(3).getSecond()));

          Assert.assertEquals("03", ByteAndBitConverter.toHex(resultsMergeOnly.get(4).getFirst()));
          Assert.assertEquals("07", ByteAndBitConverter.toHex(resultsMergeOnly.get(4).getSecond()));

          Assert.assertEquals("02", ByteAndBitConverter.toHex(resultsMergeOnly.get(5).getFirst()));
          Assert.assertEquals("05", ByteAndBitConverter.toHex(resultsMergeOnly.get(5).getSecond()));

          Assert.assertEquals("01", ByteAndBitConverter.toHex(resultsMergeOnly.get(6).getFirst()));
          Assert.assertEquals("08", ByteAndBitConverter.toHex(resultsMergeOnly.get(6).getSecond()));

          Assert.assertEquals("00", ByteAndBitConverter.toHex(resultsMergeOnly.get(7).getFirst()));
          Assert.assertEquals("06", ByteAndBitConverter.toHex(resultsMergeOnly.get(7).getSecond()));
        }
      };
    }
  }
}
