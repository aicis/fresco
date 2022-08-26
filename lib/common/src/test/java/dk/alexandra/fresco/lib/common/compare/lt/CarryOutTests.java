package dk.alexandra.fresco.lib.common.compare.lt;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.util.SIntPair;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class CarryOutTests {

  public static class TestCarryOut<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final List<BigInteger> right;
    private final BigInteger expected;

    public TestCarryOut(int l, int r) {
      expected = carry(l, r);
      List<BigInteger> left = intToBits(l);
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
                List<DRes<SInt>> leftClosed = right.stream().map(root.numeric()::known).collect(
                    Collectors.toList());
                DRes<SInt> carry = root
                    .seq(new CarryOut(right, DRes.of(leftClosed),
                        BigInteger.ZERO));
                return root.numeric().open(carry);
              };
          BigInteger actual = runApplication(app);
          Assert.assertEquals(expected, actual);
        }
      };
    }
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

  public static class TestCarrySingleton<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app =

              root -> root.seq(seq -> {
                List<SIntPair> closed = Collections
                    .singletonList(new SIntPair(seq.numeric().known(0),
                        seq.numeric().known(1)));
                return new Carry(closed).buildComputation(seq);
              }).seq((seq, result) -> Pair.lazy(seq.numeric().open(result.get(0).getFirst()), seq.numeric().open(result.get(0).getSecond()))).seq((seq, result) -> Pair.lazy(result.getFirst().out(), result.getSecond().out()));
          Pair<BigInteger, BigInteger> actual = runApplication(app);
          Assert.assertEquals(new Pair<>(BigInteger.ZERO, BigInteger.ONE), actual);
        }
      };
    }
  }

  public static class TestCarryOutSizeMismatch<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final List<BigInteger> left = Collections.nCopies(7, BigInteger.ZERO);
    private final List<BigInteger> right = Collections.nCopies(5, BigInteger.ZERO);

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              root -> {
                List<DRes<SInt>> leftClosed = left.stream().map(root.numeric()::known).collect(
                    Collectors.toList());
                DRes<SInt> carry = root
                    .seq(new CarryOut(right, DRes.of(leftClosed),
                        BigInteger.ZERO));
                return root.numeric().open(carry);
              };
          runApplication(app);
        }
      };
    }
  }

}
