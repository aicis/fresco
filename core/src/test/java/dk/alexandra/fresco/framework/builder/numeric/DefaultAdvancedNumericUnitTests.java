package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Assert;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;

public class DefaultAdvancedNumericUnitTests {
  public static class TestRandomMaskNotImplemented<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<SInt, ProtocolBuilderNumeric> app = builder -> {
            try {
              DRes<SInt> input = builder.numeric().input(BigInteger.ONE, 1);
              builder.advancedNumeric().randomBitMask(Arrays.asList(input));
              Assert.fail("No unsupported exception thrown");
            } catch (UnsupportedOperationException e) {
              // As expected
            }
            return () -> null;
          };
          runApplication(app);
        }
      };
    }
  }
}
