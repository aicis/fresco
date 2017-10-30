package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.SortingHelperUtility;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Assert;

public class SortingTests {

  public static class TestIsSorted<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private BigInteger zero = BigInteger.valueOf(0);
    private BigInteger one = BigInteger.valueOf(1);
    private BigInteger two = BigInteger.valueOf(2);
    private BigInteger three = BigInteger.valueOf(3);
    private BigInteger four = BigInteger.valueOf(4);
    private BigInteger five = BigInteger.valueOf(5);

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            DRes<SInt> zero = builder.numeric().known(TestIsSorted.this.zero);
            DRes<SInt> one = builder.numeric().known(TestIsSorted.this.one);
            DRes<SInt> two = builder.numeric().known(TestIsSorted.this.two);
            DRes<SInt> three = builder.numeric().known(TestIsSorted.this.three);
            DRes<SInt> four = builder.numeric().known(TestIsSorted.this.four);
            DRes<SInt> five = builder.numeric().known(TestIsSorted.this.five);

            List<DRes<SInt>> unsorted = Arrays.asList(one, two, three, five, zero);
            List<DRes<SInt>> sorted = Arrays.asList(three, four, four);

            DRes<SInt> firstResult = new SortingHelperUtility()
                .isSorted(builder, unsorted);
            DRes<SInt> secondResult = new SortingHelperUtility().isSorted(builder, sorted);

            DRes<BigInteger> firstOpen = builder.numeric().open(firstResult);
            DRes<BigInteger> secondOpen = builder.numeric().open(secondResult);

            return () -> new Pair<>(firstOpen.out(), secondOpen.out());
          };

          Pair<BigInteger, BigInteger> outputs = runApplication(app);
          Assert.assertEquals(BigInteger.ZERO, outputs.getFirst());
          Assert.assertEquals(BigInteger.ONE, outputs.getSecond());
        }
      };
    }
  }

  public static class TestCompareAndSwap<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = seq -> {
            DRes<SInt> one = seq.numeric().known(BigInteger.valueOf(1));
            DRes<SInt> two = seq.numeric().known(BigInteger.valueOf(2));
            List<DRes<SInt>> initialList = Arrays.asList(two, one);
            new SortingHelperUtility().compareAndSwap(seq, 0, 1, initialList);
            return seq.seq((openSeq) -> {
                  DRes<BigInteger> firstOpen = openSeq.numeric().open(initialList.get(0));
                  DRes<BigInteger> secondOpen = openSeq.numeric().open(initialList.get(1));
                  return () -> new Pair<>(firstOpen.out(), secondOpen.out());
                }
            );
          };

          Pair<BigInteger, BigInteger> outputs = runApplication(app);
          Assert.assertEquals(BigInteger.ONE, outputs.getFirst());
          Assert.assertEquals(BigInteger.valueOf(2), outputs.getSecond());
        }
      };
    }
  }

  public static class TestSort<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final List<BigInteger> values;
    private final List<BigInteger> sorted;

    public TestSort(List<BigInteger> values) {
      this.values = values;
      this.sorted = new ArrayList<>(values);
      this.sorted.sort(BigInteger::compareTo);
    }

    public TestSort() {
      this(Arrays.asList(
          BigInteger.valueOf(1),
          BigInteger.valueOf(3),
          BigInteger.valueOf(3),
          BigInteger.valueOf(5),
          BigInteger.ZERO));
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              builder -> {
                Numeric input = builder.numeric();
                List<DRes<SInt>> unsorted = values.stream().map(input::known)
                    .collect(Collectors.toList());

                return builder.seq(seq -> {
                  new SortingHelperUtility().sort(seq, unsorted);
                  return () -> unsorted;
                }).par((par, list) -> {
                  Numeric numeric = par.numeric();
                  List<DRes<BigInteger>> openList = list.stream().map(numeric::open)
                      .collect(Collectors.toList());
                  return () -> openList.stream().map(DRes::out).collect(Collectors.toList());
                });
              };

          List<BigInteger> outputs = runApplication(app);
          Assert.assertEquals(sorted, outputs);
        }

      };
    }
  }

  public static class TestBigSort<ResourcePoolT extends ResourcePool> extends
      TestSort<ResourcePoolT> {

    private static final Random random = new Random();

    public TestBigSort() {
      super(IntStream.range(0, 100)
          .mapToObj((i) -> new BigInteger(10, random))
          .collect(Collectors.toList()));
    }
  }

}
