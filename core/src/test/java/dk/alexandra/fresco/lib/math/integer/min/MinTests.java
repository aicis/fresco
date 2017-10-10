package dk.alexandra.fresco.lib.math.integer.min;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.core.Is;
import org.junit.Assert;


/**
 * Generic test cases for basic finite field operations.
 * <p>
 * Can be reused by a test case for any protocol suite that implements the basic field protocol
 * factory.
 * </p>
 */
public class MinTests {

  public static class TestMinimumProtocol<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<Integer> data1 =
            Arrays.asList(200, 144, 99, 211, 930, 543, 520, 532, 497, 450, 432);

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, List<BigInteger>>, ProtocolBuilderNumeric> app = builder -> {
            Numeric sIntFactory = builder.numeric();

            List<DRes<SInt>> inputs = data1.stream().map(BigInteger::valueOf)
                .map(sIntFactory::known).collect(Collectors.toList());

            DRes<Pair<List<DRes<SInt>>, SInt>> min = builder.seq(new Minimum(inputs));

            return builder.par((par) -> {
              Numeric open = par.numeric();
              DRes<BigInteger> resultMin = open.open(min.out().getSecond());
              List<DRes<SInt>> outputArray = min.out().getFirst();
              List<DRes<BigInteger>> openOutputArray = new ArrayList<>(outputArray.size());
              for (DRes<SInt> computation : outputArray) {
                openOutputArray.add(open.open(computation));

              }
              return () -> new Pair<>(resultMin.out(),
                  openOutputArray.stream().map(DRes::out).collect(Collectors.toList()));
            });
          };
          Pair<BigInteger, List<BigInteger>> result = runApplication(app);
          Assert.assertThat(result.getSecond().get(2), Is.is(BigInteger.ONE));
          Assert.assertThat(result.getFirst(), Is.is(new BigInteger("99")));
        }
      };
    }
  }


  public static class TestMinInfFraction<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<Integer> data1 =
            Arrays.asList(20, 14, 9, 21, 93, 54, 52, 53, 49, 45, 43);
        private final List<Integer> data2 =
            Arrays.asList(140, 120, 90, 191, 123, 4, 122, 153, 149, 145, 143);
        private final List<Integer> data3 = Arrays.asList(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0);

        @Override
        public void test() throws Exception {
          Application<MinInfResult, ProtocolBuilderNumeric> app = builder -> {
            Numeric sIntFactory = builder.numeric();

            List<DRes<SInt>> inputN = data1.stream().map(BigInteger::valueOf)
                .map(sIntFactory::known).collect(Collectors.toList());

            List<DRes<SInt>> inputD = data2.stream().map(BigInteger::valueOf)
                .map(sIntFactory::known).collect(Collectors.toList());

            List<DRes<SInt>> inputInfs = data3.stream().map(BigInteger::valueOf)
                .map(sIntFactory::known).collect(Collectors.toList());

            DRes<MinInfFrac.MinInfOutput> min =
                builder.seq(new MinInfFrac(inputN, inputD, inputInfs));

            return builder.par((par) -> {
              Numeric open = par.numeric();
              DRes<BigInteger> resultMinN = open.open(min.out().nm);
              DRes<BigInteger> resultMinD = open.open(min.out().dm);
              DRes<BigInteger> resultMinInfs = open.open(min.out().infm);
              List<DRes<SInt>> outputArray = min.out().cs;
              List<DRes<BigInteger>> openOutputArray = new ArrayList<>(outputArray.size());
              for (DRes<SInt> computation : outputArray) {
                openOutputArray.add(open.open(computation));
              }
              return () -> new MinInfResult(resultMinN.out(), resultMinD.out(), resultMinInfs.out(),
                  openOutputArray.stream().map(DRes::out).collect(Collectors.toList()));
            });
          };
          MinInfResult minInfResult = runApplication(app);
          Assert.assertThat(minInfResult.resultList.get(2), Is.is(BigInteger.ONE));
          Assert.assertThat(minInfResult.minD, Is.is(new BigInteger("90")));
          Assert.assertThat(minInfResult.minN, Is.is(new BigInteger("9")));
          Assert.assertThat(minInfResult.minInfs, Is.is(new BigInteger("0")));
        }
      };
    }
  }

  private static class MinInfResult {

    private final BigInteger minN;
    private final BigInteger minD;
    private final BigInteger minInfs;
    private final List<BigInteger> resultList;

    private MinInfResult(BigInteger minN, BigInteger minD, BigInteger minInfs,
        List<BigInteger> resultList) {
      this.minN = minN;
      this.minD = minD;
      this.minInfs = minInfs;
      this.resultList = resultList;
    }
  }

  public static class TestMinInfFractionTrivial<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<Integer> data1 = Collections.singletonList(20);
        private final List<Integer> data2 = Collections.singletonList(140);
        private final List<Integer> data3 = Collections.singletonList(0);

        @Override
        public void test() throws Exception {
          Application<MinInfResult, ProtocolBuilderNumeric> app = builder -> {
            Numeric sIntFactory = builder.numeric();

            List<DRes<SInt>> inputN = data1.stream().map(BigInteger::valueOf)
                .map(sIntFactory::known).collect(Collectors.toList());

            List<DRes<SInt>> inputD = data2.stream().map(BigInteger::valueOf)
                .map(sIntFactory::known).collect(Collectors.toList());

            List<DRes<SInt>> inputInfs = data3.stream().map(BigInteger::valueOf)
                .map(sIntFactory::known).collect(Collectors.toList());

            DRes<MinInfFrac.MinInfOutput> min =
                builder.seq(new MinInfFrac(inputN, inputD, inputInfs));

            return builder.par((par) -> {
              Numeric open = par.numeric();
              DRes<BigInteger> resultMinN = open.open(min.out().nm);
              DRes<BigInteger> resultMinD = open.open(min.out().dm);
              DRes<BigInteger> resultMinInfs = open.open(min.out().infm);
              List<DRes<SInt>> outputArray = min.out().cs;
              List<DRes<BigInteger>> openOutputArray = new ArrayList<>(outputArray.size());
              for (DRes<SInt> computation : outputArray) {
                openOutputArray.add(open.open(computation));

              }

              return () -> new MinInfResult(resultMinN.out(), resultMinD.out(), resultMinInfs.out(),
                  openOutputArray.stream().map(DRes::out).collect(Collectors.toList()));
            });
          };
          MinInfResult minInfResult = runApplication(app);
          Assert.assertThat(minInfResult.resultList.get(0), Is.is(BigInteger.ONE));
          Assert.assertThat(minInfResult.minD, Is.is(new BigInteger("140")));
          Assert.assertThat(minInfResult.minN, Is.is(new BigInteger("20")));
          Assert.assertThat(minInfResult.minInfs, Is.is(new BigInteger("0")));
        }
      };
    }
  }
}
