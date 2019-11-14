package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
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

  public static class TestNormalizeSReal<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<Double> openInputs =
          Stream.of(0.000123, 0.00123, 0.0123, 0.123, 1.234, 12.345, 123.45, 1234.5, 12345.)
              .collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app =
              builder -> builder.seq(producer -> {

                List<DRes<SReal>> closed1 = openInputs.stream().map(producer.realNumeric()::known)
                    .collect(Collectors.toList());

                List<DRes<Pair<DRes<SReal>, DRes<SInt>>>> result = new ArrayList<>();
                for (DRes<SReal> inputX : closed1) {
                  result.add(producer.realAdvanced().normalize(inputX));
                }
                return () -> result;
              }).seq((producer, result) -> {
                List<DRes<BigDecimal>> opened = result.stream().map(DRes::out).map(Pair::getFirst)
                    .map(producer.realNumeric()::open).collect(Collectors.toList());
                return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
              });

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

  public static class TestNormalizePowerSReal<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<Double> openInputs =
          Stream.of(0.000123, 0.00123, 0.0123, 0.123, 1.234, 12.345, 123.45, 1234.5, 12345.)
              .collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              builder -> builder.seq(producer -> {

                List<DRes<SReal>> closed1 = openInputs.stream().map(producer.realNumeric()::known)
                    .collect(Collectors.toList());

                List<DRes<Pair<DRes<SReal>, DRes<SInt>>>> result = new ArrayList<>();
                for (DRes<SReal> inputX : closed1) {
                  result.add(producer.realAdvanced().normalize(inputX));
                }
                return () -> result;
              }).seq((producer, result) -> {
                List<DRes<BigInteger>> opened = result.stream().map(DRes::out).map(Pair::getSecond)
                    .map(producer.numeric()::open).collect(Collectors.toList());
                return () -> opened.stream().map(DRes::out)
                    .map(producer.getBasicNumericContext().getFieldDefinition()::convertToSigned)
                    .collect(Collectors.toList());
              });

          List<BigInteger> output = runApplication(app);
          for (BigInteger x : output) {
            int idx = output.indexOf(x);
            Double input = openInputs.get(idx);
            Double scaled = input * Math.pow(2.0, x.doubleValue());
            Assert.assertTrue(scaled >= 0.5 && scaled < 1.0);
          }
        }
      };
    }
  }

  public static class TestNormalizeSInt<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigInteger> openInputs =
          Stream
              .of(-1234567, -12345, -123, -1, 1, 123, 12345, 1234567)
              .map(BigInteger::valueOf).collect(Collectors.toList());

      int l = 64;

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<Pair<List<BigInteger>, List<BigInteger>>, ProtocolBuilderNumeric> app =
              builder -> builder.seq(producer -> {

                List<DRes<SInt>> closed1 =
                    openInputs.stream().map(producer.numeric()::known).collect(Collectors.toList());

                List<DRes<Pair<DRes<SInt>, DRes<SInt>>>> result = new ArrayList<>();
                for (DRes<SInt> inputX : closed1) {
                  result.add(producer.seq(new NormalizeSInt(inputX, l)));
                }
                return () -> result;
              }).seq((producer, result) -> {
                List<DRes<BigInteger>> factors = result.stream().map(DRes::out).map(Pair::getFirst)
                    .map(producer.numeric()::open).collect(Collectors.toList());

                List<DRes<BigInteger>> exponents = result.stream().map(DRes::out).map(Pair::getSecond)
                    .map(producer.numeric()::open).collect(Collectors.toList());

                return () -> new Pair<List<BigInteger>, List<BigInteger>>(factors.stream().map(DRes::out)
                    .map(producer.getBasicNumericContext().getFieldDefinition()::convertToSigned)
                    .collect(Collectors.toList()), exponents.stream().map(DRes::out)
                    .map(producer.getBasicNumericContext().getFieldDefinition()::convertToSigned)
                    .collect(Collectors.toList()));
              });

          Pair<List<BigInteger>, List<BigInteger>> output = runApplication(app);

          for (int i = 0; i < openInputs.size(); i++) {
            BigInteger input = openInputs.get(i);
            int expected = l - input.bitLength();

            Assert.assertEquals(expected, output.getSecond().get(i).intValue());

            Assert.assertEquals(
                BigInteger.ONE.shiftLeft(expected).multiply(BigInteger.valueOf(input.signum())),
                output.getFirst().get(i));
          }
        }
      };
    }
  }

}
