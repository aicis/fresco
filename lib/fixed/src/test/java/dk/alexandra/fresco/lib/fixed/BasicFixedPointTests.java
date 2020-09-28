package dk.alexandra.fresco.lib.fixed;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Assert;

/**
 * Basic tests of computation with fixed point numbers.
 * <p>
 * NOTE: these tests all assume a precision of {@link BasicFixedPointTests#DEFAULT_PRECISION}.
 * </p>
 */
public class BasicFixedPointTests {

  /**
   * The precision assumed in all tests.
   */
  public static final int DEFAULT_PRECISION = 16;

  public static class TestInput<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> value = Stream
          .of(10.000001, 5.9, 11.0, 0.0001, 100000000.0001, 1.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .map(BigDecimal::valueOf).collect(Collectors.toList());
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);
            List<DRes<SFixed>> inputs = value.stream().map(x -> fixedNumeric.input(x, 1))
                .collect(Collectors.toList());
            List<DRes<BigDecimal>> opened =
                inputs.stream().map(fixedNumeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);
          FixedTestUtils.assertEqual(value, output, DEFAULT_PRECISION + 1);
        }
      };
    }
  }

  public static class TestUseSInt<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      List<BigInteger> value = Arrays.asList(BigInteger.ONE, BigInteger.ONE.negate(),
          BigInteger.ONE.shiftLeft(200).negate());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);
            List<DRes<SInt>> sints =
                value.stream().map(producer.numeric()::known).collect(Collectors.toList());
            List<DRes<SFixed>> inputs =
                sints.stream().map(fixedNumeric::fromSInt).collect(Collectors.toList());
            List<DRes<BigDecimal>> opened =
                inputs.stream().map(fixedNumeric::open).collect(Collectors.toList());

            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);
          FixedTestUtils.assertEqual(
              value.stream().map(BigDecimal::new).collect(Collectors.toList()), output,
              DEFAULT_PRECISION + 1);
        }
      };
    }
  }

  public static class TestOpenToParty<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      BigDecimal value = BigDecimal.ONE;

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);
            DRes<SFixed> input = fixedNumeric.input(value, 1);
            return fixedNumeric.open(input, 1);
          };
          BigDecimal output = runApplication(app);

          if (conf.getMyId() == 1) {
            FixedTestUtils.assertEqual(value, output, DEFAULT_PRECISION + 1);
          } else {
            Assert.assertNull(output);
          }
        }
      };
    }
  }

  public static class TestKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      List<Double> value = Stream
          .of(10.000001, 5.9, 11.0, 0.0001, 100000000.0001, 0.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);
            List<DRes<SFixed>> inputs =
                value.stream().map(fixedNumeric::known).collect(Collectors.toList());
            List<DRes<BigDecimal>> opened =
                inputs.stream().map(fixedNumeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (int i = 0; i < output.size(); i++) {
            FixedTestUtils.assertEqual(value.get(i), output.get(i), DEFAULT_PRECISION + 1);
          }
        }
      };
    }
  }

  public static class TestAddKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      List<Double> openInputs =
          Stream.of(1.0001, 0.000_000_001, -1_000_000_000_000.000_100_000_000_1,
              0.5 * Math.pow(2.0, -DEFAULT_PRECISION)).collect(Collectors.toList());
      List<Double> openInputs2 =
          Stream.of(-1.0001, 1_000_000_000.0, -1_000_000_000_000.000_100_000_000_1,
              0.5 * Math.pow(2.0, -DEFAULT_PRECISION)).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);
            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(fixedNumeric::known).collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              result.add(
                  fixedNumeric.add(openInputs2.get(closed1.indexOf(inputX)), inputX));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(fixedNumeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            double a = openInputs.get(idx);
            double b = openInputs2.get(idx);
            FixedTestUtils.assertEqual(BigDecimal.valueOf(a + b), openOutput, DEFAULT_PRECISION);
          }
        }
      };
    }
  }

  public static class TestSubtractSecret<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      List<BigDecimal> openInputs = Stream
          .of(1.000_2, 0.000_000_001, -1_000_000_000_000.000_100_000_000_1,
              2.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .map(BigDecimal::valueOf).collect(Collectors.toList());
      List<BigDecimal> openInputs2 = Stream
          .of(1.000_1, 1_000_000_000.0, -1_000_000_000_000.000_100_000_000_1,
              Math.pow(2.0, -DEFAULT_PRECISION))
          .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);
            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(fixedNumeric::known).collect(Collectors.toList());
            List<DRes<SFixed>> closed2 = openInputs2.stream().map(fixedNumeric::known)
                .collect(Collectors.toList());

            List<DRes<SFixed>> results = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              results.add(fixedNumeric.sub(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                results.stream().map(fixedNumeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);
            BigDecimal b = openInputs2.get(idx);
            FixedTestUtils.assertEqual(a.subtract(b), openOutput, DEFAULT_PRECISION);
          }
        }
      };
    }
  }

  public static class TestSubKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      List<Double> openInputs = Stream
          .of(1.000_2, 0.000_000_001, -1_000_000_000_000.000_100_000_000_1,
              2.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .collect(Collectors.toList());
      List<Double> openInputs2 = Stream
          .of(1.000_1, 1_000_000_000.0, -1_000_000_000_000.000_100_000_000_1,
              Math.pow(2.0, -DEFAULT_PRECISION))
          .collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);
            List<DRes<SFixed>> closed =
                openInputs.stream().map(fixedNumeric::known).collect(Collectors.toList());
            List<DRes<BigDecimal>> opened = Stream
                .concat(
                    IntStream.range(0, closed.size()).mapToObj(
                        i -> fixedNumeric.sub(closed.get(i), openInputs2.get(i))),
                    IntStream.range(0, closed.size()).mapToObj(
                        i -> fixedNumeric.sub(openInputs2.get(i), closed.get(i))))
                .map(fixedNumeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);
          assertEquals(output.size(), openInputs.size() * 2);
          IntStream.range(0, openInputs.size()).forEach(idx -> {
            double a = openInputs.get(idx);
            double b = openInputs2.get(idx);
            FixedTestUtils.assertEqual(a-b, output.get(idx), DEFAULT_PRECISION);
          });

          IntStream.range(openInputs.size(), output.size()).forEach(idx -> {
            double a = openInputs.get(idx - openInputs.size());
            double b = openInputs2.get(idx - openInputs.size());
            FixedTestUtils.assertEqual(b-a, output.get(idx), DEFAULT_PRECISION);
          });
        }
      };
    }
  }

  public static class TestMultKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<Double> openInputs =
          Arrays.asList(1.223, 222.23, 5.59703, 0.004, 5.90, 6.0, 0.007, 0.1298, 9.99);
      List<Double> openInputs2 =
          Arrays.asList(1.000, 1.0000, 0.22211, 100.1, 11.0, .07, 0.005, 10.0012, 999.0101);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);

            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(fixedNumeric::known).collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              result.add(
                  fixedNumeric.mult(openInputs2.get(closed1.indexOf(inputX)), inputX));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(fixedNumeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);
          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);
            double a = openInputs.get(idx);
            double b = openInputs2.get(idx);
            // There should be no truncation after just one multiplication
            int precision = DEFAULT_PRECISION
                - Math.max(0, Math.max(FixedTestUtils.floorLog2(a), FixedTestUtils.floorLog2(b)));
            FixedTestUtils.assertEqual(a*b, openOutput, precision);
          }
        }
      };
    }
  }

  public static class TestRepeatedMultiplication<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs =
          Stream.of(1.223, 222.23, 5.59703, 0.004, 5.90, 6.0, 0.0007, 0.1298)
              .map(BigDecimal::valueOf).collect(Collectors.toList());
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);
            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(fixedNumeric::known).collect(Collectors.toList());

            DRes<SFixed> result = fixedNumeric.known(BigDecimal.ONE);
            for (DRes<SFixed> inputX : closed1) {
              result = fixedNumeric.mult(result, inputX);
            }

            DRes<BigDecimal> opened = fixedNumeric.open(result);
            return opened;
          };
          BigDecimal output = runApplication(app);

          BigDecimal expected = BigDecimal.ONE.setScale(DEFAULT_PRECISION / 2);
          for (BigDecimal openOutput : openInputs) {
            expected = expected.multiply(openOutput);
          }
          // We lose precision for each multiplication.
          FixedTestUtils.assertEqual(output, expected, DEFAULT_PRECISION - 8);
        }
      };
    }
  }

  public static class TestDivisionKnownDivisor<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      double value = 10.00100;
      double value2 = 0.2;

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);

            DRes<SFixed> input = fixedNumeric.input(value, 1);
            DRes<SFixed> product = fixedNumeric.div(input, value2);

            return fixedNumeric.open(product);
          };
          BigDecimal output = runApplication(app);
          int precision = DEFAULT_PRECISION - 2
              - Math.max(0, FixedTestUtils.floorLog2(value) - FixedTestUtils.floorLog2(value2));
          FixedTestUtils.assertEqual(value / value2, output, precision);
        }
      };
    }
  }

  public static class TestDivisionKnownNegativeDivisor<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      double value = 10.00100;
      double value2 = -1;

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);

            DRes<SFixed> input = fixedNumeric.input(value, 1);
            DRes<SFixed> product = fixedNumeric.div(input, value2);

            return fixedNumeric.open(product);
          };
          BigDecimal output = runApplication(app);
          int precision = DEFAULT_PRECISION - 2
              - Math.max(0, FixedTestUtils.floorLog2(value) - FixedTestUtils.floorLog2(value2));
          FixedTestUtils.assertEqual(value / value2, output, precision);
        }
      };
    }
  }

  public static class TestMult<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs = Stream
          .of(1.223, 222.23, 5.59703, 0.004, 5.90, 6.0, 0.007, 0.1298,
              1.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .map(BigDecimal::valueOf).collect(Collectors.toList());
      List<BigDecimal> openInputs2 = Stream
          .of(1.000, 1.0000, 0.22211, 100.1, 11.0, .07, 0.005, 10.0012,
              3.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .map(BigDecimal::valueOf).collect(Collectors.toList());
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);
            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(fixedNumeric::known).collect(Collectors.toList());
            List<DRes<SFixed>> closed2 = openInputs2.stream().map(fixedNumeric::known)
                .collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              result.add(fixedNumeric.mult(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(fixedNumeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);
            BigDecimal b = openInputs2.get(idx);

            int precision = DEFAULT_PRECISION - 1
                - Math.max(0, Math.max(FixedTestUtils.floorLog2(a), FixedTestUtils.floorLog2(b)));
            FixedTestUtils.assertEqual(a.multiply(b), openOutput, precision);
          }
        }
      };
    }
  }

  public static class TestAdd<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      List<BigDecimal> openInputs = Stream
          .of(1.223, 222.23, 5.59703, 0.004, 5.90, 6.0, 0.0007, 0.121998,
              0.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .map(BigDecimal::valueOf).collect(Collectors.toList());
      List<BigDecimal> openInputs2 = Stream
          .of(1.000, 1.0000, 0.22211, 100.1, 11.0, .07, 0.0005, 10.00112,
              0.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);
            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(fixedNumeric::known).collect(Collectors.toList());
            List<DRes<SFixed>> closed2 = openInputs2.stream().map(fixedNumeric::known)
                .collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              result.add(fixedNumeric.add(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(fixedNumeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);
            BigDecimal b = openInputs2.get(idx);
            FixedTestUtils.assertEqual(a.add(b), openOutput, DEFAULT_PRECISION);
          }
        }
      };
    }
  }

  public static class TestDiv<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      List<BigDecimal> openInputs = Stream
          .of(2.0, Math.pow(2.0, DEFAULT_PRECISION - 1), -4.0,
              2.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .map(BigDecimal::valueOf).collect(Collectors.toList());
      List<BigDecimal> openInputs2 =
          Stream.of(-1.0, 2.2, -2.1, 2.0).map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);
            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(fixedNumeric::known).collect(Collectors.toList());
            List<DRes<SFixed>> closed2 = openInputs2.stream().map(fixedNumeric::known)
                .collect(Collectors.toList());
            List<DRes<SFixed>> result = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              result.add(fixedNumeric.div(inputX, closed2.get(closed1.indexOf(inputX))));
            }
            List<DRes<BigDecimal>> opened =
                result.stream().map(fixedNumeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);
          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);
            BigDecimal a = openInputs.get(idx).setScale(DEFAULT_PRECISION, RoundingMode.HALF_UP);
            BigDecimal b = openInputs2.get(idx).setScale(DEFAULT_PRECISION, RoundingMode.HALF_UP);
            BigDecimal expected = a.divide(b, RoundingMode.HALF_UP);
            // It's hard to approximate error after division since it depends on the input sizes. We
            // use 4 for now.
            FixedTestUtils.assertEqual(expected, openOutput, 4);
          }
        }
      };
    }
  }

  public static class TestLeq<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs = Stream.of(1.1 * Math.pow(2.0, -DEFAULT_PRECISION + 1),
          Math.pow(2.0, DEFAULT_PRECISION - 1), -1.0).map(BigDecimal::valueOf)
          .collect(Collectors.toList());
      List<BigDecimal> openInputs2 = Stream
          .of(1.2 * Math.pow(2.0, -DEFAULT_PRECISION + 1),
              Math.pow(2.0, DEFAULT_PRECISION - 1) - 1.0, 1.0)
          .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(producer);

            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(fixedNumeric::known).collect(Collectors.toList());
            List<DRes<SFixed>> closed2 = openInputs2.stream().map(fixedNumeric::known)
                .collect(Collectors.toList());

            List<DRes<SInt>> result = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              result.add(fixedNumeric.leq(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigInteger>> opened =
                result.stream().map(producer.numeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigInteger> output = runApplication(app);

          for (int i = 0; i < output.size(); i++) {
            BigDecimal a = openInputs.get(i);
            BigDecimal b = openInputs2.get(i);
            int expected = (a.compareTo(b) != 1) ? 1 : 0;
            int result = output.get(i).intValue();
            Assert.assertEquals(expected, result);
          }
        }
      };
    }
  }
}
