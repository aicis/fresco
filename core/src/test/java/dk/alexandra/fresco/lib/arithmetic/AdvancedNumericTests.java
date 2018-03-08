package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.min.MinInfFrac;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class AdvancedNumericTests {

  public static class TestDivision<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private int numerator;
    private int denominator;
    private BigInteger modulus;

    public TestDivision(int numerator, int denominator) {
      this.numerator = numerator;
      this.denominator = denominator;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            modulus = builder.getBasicNumericContext().getModulus();

            DRes<SInt> p = builder.numeric().known(BigInteger.valueOf(numerator));
            DRes<SInt> q = builder.numeric().known(BigInteger.valueOf(denominator));

            DRes<SInt> result = builder.advancedNumeric().div(p, q);

            return builder.numeric().open(result);
          };

          BigInteger result = runApplication(app);

          Assert.assertEquals(BigInteger.valueOf(numerator / denominator),
              convertRepresentation(result, modulus));
        }
      };
    }

  }


  private static BigInteger convertRepresentation(BigInteger b, BigInteger modulus) {
    // Stolen from Spdz Util
    BigInteger actual = b.mod(modulus);
    if (actual.compareTo(modulus.divide(BigInteger.valueOf(2))) > 0) {
      actual = actual.subtract(modulus);
    }
    return actual;
  }

  public static class TestDivisionWithKnownDenominator<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private int numerator;
    private int denominator;
    private BigInteger modulus;

    public TestDivisionWithKnownDenominator(int numerator, int denominator) {
      this.numerator = numerator;
      this.denominator = denominator;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            modulus = builder.getBasicNumericContext().getModulus();

            DRes<SInt> p = builder.numeric().known(BigInteger.valueOf(numerator));
            BigInteger q = BigInteger.valueOf(denominator);

            DRes<SInt> result = builder.advancedNumeric().div(p, q);

            return builder.numeric().open(result);
          };

          BigInteger result = runApplication(app);

          Assert.assertEquals(BigInteger.valueOf(numerator / denominator),
              convertRepresentation(result, modulus));
        }
      };
    }
  }

  public static class TestModulus<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    static int numerator = 9;
    static int denominator = 4;

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            DRes<SInt> p = builder.numeric().known(BigInteger.valueOf(numerator));
            BigInteger q = BigInteger.valueOf(denominator);

            DRes<SInt> result = builder.advancedNumeric().mod(p, q);

            return builder.numeric().open(result);
          };

          BigInteger result = runApplication(app);

          Assert.assertEquals(BigInteger.valueOf(numerator % denominator), result);
        }
      };
    }
  }

  public static class TestMinInfFrac<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> {
            List<BigInteger> bns = Arrays.asList(BigInteger.valueOf(10), BigInteger.valueOf(2),
                BigInteger.valueOf(30), BigInteger.valueOf(1), BigInteger.valueOf(50),
                BigInteger.valueOf(10), BigInteger.valueOf(20), BigInteger.valueOf(30),
                BigInteger.valueOf(5), BigInteger.valueOf(1));
            List<BigInteger> bds = Arrays.asList(BigInteger.valueOf(10), BigInteger.valueOf(10),
                BigInteger.valueOf(10), BigInteger.valueOf(10), BigInteger.valueOf(10),
                BigInteger.valueOf(10), BigInteger.valueOf(20), BigInteger.valueOf(30),
                BigInteger.valueOf(500), BigInteger.valueOf(50));
            List<BigInteger> binfs = Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(0),
                BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(0),
                BigInteger.valueOf(0), BigInteger.valueOf(0), BigInteger.valueOf(0),
                BigInteger.valueOf(1), BigInteger.valueOf(1));
            Numeric numeric = producer.numeric();
            List<DRes<SInt>> ns =
                bns.stream().map((n) -> numeric.input(n, 1)).collect(Collectors.toList());
            List<DRes<SInt>> ds =
                bds.stream().map((n) -> numeric.input(n, 1)).collect(Collectors.toList());
            List<DRes<SInt>> infs =
                binfs.stream().map((n) -> numeric.input(n, 1)).collect(Collectors.toList());

            return producer.seq(new MinInfFrac(ns, ds, infs)).seq((seq2, infOutput) -> {
              Numeric innerNumeric = seq2.numeric();
              List<DRes<BigInteger>> collect =
                  infOutput.cs.stream().map(innerNumeric::open).collect(Collectors.toList());
              return () -> collect.stream().map(DRes::out).collect(Collectors.toList());
            });
          };
          List<BigInteger> outputs = runApplication(app);
          int sum = 0;
          for (int i = 0; i < outputs.size(); i++) {
            sum += outputs.get(i).intValue();
            if (i == 1) {
              Assert.assertEquals(BigInteger.ONE, outputs.get(i));
            } else {
              Assert.assertEquals(BigInteger.ZERO, outputs.get(i));
            }
          }
          Assert.assertEquals(1, sum);

        }
      };
    }
  }
}
