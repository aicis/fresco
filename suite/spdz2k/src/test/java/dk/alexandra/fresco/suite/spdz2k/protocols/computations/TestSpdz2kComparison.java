package dk.alexandra.fresco.suite.spdz2k.protocols.computations;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.MathUtils;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.CompareTests.TestLessThanLogRounds;
import dk.alexandra.fresco.lib.compare.lt.CarryOut;
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
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class TestSpdz2kComparison extends
    AbstractSpdz2kTest<Spdz2kResourcePool<CompUInt128>> {

  @Test
  public void testCarryOutZero() {
    runTest(new TestCarryOutSpdz2k<>(0x00000000, 0x00000000), EvaluationStrategy.SEQUENTIAL_BATCHED,
        2);
  }

  @Test
  public void testCarryOutOne() {
    runTest(new TestCarryOutSpdz2k<>(0x80000000, 0x80000000), EvaluationStrategy.SEQUENTIAL_BATCHED,
        2);
  }

  @Test
  public void testCarryOutAllOnes() {
    runTest(new TestCarryOutSpdz2k<>(0xffffffff, 0xffffffff), EvaluationStrategy.SEQUENTIAL_BATCHED,
        2);
  }

  @Test
  public void testCarryOutOneFromCarry() {
    runTest(new TestCarryOutSpdz2k<>(0x40000000, 0xc0000000), EvaluationStrategy.SEQUENTIAL_BATCHED,
        2);
  }

  @Test
  public void testCarryOutRandom() {
    runTest(new TestCarryOutSpdz2k<>(new Random(42).nextInt(), new Random(1).nextInt()),
        EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Test
  public void testBitLessThan() {
    runTest(new TestBitLessThanOpenSpdz2k<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
  }

  @Test
  public void testLessThanLogRounds() {
    runTest(new TestLessThanLogRounds<>(64),
        EvaluationStrategy.SEQUENTIAL_BATCHED, 2);
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

  public static class TestCarryOutSpdz2k<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final List<BigInteger> left;
    private final List<BigInteger> right;
    private final BigInteger expected;

    public TestCarryOutSpdz2k(int l, int r) {
      expected = carry(l, r);
      left = intToBits(l);
      right = new ArrayList<>(left.size());
      right.addAll(intToBits(r));
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              root -> {
                DRes<List<DRes<SInt>>> leftClosed =
                    root.conversion().toBooleanBatch(root.collections().closeList(right, 1));
                OIntFactory oIntFactory = root.getOIntFactory();
                DRes<SInt> carry = root
                    .seq(new CarryOut(() -> oIntFactory.fromBigInteger(right), leftClosed,
                        oIntFactory.zero()));
                DRes<OInt> opened = root.logical().openAsBit(carry);
                return () -> oIntFactory.toBigInteger(opened.out());
              };
          BigInteger actual = runApplication(app);
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestBitLessThanOpenSpdz2k<ResourcePoolT extends NumericResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private List<BigInteger> left;
    private List<BigInteger> right;

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              root -> {
                int numBits = 32;
                OIntFactory oIntFactory = root.getOIntFactory();
                setupInputs(conf.getResourcePool().getModulus(), numBits);
                List<DRes<OInt>> results = new ArrayList<>(left.size());
                for (int i = 0; i < left.size(); i++) {
                  OInt leftValue = oIntFactory.fromBigInteger(left.get(i));
                  DRes<List<DRes<SInt>>> rightValue = toSecretBits(root, right.get(i),
                      numBits);
                  results.add(
                      root.logical().openAsBit(
                          root.comparison().compareLTBits(leftValue, rightValue)
                      )
                  );
                }
                return () -> results.stream().map(v -> oIntFactory.toBigInteger(v.out()))
                    .collect(Collectors.toList());
              };
          List<BigInteger> actual = runApplication(app);
          List<BigInteger> expected = new ArrayList<>(left.size());
          for (int i = 0; i < left.size(); i++) {
            boolean leq = left.get(i).compareTo(right.get(i)) < 0;
            expected.add(leq ? BigInteger.ONE : BigInteger.ZERO);
          }
          Assert.assertEquals(expected, actual);
        }
      };
    }

    private void setupInputs(BigInteger modulus, int numBits) {
      Random random = new Random(42);
      this.left = Arrays.asList(
          BigInteger.ZERO,
          BigInteger.ONE,
          BigInteger.ZERO,
          BigInteger.valueOf(5),
          BigInteger.valueOf(111),
          BigInteger.valueOf(111),
          modulus.subtract(BigInteger.ONE),
          modulus.subtract(BigInteger.ONE),
          BigInteger.valueOf(2055014152),
          new BigInteger(numBits, random).mod(modulus)
      );
      this.right = Arrays.asList(
          BigInteger.ONE,
          BigInteger.ZERO,
          BigInteger.ZERO,
          BigInteger.valueOf(4),
          BigInteger.valueOf(111),
          BigInteger.valueOf(112),
          modulus.subtract(BigInteger.ONE),
          modulus.subtract(BigInteger.valueOf(2)),
          BigInteger.valueOf(2055014153),
          new BigInteger(numBits, random).mod(modulus)
      );
    }

  }

  private static DRes<List<DRes<SInt>>> toSecretBits(ProtocolBuilderNumeric root,
      BigInteger value,
      int numBits) {
    List<BigInteger> openList = MathUtils.toBits(value, numBits);
    Collections.reverse(openList);
    return root.collections().closeList(openList, 1);
  }

  private static BigInteger carry(int a, int b) {
    long res = Integer.toUnsignedLong(a) + Integer.toUnsignedLong(b);
    int carry = (int) ((res & (1L << 32)) >> 32);
    return BigInteger.valueOf(carry);
  }

  private static List<BigInteger> intToBits(int value) {
    int numBits = Integer.SIZE;
    List<BigInteger> bits = new ArrayList<>(numBits);
    for (int i = 0; i < numBits; i++) {
      int bit = (value & (1 << i)) >>> i;
      bits.add(BigInteger.valueOf(bit));
    }
    Collections.reverse(bits);
    return bits;
  }

}
