package dk.alexandra.fresco.lib.common.compare.lt;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class LessThanZeroTests {

  public static class TestLessThanZero<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final List<BigInteger> openInputs;
    private final List<BigInteger> expected;

    public TestLessThanZero(BigInteger modulus) {
      this.openInputs = Arrays.asList(
          BigInteger.ZERO,
          BigInteger.ONE,
          BigInteger.valueOf(-1),
          BigInteger.valueOf(-111111),
          BigInteger.valueOf(-123123),
          modulus
      );
      this.expected = computeExpected(openInputs);
    }

    private static List<BigInteger> computeExpected(List<BigInteger> inputs) {
      List<BigInteger> expected = new ArrayList<>(inputs.size());
      for (BigInteger input : inputs) {
        boolean lessThanZero = input.compareTo(BigInteger.ZERO) < 0;
        expected.add(lessThanZero ? BigInteger.ONE : BigInteger.ZERO);
      }
      return expected;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric numeric = builder.numeric();
            List<DRes<SInt>> inputs = openInputs.stream().map(numeric::known).collect(Collectors.toList());
            List<DRes<SInt>> actualInner = new ArrayList<>(inputs.size());
            for (DRes<SInt> input : inputs) {
              actualInner.add(builder.seq(
                  new LessThanZero(input, builder.getBasicNumericContext().getMaxBitLength())));
            }
            DRes<List<DRes<BigInteger>>> opened = Collections.using(builder).openList(DRes.of(actualInner));
            return () -> opened.out().stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigInteger> actual = runApplication(app);
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

}