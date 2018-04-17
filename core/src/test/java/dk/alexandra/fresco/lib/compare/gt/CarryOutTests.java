package dk.alexandra.fresco.lib.compare.gt;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;

public class CarryOutTests {

  public static class TestCarryOut<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final List<BigInteger> left;
    private final List<DRes<BigInteger>> right;
    private final BigInteger expected;

    public TestCarryOut(int[] leftBits, int[] rightClearBits, int expected) {
      this.expected = BigInteger.valueOf(expected);
      int numBits = leftBits.length;
      left = new ArrayList<>(numBits);
      right = new ArrayList<>(numBits);
      for (int i = 0; i < numBits; i++) {
        left.add(BigInteger.valueOf(leftBits[i]));
        int finalI = i;
        right.add(() -> BigInteger.valueOf(rightClearBits[finalI]));
      }
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app =
              root -> {
                int myId = root.getBasicNumericContext().getMyId();
                DRes<List<DRes<SInt>>> leftClosed =
                    (myId == 1) ?
                        root.collections().closeList(left, 1)
                        : root.collections().closeList(left.size(), 1);
                DRes<SInt> carry = root.seq(new CarryOut(leftClosed, () -> right));
                return root.numeric().open(carry);
              };
          BigInteger actual = runApplication(app);
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

}
