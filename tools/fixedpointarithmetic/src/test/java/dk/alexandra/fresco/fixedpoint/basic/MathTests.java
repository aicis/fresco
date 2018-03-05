package dk.alexandra.fresco.fixedpoint.basic;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.decimal.RealNumeric;
import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.decimal.fixed.FixedNumeric;
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

  public static class TestExp<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    int precision = 16;
    
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
            RealNumeric fixed = new FixedNumeric(root, precision);
            DRes<SReal> secret = fixed.numeric().input(input, 1);
            DRes<SReal> result = fixed.advanced().exp(secret);
            return fixed.numeric().open(result);
          };
          BigDecimal output = runApplication(testApplication);
          int expectedPrecision = precision - 1;
          System.out.println(expectedPrecision);
          new TestUtils().assertEqual(expected,
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
                RealNumeric fixed = new FixedNumeric(seq);

                List<DRes<SReal>> result = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                  result.add(fixed.advanced().random());
                }

                List<DRes<BigDecimal>> opened =
                    result.stream().map(fixed.numeric()::open).collect(Collectors.toList());
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
      int precision = 16;

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            RealNumeric numeric = new FixedNumeric(producer, precision);

            List<DRes<SReal>> closed1 =
                openInputs.stream().map(numeric.numeric()::known).collect(Collectors.toList());

            List<DRes<SReal>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(numeric.advanced().log(inputX));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(numeric.numeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          TestUtils utils = new TestUtils();
          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);
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
      int precision = 16;
      List<BigDecimal> openInputs = Stream.of(1000_000.0, 1_000.0 + 0.5 * Math.pow(2.0, precision), 40.1)
          .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            RealNumeric numeric = new FixedNumeric(producer, precision);

            List<DRes<SReal>> closed1 =
                openInputs.stream().map(numeric.numeric()::known).collect(Collectors.toList());

            List<DRes<SReal>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(numeric.advanced().sqrt(inputX));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(numeric.numeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          TestUtils utils = new TestUtils();
          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal expected = new BigDecimal(Math.sqrt(openInputs.get(idx).doubleValue()));
            utils.assertEqual(expected, openOutput, precision / 2);
          }
        }
      };
    }
  }
}
