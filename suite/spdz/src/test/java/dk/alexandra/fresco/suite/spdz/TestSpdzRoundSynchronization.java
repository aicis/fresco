package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;

public class TestSpdzRoundSynchronization extends AbstractSpdzTest {

  @Test
  public void testFinishedEvalMacCheck() {
    runTest(new TestMacCheckEvalFinished<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testMacCheckExceedThreshold() {
    runTest(new TestMacCheckExceedThreshold<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Override
  protected SpdzProtocolSuite createProtocolSuite(int maxBitLength) {
    return new LowThresholdSpdzSuite(64);
  }

  private class LowThresholdSpdzSuite extends SpdzProtocolSuite {

    public LowThresholdSpdzSuite(int maxBitLength) {
      super(maxBitLength);
    }

    @Override
    public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
      return new SpdzRoundSynchronization(this, 0, 128);
    }
  }

  private static class TestMacCheckEvalFinished<ResourcePoolT extends SpdzResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<SInt, ProtocolBuilderNumeric> testApplication = root -> {
            DRes<SInt> left = root.numeric().known(BigInteger.ZERO);
            DRes<SInt> right = root.numeric().known(BigInteger.ONE);
            return root.numeric().mult(left, right);
          };
          // this tests verifies that the round synchronization logic works correctly when we
          // do not have output protocols in our application but still open values during
          // multiplication
          runApplication(testApplication);
          Assert.assertFalse(
              "There should be no unchecked opened values after the evaluation has finished",
              conf.getResourcePool().getOpenedValueStore().hasPendingValues());
        }
      };
    }
  }

  private static class TestMacCheckExceedThreshold<ResourcePoolT extends SpdzResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<SInt, ProtocolBuilderNumeric> testApplication = root -> {
            DRes<SInt> left = root.numeric().known(BigInteger.ZERO);
            DRes<SInt> right = root.numeric().known(BigInteger.ONE);
            return root.numeric().mult(left, right);
          };
          // this tests verifies that the round synchronization logic works correctly when we
          // do not have output protocols in our application but still open values during
          // multiplication
          runApplication(testApplication);
          Assert.assertFalse(
              "There should be no unchecked opened values after the evaluation has finished",
              conf.getResourcePool().getOpenedValueStore().hasPendingValues());
        }
      };
    }
  }

}
