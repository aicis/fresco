package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.fixed.utils.NormalizeSInt;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;

public class NormalizeTests {

  public static class TestNormalizeSInt<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigInteger> openInputs = Stream.of(123, 1234, 12345, 123456, 1234567, 12345678, 123456789)
          .map(BigInteger::valueOf).collect(Collectors.toList());
      
      int l = 32;
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> {

            List<DRes<SInt>> closed1 =
                openInputs.stream().map(producer.numeric()::known).collect(Collectors.toList());

            List<DRes<SInt>> result = new ArrayList<>();
            for (DRes<SInt> inputX : closed1) {
              result.add(producer.seq(new NormalizeSInt(inputX, l)));
            }

            List<DRes<BigInteger>> opened =
                result.stream().map(producer.numeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigInteger> output = runApplication(app);

          for (BigInteger x : output) {
            int idx = output.indexOf(x);
            BigInteger expected = BigInteger.ONE.shiftLeft(l - openInputs.get(idx).bitLength());
            Assert.assertEquals(expected, x);
            System.out.println(x);
          }
        }
      };
    }
  }
  
  public static class TestNormalizeSReal<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

@Override
public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
  List<Double> openInputs = Stream.of(0.000123, 0.00123, 0.0123, 0.123, 1.234, 12.345, 123.45, 1234.5, 12345.)
      .collect(Collectors.toList());
  
  return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
    @Override
    public void test() throws Exception {
      Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {

        List<DRes<SReal>> closed1 =
            openInputs.stream().map(producer.realNumeric()::known).collect(Collectors.toList());

        List<DRes<SReal>> result = new ArrayList<>();
        for (DRes<SReal> inputX : closed1) {
          result.add(producer.realAdvanced().normalize(inputX));
        }

        List<DRes<BigDecimal>> opened =
            result.stream().map(producer.realNumeric()::open).collect(Collectors.toList());
        return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
      };
      List<BigDecimal> output = runApplication(app);

      for (BigDecimal x : output) {
        int idx = output.indexOf(x);
        Double input = openInputs.get(idx);
        Double scaled = input * x.doubleValue();
        Assert.assertTrue(scaled >= 0.5 && scaled < 1.0);
      }
    }
  };
}
}
}
