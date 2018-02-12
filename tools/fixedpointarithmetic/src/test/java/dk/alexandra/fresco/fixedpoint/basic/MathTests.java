package dk.alexandra.fresco.fixedpoint.basic;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;

import dk.alexandra.fresco.fixedpoint.DefaultFixedNumeric;
import dk.alexandra.fresco.fixedpoint.FixedNumeric;
import dk.alexandra.fresco.fixedpoint.SFixed;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public class MathTests {

  public static class TestExp<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          int precision = 5;
          double x = 2.1;
          BigDecimal input = BigDecimal.valueOf(x);
          BigDecimal expected = BigDecimal.valueOf(Math.exp(x)).setScale(precision,
              RoundingMode.DOWN);

          // functionality to be tested
          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = root -> {
            // close inputs
            FixedNumeric fixed = new DefaultFixedNumeric(root, precision);
            DRes<SFixed> secret = fixed.numeric().input(input, 1);
            DRes<SFixed> result = fixed.advanced().exp(secret);
            return fixed.numeric().open(result);
          };
          BigDecimal output = runApplication(testApplication);
          Assert.assertEquals(expected, output);
        }
      };
    }
  }

  public static class TestRandom<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> producer
              .seq(seq -> {
            FixedNumeric fixed = new DefaultFixedNumeric(seq, 5);

            List<DRes<SFixed>> result = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
              result.add(fixed.advanced().random());
            }
            return () -> result;
          }).seq((seq, dat) -> {
            FixedNumeric fixed = new DefaultFixedNumeric(seq, 5);
            List<DRes<BigDecimal>> opened = dat.stream().map(fixed.numeric()::open)
                .collect(Collectors.toList());
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
            assertTrue(BigDecimal.ONE.compareTo(random) >= 0);
            assertTrue(BigDecimal.ZERO.compareTo(random) <= 0);
          }
        }
      };
    }
  }
}