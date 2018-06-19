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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class TestTruncateSpdz2k extends
    AbstractSpdz2kTest<Spdz2kResourcePool<CompUInt128>> {

  @Override
  protected Spdz2kResourcePool<CompUInt128> createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier) {
    CompUIntFactory<CompUInt128> factory = new CompUInt128Factory();
    CompUInt128 keyShare = factory.createRandom();
    Spdz2kResourcePool<CompUInt128> resourcePool =
        new Spdz2kResourcePoolImpl<>(
            playerId,
            noOfParties, new AesCtrDrbg(new byte[32]),
            new Spdz2kOpenedValueStoreImpl<>(),
            new Spdz2kDummyDataSupplier<>(playerId, noOfParties, keyShare, factory),
            factory);
    resourcePool.initializeJointRandomness(networkSupplier, AesCtrDrbg::new, 32);
    return resourcePool;
  }

  @Override
  protected ProtocolSuiteNumeric<Spdz2kResourcePool<CompUInt128>> createProtocolSuite() {
    return new Spdz2kProtocolSuite128(true);
  }

  @Test
  public void testTruncate() {
    runTest(new TestTruncate<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  public static class TestTruncate<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          List<BigInteger> inputs = Arrays.asList(
              BigInteger.valueOf(-1),
              BigInteger.ONE,
              BigInteger.ZERO,
              BigInteger.valueOf(2723121121381238012L),
              BigInteger.valueOf(121121381238012L)
          );
          Application<List<DRes<BigInteger>>, ProtocolBuilderNumeric> app =
              root -> {
                List<DRes<SInt>> result = new ArrayList<>(inputs.size());
                for (BigInteger input : inputs) {
                  result.add(
                      root.advancedNumeric().truncate(root.numeric().input(input, 1), 16)
                  );
                }
                return root.collections().openList(() -> result);
              };
          List<BigInteger> actuals = runApplication(app).stream().map(DRes::out)
              .collect(Collectors.toList());
          for (int i = 0; i < inputs.size(); i++) {
            BigInteger expected = inputs.get(i).shiftRight(16).mod(BigInteger.ONE.shiftLeft(64));
            BigInteger actual = actuals.get(i);
            System.out.println(actual);
            Assert.assertEquals(expected, actual);
          }
        }
      };
    }
  }
}
