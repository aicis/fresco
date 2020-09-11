package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
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
public class NumericSortingTests {

  public static class TestOddEvenMergeSort<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    public TestOddEvenMergeSort() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {

          BigInteger left1 = new BigInteger("1");
          BigInteger left2 = new BigInteger("8");
          BigInteger left3 = new BigInteger("7");
          BigInteger left4 = new BigInteger("3");
          BigInteger left5 = new BigInteger("0");
          BigInteger left6 = new BigInteger("6");
          BigInteger left7 = new BigInteger("5");
          BigInteger left8 = new BigInteger("2");


          Application<List<Pair<BigInteger, List<BigInteger>>>, ProtocolBuilderNumeric> app =
              new Application<List<Pair<BigInteger, List<BigInteger>>>, ProtocolBuilderNumeric>() {

                @Override
                public DRes<List<Pair<BigInteger, List<BigInteger>>>> buildComputation(
                    ProtocolBuilderNumeric producer) {
                  return producer.seq(seq -> {
                    Numeric builder = seq.numeric();
                    List<Pair<DRes<SInt>, List<DRes<SInt>>>> unSorted = new ArrayList<>();

                    // Construct pairs with empty payloads
                    unSorted.add(new Pair<>(builder.known(left1), new ArrayList<>()));
                    unSorted.add(new Pair<>(builder.known(left2), new ArrayList<>()));
                    unSorted.add(new Pair<>(builder.known(left3), new ArrayList<>()));
                    unSorted.add(new Pair<>(builder.known(left4), new ArrayList<>()));
                    unSorted.add(new Pair<>(builder.known(left5), new ArrayList<>()));
                    unSorted.add(new Pair<>(builder.known(left6), new ArrayList<>()));
                    unSorted.add(new Pair<>(builder.known(left7), new ArrayList<>()));
                    unSorted.add(new Pair<>(builder.known(left8), new ArrayList<>()));

                    DRes<List<Pair<DRes<SInt>, List<DRes<SInt>>>>> sorted =
                        new OddEvenIntegerMerge(unSorted).buildComputation(seq);
                    return sorted;
                  }).seq((seq, sorted) -> {
                    Numeric builder = seq.numeric();
                    List<Pair<DRes<BigInteger>, List<DRes<BigInteger>>>> opened = new ArrayList<>();
                    for (Pair<DRes<SInt>, List<DRes<SInt>>> p : sorted) {
                      DRes<BigInteger> oKey = builder.open(p.getFirst());
                      List<DRes<BigInteger>> oValues = new ArrayList<>();
                      for (DRes<SInt> value : p.getSecond()) {
                        oValues.add(builder.open(value));
                      }
                      opened.add(new Pair<>(oKey, oValues));
                    }
                    return () -> opened;
                  }).seq((seq, opened) -> {
                    return () -> opened.stream().map((p) -> {
                      BigInteger key = p.getFirst().out();
                      List<BigInteger> value =
                          p.getSecond().stream().map(DRes::out).collect(Collectors.toList());
                      return new Pair<>(key, value);
                    }).collect(Collectors.toList());
                  });
                }
              };

          List<Pair<BigInteger, List<BigInteger>>> results = runApplication(app);

          // The payload for all is simply the value false
          Assert.assertEquals(left2, results.get(0).getFirst());
          Assert.assertTrue(results.get(0).getSecond().isEmpty());
          Assert.assertEquals(left3, results.get(1).getFirst());
          Assert.assertTrue(results.get(1).getSecond().isEmpty());
          Assert.assertEquals(left6, results.get(2).getFirst());
          Assert.assertTrue(results.get(2).getSecond().isEmpty());
          Assert.assertEquals(left7, results.get(3).getFirst());
          Assert.assertTrue(results.get(3).getSecond().isEmpty());
          Assert.assertEquals(left4, results.get(4).getFirst());
          Assert.assertTrue(results.get(4).getSecond().isEmpty());
          Assert.assertEquals(left8, results.get(5).getFirst());
          Assert.assertTrue(results.get(5).getSecond().isEmpty());
          Assert.assertEquals(left1, results.get(6).getFirst());
          Assert.assertTrue(results.get(6).getSecond().isEmpty());
          Assert.assertEquals(left5, results.get(7).getFirst());
          Assert.assertTrue(results.get(7).getSecond().isEmpty());
        }
      };
    }
  }


  public static class TestOddEvenMergeSortLargeList<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    public TestOddEvenMergeSortLargeList() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          int size = 64;
          int payloadSize = 2;
          Random rand = new Random();

          List<BigInteger> keys = IntStream.range(0, size).mapToObj(i ->
              new BigInteger(String.valueOf(Math.abs(rand.nextInt()))))
              .collect(Collectors.toList());
          // Payload simply contains integers 0, 1, 2 .. payloadSize-1
          List<BigInteger> payload = IntStream.range(0, payloadSize).mapToObj(j ->
              new BigInteger(String.valueOf(j)))
              .collect(Collectors.toList());

          // We sort random integers, each with a copy of the same payload
          List<Pair<BigInteger, List<BigInteger>>> unsorted = new ArrayList<>(size);
          IntStream.range(0, size).forEach(i ->
            unsorted.add(new Pair<>(keys.get(i), new ArrayList<>(payload))));

          Application<List<Pair<BigInteger, List<BigInteger>>>, ProtocolBuilderNumeric> app =
              new Application<List<Pair<BigInteger, List<BigInteger>>>, ProtocolBuilderNumeric>() {

                @Override
                public DRes<List<Pair<BigInteger, List<BigInteger>>>> buildComputation(
                    ProtocolBuilderNumeric producer) {
                  return producer.seq(seq -> {
                    Numeric builder = seq.numeric();
                    // Input the unsorted list into the MPC as public values
                    List<Pair<DRes<SInt>, List<DRes<SInt>>>> sharedUnsorted = new ArrayList<>();
                    unsorted.stream().forEach(current -> sharedUnsorted.add(
                        new Pair<>(builder.known(current.getFirst()),
                            current.getSecond().stream().map( currentPayload -> builder.known(currentPayload))
                                .collect(Collectors.toList()))));
                    // Sort the list in MPC
                    return new OddEvenIntegerMerge(sharedUnsorted).buildComputation(seq);
                  }).seq((seq, sorted) -> {
                    Numeric builder = seq.numeric();
                    // Open the sorted list
                    List<Pair<DRes<BigInteger>, List<DRes<BigInteger>>>> opened = new ArrayList<>();
                    sorted.forEach(current -> opened.add(new Pair<>(builder.open(current.getFirst()),
                        current.getSecond().stream().map(currentPayload -> builder.open(currentPayload))
                            .collect(Collectors.toList()))));
                    return () -> opened;
                  }).seq((seq, opened) -> {
                    // Return as defered output
                    return () -> opened.stream().map((p) ->
                        new Pair<>(p.getFirst().out(),
                            p.getSecond().stream().map(DRes::out).collect(Collectors.toList())))
                        .collect(Collectors.toList());
                  });
                }
              };

          List<Pair<BigInteger, List<BigInteger>>> results = runApplication(app);

          // Verify the result
          Collections.sort(keys);
          // Reverse since the MPC protocol gives the largest value first
          Collections.reverse(keys);
          IntStream.range(0, keys.size()).forEach(i -> {
            Assert.assertEquals(keys.get(i), results.get(i).getFirst());
            Assert.assertEquals(payload, results.get(i).getSecond());
          });
        }
      };
    }
  }
}
