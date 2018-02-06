package dk.alexandra.fresco.lib.math.integer.sqrt;

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

public class SqrtTests {

  public static class TestSquareRoot<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        private final int maxBitLength = 32;
        private final BigInteger[] x = new BigInteger[] {BigInteger.valueOf(1234),
            BigInteger.valueOf(12345), BigInteger.valueOf(123456), BigInteger.valueOf(1234567),
            BigInteger.valueOf(12345678), BigInteger.valueOf(123456789)};
        private final int n = x.length;


        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric numBuilder = builder.numeric();

            List<DRes<BigInteger>> results = new ArrayList<>(n);

            for (BigInteger input : x) {
              DRes<SInt> actualInput = numBuilder.input(input, 1);
              DRes<SInt> result = builder.advancedNumeric().sqrt(actualInput, maxBitLength);
              DRes<BigInteger> openResult = builder.numeric().open(result);
              results.add(openResult);
            }
            return () -> results.stream().map(DRes::out).collect(Collectors.toList());
          };

          List<BigInteger> results = runApplication(app);

          Assert.assertEquals(n, results.size());

          for (int i = 0; i < results.size(); i++) {
            BigInteger result = results.get(i);
            BigInteger expected = BigInteger.valueOf((long) Math.sqrt(x[i].intValue()));

            BigInteger difference = expected.subtract(result).abs();

            int precision = expected.bitLength() - difference.bitLength();

            boolean shouldBeCorrect = precision >= expected.bitLength();
            boolean isCorrect = expected.equals(result);

            Assert.assertFalse(shouldBeCorrect && !isCorrect);
            Assert.assertTrue(isCorrect);
          }
        }
      };
    }
  }
}
