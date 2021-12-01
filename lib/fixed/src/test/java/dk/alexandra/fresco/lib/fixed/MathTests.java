package dk.alexandra.fresco.lib.fixed;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;

public class MathTests {

  private static final int DEFAULT_PRECISION = BasicFixedPointTests.DEFAULT_PRECISION;

  public static class TestExp<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {
    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs = Stream.of(-5., -1., -0.1, -.01, 0., .01, 0.1, 1., 5.)
          .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(FixedNumeric.using(producer)::known).collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              result.add(AdvancedFixedNumeric.using(producer).exp(inputX));
            }
            List<DRes<BigDecimal>> opened =
                result.stream().map(FixedNumeric.using(producer)::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal expected = new BigDecimal(Math.exp(openInputs.get(idx).doubleValue()));
            FixedTestUtils.assertEqual(expected, openOutput, DEFAULT_PRECISION / 4); // TODO: Make
                                                                                    // relative to
                                                                                    // expected
          }
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
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app =
              producer -> producer.seq(seq -> {
                List<DRes<SFixed>> result = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                  result.add(AdvancedFixedNumeric.using(seq).random());
                }

                List<DRes<BigDecimal>> opened =
                    result.stream().map(FixedNumeric.using(seq)::open).collect(Collectors.toList());
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

  public static class TestLog<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs = Stream.of(.01, 0.1, 1., 10., 100., 1000.)
          .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {

            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(FixedNumeric.using(producer)::known).collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              result.add(AdvancedFixedNumeric.using(producer).log(inputX));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(FixedNumeric.using(producer)::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);
            FixedTestUtils.assertEqual(new BigDecimal(Math.log(a.doubleValue())), openOutput,
                DEFAULT_PRECISION / 2);
          }
        }
      };
    }
  }

  public static class TestSqrt<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs =
          Stream.of(0., .01, 0.1, 1., 10., 100., 1000.)
              .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(FixedNumeric.using(producer)::known).collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              result.add(AdvancedFixedNumeric.using(producer).sqrt(inputX));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(FixedNumeric.using(producer)::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal expected = BigDecimal.valueOf(Math.sqrt(openInputs.get(idx).doubleValue()));
            FixedTestUtils.assertEqual(expected, openOutput, DEFAULT_PRECISION / 2);
          }
        }
      };
    }
  }

  public static class TestSum<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs = Stream.of(-1., -0.1, -.01, 0., .01, 0.1, 1., 10., 100.)
          .map(BigDecimal::valueOf).collect(Collectors.toList());
      BigDecimal expectedOutput = openInputs.stream().reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            List<DRes<SFixed>> closed =
                openInputs.stream().map(FixedNumeric.using(producer)::known).collect(Collectors.toList());
            return FixedNumeric.using(producer).open(AdvancedFixedNumeric.using(producer).sum(closed));
          };
          BigDecimal output = runApplication(app);
          FixedTestUtils.assertEqual(expectedOutput, output, DEFAULT_PRECISION);
        }
      };
    }
  }

  public static class TestInnerProduct<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs1 =
          Stream.of(64.0, 128.0, 8.0).map(BigDecimal::valueOf).collect(Collectors.toList());
      List<BigDecimal> openInputs2 = Stream.of(1. / 64.0, 1. / 128.0, 1. / 8.0)
          .map(BigDecimal::valueOf).collect(Collectors.toList());
      BigDecimal expectedOutput = BigDecimal.valueOf(3);

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            return producer.par(par -> {
              List<DRes<SFixed>> closed1 =
                  openInputs1.stream().map(FixedNumeric.using(par)::known).collect(Collectors.toList());
              List<DRes<SFixed>> closed2 =
                  openInputs2.stream().map(FixedNumeric.using(par)::known).collect(Collectors.toList());
              return () -> new Pair<>(closed1, closed2);
            }).seq((seq, closedPair) -> {
              DRes<SFixed> result =
                  AdvancedFixedNumeric.using(seq).innerProduct(closedPair.getFirst(), closedPair.getSecond());
              return FixedNumeric.using(seq).open(result);
            });
          };
          BigDecimal output = runApplication(app);
          FixedTestUtils.assertEqual(expectedOutput, output, DEFAULT_PRECISION);
        }
      };
    }
  }

  public static class TestInnerProductUnmatchedDimensions<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs1 = Arrays.asList(BigDecimal.ONE);
      List<BigDecimal> openInputs2 = Arrays.asList(BigDecimal.ONE, BigDecimal.ONE);

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            return producer.par(par -> {
              List<DRes<SFixed>> closed1 =
                  openInputs1.stream().map(FixedNumeric.using(par)::known).collect(Collectors.toList());
              List<DRes<SFixed>> closed2 =
                  openInputs2.stream().map(FixedNumeric.using(par)::known).collect(Collectors.toList());
              return () -> new Pair<>(closed1, closed2);
            }).seq((seq, closedPair) -> {
              AdvancedFixedNumeric.using(seq).innerProduct(closedPair.getFirst(), closedPair.getSecond());
              return () -> null;
            });
          };
          try {
            runApplication(app);
          } catch (RuntimeException e) {
            if (e.getCause().getClass() == IllegalArgumentException.class) {
              // Success - ignore exception
            } else {
              throw e;
            }
          }
        }
      };
    }
  }

  public static class TestInnerProductPublicPart<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs1 =
          Stream.of(64.0, 128.0, 8.0).map(BigDecimal::valueOf).collect(Collectors.toList());
      List<BigDecimal> openInputs2 = Stream.of(1 / 64.0, 1 / 128.0, 1 / 8.0)
          .map(BigDecimal::valueOf).collect(Collectors.toList());
      BigDecimal expectedOutput = BigDecimal.valueOf(3);

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            return producer.par(par -> {
              List<DRes<SFixed>> closed =
                  openInputs1.stream().map(FixedNumeric.using(par)::known).collect(Collectors.toList());
              return () -> closed;
            }).seq((seq, closed) -> {
              DRes<SFixed> result =
                  AdvancedFixedNumeric.using(seq).innerProductWithPublicPart(openInputs2, closed);
              return FixedNumeric.using(seq).open(result);
            });
          };
          BigDecimal output = runApplication(app);
          FixedTestUtils.assertEqual(expectedOutput, output, DEFAULT_PRECISION);
        }
      };
    }
  }

  public static class TestInnerProductPublicPartUnmatched<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs1 = Arrays.asList(BigDecimal.ONE);
      List<BigDecimal> openInputs2 = Arrays.asList(BigDecimal.ONE, BigDecimal.ONE);

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigDecimal, ProtocolBuilderNumeric> app = producer -> {
            return producer.par(par -> {
              List<DRes<SFixed>> closed =
                  openInputs1.stream().map(FixedNumeric.using(par)::known).collect(Collectors.toList());
              return () -> closed;
            }).seq((seq, closed) -> {
              AdvancedFixedNumeric.using(seq).innerProductWithPublicPart(openInputs2, closed);
              return () -> null;
            });
          };
          try {
            runApplication(app);
          } catch (RuntimeException e) {
            if (e.getCause().getClass() == IllegalArgumentException.class) {
              // Success - ignore exception
            } else {
              throw e;
            }
          }
        }
      };
    }
  }

  public static class TestReciprocal<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs = Stream.of(.01, 0.1, 1., 10., 100.)
          .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {

            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(FixedNumeric.using(producer)::known).collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              result.add(AdvancedFixedNumeric.using(producer).reciprocal(inputX));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(FixedNumeric.using(producer)::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            BigDecimal a = openInputs.get(idx);

            BigDecimal expected = BigDecimal.ONE.divide(a, 10, BigDecimal.ROUND_HALF_UP);
            FixedTestUtils.assertEqual(expected, openOutput, DEFAULT_PRECISION / 4);
          }
        }
      };
    }
  }

  public static class TestFixedSign<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs =
          Stream.of(-Math.pow(2.0, DEFAULT_PRECISION - 1), -1e-4, 0.0, 1e-4, 1.0)
              .map(BigDecimal::valueOf).collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> {

            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(FixedNumeric.using(producer)::known).collect(Collectors.toList());

            List<DRes<SInt>> result = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              result.add(AdvancedFixedNumeric.using(producer).sign(inputX));
            }

            List<DRes<BigInteger>> opened =
                result.stream().map(producer.numeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out)
                .map(producer.getBasicNumericContext().getFieldDefinition()::convertToSigned)
                .collect(Collectors.toList());
          };
          List<BigInteger> output = runApplication(app);

          for (BigInteger openOutput : output) {
            int idx = output.indexOf(openOutput);

            int a = openInputs.get(idx).intValue();

            BigInteger expected = BigInteger.valueOf(a >= 0 ? 1 : -1);
            Assert.assertEquals(expected, openOutput);
          }
        }
      };
    }
  }

  public static class TestTwoPower<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigInteger> openInputs = Stream.of(-10, -5, -2, -1, 0, 1, 2, 5, 10).map(BigInteger::valueOf)
          .collect(Collectors.toList());

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {

            List<DRes<SInt>> closed1 =
                openInputs.stream().map(producer.numeric()::known).collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for (DRes<SInt> inputX : closed1) {
              result.add(AdvancedFixedNumeric.using(producer).twoPower(inputX));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(FixedNumeric.using(producer)::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            int idx = output.indexOf(openOutput);

            int a = openInputs.get(idx).intValue();

            BigDecimal expected = BigDecimal.valueOf(Math.pow(2.0, a));
            FixedTestUtils.assertEqual(expected, openOutput, DEFAULT_PRECISION / 4);
          }
        }
      };
    }
  }

  public static class TestConstantPolynomial<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigDecimal> openInputs = Stream.of(-10., -1., -0.1, -.01, 0., .01, 0.1, 1., 10.)
          .map(BigDecimal::valueOf).collect(Collectors.toList());

      double[] p = new double[] {7.0}; // Constant polynomial
              
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> app = producer -> {

            List<DRes<SFixed>> closed1 =
                openInputs.stream().map(FixedNumeric.using(producer)::known).collect(Collectors.toList());

            List<DRes<SFixed>> result = new ArrayList<>();
            for (DRes<SFixed> inputX : closed1) {
              result.add(AdvancedFixedNumeric.using(producer).polynomialEvalutation(inputX, p));
            }

            List<DRes<BigDecimal>> opened =
                result.stream().map(FixedNumeric.using(producer)::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigDecimal> output = runApplication(app);

          for (BigDecimal openOutput : output) {
            FixedTestUtils.assertEqual(p[0], openOutput, DEFAULT_PRECISION);
          }
        }
      };
    }

  }
}
