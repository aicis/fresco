package dk.alexandra.fresco.lib.real;

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
import java.util.stream.Stream;
import org.junit.Assert;

public class BasicFixedPointTests {

  // SReals are implemented as binary fixed point numbers with 16 bits precision.
  private static final int DEFAULT_PRECISION = 16;

  public static class TestInput<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      int precision = 16;
      List<BigDecimal> value = Arrays
          .asList(10.000001, 5.9, 11.0, 0.0001, 100000000.0001, 1.5 * Math.pow(2.0, -precision))
          .stream().map(BigDecimal::valueOf).collect(Collectors.toList());
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SReal>> inputs = value.stream().map(x -> producer.realNumeric().input(x, 1))
                .collect(Collectors.toList());
            List<DRes<BigDecimal>> opened =
                inputs.stream().map(producer.realNumeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          new RealTestUtils().assertEqual(value, output, DEFAULT_PRECISION + 1);
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
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SInt>> sints =
                value.stream().map(producer.numeric()::known).collect(Collectors.toList());
            List<DRes<SReal>> inputs =
                sints.stream().map(producer.realNumeric()::fromSInt).collect(Collectors.toList());
            List<DRes<BigDecimal>> opened =
                inputs.stream().map(producer.realNumeric()::open).collect(Collectors.toList());

            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          new RealTestUtils().assertEqual(
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
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            DRes<SReal> input = producer.realNumeric().input(value, 1);
            return producer.realNumeric().open(input, 1);
          };
          BigDecimal output = runApplication(app);

          if (conf.getMyId() == 1) {
            new RealTestUtils().assertEqual(value, output, DEFAULT_PRECISION + 1);
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

      List<BigDecimal> value = Arrays
          .asList(10.000001, 5.9, 11.0, 0.0001, 100000000.0001,
              0.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .stream().map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SReal>> inputs =
                value.stream().map(producer.realNumeric()::known).collect(Collectors.toList());
            List<DRes<BigDecimal>> opened =
                inputs.stream().map(producer.realNumeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          new RealTestUtils().assertEqual(value, output, DEFAULT_PRECISION + 1);
        }
      };
    }
  }

