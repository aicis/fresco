package dk.alexandra.fresco.suite.spdz2k.protocols.computations;

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
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.spdz2k.AbstractSpdz2kTest;
import dk.alexandra.fresco.suite.spdz2k.Spdz2kProtocolSuite128;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128Factory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.InsecureBroadcastProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.BroadcastValidationProtocol;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kOpenedValueStoreImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.Test;

public class TestSpdz2kBroadcastComputation extends
    AbstractSpdz2kTest<Spdz2kResourcePool<CompUInt128>> {

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

  @Override
  protected Spdz2kResourcePool<CompUInt128> createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier) {
    CompUIntFactory<CompUInt128> factory = new CompUInt128Factory();
    Spdz2kResourcePool<CompUInt128> resourcePool =
        new Spdz2kResourcePoolImpl<>(
            playerId,
            noOfParties, null,
            new Spdz2kOpenedValueStoreImpl<>(),
            new Spdz2kDummyDataSupplier<>(playerId, noOfParties, factory.createRandom(), factory),
            factory);
    resourcePool.initializeJointRandomness(networkSupplier, AesCtrDrbg::new, 32);
    return resourcePool;
  }

  @Override
  protected ProtocolSuiteNumeric<Spdz2kResourcePool<CompUInt128>> createProtocolSuite() {
    return new Spdz2kProtocolSuite128();
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
          try {
            runApplication(testApplication);
          } catch (Exception e) {
            assertTrue(e.getCause() instanceof MaliciousException);
          }
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
          broadcastValues.add(par.append(new MaliciousAllBroadcast(singleInput)));
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

  private static class MaliciousAllBroadcast extends
      InsecureBroadcastProtocol<Spdz2kResourcePool<CompUInt128>> {

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
    public EvaluationStatus evaluate(int round, Spdz2kResourcePool<CompUInt128> resourcePool,
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
