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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

  public static class TestOddEvenMergeSort<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestOddEvenMergeSort() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {

          Boolean[] left1 = ByteAndBitConverter.toBoolean("01");
          Boolean[] left2 = ByteAndBitConverter.toBoolean("08");
          Boolean[] left3 = ByteAndBitConverter.toBoolean("07");
          Boolean[] left4 = ByteAndBitConverter.toBoolean("03");
          Boolean[] left5 = ByteAndBitConverter.toBoolean("00");
          Boolean[] left6 = ByteAndBitConverter.toBoolean("06");
          Boolean[] left7 = ByteAndBitConverter.toBoolean("02");
          Boolean[] left8 = ByteAndBitConverter.toBoolean("05");

          Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary> app =
              new Application<List<Pair<List<Boolean>, List<Boolean>>>, ProtocolBuilderBinary>() {

                @Override
                public DRes<List<Pair<List<Boolean>, List<Boolean>>>> buildComputation(
                    ProtocolBuilderBinary producer) {
                  return producer.seq(seq -> {
                    Binary builder = seq.binary();
                    List<DRes<SBool>> l1 =
                        Arrays.asList(left1).stream().map(builder::known).collect(Collectors.toList());
                    List<DRes<SBool>> l2 =
                        Arrays.asList(left2).stream().map(builder::known).collect(Collectors.toList());
                    List<DRes<SBool>> l3 =
                        Arrays.asList(left3).stream().map(builder::known).collect(Collectors.toList());
                    List<DRes<SBool>> l4 =
                        Arrays.asList(left4).stream().map(builder::known).collect(Collectors.toList());
                    List<DRes<SBool>> l5 =
                        Arrays.asList(left5).stream().map(builder::known).collect(Collectors.toList());
                    List<DRes<SBool>> l6 =
                        Arrays.asList(left6).stream().map(builder::known).collect(Collectors.toList());
                    List<DRes<SBool>> l7 =
                        Arrays.asList(left7).stream().map(builder::known).collect(Collectors.toList());
                    List<DRes<SBool>> l8 =
                        Arrays.asList(left8).stream().map(builder::known).collect(Collectors.toList());

                    // Constant data payloads
                    List<DRes<SBool>> falseSingleton =
                        Arrays.asList(false).stream().map(builder::known).collect(Collectors.toList());
                    List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> unSorted = new ArrayList<>();

                    unSorted.add(new Pair<>(l1, falseSingleton));
                    unSorted.add(new Pair<>(l2, falseSingleton));
                    unSorted.add(new Pair<>(l3, falseSingleton));
                    unSorted.add(new Pair<>(l4, falseSingleton));
                    unSorted.add(new Pair<>(l5, falseSingleton));
                    unSorted.add(new Pair<>(l6, falseSingleton));
                    unSorted.add(new Pair<>(l7, falseSingleton));
                    unSorted.add(new Pair<>(l8, falseSingleton));

                    return seq.advancedBinary().sort(unSorted);
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

          // The payload for all is simply the value false
          Assert.assertEquals(Arrays.asList(left2), results.get(0).getFirst());
          Assert.assertEquals(Arrays.asList(false), results.get(0).getSecond());
          Assert.assertEquals(Arrays.asList(left3), results.get(1).getFirst());
          Assert.assertEquals(Arrays.asList(false), results.get(1).getSecond());
          Assert.assertEquals(Arrays.asList(left6), results.get(2).getFirst());
          Assert.assertEquals(Arrays.asList(false), results.get(2).getSecond());
          Assert.assertEquals(Arrays.asList(left8), results.get(3).getFirst());
          Assert.assertEquals(Arrays.asList(false), results.get(3).getSecond());
          Assert.assertEquals(Arrays.asList(left4), results.get(4).getFirst());
          Assert.assertEquals(Arrays.asList(false), results.get(4).getSecond());
          Assert.assertEquals(Arrays.asList(left7), results.get(5).getFirst());
          Assert.assertEquals(Arrays.asList(false), results.get(5).getSecond());
          Assert.assertEquals(Arrays.asList(left1), results.get(6).getFirst());
          Assert.assertEquals(Arrays.asList(false), results.get(6).getSecond());
          Assert.assertEquals(Arrays.asList(left5), results.get(7).getFirst());
          Assert.assertEquals(Arrays.asList(false), results.get(7).getSecond());
        }
      };
    }
  }

  public static class TestOddEvenMergeSortLargeList<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestOddEvenMergeSortLargeList() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {
          int size = 83;
          Random rand = new Random();

          List<BigInteger> keys = IntStream.range(0, size).mapToObj(i ->
              // Ensure that we are only using 1 for simplicity in conversion to hex
              new BigInteger(String.valueOf(Math.abs(rand.nextInt() % 256 ))))
              .collect(Collectors.toList());
          List<BigInteger> payload = IntStream.range(0, size).mapToObj(j ->
             keys.get(j))
              .collect(Collectors.toList());

          // We sort random integers, each with a copy of the same payload
          List<Pair<BigInteger, BigInteger>> unsorted = new ArrayList<>(size);
          IntStream.range(0, size).forEach(i ->
              unsorted.add(new Pair<>(keys.get(i), payload.get(i))));

          Application<List<Pair<BigInteger, BigInteger>>, ProtocolBuilderBinary> app =
              new Application<List<Pair<BigInteger, BigInteger>>, ProtocolBuilderBinary>() {

                @Override
                public DRes<List<Pair<BigInteger, BigInteger>>> buildComputation(
                    ProtocolBuilderBinary producer) {
                  return producer.par(par -> {
                    Binary builder = par.binary();
                    // Input the unsorted list into the MPC as public values
                    List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> sharedUnsorted = new ArrayList<>();
                    unsorted.stream().forEach(current -> {
                      List<DRes<SBool>> key = Arrays.stream(
                          ByteAndBitConverter.toBoolean(toHex(current.getFirst())))
                          .map(c -> builder.known(c)).collect(Collectors.toList());
                      List<DRes<SBool>> value = Arrays.stream(
                          ByteAndBitConverter.toBoolean(toHex(current.getSecond())))
                          .map(c -> builder.known(c)).collect(Collectors.toList());
                      sharedUnsorted.add(new Pair<>(key, value));
                    });
                    // Sort the list in MPC
                    return par.advancedBinary().sort(sharedUnsorted);
                  }).par((par, sorted) -> {
                    Binary builder = par.binary();
                    // Open the sorted list
                    List<Pair<List<DRes<Boolean>>, List<DRes<Boolean>>>> opened = new ArrayList<>();
                    sorted.forEach(current -> opened.add(new Pair<>(
                        current.getFirst().stream().map(currentPayload -> builder.open(currentPayload))
                        .collect(Collectors.toList()),
                        current.getSecond().stream().map(currentPayload -> builder.open(currentPayload))
                            .collect(Collectors.toList()))));
                    return () -> opened;
                  }).par((par, opened) -> {
                    // Return as defered output
                    return () -> opened.stream().map((p) ->
                        new Pair<>(
                            new BigInteger(ByteAndBitConverter.toHex(
                                p.getFirst().stream().map(DRes::out).collect(Collectors.toList())), 16),
                            new BigInteger(ByteAndBitConverter.toHex(
                                p.getSecond().stream().map(DRes::out).collect(Collectors.toList())), 16)))
                        .collect(Collectors.toList());
                  });
                }
              };

          List<Pair<BigInteger, BigInteger>> results = runApplication(app);

          // Verify the result
          Collections.sort(keys);
          // Reverse since the MPC protocol gives the largest value first
          Collections.reverse(keys);
          IntStream.range(0, keys.size()).forEach(i -> {
            Assert.assertEquals(keys.get(i), results.get(i).getFirst());
            Assert.assertEquals(keys.get(i), results.get(i).getSecond());
          });
        }
      };
    }
    private static String toHex(BigInteger x) {
      String current = x.toString(16);
      if (current.length() % 2 != 0) {
        return "0"+current;
      } else {
        return current;
      }
    }
  }
}
