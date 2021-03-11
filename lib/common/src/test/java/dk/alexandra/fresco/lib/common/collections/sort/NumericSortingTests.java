package dk.alexandra.fresco.lib.common.collections.sort;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Assert;

public class NumericSortingTests {

  public static class TestOddEvenMergeSort<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final int size, payloadSize, bitlength;

    public TestOddEvenMergeSort() {
      this(7, 0, 5);
    }

    public TestOddEvenMergeSort(int size, int payloadSize, int bitlength) {
      this.size = size;
      this.payloadSize = payloadSize;
      this.bitlength = bitlength;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {

          Random random = new Random(1234);

          List<Pair<BigInteger, List<BigInteger>>> unsorted = Stream.generate(() -> new Pair<>(
              new BigInteger(bitlength, random), Stream.generate(() -> new BigInteger(bitlength, random)).limit(payloadSize)
              .collect(Collectors.toList()))).limit(size).collect(Collectors.toList());

          Application<List<Pair<BigInteger, List<BigInteger>>>, ProtocolBuilderNumeric> app =
              producer -> producer.seq(seq -> {
                Numeric builder = seq.numeric();
                List<Pair<DRes<SInt>, List<DRes<SInt>>>> unsortedShared = unsorted.stream().map(pair ->
                  new Pair<>(builder.known(pair.getFirst()), pair.getSecond().stream().map(builder::known).collect(Collectors.toList()))).collect(
                    Collectors.toList());

                return Collections.using(seq).sort(unsortedShared);
              }).seq((seq, sorted) -> {
                Numeric builder = seq.numeric();
                List<Pair<DRes<BigInteger>, List<DRes<BigInteger>>>> opened = new ArrayList<>();
                for (Pair<DRes<SInt>, List<DRes<SInt>>> p : sorted) {
                  DRes<BigInteger> oKey = builder.open(p.getFirst());
                  List<DRes<BigInteger>> oValues = p.getSecond().stream().map(builder::open).collect(
                      Collectors.toList());
                  opened.add(new Pair<>(oKey, oValues));
                }
                return () -> opened;
              }).seq((seq, opened) -> () -> opened.stream().map(p -> {
                BigInteger key = p.getFirst().out();
                List<BigInteger> value =
                    p.getSecond().stream().map(DRes::out).collect(Collectors.toList());
                return new Pair<>(key, value);
              }).collect(Collectors.toList()));

          List<Pair<BigInteger, List<BigInteger>>> results = runApplication(app);
          Assert.assertEquals(results.size(), unsorted.size());

          java.util.Collections.sort(unsorted, Comparator.comparing(Pair::getFirst));
          java.util.Collections.reverse(unsorted);

          for (int i = 0; i < size; i++) {
            Assert.assertEquals(unsorted.get(i).getFirst(), results.get(i).getFirst());

            // The sorting algorithm is not stable, so either the payloads are the same,
            // or another pair with the same key has the payload.
            if (!unsorted.get(i).getSecond().equals(results.get(i).getSecond())) {
              int finalI = i;
              List<List<BigInteger>> ties = unsorted.
                  stream().filter(p -> p.getFirst().equals(results.get(finalI).getFirst())).map(Pair::getSecond)
                  .collect(Collectors.toList());
              Assert.assertTrue(ties.contains(results.get(i).getSecond()));
            }
          }
        }
      };
    }
  }

  public static class TestOddEvenMergeSortDifferentValueLength<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {

          BigInteger left1 = new BigInteger("1");
          BigInteger left2 = new BigInteger("8");

          List<BigInteger> right1 = Arrays.asList(BigInteger.ZERO);
          List<BigInteger> right2 = Arrays.asList(BigInteger.ZERO, BigInteger.ONE);

          Application<List<Pair<BigInteger, List<BigInteger>>>, ProtocolBuilderNumeric> app =
              producer -> producer.seq(seq -> {
                Numeric builder = seq.numeric();
                List<Pair<DRes<SInt>, List<DRes<SInt>>>> unSorted = new ArrayList<>();

                // Construct pairs with payloads of different sizes
                unSorted.add(new Pair<>(builder.known(left1), right1.stream().map(builder::known).collect(
                    Collectors.toList())));
                unSorted.add(new Pair<>(builder.known(left2), right2.stream().map(builder::known).collect(
                    Collectors.toList())));

                DRes<List<Pair<DRes<SInt>, List<DRes<SInt>>>>> sorted =
                    OddEvenMerge.numeric(unSorted).buildComputation(seq);

                return null;
              });

          runApplication(app);
        }
      };
    }
  }


  public static class TestKeyedCompareAndSwap<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {

          BigInteger left1 = new BigInteger("2");
          BigInteger left2 = new BigInteger("1");
          BigInteger right1 = new BigInteger("3");
          BigInteger right2 = new BigInteger("7");


          Application<List<Pair<BigInteger, List<BigInteger>>>, ProtocolBuilderNumeric> app =
              producer -> producer.seq(seq -> {
                Numeric builder = seq.numeric();
                Pair<DRes<SInt>, List<DRes<SInt>>> left = new Pair<>(builder.known(left1), Arrays.asList(builder.known(left2)));
                Pair<DRes<SInt>, List<DRes<SInt>>> right = new Pair<>(builder.known(right1), Arrays.asList(builder.known(right2)));

                DRes<List<Pair<DRes<SInt>, List<DRes<SInt>>>>> sorted =
                    AdvancedNumeric.using(seq).keyedCompareAndSwap(left, right);

                return sorted;
              }).seq((seq, sorted) -> {
                List<Pair<DRes<BigInteger>, List<DRes<BigInteger>>>> open = sorted.stream().map(p -> new Pair<>(seq.numeric().open(p.getFirst()),
                    p.getSecond().stream().map(seq.numeric()::open).collect(Collectors.toList()))).collect(Collectors.toList());
                return () -> open.stream().map(p -> new Pair<>(p.getFirst().out(),
                    p.getSecond().stream().map(DRes::out).collect(Collectors.toList()))).collect(Collectors.toList());
              });

          List<Pair<BigInteger, List<BigInteger>>> out = runApplication(app);
          System.out.println(out);
        }
      };
    }
  }

}
