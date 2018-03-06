package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.fixed.utils.Truncate;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;

public class TruncationTests {

  public static class TestTruncation<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigInteger> openInputs = Stream.of(123, 1234, 12345, 123456, 1234567, 12345678)
          .map(BigInteger::valueOf).collect(Collectors.toList());
      int shifts = 5;
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> {

            List<DRes<SInt>> closed1 =
                openInputs.stream().map(producer.numeric()::known).collect(Collectors.toList());

            List<DRes<SInt>> result = new ArrayList<>();
            for (DRes<SInt> inputX : closed1) {
              result.add(producer.seq(new Truncate(inputX, shifts)));
            }

            List<DRes<BigInteger>> opened =
                result.stream().map(producer.numeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigInteger> output = runApplication(app);

          for (BigInteger x : output) {
            int idx = output.indexOf(x);
            BigInteger expected = openInputs.get(idx).shiftRight(shifts);
            Assert.assertTrue(x.subtract(expected).equals(BigInteger.ONE)
                || x.subtract(expected).equals(BigInteger.ZERO));
          }
        }
      };
    }
  }
}
