package dk.alexandra.fresco.lib.math.integer.exp;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class ExponentiationTests {

  /**
   * Test binary right shift of a shared secret.
   */
  public static class TestExponentiation<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final BigInteger input = BigInteger.valueOf(12332157);
        private final int exp = 12;

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> base = numeric.input(input, 1);
            DRes<SInt> exponent = numeric.input(BigInteger.valueOf(exp), 1);

            DRes<SInt> result = producer.advancedNumeric().exp(base, exponent, 5);

            return numeric.open(result);
          };
          BigInteger result = runApplication(app);

          Assert.assertEquals(input.pow(exp), result);
        }
      };
    }
  }

  public static class TestExponentiationOpenExponent<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final BigInteger input = BigInteger.valueOf(12332157);
        // One, powers of two, odd numbers, non-two-power even numbers
        private final List<Integer> exps = Arrays.asList(1, 2, 3, 6, 12, 16, 19);

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              producer -> producer.seq(
                  seq -> seq.numeric().known(input)
              ).par((par, base) -> {
                List<DRes<SInt>> closedResults = new ArrayList<>(exps.size());
                for (int exp : exps) {
                  DRes<SInt> res = par.advancedNumeric().exp(base, exp);
                  closedResults.add(res);
                }
                return () -> closedResults;
              }).par((par, closedResults) -> {
                List<DRes<BigInteger>> openResults = closedResults.stream()
                    .map(par.numeric()::open)
                    .collect(Collectors.toList());
                return () -> openResults;
              }).seq((seq, openResults) ->
                  () -> openResults.stream().map(DRes::out).collect(Collectors.toList())
              );
          List<BigInteger> results = runApplication(app);
          assertEquals(results.size(), exps.size());
          Iterator<BigInteger> resIt = results.iterator();
          Iterator<Integer> expIt = exps.iterator();
          while (resIt.hasNext() && expIt.hasNext()) {
            BigInteger actual = resIt.next();
            BigInteger expected = input.pow(expIt.next());
            assertEquals(expected, actual);
          }
        }
      };
    }
  }

  public static class TestExponentiationOpenBase<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final long input = 12332157L;
        private final int exp = 12;

        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            DRes<SInt> exponent = numeric.known(BigInteger.valueOf(exp));

            DRes<SInt> result = producer.advancedNumeric().exp(input, exponent, 5);

            return numeric.open(result);
          };
          BigInteger result = runApplication(app);

          Assert.assertEquals(BigInteger.valueOf(input).pow(exp), result);
        }
      };
    }
  }

  public static class TestExponentiationZeroExponent<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final BigInteger input = BigInteger.valueOf(12332157);

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {

            Numeric numeric = producer.numeric();
            DRes<SInt> base = numeric.known(input);
            BigInteger exponent = BigInteger.ZERO;

            DRes<SInt> result = producer.advancedNumeric().exp(base, exponent);

            return numeric.open(result);
          };
          try {
            runApplication(app);
          } catch (RuntimeException e) {
            // Cause is wrapped in an intermediate concurrent exception.
            if (e.getCause() instanceof IllegalArgumentException) {
              return;
            }
          }
          Assert.fail(
              "Should have thrown an Illegal argument exception since exponent is not allowed to be 0");
        }
      };
    }
  }
}
