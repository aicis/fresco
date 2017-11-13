package dk.alexandra.fresco.lib.conditional;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import org.junit.Assert;

public class ConditionalSelectTests {

  public static class TestSelect<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    final BigInteger selectorOpen;
    final BigInteger expected;
    final BigInteger leftOpen;
    final BigInteger rightOpen;

    public TestSelect(BigInteger selectorOpen, BigInteger leftOpen, BigInteger rightOpen,
        BigInteger expected) {
      this.selectorOpen = selectorOpen;
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
          Application<BigInteger, ProtocolBuilderNumeric> testApplication = root -> {
            Numeric numeric = root.numeric();
            AdvancedNumeric advancedNumeric = root.advancedNumeric();
            DRes<SInt> left = numeric.input(leftOpen, 1);
            DRes<SInt> right = numeric.input(rightOpen, 1);
            DRes<SInt> selector = numeric.input(selectorOpen, 1);
            DRes<SInt> selected = advancedNumeric.condSelect(selector, left, right);
            return numeric.open(selected);
          };
          BigInteger output = runApplication(testApplication);
          Assert.assertEquals(expected, output);
        }
      };
    }
  }

  public static <ResourcePoolT extends ResourcePool> TestSelect<ResourcePoolT> testSelectLeft() {
    BigInteger selector = BigInteger.valueOf(1);
    BigInteger leftOpen = BigInteger.valueOf(11);
    BigInteger rightOpen = BigInteger.valueOf(42);
    return new TestSelect<>(selector, leftOpen, rightOpen, leftOpen);
  }

  public static <ResourcePoolT extends ResourcePool> TestSelect<ResourcePoolT> testSelectRight() {
    BigInteger selector = BigInteger.valueOf(0);
    BigInteger leftOpen = BigInteger.valueOf(11);
    BigInteger rightOpen = BigInteger.valueOf(42);
    return new TestSelect<>(selector, leftOpen, rightOpen, rightOpen);
  }
}
