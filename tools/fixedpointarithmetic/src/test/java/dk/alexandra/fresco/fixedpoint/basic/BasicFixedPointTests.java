package dk.alexandra.fresco.fixedpoint.basic;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.fixedpoint.SIntWrapperFixedNumeric;
import dk.alexandra.fresco.fixedpoint.FixedNumeric;
import dk.alexandra.fresco.fixedpoint.SFixed;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;

public class BasicFixedPointTests {

  public static class TestInput<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);

            return numeric.open(input);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.setScale(5, RoundingMode.DOWN), output);
        }
      };
    }
  }

  public static class TestOpenToParty<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);

            return numeric.open(input, 1);
          };
          BigDecimal output = runApplication(app);

          if (conf.getMyId() == 1) {
            Assert.assertEquals(value.setScale(5, RoundingMode.DOWN), output);
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
      BigDecimal value = BigDecimal.valueOf(10.00100);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.known(value);

            return numeric.open(input);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.setScale(5, RoundingMode.DOWN), output);
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
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);
            DRes<SFixed> sum = numeric.add(value2, input);

            return numeric.open(sum);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.add(value2)
              .setScale(5, RoundingMode.DOWN), output);
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
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);
            DRes<SFixed> input2 = numeric.input(value2, 1);
            DRes<SFixed> sum = numeric.add(input2, input);

            return numeric.open(sum);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.add(value2)
              .setScale(5, RoundingMode.DOWN), output);
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
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);
            DRes<SFixed> input2 = numeric.input(value2, 1);
            DRes<SFixed> diff = numeric.sub(input, input2);

            return numeric.open(diff);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.subtract(value2)
              .setScale(5, RoundingMode.DOWN), output);
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
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);
            DRes<SFixed> diff = numeric.sub(input, value2);

            return numeric.open(diff);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.subtract(value2)
              .setScale(5, RoundingMode.DOWN), output);
        }
      };
    }
  }

  public static class TestSubKnown2<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      BigDecimal value2 = BigDecimal.valueOf(20.1);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, 5);

            DRes<SFixed> input2 = numeric.input(value2, 1);
            DRes<SFixed> diff = numeric.sub(value, input2);

            return numeric.open(diff);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.subtract(value2)
              .setScale(5, RoundingMode.DOWN), output);
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
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);
            DRes<SFixed> input2 = numeric.input(value2, 1);
            DRes<SFixed> product = numeric.mult(input, input2);

            return numeric.open(product);
          };
          BigDecimal output = runApplication(app);
          Assert.assertEquals(value.multiply(value2)
              .setScale(5, RoundingMode.DOWN), output);
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
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, 5);

            DRes<SFixed> input2 = numeric.input(value2, 1);
            DRes<SFixed> product = numeric.mult(value, input2);

            return numeric.open(product);
          };
          BigDecimal output = runApplication(app);
          Assert.assertEquals(value.multiply(value2)
              .setScale(5, RoundingMode.DOWN), output);
        }
      };
    }
  }

  public static class TestDivisionSecret<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      BigDecimal value2 = BigDecimal.valueOf(0.2);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);
            DRes<SFixed> input2 = numeric.input(value2, 1);
            DRes<SFixed> product = numeric.div(input, input2);

            return numeric.open(product);
          };
          BigDecimal output = runApplication(app);
          Assert.assertEquals(value.divide(value2)
              .setScale(5, RoundingMode.DOWN), output);
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
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);
            DRes<SFixed> product = numeric.div(input, value2);

            return numeric.open(product);
          };
          BigDecimal output = runApplication(app);
          Assert.assertEquals(value.divide(value2)
              .setScale(5, RoundingMode.DOWN), output);
        }
      };
    }
  }  

  public static class TestMult<ResourcePoolT extends ResourcePool>
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
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, precision);

            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(numeric::known).collect(Collectors.toList());
            List<DRes<SFixed>> closed2 =
                openInputs2.stream().map(numeric::known).collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for(DRes<SFixed> inputX : closed1) {
              result.add(numeric.mult(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(numeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx).setScale(precision, RoundingMode.DOWN); 
            BigDecimal b = openInputs2.get(idx).setScale(precision, RoundingMode.DOWN);
            assertThat(openOutput, 
                is(a.multiply(b).setScale(precision, RoundingMode.DOWN)));
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
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, precision);

            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(numeric::known).collect(Collectors.toList());
            List<DRes<SFixed>> closed2 =
                openInputs2.stream().map(numeric::known).collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for(DRes<SFixed> inputX : closed1) {
              result.add(numeric.add(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(numeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx).setScale(precision, RoundingMode.DOWN); 
            BigDecimal b = openInputs2.get(idx).setScale(precision, RoundingMode.DOWN);
            assertThat(openOutput, 
                is(a.add(b).setScale(precision, RoundingMode.DOWN)));
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
            FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(producer, precision);

            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(numeric::known).collect(Collectors.toList());
            List<DRes<SFixed>> closed2 =
                openInputs2.stream().map(numeric::known).collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for(DRes<SFixed> inputX : closed1) {
              result.add(numeric.div(inputX, closed2.get(closed1.indexOf(inputX))));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(numeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx).setScale(precision, RoundingMode.DOWN); 
            BigDecimal b = openInputs2.get(idx).setScale(precision, RoundingMode.DOWN);
            assertThat(openOutput, 
                is(a.divide(b, RoundingMode.DOWN).setScale(precision, RoundingMode.DOWN)));
          }
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
                FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(seq, 5);

                List<DRes<SFixed>> result = new ArrayList<>();
                for(int i = 0; i< 100; i++) {
                  result.add(numeric.random());
                }
                return () -> result;
              }).seq((seq, dat) -> {
                FixedNumeric<SFixed> numeric = new SIntWrapperFixedNumeric(seq, 5);
                List<DRes<BigDecimal>> opened =
                    dat.stream().map(numeric::open).collect(Collectors.toList());
                return () -> opened.stream().map(DRes::out).collect(Collectors.toList());  
              });

              List<BigDecimal> output = runApplication(app);
              BigDecimal sum = BigDecimal.ZERO;
              BigDecimal min = BigDecimal.ONE;
              BigDecimal max = BigDecimal.ZERO;
              for(BigDecimal random : output){
                sum = sum.add(random);
                if(random.compareTo(min) == -1) {
                  min = random;
                }
                if(random.compareTo(max) == 1) {
                  max = random;
                }
                assertTrue(BigDecimal.ONE.compareTo(random) >= 0);
                assertTrue(BigDecimal.ZERO.compareTo(random) <= 0);
              }
        }
      };
    }
  }
}