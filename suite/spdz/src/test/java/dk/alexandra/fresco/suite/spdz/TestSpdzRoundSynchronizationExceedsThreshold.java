package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;

public class TestSpdzRoundSynchronizationExceedsThreshold extends AbstractSpdzTest {

  @Test
  public void testFinishedEvalMacCheck() {
    runTest(new TestMacCheckEvalFinished<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Override
  protected SpdzProtocolSuite getProtocolSuite(int maxBitLength) {
    return new MockSpdzProtocolSuite(maxBitLength);
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
          // this test verifies that the round synchronization logic works when the threshold for
          // open values is exceeded
          runApplication(testApplication);
          Assert.assertFalse(
              "There should be no unchecked opened values after the evaluation has finished",
              conf.getResourcePool().getOpenedValueStore().hasPendingValues());
        }
      };
    }
  }

  private class MockSpdzProtocolSuite extends SpdzProtocolSuite {

    public MockSpdzProtocolSuite(int maxBitLength) {
      super(maxBitLength);
    }

    @Override
    public RoundSynchronization<SpdzResourcePool> createRoundSynchronization() {
      return new SpdzRoundSynchronization(this, 0, 128);
    }

  }

}
