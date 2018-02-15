package dk.alexandra.fresco.fixedpoint.basic;

import dk.alexandra.fresco.decimal.BasicRealNumeric;
import dk.alexandra.fresco.decimal.RealNumeric;
import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.decimal.fixed.FixedNumeric;
import dk.alexandra.fresco.decimal.floating.BasicFloatNumeric;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;

public class BasicFloatingPointTests {

  private static boolean isEqual(BigDecimal a, BigDecimal b) {
    if (a.compareTo(b) != 0) {
      System.out.println(a + " != " + b);
      return false;
    }
    return true;
  }
  
    public static class TestInput<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

  @Override
  public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
    BigDecimal value = BigDecimal.valueOf(10.00100);
    return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
      @Override
      public void test() throws Exception {
        Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
          BasicRealNumeric fixed = new BasicFloatNumeric(producer);

          DRes<SReal> input = fixed.input(value, 1);

          return fixed.open(input);
        };
        BigDecimal output = runApplication(app);
        System.out.println(output);
        Assert.assertTrue(output.compareTo(value) == 0);
      }
    };
  }
}

  public static class TestUseSInt<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      BigInteger value = BigInteger.valueOf(11);

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            BasicRealNumeric fixed = new BasicFloatNumeric(producer);

            DRes<SInt> sint = producer.numeric().input(value, 1);
            DRes<SReal> input = fixed.fromSInt(sint);

            return fixed.open(input);
          };
          BigDecimal output = runApplication(app);
          Assert.assertTrue(output.compareTo(new BigDecimal(value)) == 0);
        }
      };
    }
  }

