package dk.alexandra.fresco.lib.generic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
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
    runTest(new TestMaliciousCommitment<>(1), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Test
  public void testCommitmentThree() {
    runTest(new TestCommitment<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3);
  }

  @Test
  public void testMaliciousCommitmentThree() {
    runTest(new TestMaliciousCommitment<>(1), EvaluationStrategy.SEQUENTIAL_BATCHED, 3);
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
                  inputs.get(root.getBasicNumericContext().getMyId() - 1), noParties,
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
                  return new MaliciousTwoPartyCommitmentComputation(
                      commitmentSerializer,
                      inputs.get(myId - 1), noParties,
                      localDrbg)
                      .buildComputation(root);
                } else {
                  return new CommitmentComputation(
                      commitmentSerializer,
                      inputs.get(myId - 1), noParties,
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

  static class MaliciousTwoPartyCommitmentComputation extends CommitmentComputation {

    private final ByteSerializer<HashBasedCommitment> commitmentSerializer;
    private final byte[] value;
    private final int noOfParties;
    private final Drbg localDrbg;

    MaliciousTwoPartyCommitmentComputation(
        ByteSerializer<HashBasedCommitment> commitmentSerializer,
        byte[] value, int noOfParties, Drbg localDrbg) {
      super(commitmentSerializer, value, noOfParties, localDrbg);
      this.commitmentSerializer = commitmentSerializer;
      this.value = value;
      this.noOfParties = noOfParties;
      this.localDrbg = localDrbg;
    }

    @Override
    public DRes<List<byte[]>> buildComputation(ProtocolBuilderNumeric builder) {
      HashBasedCommitment ownCommitment = new HashBasedCommitment();
      byte[] ownOpening = ownCommitment.commit(localDrbg, value);
      return builder.seq(
          seq -> {
            if (noOfParties > 2) {
              return new BroadcastComputation<ProtocolBuilderNumeric>(
                  commitmentSerializer.serialize(ownCommitment))
                  .buildComputation(seq);
            } else {
              return seq.append(new InsecureBroadcastProtocol<>(
                  commitmentSerializer.serialize(ownCommitment)));
            }
          })
          .seq((seq, rawCommitments) -> {
            // tamper with opening
            ownOpening[1] = (byte) (ownOpening[1] ^ 1);
            DRes<List<byte[]>> res = seq.append(new InsecureBroadcastProtocol<>(ownOpening));
            final Pair<DRes<List<byte[]>>, List<byte[]>> dResListPair = new Pair<>(res,
                rawCommitments);
            return () -> dResListPair;
          })
          .seq((seq, pair) -> null);
    }
  }

}
