package dk.alexandra.fresco.lib.conditional;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Collections;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import org.junit.Assert;

public class SwapIfTests {

  public static class TestSwap<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    final BigInteger swapperOpen;
    final Pair<BigInteger, BigInteger> expected;
    final BigInteger leftOpen;
    final BigInteger rightOpen;

    public TestSwap(BigInteger selectorOpen, BigInteger leftOpen, BigInteger rightOpen,
        Pair<BigInteger, BigInteger> expected) {
      this.swapperOpen = selectorOpen;
      this.expected = expected;
      this.leftOpen = leftOpen;
      this.rightOpen = rightOpen;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<Pair<DRes<BigInteger>, DRes<BigInteger>>, ProtocolBuilderNumeric> testApplication =
              root -> {
                Numeric nb = root.numeric();
                AdvancedNumeric advancedNumeric = root.advancedNumeric();
                Collections collections = root.collections();
                DRes<SInt> left = nb.input(leftOpen, 1);
                DRes<SInt> right = nb.input(rightOpen, 1);
                DRes<SInt> selector = nb.input(swapperOpen, 1);
                DRes<Pair<DRes<SInt>, DRes<SInt>>> swapped =
                    advancedNumeric.swapIf(selector, left, right);
                return collections.openPair(swapped);
              };
          Pair<DRes<BigInteger>, DRes<BigInteger>> output = runApplication(testApplication);
          Pair<BigInteger, BigInteger> actual =
              new Pair<>(output.getFirst().out(), output.getSecond().out());
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static <ResourcePoolT extends ResourcePool> TestSwap<ResourcePoolT> testSwapYes() {
    BigInteger swapper = BigInteger.valueOf(1);
    BigInteger leftOpen = BigInteger.valueOf(11);
    BigInteger rightOpen = BigInteger.valueOf(42);
    Pair<BigInteger, BigInteger> expected =
        new Pair<>(BigInteger.valueOf(42), BigInteger.valueOf(11));
    return new TestSwap<>(swapper, leftOpen, rightOpen, expected);
  }

  public static <ResourcePoolT extends ResourcePool> TestSwap<ResourcePoolT> testSwapNo() {
    BigInteger swapper = BigInteger.valueOf(0);
    BigInteger leftOpen = BigInteger.valueOf(11);
    BigInteger rightOpen = BigInteger.valueOf(42);
    Pair<BigInteger, BigInteger> expected =
        new Pair<>(BigInteger.valueOf(11), BigInteger.valueOf(42));
    return new TestSwap<>(swapper, leftOpen, rightOpen, expected);
  }
}
