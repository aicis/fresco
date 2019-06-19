package dk.alexandra.fresco.lib.generic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class TestCommitmentComputation extends AbstractDummyArithmeticTest {

  @Test
  public void testCommitmentTwo() {
    runTest(new TestCommitment<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Test
  public void testMaliciousCommitmentTwo() {
    int noOfParties = 2;
    for (int cheater = 1; cheater <= noOfParties; cheater++) {
      runTest(new TestMaliciousCommitment<>(cheater), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
    }
  }

  @Test
  public void testCommitmentThree() {
    runTest(new TestCommitment<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3);
  }

  @Test
  public void testMaliciousCommitmentThree() {
    int noOfParties = 3;
    for (int cheater = 1; cheater <= noOfParties; cheater++) {
      runTest(new TestMaliciousCommitment<>(1), EvaluationStrategy.SEQUENTIAL_BATCHED, 3);
    }
  }

  private static class TestCommitment<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          int noParties = conf.getResourcePool().getNoOfParties();
          List<byte[]> inputs = new ArrayList<>();
          Random random = new Random(42);
          for (int i = 1; i <= noParties; i++) {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            inputs.add(bytes);
          }
          final AesCtrDrbg localDrbg = new AesCtrDrbg();
          final HashBasedCommitmentSerializer commitmentSerializer = new HashBasedCommitmentSerializer();
          Application<List<byte[]>, ProtocolBuilderNumeric> testApplication =
              root -> new CommitmentComputation(
                  commitmentSerializer,
                  inputs.get(root.getBasicNumericContext().getMyId() - 1),
                  localDrbg)
                  .buildComputation(root);
          List<byte[]> actual = runApplication(testApplication);
          assertEquals(inputs.size(), actual.size());
          for (int i = 0; i < actual.size(); i++) {
            assertArrayEquals(inputs.get(i), actual.get(i));
          }
        }
      };
    }
  }

  private static class TestMaliciousCommitment<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final int cheatingPartyId;

    private TestMaliciousCommitment(int cheatingPartyId) {
      this.cheatingPartyId = cheatingPartyId;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          int noParties = conf.getResourcePool().getNoOfParties();
          List<byte[]> inputs = new ArrayList<>();
          Random random = new Random(42);
          for (int i = 1; i <= noParties; i++) {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            inputs.add(bytes);
          }
          final AesCtrDrbg localDrbg = new AesCtrDrbg();
          final HashBasedCommitmentSerializer commitmentSerializer = new HashBasedCommitmentSerializer();
          Application<List<byte[]>, ProtocolBuilderNumeric> testApplication =
              root -> {
                final int myId = root.getBasicNumericContext().getMyId();
                if (myId == cheatingPartyId) {
                  return new MaliciousCommitmentComputation(
                      commitmentSerializer,
                      inputs.get(myId - 1), noParties,
                      localDrbg)
                      .buildComputation(root);
                } else {
                  return new CommitmentComputation(
                      commitmentSerializer,
                      inputs.get(myId - 1),
                      localDrbg)
                      .buildComputation(root);
                }
              };
          if (conf.getMyId() == cheatingPartyId) {
            runApplication(testApplication);
          } else {
            // all honest parties should detect cheating
            boolean thrown = false;
            try {
              runApplication(testApplication);
            } catch (Exception e) {
              assertTrue(e.getCause() instanceof MaliciousException);
              thrown = true;
            }
            assertTrue("Should have caused malicious exception", thrown);
          }
        }
      };
    }
  }

}
