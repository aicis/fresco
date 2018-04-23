package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;

public class CarryOutTests {

  public static class TestCarryOut<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final List<BigInteger> left;
    private final List<BigInteger> right;
    private final BigInteger expected;

    public TestCarryOut(int l, int r) {
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
                List<DRes<SInt>> leftClosed = root.numeric().known(right);
                OIntFactory oIntFactory = root.getOIntFactory();
                DRes<SInt> carry = root
                    .seq(new CarryOut(() -> oIntFactory.fromBigInteger(right), () -> leftClosed));
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

}