  public static class TestAddKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      List<BigDecimal> openInputs = Stream
          .of(1.0001, 0.000_000_001, -1_000_000_000_000.000_100_000_000_1,
              0.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .map(BigDecimal::valueOf).collect(Collectors.toList());
      List<BigDecimal> openInputs2 = Stream
          .of(-1.0001, 1_000_000_000.0, -1_000_000_000_000.000_100_000_000_1,
              0.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SReal>> closed1 =
                openInputs.stream().map(producer.realNumeric()::known).collect(Collectors.toList());

            List<DRes<SReal>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(
                  producer.realNumeric().add(openInputs2.get(closed1.indexOf(inputX)), inputX));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(producer.realNumeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);
            BigDecimal b = openInputs2.get(idx);
            new RealTestUtils().assertEqual(a.add(b), openOutput, DEFAULT_PRECISION);
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
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SReal>> closed1 =
                openInputs.stream().map(producer.realNumeric()::known).collect(Collectors.toList());
            List<DRes<SReal>> closed2 = openInputs2.stream().map(producer.realNumeric()::known)
                .collect(Collectors.toList());

            List<DRes<SReal>> results = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              results.add(producer.realNumeric().sub(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                results.stream().map(producer.realNumeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);
            BigDecimal b = openInputs2.get(idx);
            new RealTestUtils().assertEqual(a.subtract(b), openOutput, DEFAULT_PRECISION);
          }
        }
      };
    }
  }

  public static class TestSubKnown<ResourcePoolT extends ResourcePool>
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
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SReal>> closed1 =
                openInputs.stream().map(producer.realNumeric()::known).collect(Collectors.toList());

            List<DRes<SReal>> results = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              results.add(
                  producer.realNumeric().sub(inputX, openInputs2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                results.stream().map(producer.realNumeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);
            BigDecimal b = openInputs2.get(idx);
            new RealTestUtils().assertEqual(a.subtract(b), openOutput, DEFAULT_PRECISION);
          }
        }
      };
    }
  }

  public static class TestMultKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs =
          Stream.of(1.223, 222.23, 5.59703, 0.004, 5.90, 6.0, 0.0007, 0.1298, 9.99)
              .map(BigDecimal::valueOf).collect(Collectors.toList());
      List<BigDecimal> openInputs2 =
          Stream.of(1.000, 1.0000, 0.22211, 100.1, 11.0, .07, 0.0005, 10.0012, 999.0101)
              .map(BigDecimal::valueOf).collect(Collectors.toList());
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {

            List<DRes<SReal>> closed1 =
                openInputs.stream().map(producer.realNumeric()::known).collect(Collectors.toList());

            List<DRes<SReal>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(
                  producer.realNumeric().mult(openInputs2.get(closed1.indexOf(inputX)), inputX));
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
            BigDecimal b = openInputs2.get(idx);

            // There should be no truncation after just one multiplication
            utils.assertEqual(a.multiply(b), openOutput,
                DEFAULT_PRECISION - Math.max(utils.floorLog2(a), utils.floorLog2(b)));
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
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SReal>> closed1 =
                openInputs.stream().map(producer.realNumeric()::known).collect(Collectors.toList());

            DRes<SReal> result = producer.realNumeric().known(BigDecimal.ONE);
            for (DRes<SReal> inputX : closed1) {
              result = producer.realNumeric().mult(result, inputX);
            }

            DRes<BigDecimal> opened = producer.realNumeric().open(result);
            return opened;
          };
          BigDecimal output = runApplication(app);

          RealTestUtils utils = new RealTestUtils();
          BigDecimal expected = BigDecimal.ONE.setScale(DEFAULT_PRECISION / 2);
          for (BigDecimal openOutput : openInputs) {
            expected = expected.multiply(openOutput);
          }
          // We lose precision for each multiplication.
          utils.assertEqual(output, expected, DEFAULT_PRECISION - 8);
        }
      };
    }
  }

  public static class TestDivisionKnownDivisor<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      BigDecimal value = BigDecimal.valueOf(10.00100);
      BigDecimal value2 = BigDecimal.valueOf(0.2);

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {

            DRes<SReal> input = producer.realNumeric().input(value, 1);
            DRes<SReal> product = producer.realNumeric().div(input, value2);

            return producer.realNumeric().open(product);
          };
          BigDecimal output = runApplication(app);
          RealTestUtils utils = new RealTestUtils();
          utils.assertEqual(value.divide(value2), output, DEFAULT_PRECISION - 2
              - Math.max(0, utils.floorLog2(value) - utils.floorLog2(value2)));
        }
      };
    }
  }

  public static class TestMult<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs = Stream
          .of(1.223, 222.23, 5.59703, 0.004, 5.90, 6.0, 0.0007, 0.1298,
              1.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .map(BigDecimal::valueOf).collect(Collectors.toList());
      List<BigDecimal> openInputs2 = Stream
          .of(1.000, 1.0000, 0.22211, 100.1, 11.0, .07, 0.0005, 10.0012,
              3.5 * Math.pow(2.0, -DEFAULT_PRECISION))
          .map(BigDecimal::valueOf).collect(Collectors.toList());
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SReal>> closed1 =
                openInputs.stream().map(producer.realNumeric()::known).collect(Collectors.toList());
            List<DRes<SReal>> closed2 = openInputs2.stream().map(producer.realNumeric()::known)
                .collect(Collectors.toList());

            List<DRes<SReal>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(producer.realNumeric().mult(inputX, closed2.get(closed1.indexOf(inputX))));
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
            BigDecimal b = openInputs2.get(idx);

            utils.assertEqual(a.multiply(b), openOutput,
                DEFAULT_PRECISION - 1 - Math.max(utils.floorLog2(a), utils.floorLog2(b)));
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
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SReal>> closed1 =
                openInputs.stream().map(producer.realNumeric()::known).collect(Collectors.toList());
            List<DRes<SReal>> closed2 = openInputs2.stream().map(producer.realNumeric()::known)
                .collect(Collectors.toList());

            List<DRes<SReal>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(producer.realNumeric().add(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(producer.realNumeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);
            BigDecimal b = openInputs2.get(idx);
            new RealTestUtils().assertEqual(a.add(b), openOutput, DEFAULT_PRECISION);
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
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SReal>> closed1 =
                openInputs.stream().map(producer.realNumeric()::known).collect(Collectors.toList());
            List<DRes<SReal>> closed2 = openInputs2.stream().map(producer.realNumeric()::known)
                .collect(Collectors.toList());

            List<DRes<SReal>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(producer.realNumeric().div(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(producer.realNumeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          RealTestUtils utils = new RealTestUtils();
          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx).setScale(DEFAULT_PRECISION, RoundingMode.HALF_UP);
            BigDecimal b = openInputs2.get(idx).setScale(DEFAULT_PRECISION, RoundingMode.HALF_UP);
            System.out.println(a + "/" + b);
            BigDecimal expected = a.divide(b, RoundingMode.HALF_UP);
            // It's hard to approximate error after division since it depends on the input sizes. We
            // use 4 for now.
            utils.assertEqual(expected, openOutput, 4);
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
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> {

            List<DRes<SReal>> closed1 =
                openInputs.stream().map(producer.realNumeric()::known).collect(Collectors.toList());
            List<DRes<SReal>> closed2 = openInputs2.stream().map(producer.realNumeric()::known)
                .collect(Collectors.toList());

            List<DRes<SInt>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(producer.realNumeric().leq(inputX, closed2.get(closed1.indexOf(inputX))));
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
            System.out.println(a + " <= " + b + " = " + (output.get(i).intValue() == 1));

            Assert.assertEquals(expected, result);
          }
        }
      };
    }
  }
}
