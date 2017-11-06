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

          Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary> app =
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

                List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> unSorted = new ArrayList<>();

                unSorted.add(new Pair<>(l11, l12));
                unSorted.add(new Pair<>(l21, l22));
                unSorted.add(new Pair<>(l31, l32));
                unSorted.add(new Pair<>(l41, l42));

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

          List<Pair<List<Boolean>, List<Boolean>>> results = runApplication(app);

          Assert.assertEquals(Arrays.asList(left21), results.get(0).getFirst());
          Assert.assertEquals(Arrays.asList(left22), results.get(0).getSecond());
          Assert.assertEquals(Arrays.asList(left41), results.get(1).getFirst());
          Assert.assertEquals(Arrays.asList(left42), results.get(1).getSecond());
          Assert.assertEquals(Arrays.asList(left11), results.get(2).getFirst());
          Assert.assertEquals(Arrays.asList(left12), results.get(2).getSecond());
          Assert.assertEquals(Arrays.asList(left31), results.get(3).getFirst());
          Assert.assertEquals(Arrays.asList(left32), results.get(3).getSecond());
        }
      };
    }
  }
}
