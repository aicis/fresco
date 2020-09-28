package dk.alexandra.fresco.lib.common.math.integer;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestProductAndSum {

  public static class TestProduct<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    List<BigInteger> inputs = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L).stream().map(
        BigInteger::valueOf).collect(Collectors.toList());

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<BigInteger, ProtocolBuilderNumeric> testApplication =
              root -> {
                List<DRes<SInt>> closed = inputs.stream().map(root.numeric()::known)
                    .collect(Collectors.toList());
                DRes<SInt> result = AdvancedNumeric.using(root).product(closed);
                DRes<BigInteger> open = root.numeric().open(result);
                return () -> open.out();
              };
          BigInteger output = runApplication(testApplication);
          assertEquals(output, inputs.stream().reduce(BigInteger.ONE, (a, b) -> a.multiply(b)));
        }
      };
    }
  }

  public static class TestSum<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    List<BigInteger> inputs = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L).stream().map(
        BigInteger::valueOf).collect(Collectors.toList());

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<BigInteger, ProtocolBuilderNumeric> testApplication =
              root -> {
                List<DRes<SInt>> closed = inputs.stream().map(root.numeric()::known)
                    .collect(Collectors.toList());
                DRes<SInt> result = AdvancedNumeric.using(root).sum(closed);
                DRes<BigInteger> open = root.numeric().open(result);
                return () -> open.out();
              };
          BigInteger output = runApplication(testApplication);
          assertEquals(output, inputs.stream().reduce(BigInteger.ZERO, (a, b) -> a.add(b)));
        }
      };
    }
  }
}
