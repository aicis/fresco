package dk.alexandra.fresco.framework.builder.numeric;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.math.BigInteger;

public class TestDefaultPreprocessedValues {

  public static class TestGetNextBit<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = seq -> {
            return seq.numeric().open(seq.preprocessedValues().getNextBit());
          };
          BigInteger bit = runApplication(app);
          boolean isBit = bit.equals(BigInteger.ONE) || bit.equals(BigInteger.ZERO);
          assertEquals("Value is not bit: " + bit, true, isBit);
        }
      };
    }
  }

}
