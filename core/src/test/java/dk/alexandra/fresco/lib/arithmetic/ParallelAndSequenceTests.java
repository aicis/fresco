package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

/**
 * Tests which ensures that the SecureComputationEngine's parallel and sequential evaluations of
 * application works.
 *
 */
public class ParallelAndSequenceTests {

  private static final Integer[] inputAsArray = {1, 2, 3, 4, 5, 6, 7, 8, 9};

  // TODO Split these tests into two
  public static class TestSumAndProduct<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          TestApplicationSum sumApp = new ParallelAndSequenceTests().new TestApplicationSum();
          TestApplicationMult multApp = new ParallelAndSequenceTests().new TestApplicationMult();

          BigInteger sum = runApplication(sumApp);
          BigInteger mult = runApplication(multApp);

          Assert.assertEquals(BigInteger.valueOf(1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9), sum);
          Assert.assertEquals(BigInteger.valueOf(1 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9), mult);
        }
      };
    }
  }

  private class TestApplicationSum implements Application<BigInteger, ProtocolBuilderNumeric> {

    @Override
    public DRes<BigInteger> buildComputation(
        ProtocolBuilderNumeric producer) {
      List<DRes<SInt>> input =
          Arrays.stream(inputAsArray)
              .map((integer) -> convertToSInt(integer, producer))
              .collect(Collectors.toList());
      DRes<SInt> result = producer.advancedNumeric().sum(input);
      return producer.numeric().open(result);
    }

  }

  private class TestApplicationMult implements Application<BigInteger, ProtocolBuilderNumeric> {

    @Override
    public DRes<BigInteger> buildComputation(
        ProtocolBuilderNumeric producer) {
      DRes<SInt> result = producer.advancedNumeric().product(
          Arrays.stream(inputAsArray)
              .map((integer) -> convertToSInt(integer, producer))
              .collect(Collectors.toList()));
      return producer.numeric().open(result);
    }
  }

  private DRes<SInt> convertToSInt(int integer, ProtocolBuilderNumeric producer) {
    Numeric numeric = producer.numeric();
    BigInteger value = BigInteger.valueOf(integer);
    return numeric.input(value, 1);
  }
}
