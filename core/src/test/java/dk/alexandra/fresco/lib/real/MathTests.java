package dk.alexandra.fresco.lib.real;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MathTests {

  private static final int DEFAULT_PRECISION = 16;
  
  public static class TestExp<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          double x = 1.1;
          BigDecimal input = BigDecimal.valueOf(x);
          BigDecimal expected = BigDecimal.valueOf(Math.exp(x));

          // functionality to be tested
          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = root -> {
            // close inputs
            DRes<SReal> secret = root.realNumeric().input(input, 1);
            DRes<SReal> result = root.realAdvanced().exp(secret);
            return root.realNumeric().open(result);
          };
          BigDecimal output = runApplication(testApplication);
          int expectedPrecision = DEFAULT_PRECISION - 1; // 
          System.out.println(expectedPrecision);
          new RealTestUtils().assertEqual(expected,
              output, expectedPrecision);
        }
      };
    }
  }

  public static class TestRandom<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app =
              producer -> producer.seq(seq -> {
                List<DRes<SReal>> result = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                  result.add(seq.realAdvanced().random());
                }

                List<DRes<BigDecimal>> opened =
                    result.stream().map(seq.realNumeric()::open).collect(Collectors.toList());
                return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
              });

          List<BigDecimal> output = runApplication(app);
          BigDecimal sum = BigDecimal.ZERO;
          BigDecimal min = BigDecimal.ONE;
          BigDecimal max = BigDecimal.ZERO;
          for (BigDecimal random : output) {
            sum = sum.add(random);
            if (random.compareTo(min) == -1) {
              min = random;
            }
            if (random.compareTo(max) == 1) {
              max = random;
            }
            System.out.println(random);
            assertTrue(BigDecimal.ONE.compareTo(random) >= 0);
            assertTrue(BigDecimal.ZERO.compareTo(random) <= 0);
          }
        }
      };
    }
  }

  public static class TestLog<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs = Stream.of(1.1, 2.1, 3.1, 4.1, 5.1, 10.1)
          .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {

            List<DRes<SReal>> closed1 =
                openInputs.stream().map(producer.realNumeric()::known).collect(Collectors.toList());

            List<DRes<SReal>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(producer.realAdvanced().log(inputX));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(producer.realNumeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          RealTestUtils utils = new RealTestUtils();
          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);
            // For large inputs, the result is quite imprecise. How imprecise is hard to estimate,
            // but for now we use 8 bits precision as bound.
            utils.assertEqual(new BigDecimal(Math.log(a.doubleValue())), openOutput, 8);
          }
        }
      };
    }
  }

  public static class TestSqrt<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs = Stream.of(1000_000.0, 1_000.0 + 0.5 * Math.pow(2.0, DEFAULT_PRECISION), 40.1)
          .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SReal>> closed1 =
                openInputs.stream().map(producer.realNumeric()::known).collect(Collectors.toList());

            List<DRes<SReal>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(producer.realAdvanced().sqrt(inputX));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(producer.realNumeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          RealTestUtils utils = new RealTestUtils();
          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal expected = new BigDecimal(Math.sqrt(openInputs.get(idx).doubleValue()));
            utils.assertEqual(expected, openOutput, DEFAULT_PRECISION / 2);
          }
        }
      };
    }
  }
}
