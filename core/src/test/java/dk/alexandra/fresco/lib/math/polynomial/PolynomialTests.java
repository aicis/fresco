package dk.alexandra.fresco.lib.math.polynomial;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.polynomial.evaluator.PolynomialEvaluator;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class PolynomialTests {

  public static class TestPolynomialEvaluator<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final int[] coefficients = {1, 0, 1, 2};
        private final int x = 3;

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = provider -> {
            Numeric numeric = provider.numeric();
            List<DRes<SInt>> secretCoefficients =
                Arrays.stream(coefficients).mapToObj(BigInteger::valueOf)
                    .map((n) -> numeric.input(n, 1)).collect(Collectors.toList());

            PolynomialImpl polynomial = new PolynomialImpl(secretCoefficients);
            DRes<SInt> secretX = numeric.input(BigInteger.valueOf(x), 1);

            DRes<SInt> result = provider.seq(new PolynomialEvaluator(secretX, polynomial));
            return numeric.open(result);
          };
          BigInteger result = runApplication(app);

          int f = 0;
          int power = 1;
          for (int coefficient : coefficients) {
            f += coefficient * power;
            power *= x;
          }
          Assert.assertTrue(result.intValue() == f);
          System.out.println("Party " + conf.getMyId() + " is done");
        }
      };
    }
  }
}