//  public static class TestOpenToParty<ResourcePoolT extends ResourcePool>
//      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {
//
//    @Override
//    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
//      BigDecimal value = BigDecimal.valueOf(10.00100);
//      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
//        @Override
//        public void test() throws Exception {
//          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
//            FixedNumeric fixed = new DefaultFixedNumeric(producer, 5);
//
//            DRes<SFixed> input = fixed.numeric().input(value, 1);
//
//            return fixed.numeric().open(input, 1);
//          };
//          BigDecimal output = runApplication(app);
//
//          if (conf.getMyId() == 1) {
//            Assert.assertEquals(value.setScale(5, RoundingMode.DOWN), output);
//          } else {
//            Assert.assertNull(output);
//          }
//
//        }
//      };
//    }
//  }

  public static class TestKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            BasicRealNumeric fixed = new BasicFloatNumeric(producer);

            DRes<SReal> input = fixed.known(value);

            return fixed.open(input);
          };
          BigDecimal output = runApplication(app);

          Assert.assertTrue(output.compareTo(value) == 0);
        }
      };
    }
  }

  public static class TestAddKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      BigDecimal value2 = BigDecimal.valueOf(20.1);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            BasicRealNumeric fixed = new BasicFloatNumeric(producer);

            DRes<SReal> input = fixed.input(value, 1);
            DRes<SReal> sum = fixed.add(value2, input);

            return fixed.open(sum);
          };
          BigDecimal output = runApplication(app);

          Assert.assertTrue(output.compareTo(value.add(value2)) == 0);
        }
      };
    }
  }

  public static class TestAddSecret<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      BigDecimal value2 = BigDecimal.valueOf(20.1);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            BasicRealNumeric fixed = new BasicFloatNumeric(producer);

            DRes<SReal> input = fixed.input(value, 1);
            DRes<SReal> input2 = fixed.input(value2, 1);
            DRes<SReal> sum = fixed.add(input2, input);

            return fixed.open(sum);
          };
          BigDecimal output = runApplication(app);

          Assert.assertTrue(output.compareTo(value.add(value2)) == 0);
        }
      };
    }
  }

  public static class TestSubtractSecret<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      BigDecimal value2 = BigDecimal.valueOf(20.1);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            BasicRealNumeric fixed = new BasicFloatNumeric(producer);

            DRes<SReal> input = fixed.input(value, 1);
            DRes<SReal> input2 = fixed.input(value2, 1);
            DRes<SReal> diff = fixed.sub(input, input2);

            return fixed.open(diff);
          };
          BigDecimal output = runApplication(app);

          Assert.assertTrue(output.compareTo(value.subtract(value2)) == 0);
        }
      };
    }
  }

  public static class TestSubKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      BigDecimal value2 = BigDecimal.valueOf(20.1);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            BasicRealNumeric fixed = new BasicFloatNumeric(producer);

            DRes<SReal> input = fixed.input(value, 1);
            DRes<SReal> diff = fixed.sub(input, value2);

            return fixed.open(diff);
          };
          BigDecimal output = runApplication(app);

          Assert.assertTrue(output.compareTo(value.subtract(value2)) == 0);
        }
      };
    }
  }

  public static class TestSubKnown2<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100).setScale(5);
      BigDecimal value2 = BigDecimal.valueOf(20.1);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            BasicRealNumeric fixed = new BasicFloatNumeric(producer);

            DRes<SReal> input2 = fixed.input(value2, 1);
            DRes<SReal> diff = fixed.sub(value, input2);

            return fixed.open(diff);
          };
          BigDecimal output = runApplication(app);

          Assert.assertTrue(output.compareTo(value.subtract(value2)) == 0);
        }
      };
    }
  }

  public static class TestMultSecret<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      BigDecimal value2 = BigDecimal.valueOf(0.2);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            BasicRealNumeric fixed = new BasicFloatNumeric(producer);

            DRes<SReal> input = fixed.input(value, 1);
            DRes<SReal> input2 = fixed.input(value2, 1);
            DRes<SReal> product = fixed.mult(input, input2);

            return fixed.open(product);
          };
          BigDecimal output = runApplication(app);
          Assert.assertTrue(output.compareTo(value.multiply(value2)) == 0);
        }
      };
    }
  }

  public static class TestMultKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      BigDecimal value2 = BigDecimal.valueOf(0.2);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            BasicRealNumeric fixed = new BasicFloatNumeric(producer);

            DRes<SReal> input2 = fixed.input(value2, 1);
            DRes<SReal> product = fixed.mult(value, input2);

            return fixed.open(product);
          };
          BigDecimal output = runApplication(app);
          Assert.assertTrue(output.compareTo(value.multiply(value2)) == 0);
        }
      };
    }
  }

  public static class TestDivisionSecret<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.001);
      BigDecimal value2 = BigDecimal.valueOf(0.21);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            BasicRealNumeric fixed = new BasicFloatNumeric(producer);

            DRes<SReal> input = fixed.input(value, 1);
            DRes<SReal> input2 = fixed.input(value2, 1);
            DRes<SReal> product = fixed.div(input, input2);

            return fixed.open(product);
          };
          BigDecimal output = runApplication(app);
          Assert.assertTrue(isEqual(value.divide(value2, output.scale(), RoundingMode.DOWN), output));
        }
      };
    }
  }

  public static class TestDivisionKnownDivisor<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      BigDecimal value2 = BigDecimal.valueOf(0.21);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            BasicRealNumeric fixed = new BasicFloatNumeric(producer);

            DRes<SReal> input = fixed.input(value, 1);
            DRes<SReal> product = fixed.div(input, value2);

            return fixed.open(product);
          };
          BigDecimal output = runApplication(app);
          Assert.assertTrue(isEqual(value.divide(value2, output.scale(), RoundingMode.DOWN), output));
        }
      };
    }
  }

  public static class TestMult<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs =
          Stream.of(1.223, 222.23, 5.59703, 0.004, 5.90, 6.0, 0.0007, 0.121998, 9.999999)
              .map(BigDecimal::valueOf).collect(Collectors.toList());
      List<BigDecimal> openInputs2 =
          Stream.of(1.000, 1.0000, 0.22211, 100.1, 11.0, .07, 0.0005, 10.00112, 999991.0)
              .map(BigDecimal::valueOf).collect(Collectors.toList());
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            BasicRealNumeric fixed = new BasicFloatNumeric(producer);

            List<DRes<SReal>> closed1 =
                openInputs.stream().map(fixed::known).collect(Collectors.toList());
            List<DRes<SReal>> closed2 =
                openInputs2.stream().map(fixed::known).collect(Collectors.toList());

            List<DRes<SReal>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(fixed.mult(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(fixed::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);
            BigDecimal b = openInputs2.get(idx);
            BigDecimal expected = a.multiply(b);
            Assert.assertTrue(isEqual(expected, openOutput));
          }
        }
      };
    }
  }

  public static class TestAdd<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      final int precision = 3;
      List<BigDecimal> openInputs =
          Stream.of(1.223, 222.23, 5.59703, 0.004, 5.90, 6.0, 0.0007, 0.121998, 9.999999)
              .map(BigDecimal::valueOf).collect(Collectors.toList());
      List<BigDecimal> openInputs2 =
          Stream.of(1.000, 1.0000, 0.22211, 100.1, 11.0, .07, 0.0005, 10.00112, 999991.0)
              .map(BigDecimal::valueOf).collect(Collectors.toList());
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            RealNumeric fixed = new FixedNumeric(producer, precision);

            List<DRes<SReal>> closed1 =
                openInputs.stream().map(fixed.numeric()::known).collect(Collectors.toList());
            List<DRes<SReal>> closed2 =
                openInputs2.stream().map(fixed.numeric()::known).collect(Collectors.toList());

            List<DRes<SReal>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(fixed.numeric().add(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(fixed.numeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx).setScale(precision, RoundingMode.DOWN);
            BigDecimal b = openInputs2.get(idx).setScale(precision, RoundingMode.DOWN);
            Assert.assertTrue(openOutput.compareTo(a.add(b)) == 0);
          }
        }
      };
    }
  }

  public static class TestDiv<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      final int precision = 5;
      List<BigDecimal> openInputs =
          Stream.of(1.223, 222.23, 5.59703, 0.004, 5.90, 6.0, 0.00007, 0.121998, 9.999999)
              .map(BigDecimal::valueOf).collect(Collectors.toList());
      List<BigDecimal> openInputs2 =
          Stream.of(1.000, 1.0000, 0.22211, 100.1, 11.0, 0.5, 0.0005, 10.00112, 999991.0)
              .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            RealNumeric fixed = new FixedNumeric(producer, precision);

            List<DRes<SReal>> closed1 =
                openInputs.stream().map(fixed.numeric()::known).collect(Collectors.toList());
            List<DRes<SReal>> closed2 =
                openInputs2.stream().map(fixed.numeric()::known).collect(Collectors.toList());

            List<DRes<SReal>> result = new ArrayList<>();
            for (DRes<SReal> inputX : closed1) {
              result.add(fixed.numeric().div(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(fixed.numeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);
            BigDecimal b = openInputs2.get(idx);
            BigDecimal expected = a.divide(b, openOutput.scale(), RoundingMode.DOWN);
            Assert.assertTrue(isEqual(expected, openOutput));
          }
        }
      };
    }
  }
}
