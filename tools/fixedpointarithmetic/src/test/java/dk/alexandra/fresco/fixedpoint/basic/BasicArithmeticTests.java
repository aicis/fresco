package dk.alexandra.fresco.fixedpoint.basic;

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
import org.junit.Assert;

public class BasicArithmeticTests {

  public static class TestInput<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      BigDecimal value = BigDecimal.valueOf(10.00100);
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            FixedNumeric numeric = new FixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);

            return numeric.open(input);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.setScale(5, RoundingMode.HALF_UP), output);
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
            FixedNumeric numeric = new FixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.known(value);

            return numeric.open(input);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.setScale(5, RoundingMode.HALF_UP), output);
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
            FixedNumeric numeric = new FixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);
            DRes<SFixed> sum = numeric.add(value2, input);

            return numeric.open(sum);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.add(value2).setScale(5, RoundingMode.HALF_UP), output);
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
            //Numeric numeric = producer.numeric();

            FixedNumeric numeric = new FixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);
            DRes<SFixed> input2 = numeric.input(value2, 1);
            DRes<SFixed> sum = numeric.add(input2, input);

            return numeric.open(sum);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.add(value2).setScale(5, RoundingMode.HALF_UP), output);
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
            //Numeric numeric = producer.numeric();

            FixedNumeric numeric = new FixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);
            DRes<SFixed> input2 = numeric.input(value2, 1);
            DRes<SFixed> sum = numeric.sub(input, input2);

            return numeric.open(sum);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.subtract(value2).setScale(5, RoundingMode.HALF_UP), output);
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
            //Numeric numeric = producer.numeric();

            FixedNumeric numeric = new FixedNumeric(producer, 5);

            DRes<SFixed> input = numeric.input(value, 1);
            DRes<SFixed> sum = numeric.sub(input, value2);

            return numeric.open(sum);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.subtract(value2).setScale(5, RoundingMode.HALF_UP), output);
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
            //Numeric numeric = producer.numeric();

            FixedNumeric numeric = new FixedNumeric(producer, 5);

            DRes<SFixed> input2 = numeric.input(value2, 1);
            DRes<SFixed> sum = numeric.sub(value, input2);

            return numeric.open(sum);
          };
          BigDecimal output = runApplication(app);

          Assert.assertEquals(value.subtract(value2).setScale(5, RoundingMode.HALF_UP), output);
        }
      };
    }
  }
}
