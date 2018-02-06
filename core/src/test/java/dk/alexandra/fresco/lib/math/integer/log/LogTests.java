package dk.alexandra.fresco.lib.math.integer.log;

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
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;


/**
 * Generic test cases for basic finite field operations.
 * <p>
 * Can be reused by a test case for any protocol suite that implements the basic field protocol
 * factory.
 * </p>
 */
public class LogTests {

  public static class TestLogarithm<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final BigInteger[] x = {BigInteger.valueOf(201235), BigInteger.valueOf(1234),
            BigInteger.valueOf(405068), BigInteger.valueOf(123456), BigInteger.valueOf(110)};

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric sIntFactory = builder.numeric();

            ArrayList<DRes<BigInteger>> results = new ArrayList<>();
            for (BigInteger input : x) {
              DRes<SInt> actualInput = sIntFactory.input(input, 1);
              DRes<SInt> result = builder.advancedNumeric().log(actualInput, input.bitLength());
              DRes<BigInteger> openResult = builder.numeric().open(result);
              results.add(openResult);
            }
            return () -> results.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigInteger> results = runApplication(app);

          for (int i = 0; i < x.length; i++) {
            int actual = results.get(i).intValue();
            int expected = (int) Math.log(x[i].doubleValue());
            int difference = Math.abs(actual - expected);
            Assert.assertTrue(difference <= 1); // Difference should be less than a bit
          }
        }
      };
    }
  }
}
