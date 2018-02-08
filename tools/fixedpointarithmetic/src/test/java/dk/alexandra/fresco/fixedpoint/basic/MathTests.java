package dk.alexandra.fresco.fixedpoint.basic;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Assert;

import dk.alexandra.fresco.fixedpoint.DefaultFixedNumeric;
import dk.alexandra.fresco.fixedpoint.FixedNumeric;
import dk.alexandra.fresco.fixedpoint.SFixed;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public class MathTests {

  public static class TestExp<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          int precision = 5;
          double x = 2.1;
          BigDecimal input = BigDecimal.valueOf(x);
          BigDecimal expected = BigDecimal.valueOf(Math.exp(x)).setScale(precision, RoundingMode.DOWN);
          
          // functionality to be tested
          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = root -> {
            // close inputs
            FixedNumeric fixed = new DefaultFixedNumeric(root, precision);
            DRes<SFixed> secret = fixed.numeric().input(input, 1);
            DRes<SFixed> result = fixed.advanced().exp(secret);
            
            return fixed.numeric().open(result);
          };
          BigDecimal output = runApplication(testApplication);
          Assert.assertEquals(expected, output);
        }
      };
    }
  }
}