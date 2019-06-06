package dk.alexandra.fresco.lib.generic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Test;

public class TestBroadcastComputation extends AbstractDummyArithmeticTest {

  @Test
  public void testValidBroadcast() {
    runTest(new TestValidBroadcast<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Test
  public void testValidBroadcastThree() {
    runTest(new TestValidBroadcast<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3);
  }

  @Test
  public void testInvalidBroadcastThree() {
    runTest(new TestInvalidBroadcast<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 3);
  }

  private static class TestValidBroadcast<ResourcePoolT extends ResourcePool>
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
          Application<List<byte[]>, ProtocolBuilderNumeric> testApplication =
              root -> new BroadcastComputation<ProtocolBuilderNumeric>(
                  inputs.get(root.getBasicNumericContext().getMyId() - 1)).buildComputation(root);
          List<byte[]> actual = runApplication(testApplication);
          assertEquals(inputs.size(), actual.size());
          for (int i = 0; i < actual.size(); i++) {
            assertArrayEquals(inputs.get(i), actual.get(i));
          }
        }
      };
    }
  }

  private static class TestInvalidBroadcast<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          int noParties = conf.getResourcePool().getNoOfParties();
          int partyId = conf.getMyId();
          List<byte[]> inputs = new ArrayList<>();
          Random random = new Random(42);
          for (int i = 1; i <= noParties; i++) {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            inputs.add(bytes);
          }
          Application<List<byte[]>, ProtocolBuilderNumeric> testApplication;
          if (partyId == 2) {
            testApplication = root -> new MaliciousBroadcastComputation(
                inputs.get(root.getBasicNumericContext().getMyId() - 1)).buildComputation(root);
          } else {
            testApplication = root -> new BroadcastComputation<ProtocolBuilderNumeric>(
                inputs.get(root.getBasicNumericContext().getMyId() - 1)).buildComputation(root);
          }
          // we need that new test framework...
          boolean thrown = false;
          try {
            runApplication(testApplication);
          } catch (Exception e) {
            assertTrue(e.getCause() instanceof MaliciousException);
            thrown = true;
          }
          assertTrue("Should have caused malicious exception", thrown);
        }
      };
    }
  }

  private static class MaliciousBroadcastComputation extends
      BroadcastComputation<ProtocolBuilderNumeric> {

    private final List<byte[]> inputCopy;

    MaliciousBroadcastComputation(byte[] input) {
      super(input);
      this.inputCopy = Collections.singletonList(input);
    }

    @Override
    public DRes<List<byte[]>> buildComputation(ProtocolBuilderNumeric builder) {
      return builder.par(par -> {
        List<DRes<List<byte[]>>> broadcastValues = new LinkedList<>();
        for (byte[] singleInput : inputCopy) {
          broadcastValues.add(par.append(new MaliciousAllBroadcast<>(singleInput)));
        }
        return () -> broadcastValues;
      }).seq((seq, lst) -> {
        List<byte[]> toValidate = lst.stream()
            .flatMap(broadcast -> broadcast.out().stream())
            .collect(Collectors.toList());
        seq.append(new BroadcastValidationProtocol<>(toValidate));
        return () -> toValidate;
      });
    }
  }

  private static class MaliciousAllBroadcast<ResourcePoolT extends ResourcePool> extends
      InsecureBroadcastProtocol<ResourcePoolT> {

    private final byte[] inputCopy;
    private List<byte[]> resultCopy;

    MaliciousAllBroadcast(byte[] input) {
      super(input);
      this.inputCopy = input;
    }

    @Override
    public List<byte[]> out() {
      return resultCopy;
    }

    @Override
    public EvaluationStatus evaluate(int round, ResourcePoolT resourcePool,
        Network network) {
      if (round == 0) {
        for (int i = 1; i <= resourcePool.getNoOfParties(); i++) {
          if (i == 1) {
            byte[] malicious = inputCopy.clone();
            malicious[0] = (byte) ~malicious[0];
            network.send(i, malicious);
          } else {
            network.send(i, inputCopy);
          }

        }
        return EvaluationStatus.HAS_MORE_ROUNDS;
      } else {
        resultCopy = network.receiveFromAll();
        return EvaluationStatus.IS_DONE;
      }
    }
  }

}
