package dk.alexandra.fresco.suite.spdz2k.protocols.computations;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.spdz2k.AbstractSpdz2kTest;
import dk.alexandra.fresco.suite.spdz2k.Spdz2kProtocolSuite128;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128Factory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kOpenedValueStoreImpl;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class TestSpdz2kConversion extends
    AbstractSpdz2kTest<Spdz2kResourcePool<CompUInt128>> {

  @Test
  public void testArithmeticToBool() {
    runTest(new TestArithmeticToBool<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
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
    return new Spdz2kProtocolSuite128(true);
  }

  public static class TestArithmeticToBool<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        private final List<BigInteger> input = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ZERO
        );

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              root -> {
                OIntFactory factory = root.getOIntFactory();
                DRes<List<DRes<SInt>>> inputClosed = root.numeric().knownAsDRes(input);
                DRes<List<DRes<SInt>>> inputBool = root.conversion().toBooleanBatch(inputClosed);
                DRes<List<DRes<OInt>>> opened = root.logical().openAsBits(inputBool);
                return () -> opened.out().stream().map(v -> factory.toBigInteger(v.out()))
                    .collect(Collectors.toList());
              };
          List<BigInteger> actual = runApplication(app);
          List<BigInteger> expected = Arrays.asList(
              BigInteger.ONE,
              BigInteger.ZERO
          );
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }


}
