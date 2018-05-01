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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class TestSpdz2kLogicalOperations extends
    AbstractSpdz2kTest<Spdz2kResourcePool<CompUInt128>> {

  @Test
  public void testAnd() {
    runTest(new TestAndSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Test
  public void testAndRandom() {
    runTest(new TestAndSpdz2kRandom<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Override
  protected Spdz2kResourcePool<CompUInt128> createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier) {
    System.err.println("TestSpdz2kLogicalOperations change me back!");
    CompUIntFactory<CompUInt128> factory = new CompUInt128Factory();
    Random random = new Random(playerId);
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    CompUInt128 keyShare = new CompUInt128(bytes);
    Spdz2kResourcePool<CompUInt128> resourcePool =
        new Spdz2kResourcePoolImpl<>(
            playerId,
            noOfParties, new AesCtrDrbg(new byte[32]),
            new Spdz2kOpenedValueStoreImpl<>(),
            new Spdz2kDummyDataSupplier<>(playerId, noOfParties, keyShare, factory),
            factory);
//    resourcePool.initializeJointRandomness(networkSupplier, AesCtrDrbg::new, 32);
    return resourcePool;
  }

  @Override
  protected ProtocolSuiteNumeric<Spdz2kResourcePool<CompUInt128>> createProtocolSuite() {
    return new Spdz2kProtocolSuite128(true);
  }

  public static class TestAndSpdz2k<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> left = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.ZERO
        );
        private final List<BigInteger> right = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ZERO
        );

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              root -> {
                DRes<List<DRes<SInt>>> leftClosed = root.numeric().knownAsDRes(left);
                DRes<List<DRes<SInt>>> rightClosed = root.numeric().knownAsDRes(right);
                DRes<List<DRes<SInt>>> leftConverted = root.conversion()
                    .toBooleanBatch(leftClosed);
                DRes<List<DRes<SInt>>> rightConverted = root.conversion()
                    .toBooleanBatch(rightClosed);
                DRes<List<DRes<SInt>>> anded = root.logical().pairWiseAnd(
                    leftConverted,
                    rightConverted
                );
                DRes<List<DRes<OInt>>> opened = root.logical().openAsBits(anded);
                OIntFactory factory = root.getOIntFactory();
                return () -> opened.out().stream().map(v -> factory.toBigInteger(v.out()))
                    .collect(Collectors.toList());
              };
          List<BigInteger> actual = runApplication(app);
          List<BigInteger> expected = Arrays.asList(
              BigInteger.ONE,
              BigInteger.ZERO,
              BigInteger.ZERO,
              BigInteger.ZERO
          );
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestAndSpdz2kRandom<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> left = randomBits(100, 1);
        private final List<BigInteger> right = randomBits(100, 2);

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              root -> {
                DRes<List<DRes<SInt>>> leftClosed = root.numeric().knownAsDRes(left);
                DRes<List<DRes<SInt>>> rightClosed = root.numeric().knownAsDRes(right);
                DRes<List<DRes<SInt>>> leftConverted = root.conversion().toBooleanBatch(leftClosed);
                DRes<List<DRes<SInt>>> rightConverted = root.conversion()
                    .toBooleanBatch(rightClosed);
                DRes<List<DRes<SInt>>> anded = root.logical().pairWiseAnd(
                    leftConverted,
                    rightConverted
                );
                DRes<List<DRes<OInt>>> opened = root.logical().openAsBits(anded);
                OIntFactory factory = root.getOIntFactory();
                return () -> opened.out().stream().map(v -> factory.toBigInteger(v.out()))
                    .collect(Collectors.toList());
              };
          List<BigInteger> actual = runApplication(app);
          List<BigInteger> expected = and(left, right);
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  private static List<BigInteger> randomBits(int num, int seed) {
    Random random = new Random(seed);
    List<BigInteger> bits = new ArrayList<>(num);
    for (int i = 0; i < num; i++) {
      bits.add(random.nextBoolean() ? BigInteger.ONE : BigInteger.ZERO);
    }
    return bits;
  }

  private static List<BigInteger> and(List<BigInteger> left, List<BigInteger> right) {
    List<BigInteger> bits = new ArrayList<>(left.size());
    for (int i = 0; i < left.size(); i++) {
      bits.add(left.get(i).multiply(right.get(i)).mod(BigInteger.valueOf(2)));
    }
    return bits;
  }


}
