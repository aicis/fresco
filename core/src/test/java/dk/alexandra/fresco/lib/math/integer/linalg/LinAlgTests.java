package dk.alexandra.fresco.lib.math.integer.linalg;

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

public class LinAlgTests {


  public static class TestInnerProductClosed<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<Integer> data1 = Arrays.asList(200, 144, 99, 211);
        private final List<Integer> data2 = Arrays.asList(87, 14, 11, 21);
        private final BigInteger expected = new BigInteger("24936");

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            Numeric sIntFactory = builder.numeric();

            List<DRes<SInt>> input1 = data1.stream().map(BigInteger::valueOf)
                .map(sIntFactory::known).collect(Collectors.toList());
            List<DRes<SInt>> input2 = data2.stream().map(BigInteger::valueOf)
                .map(sIntFactory::known).collect(Collectors.toList());
            DRes<SInt> min = builder.seq(new InnerProduct(input1, input2));

            return builder.numeric().open(min);
          };

          BigInteger result = runApplication(app);

          Assert.assertEquals(expected, result);
        }
      };
    }
  }

  public static class TestInnerProductOpen<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<Integer> data1 = Arrays.asList(200, 144, 99, 211);
        private final List<BigInteger> data2 = Arrays.asList(BigInteger.valueOf(87),
            BigInteger.valueOf(14), BigInteger.valueOf(11), BigInteger.valueOf(21));
        private final BigInteger expected = new BigInteger("24936");

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            Numeric sIntFactory = builder.numeric();

            List<DRes<SInt>> input1 = data1.stream().map(BigInteger::valueOf)
                .map(sIntFactory::known).collect(Collectors.toList());
            DRes<SInt> min = builder.seq(new InnerProductOpen(data2, input1));

            return builder.numeric().open(min);
          };

          BigInteger result = runApplication(app);

          Assert.assertEquals(expected, result);
        }
      };
    }
  }
}
