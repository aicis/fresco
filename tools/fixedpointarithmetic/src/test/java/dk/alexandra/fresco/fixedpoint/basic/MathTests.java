package dk.alexandra.fresco.fixedpoint.basic;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.decimal.RealNumeric;
import dk.alexandra.fresco.decimal.RealNumericProvider;
import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.decimal.utils.Truncate;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class MathTests {

  public static class TestExp<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private RealNumericProvider provider;

    public TestExp(RealNumericProvider provider) {
      this.provider = provider;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          int precision = 4;
          double x = 2.1;
          BigDecimal input = BigDecimal.valueOf(x);
          BigDecimal expected = BigDecimal.valueOf(Math.exp(x));

          // functionality to be tested
          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = root -> {
            // close inputs
            RealNumeric fixed = provider.apply(root);
            DRes<SReal> secret = fixed.numeric().input(input, 1);
            DRes<SReal> result = fixed.advanced().exp(secret);
            return fixed.numeric().open(result);
          };
          BigDecimal output = runApplication(testApplication);
          Assert.assertTrue(TestUtils.isEqual(expected.setScale(precision, RoundingMode.DOWN),
              output.setScale(precision, RoundingMode.DOWN)));
        }
      };
    }
  }

  public static class TestLeq<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private RealNumericProvider provider;

    public TestLeq(RealNumericProvider provider) {
      this.provider = provider;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          BigDecimal x = BigDecimal.valueOf(1.010100001);
          BigDecimal y = BigDecimal.valueOf(1.011);

          // functionality to be tested
          Application<BigInteger, ProtocolBuilderNumeric> testApplication = root -> {
            // close inputs
            RealNumeric fixed = provider.apply(root);
            DRes<SReal> secret1 = fixed.numeric().input(x, 1);
            DRes<SReal> secret2 = fixed.numeric().input(y, 2);
            DRes<SInt> result = fixed.advanced().leq(secret1, secret2);
            return root.numeric().open(result);
          };
          BigInteger output = runApplication(testApplication);
          Assert.assertTrue(output.equals(BigInteger.ONE));
        }
      };
    }
  }

  public static class TestRandom<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private RealNumericProvider provider;

    public TestRandom(RealNumericProvider provider) {
      this.provider = provider;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app =
              producer -> producer.seq(seq -> {
                RealNumeric fixed = provider.apply(seq);

                List<DRes<SReal>> result = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                  result.add(fixed.advanced().random());
                }

                List<DRes<BigDecimal>> opened =
                    result.stream().map(fixed.numeric()::open).collect(Collectors.toList());
                return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
              });

          List<BigDecimal> output = runApplication(app);
          BigDecimal sum = BigDecimal.ZERO;
          BigDecimal min = BigDecimal.ONE;
          BigDecimal max = BigDecimal.ZERO;
          for (BigDecimal random : output) {
            sum = sum.add(random);
            if (random.compareTo(min) == -1) {
              min = random;
            }
            if (random.compareTo(max) == 1) {
              max = random;
            }
            System.out.println(random);
            assertTrue(BigDecimal.ONE.compareTo(random) >= 0);
            assertTrue(BigDecimal.ZERO.compareTo(random) <= 0);
          }
        }
      };
    }
  }
  
  public static class TestTrunc<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

@Override
public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
  return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
    @Override
    public void test() throws Exception {
      Application<BigInteger, ProtocolBuilderNumeric> app =
          producer -> producer.seq(seq -> {

            DRes<SInt> input = seq.numeric().known(new BigInteger("1024"));
            
            DRes<SInt> result = seq.seq(new Truncate(12, input, 2));
            return seq.numeric().open(result);
          });

      BigInteger output = runApplication(app);
      System.out.println(output);

    }
  };
}
}
  
}
