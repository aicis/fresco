package dk.alexandra.fresco.fixedpoint.basic;

import dk.alexandra.fresco.fixedpoint.FixedNumeric;
import dk.alexandra.fresco.fixedpoint.SFixed;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;
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
            //Numeric numeric = producer.numeric();
            
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

}
