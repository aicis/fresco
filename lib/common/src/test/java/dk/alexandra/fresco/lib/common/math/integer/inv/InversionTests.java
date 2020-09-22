package dk.alexandra.fresco.lib.common.math.integer.inv;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class InversionTests {

  public static class TestInversion<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> inputs = Arrays.asList(
            BigInteger.valueOf(1),
            BigInteger.valueOf(2),
            BigInteger.valueOf(1234),
            BigInteger.valueOf(123456),
            BigInteger.valueOf(-1),
            BigInteger.valueOf(-1234));

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();
            List<DRes<SInt>> outputs = new ArrayList<>();

            for (BigInteger input : inputs) {
              DRes<SInt> base = numeric.input(input, 1);
              outputs.add(AdvancedNumeric.using(producer).invert(base));
            }

            List<DRes<BigInteger>> open = outputs.stream().map(numeric::open)
                .collect(Collectors.toList());
            return () -> open.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigInteger> result = runApplication(app);

          for (int i = 0; i < inputs.size(); i++) {
            Assert.assertEquals(inputs.get(i).modInverse(this.getFieldDefinition().getModulus()),
                result.get(i));
          }
        }
      };
    }
  }

  public static class TestInvertZero<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            Numeric numeric = producer.numeric();

            DRes<SInt> base = numeric.known(0);
            DRes<SInt> output = AdvancedNumeric.using(producer).invert(base);
            DRes<BigInteger> open = numeric.open(output);

            return () -> open.out();
          };

          // Should throw an RuntimeException caused by an ArithmeticException
          runApplication(app);
        }
      };
    }
  }
}
