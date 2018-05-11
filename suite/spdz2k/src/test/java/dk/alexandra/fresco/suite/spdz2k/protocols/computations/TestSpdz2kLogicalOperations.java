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
    runTest(new TestAndSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testOr() {
    runTest(new TestOrSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testAndXorSequence() {
    runTest(new TestAndXorSequence<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testSequentialAnd() {
    runTest(new TestSequentialAndSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testXor() {
    runTest(new TestXorSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testXorKnown() {
    runTest(new TestXorKnownSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testSequentialXor() {
    runTest(new TestSequentialXorSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testSequentialAndKnown() {
    runTest(new TestAndKnownSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testXorRandom() {
    runTest(new TestXorSpdz2kRandom<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testAndRandom() {
    runTest(new TestAndSpdz2kRandom<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testNot() {
    runTest(new TestNotSpdz2k<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }


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

  public static class TestNotSpdz2k<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              root -> {
                DRes<SInt> notOne = root.logical().not(
                    root.conversion().toBoolean(root.numeric().input(BigInteger.ONE, 1)));
                DRes<SInt> notZero = root.logical().not(
                    root.conversion().toBoolean(root.numeric().input(BigInteger.ZERO, 2)));
                List<DRes<SInt>> notted = Arrays.asList(notOne, notZero);
                DRes<List<DRes<OInt>>> opened = root.logical().openAsBits(() -> notted);
                return () -> opened.out().stream()
                    .map(v -> root.getOIntFactory().toBigInteger(v.out()))
                    .collect(Collectors.toList());
              };
          List<BigInteger> actual = runApplication(app);
          List<BigInteger> expected = Arrays.asList(
              BigInteger.ZERO,
              BigInteger.ONE
          );
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestXorKnownSpdz2k<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> left = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.ZERO);
        private final List<BigInteger> right = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ZERO);

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              root -> {
                DRes<List<DRes<SInt>>> leftClosed = root.conversion()
                    .toBooleanBatch(root.collections().closeList(left, 1));
                OIntFactory oIntFactory = root.getOIntFactory();
                List<OInt> rightOInts = oIntFactory.fromBigInteger(right);
                DRes<List<DRes<SInt>>> xored = root.logical()
                    .pairWiseXorKnown(() -> rightOInts, leftClosed);
                DRes<List<DRes<OInt>>> opened = root.logical().openAsBits(xored);
                return () -> opened.out().stream().map(v -> oIntFactory.toBigInteger(v.out()))
                    .collect(Collectors.toList());
              };
          List<BigInteger> actual = runApplication(app);
          List<BigInteger> expected = Arrays.asList(
              BigInteger.ZERO,
              BigInteger.ONE,
              BigInteger.ONE,
              BigInteger.ZERO
          );
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestAndKnownSpdz2k<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> left = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.ZERO);
        private final List<BigInteger> right = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ZERO);

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              root -> {
                DRes<List<DRes<SInt>>> leftClosed = root.conversion()
                    .toBooleanBatch(root.collections().closeList(left, 1));
                OIntFactory oIntFactory = root.getOIntFactory();
                List<OInt> rightOInts = oIntFactory.fromBigInteger(right);
                DRes<List<DRes<SInt>>> anded = root.logical()
                    .pairWiseAndKnown(() -> rightOInts, leftClosed);
                DRes<List<DRes<OInt>>> opened = root.logical().openAsBits(anded);
                return () -> opened.out().stream().map(v -> oIntFactory.toBigInteger(v.out()))
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

  public static class TestSequentialXorSpdz2k<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> left = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ZERO,
            BigInteger.ONE
        );

        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              root -> {
                DRes<SInt> bit = root.conversion().toBoolean(root.numeric().input(left.get(0), 1));
                for (int i = 1; i < left.size(); i++) {
                  bit = root.logical().xor(bit,
                      root.conversion().toBoolean(root.numeric().input(left.get(i), 1)));
                }
                DRes<OInt> result = root.logical().openAsBit(bit);
                return () -> root.getOIntFactory().toBigInteger(result.out());
              };
          BigInteger actual = runApplication(app);
          Assert.assertEquals(BigInteger.ONE, actual);
        }
      };
    }
  }

  public static class TestSequentialAndSpdz2k<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> left = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ZERO,
            BigInteger.ONE
        );

        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              root -> {
                DRes<SInt> bit = root.conversion().toBoolean(root.numeric().input(left.get(0), 1));
                for (int i = 1; i < left.size(); i++) {
                  bit = root.logical().and(bit,
                      root.conversion().toBoolean(root.numeric().input(left.get(i), 1)));
                }
                DRes<OInt> result = root.logical().openAsBit(bit);
                return () -> root.getOIntFactory().toBigInteger(result.out());
              };
          BigInteger actual = runApplication(app);
          Assert.assertEquals(BigInteger.ZERO, actual);
        }
      };
    }
  }

  public static class TestOrSpdz2k<ResourcePoolT extends ResourcePool>
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
            BigInteger.ZERO,
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.ONE
        );

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              root -> {
                DRes<List<DRes<SInt>>> leftClosed =
                    root.collections().closeList(left, 1);
                DRes<List<DRes<SInt>>> rightClosed = root.collections().closeList(right, 1);
                DRes<List<DRes<SInt>>> leftConverted = root.conversion()
                    .toBooleanBatch(leftClosed);
                DRes<List<DRes<SInt>>> rightConverted = root.conversion()
                    .toBooleanBatch(rightClosed);
                DRes<List<DRes<SInt>>> anded = root.logical().pairWiseOr(
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
              BigInteger.ONE,
              BigInteger.ONE
          );
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestXorSpdz2k<ResourcePoolT extends ResourcePool>
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
            BigInteger.ZERO,
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.ONE
        );

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              root -> {
                DRes<List<DRes<SInt>>> leftClosed =
                    root.collections().closeList(left, 1);
                DRes<List<DRes<SInt>>> rightClosed = root.collections().closeList(right, 1);
                DRes<List<DRes<SInt>>> leftConverted = root.conversion()
                    .toBooleanBatch(leftClosed);
                DRes<List<DRes<SInt>>> rightConverted = root.conversion()
                    .toBooleanBatch(rightClosed);
                DRes<List<DRes<SInt>>> anded = root.logical().pairWiseXor(
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
              BigInteger.ONE
          );
          Assert.assertEquals(expected, actual);
        }
      };
    }
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
            BigInteger.ONE
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

  public static class TestXorSpdz2kRandom<ResourcePoolT extends ResourcePool>
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
                DRes<List<DRes<SInt>>> anded = root.logical().pairWiseXor(
                    leftConverted,
                    rightConverted
                );
                DRes<List<DRes<OInt>>> opened = root.logical().openAsBits(anded);
                OIntFactory factory = root.getOIntFactory();
                return () -> opened.out().stream().map(v -> factory.toBigInteger(v.out()))
                    .collect(Collectors.toList());
              };
          List<BigInteger> actual = runApplication(app);
          List<BigInteger> expected = xor(left, right);
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

  public static class TestAndXorSequence<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              root -> {
                DRes<SInt> b = bit(root, 1);
                b = root.logical().and(b, bit(root, 1));
                b = root.logical().and(b, bit(root, 0));
                b = root.logical().xor(b, bit(root, 0));
                b = root.logical().xor(b, bit(root, 1));
                b = root.logical().xor(b, bit(root, 1));
                b = root.logical().and(b, bit(root, 0));
                DRes<OInt> opened = root.logical().openAsBit(b);
                OIntFactory factory = root.getOIntFactory();
                return () -> factory.toBigInteger(opened.out());
              };
          BigInteger actual = runApplication(app);
          Assert.assertEquals(BigInteger.ZERO, actual);
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

  private static List<BigInteger> xor(List<BigInteger> left, List<BigInteger> right) {
    List<BigInteger> bits = new ArrayList<>(left.size());
    for (int i = 0; i < left.size(); i++) {
      bits.add(left.get(i).add(right.get(i)).mod(BigInteger.valueOf(2)));
    }
    return bits;
  }

  private static DRes<SInt> bit(ProtocolBuilderNumeric root, int bit) {
    return root.conversion().toBoolean(root.numeric().input(BigInteger.valueOf(bit), 1));
  }

}
