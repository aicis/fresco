package dk.alexandra.fresco.lib.generic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class TestCoinTossingComputation extends AbstractDummyArithmeticTest {

  @Test
  public void testCoinTossing() {
    runTest(new TestCoinTossing<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
    runTest(new TestCoinTossing<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3);
  }

  @Test
  public void testDefaultConstructor() {
    final AesCtrDrbg localDrbg = new AesCtrDrbg();
    final HashBasedCommitmentSerializer commitmentSerializer = new HashBasedCommitmentSerializer();
    CoinTossingComputation ct = new CoinTossingComputation(32, commitmentSerializer, localDrbg);
    assertNotNull(ct);
  }

  private static class TestCoinTossing<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          int noParties = conf.getResourcePool().getNoOfParties();
          List<byte[]> inputs = new ArrayList<>();
          Random random = new Random(42);
          for (int i = 1; i <= noParties; i++) {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            inputs.add(bytes);
          }
          Application<byte[], ProtocolBuilderNumeric> testApplication =
              root -> {
                final AesCtrDrbg localDrbg = new AesCtrDrbg();
                final HashBasedCommitmentSerializer commitmentSerializer = new HashBasedCommitmentSerializer();
                final byte[] bytes = inputs.get(root.getBasicNumericContext().getMyId() - 1);
                return new CoinTossingComputation(bytes, commitmentSerializer, localDrbg)
                    .buildComputation(root);
              };
          byte[] actual = runApplication(testApplication);
          byte[] expected = new byte[32];
          for (byte[] input : inputs) {
            ByteArrayHelper.xor(expected, input);
          }
          assertArrayEquals(expected, actual);
        }
      };
    }
  }

}
