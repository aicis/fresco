package dk.alexandra.fresco.lib.collections.sort;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

/**
 * Test class for the DEASolver. Will generate a random data sample and perform a Data Envelopment
 * Analysis on it. The TestDEADSolver takes the size of the problem as inputs (i.e. the number of
 * input and output variables, the number of rows in the basis and the number of queries to perform.
 * The MPC result is compared with the result of a plaintext DEA solver.
 *
 */
public class CollectionsSortingTests {

  public static class TestKeyedCompareAndSwap<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestKeyedCompareAndSwap() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {

          List<Boolean> rawLeftKey = Arrays.asList(ByteAndBitConverter.toBoolean("49"));
          List<Boolean> rawLeftValue = Arrays.asList(ByteAndBitConverter.toBoolean("00"));
          List<Boolean> rawRightKey = Arrays.asList(ByteAndBitConverter.toBoolean("ff"));
          List<Boolean> rawRightValue = Arrays.asList(ByteAndBitConverter.toBoolean("ee"));

          Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary> app =
              builder -> {

            ProtocolBuilderBinary seqBuilder = (ProtocolBuilderBinary) builder;

            return seqBuilder.seq(seq -> {
              List<DRes<SBool>> leftKey =
                  rawLeftKey.stream().map(builder.binary()::known).collect(Collectors.toList());
              List<DRes<SBool>> rightKey =
                  rawRightKey.stream().map(builder.binary()::known).collect(Collectors.toList());
              List<DRes<SBool>> leftValue =
                  rawLeftValue.stream().map(builder.binary()::known).collect(Collectors.toList());
              List<DRes<SBool>> rightValue =
                  rawRightValue.stream().map(builder.binary()::known).collect(Collectors.toList());

              return seq.advancedBinary().keyedCompareAndSwap(new Pair<>(leftKey, leftValue),
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
          };

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

    public TestOddEvenMerge() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {

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

          // Test sorting without using the mergePresortedHalves flag
          Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary> appSort =
              new Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary>() {

            @Override
            public DRes<List<Pair<List<Boolean>, List<Boolean>>>> buildComputation(
                ProtocolBuilderBinary producer) {
              return producer.seq(seq -> {
                Binary builder = seq.binary();
                List<DRes<SBool>> l11 =
                    Arrays.asList(left11).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l12 =
                    Arrays.asList(left12).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l21 =
                    Arrays.asList(left21).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l22 =
                    Arrays.asList(left22).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l31 =
                    Arrays.asList(left31).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l32 =
                    Arrays.asList(left32).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l41 =
                    Arrays.asList(left41).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l42 =
                    Arrays.asList(left42).stream().map(builder::known).collect(Collectors.toList());

                List<DRes<SBool>> l51 =
                    Arrays.asList(left51).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l52 =
                    Arrays.asList(left52).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l61 =
                    Arrays.asList(left61).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l62 =
                    Arrays.asList(left62).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l71 =
                    Arrays.asList(left71).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l72 =
                    Arrays.asList(left72).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l81 =
                    Arrays.asList(left81).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l82 =
                    Arrays.asList(left82).stream().map(builder::known).collect(Collectors.toList());

                List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> unSorted = new ArrayList<>();

                unSorted.add(new Pair<>(l11, l12));
                unSorted.add(new Pair<>(l21, l22));
                unSorted.add(new Pair<>(l31, l32));
                unSorted.add(new Pair<>(l41, l42));
                unSorted.add(new Pair<>(l51, l52));
                unSorted.add(new Pair<>(l61, l62));
                unSorted.add(new Pair<>(l71, l72));
                unSorted.add(new Pair<>(l81, l82));


                DRes<List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>>> sorted =
                    new OddEvenMerge(unSorted).buildComputation(seq);
                return sorted;
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
              }).seq((seq, opened) -> {
                return () -> opened.stream().map((p) -> {
                  List<Boolean> key =
                      p.getFirst().stream().map(DRes::out).collect(Collectors.toList());
                  List<Boolean> value =
                      p.getSecond().stream().map(DRes::out).collect(Collectors.toList());
                  return new Pair<>(key, value);
                }).collect(Collectors.toList());
              });
            }

          };

          List<Pair<List<Boolean>, List<Boolean>>> resultsSort = runApplication(appSort);

          Assert.assertEquals(Arrays.asList(left71), resultsSort.get(0).getFirst());
          Assert.assertEquals(Arrays.asList(left72), resultsSort.get(0).getSecond());
          Assert.assertEquals(Arrays.asList(left61), resultsSort.get(1).getFirst());
          Assert.assertEquals(Arrays.asList(left62), resultsSort.get(1).getSecond());
          Assert.assertEquals(Arrays.asList(left81), resultsSort.get(2).getFirst());
          Assert.assertEquals(Arrays.asList(left82), resultsSort.get(2).getSecond());
          Assert.assertEquals(Arrays.asList(left51), resultsSort.get(3).getFirst());
          Assert.assertEquals(Arrays.asList(left52), resultsSort.get(3).getSecond());

          Assert.assertEquals(Arrays.asList(left21), resultsSort.get(4).getFirst());
          Assert.assertEquals(Arrays.asList(left22), resultsSort.get(4).getSecond());
          Assert.assertEquals(Arrays.asList(left41), resultsSort.get(5).getFirst());
          Assert.assertEquals(Arrays.asList(left42), resultsSort.get(5).getSecond());
          Assert.assertEquals(Arrays.asList(left11), resultsSort.get(6).getFirst());
          Assert.assertEquals(Arrays.asList(left12), resultsSort.get(6).getSecond());
          Assert.assertEquals(Arrays.asList(left31), resultsSort.get(7).getFirst());
          Assert.assertEquals(Arrays.asList(left32), resultsSort.get(7).getSecond());


          // Test using the mergePresortedHalves flag
          Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary> appMergeOnly =
              new Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary>() {
            @Override
            public DRes<List<Pair<List<Boolean>, List<Boolean>>>> buildComputation(
                    ProtocolBuilderBinary producer) {
              return producer.seq(seq -> {
                Binary builder = seq.binary();
                List<DRes<SBool>> l11 =
                        Arrays.asList(left11).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l12 =
                        Arrays.asList(left12).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l21 =
                        Arrays.asList(left21).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l22 =
                        Arrays.asList(left22).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l31 =
                        Arrays.asList(left31).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l32 =
                        Arrays.asList(left32).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l41 =
                        Arrays.asList(left41).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l42 =
                        Arrays.asList(left42).stream().map(builder::known).collect(Collectors.toList());

                List<DRes<SBool>> l51 =
                        Arrays.asList(left51).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l52 =
                        Arrays.asList(left52).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l61 =
                        Arrays.asList(left61).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l62 =
                        Arrays.asList(left62).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l71 =
                        Arrays.asList(left71).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l72 =
                        Arrays.asList(left72).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l81 =
                        Arrays.asList(left81).stream().map(builder::known).collect(Collectors.toList());
                List<DRes<SBool>> l82 =
                        Arrays.asList(left82).stream().map(builder::known).collect(Collectors.toList());

                List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> unSorted = new ArrayList<>();
                
                // Keep in mind that the first n / 2 keys need to be sorted, as do the second n / 2 keys.
                // With the way comparisons work currently, we need the two halves to be sorted in descending order.
                unSorted.add(new Pair<>(l21, l22));
                unSorted.add(new Pair<>(l41, l42));
                unSorted.add(new Pair<>(l11, l12));
                unSorted.add(new Pair<>(l31, l32));
                unSorted.add(new Pair<>(l71, l72));
                unSorted.add(new Pair<>(l61, l62));
                unSorted.add(new Pair<>(l81, l82));
                unSorted.add(new Pair<>(l51, l52));

                DRes<List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>>> sorted =
                        new OddEvenMerge(unSorted, true).buildComputation(seq);
                return sorted;
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
              }).seq((seq, opened) -> {
                return () -> opened.stream().map((p) -> {
                  List<Boolean> key =
                          p.getFirst().stream().map(DRes::out).collect(Collectors.toList());
                  List<Boolean> value =
                          p.getSecond().stream().map(DRes::out).collect(Collectors.toList());
                  return new Pair<>(key, value);
                }).collect(Collectors.toList());
              });
            }

          };

          List<Pair<List<Boolean>, List<Boolean>>> resultsMergeOnly = runApplication(appMergeOnly);

          Assert.assertEquals(Arrays.asList(left71), resultsMergeOnly.get(0).getFirst());
          Assert.assertEquals(Arrays.asList(left72), resultsMergeOnly.get(0).getSecond());
          Assert.assertEquals(Arrays.asList(left61), resultsMergeOnly.get(1).getFirst());
          Assert.assertEquals(Arrays.asList(left62), resultsMergeOnly.get(1).getSecond());
          Assert.assertEquals(Arrays.asList(left81), resultsMergeOnly.get(2).getFirst());
          Assert.assertEquals(Arrays.asList(left82), resultsMergeOnly.get(2).getSecond());
          Assert.assertEquals(Arrays.asList(left51), resultsMergeOnly.get(3).getFirst());
          Assert.assertEquals(Arrays.asList(left52), resultsMergeOnly.get(3).getSecond());

          Assert.assertEquals(Arrays.asList(left21), resultsMergeOnly.get(4).getFirst());
          Assert.assertEquals(Arrays.asList(left22), resultsMergeOnly.get(4).getSecond());
          Assert.assertEquals(Arrays.asList(left41), resultsMergeOnly.get(5).getFirst());
          Assert.assertEquals(Arrays.asList(left42), resultsMergeOnly.get(5).getSecond());
          Assert.assertEquals(Arrays.asList(left11), resultsMergeOnly.get(6).getFirst());
          Assert.assertEquals(Arrays.asList(left12), resultsMergeOnly.get(6).getSecond());
          Assert.assertEquals(Arrays.asList(left31), resultsMergeOnly.get(7).getFirst());
          Assert.assertEquals(Arrays.asList(left32), resultsMergeOnly.get(7).getSecond());
        }
      };
    }
  }
}
