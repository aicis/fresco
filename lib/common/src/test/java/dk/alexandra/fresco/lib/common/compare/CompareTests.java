package dk.alexandra.fresco.lib.common.compare;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.Comparison.Algorithm;
import dk.alexandra.fresco.lib.common.compare.eq.FracEq;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class CompareTests {

  /**
   * Compares the two numbers 3 and 5 and checks that 3 <= 5. Also checks that 5 is not <= 3 and
   * that 3 <= 3
   */
  public static class TestCompareLT<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            DRes<SInt> x = input.input(BigInteger.valueOf(3), 1);
            DRes<SInt> y = input.input(BigInteger.valueOf(5), 1);
            Comparison comparison = Comparison.using(builder);
            DRes<SInt> compResult1 = comparison.compareLEQ(x, y);
            DRes<SInt> compResult2 = comparison.compareLEQ(y, x);
            DRes<SInt> compResult3 = comparison.compareLEQ(x, x);
            Numeric open = builder.numeric();
            DRes<BigInteger> res1;
            DRes<BigInteger> res2;
            res1 = open.open(compResult1);
            res2 = open.open(compResult2);
            DRes<BigInteger> res3 = open.open(compResult3);
            return () -> Arrays.asList(res1.out(), res2.out(), res3.out());
          };
          List<BigInteger> output = runApplication(app);
          Assert.assertEquals(BigInteger.ONE, output.get(0));
          Assert.assertEquals(BigInteger.ZERO, output.get(1));
          Assert.assertEquals(BigInteger.ONE, output.get(2));
        }
      };
    }
  }

  /**
   * Compares the two numbers 3 and 5 and checks that 3 == 3. Also checks that 3 != 5 but that the
   * least significant bit of 3 and 5 is the same.
   */
  public static class TestCompareEQ<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            DRes<SInt> x = input.known(BigInteger.valueOf(3));
            DRes<SInt> y = input.known(BigInteger.valueOf(5));
            Comparison comparison = Comparison.using(builder);
            DRes<SInt> compResult1 = comparison.equals(x, x);
            DRes<SInt> compResult2 = comparison.equals(x, y);
            DRes<SInt> compResult3 = comparison.equals(x, y, 1);
            Numeric open = builder.numeric();
            DRes<BigInteger> res1 = open.open(compResult1);
            DRes<BigInteger> res2 = open.open(compResult2);
            DRes<BigInteger> res3 = open.open(compResult3);
            return () -> Arrays.asList(res1.out(), res2.out(), res3.out());
          };
          List<BigInteger> output = runApplication(app);
          Assert.assertEquals(BigInteger.ONE, output.get(0));
          Assert.assertEquals(BigInteger.ZERO, output.get(1));
          Assert.assertEquals(BigInteger.ONE, output.get(2));
        }
      };
    }
  }

  public static class TestCompareEQEdgeCases<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    // returns modulus / 2 + added
    private BigInteger halfModPlus(BigInteger modulus, String added) {
      return modulus.divide(BigInteger.valueOf(2)).add(new BigInteger(added));
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            Comparison comparison = Comparison.using(builder);
            BigInteger modulus = builder.getBasicNumericContext().getModulus();

            // check (mod / 2) == (mod / 2)
            DRes<SInt> compResultOne = comparison
                .equals(input.known(halfModPlus(modulus, "0")),
                    input.known(halfModPlus(modulus, "0")));
            // check (mod / 2 + 1) == (mod / 2 + 1)
            DRes<SInt> compResultTwo = comparison
                .equals(input.known(halfModPlus(modulus, "1")),
                    input.known(halfModPlus(modulus, "1")));
            // check (mod / 2 + 1) != (mod / 2 + 2)
            DRes<SInt> compResultThree = comparison
                .equals(input.known(halfModPlus(modulus, "1")),
                    input.known(halfModPlus(modulus, "2")));
            // check (mod / 2 + 2) != 2
            DRes<SInt> compResultFour = comparison
                .equals(input.known(halfModPlus(modulus, "2")), input.known(new BigInteger("2")));
            // check -1 == -1
            DRes<SInt> compResultFive = comparison
                .equals(input.known(BigInteger.valueOf(-1)), input.known(BigInteger.valueOf(-1)));
            // check -1 != -2
            DRes<SInt> compResultSix = comparison
                .equals(input.known(BigInteger.valueOf(-1)), input.known(BigInteger.valueOf(-2)));
            // check -1 != 1
            DRes<SInt> compResultSeven = comparison
                .equals(input.known(BigInteger.valueOf(-1)), input.known(BigInteger.valueOf(1)));

            List<DRes<SInt>> comps = Arrays
                .asList(compResultOne, compResultTwo, compResultThree, compResultFour,
                    compResultFive, compResultSix, compResultSeven);
            DRes<List<DRes<BigInteger>>> opened = Collections.using(builder).openList(() -> comps);

            return builder.seq((seq) -> {
              return () -> opened.out().stream().map(DRes::out).collect(Collectors.toList());
            });
          };
          List<BigInteger> output = runApplication(app);
          Assert.assertEquals(BigInteger.ONE, output.get(0));
          Assert.assertEquals(BigInteger.ONE, output.get(1));
          Assert.assertEquals(BigInteger.ZERO, output.get(2));
          Assert.assertEquals(BigInteger.ZERO, output.get(3));
          Assert.assertEquals(BigInteger.ONE, output.get(4));
          Assert.assertEquals(BigInteger.ZERO, output.get(5));
          Assert.assertEquals(BigInteger.ZERO, output.get(6));
        }
      };
    }
  }

  public static class TestCompareLTEdgeCases<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    // returns 2^maxBitLength - toSubtract
    private BigInteger maxValMinus(int maxBitLength, String toSubtract) {
      return BigInteger.valueOf(2).pow(maxBitLength).subtract(new BigInteger(toSubtract));
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            Comparison comparison = Comparison.using(builder);
            int maxBitLength = builder.getBasicNumericContext().getMaxBitLength();

            List<DRes<SInt>> comps = Arrays.asList(
                // check MAX <= MAX
                comparison.compareLEQ(
                    input.known(maxValMinus(maxBitLength, "0")),
                    input.known(maxValMinus(maxBitLength, "0"))),
                // check not MAX <= 1
                comparison.compareLEQ(
                    input.known(maxValMinus(maxBitLength, "0")),
                    input.known(BigInteger.ONE)),
                // check -3 <= -1
                comparison.compareLEQ(
                    input.known(BigInteger.valueOf(-3)),
                    input.known(BigInteger.valueOf(-1))),
                // check not -1 <= -3
                comparison.compareLEQ(
                    input.known(BigInteger.valueOf(-1)),
                    input.known(BigInteger.valueOf(-3))),
                // check -3 <= 0
                comparison.compareLEQ(
                    input.known(BigInteger.valueOf(-3)),
                    input.known(BigInteger.valueOf(0))),
                // check -3 <= 1
                comparison.compareLEQ(
                    input.known(BigInteger.valueOf(-3)),
                    input.known(BigInteger.valueOf(1)))
            );
            DRes<List<DRes<BigInteger>>> opened = Collections.using(builder).openList(() -> comps);

            return builder.seq((seq) -> {
              return () -> opened.out().stream().map(DRes::out).collect(Collectors.toList());
            });
          };
          List<BigInteger> output = runApplication(app);
          Assert.assertEquals(BigInteger.ONE, output.get(0));
          Assert.assertEquals(BigInteger.ZERO, output.get(1));
          Assert.assertEquals(BigInteger.ONE, output.get(2));
          Assert.assertEquals(BigInteger.ZERO, output.get(3));
          Assert.assertEquals(BigInteger.ONE, output.get(4));
          Assert.assertEquals(BigInteger.ONE, output.get(5));
        }
      };
    }
  }

  /**
   * Compares the two numbers 3/5 and -6/-10 and checks that they are equal. Also checks that 3/5 != -6/10
   */
  public static class TestCompareFracEQ<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            DRes<SInt> n0 = input.known(BigInteger.valueOf(3));
            DRes<SInt> d0 = input.known(BigInteger.valueOf(5));
            DRes<SInt> n1 = input.known(BigInteger.valueOf(-6));
            DRes<SInt> d1 = input.known(BigInteger.valueOf(-10));
            DRes<SInt> d2 = input.known(BigInteger.valueOf(10));
            DRes<SInt> compResult1 = new FracEq(n0, d0, n1, d1).buildComputation(builder);
            DRes<SInt> compResult2 = new FracEq(n0, d0, n1, d2).buildComputation(builder);
            Numeric open = builder.numeric();
            DRes<BigInteger> res1 = open.open(compResult1);
            DRes<BigInteger> res2 = open.open(compResult2);
            return () -> new Pair<>(res1.out(), res2.out());
          };
          Pair<BigInteger, BigInteger> output = runApplication(app);
          Assert.assertEquals(BigInteger.ONE, output.getFirst());
          Assert.assertEquals(BigInteger.ZERO, output.getSecond());
        }
      };
    }
  }

  public static class TestLessThanLogRounds<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final List<BigInteger> openLeft;
    private final List<BigInteger> openRight;
    private final List<BigInteger> expected;

    public TestLessThanLogRounds(int maxBitLength) {
      BigInteger two = BigInteger.valueOf(2);
      this.openLeft = Arrays.asList(
          BigInteger.ZERO,
          BigInteger.ONE,
          BigInteger.ONE,
          BigInteger.valueOf(-1),
          BigInteger.valueOf(-111111),
          BigInteger.valueOf(-111),
          BigInteger.valueOf(-110),
          BigInteger.ONE,
          two.pow(maxBitLength - 1).subtract(BigInteger.ONE),
          two.pow(maxBitLength - 1).subtract(two),
          BigInteger.TEN,
          two.pow(maxBitLength - 1).subtract(BigInteger.ONE),
          two.pow(maxBitLength - 1).subtract(two)
      );
      this.openRight = Arrays.asList(
          BigInteger.ZERO,
          BigInteger.ONE,
          BigInteger.ZERO,
          BigInteger.valueOf(-1),
          BigInteger.valueOf(-111112),
          BigInteger.valueOf(-110),
          BigInteger.valueOf(10),
          BigInteger.valueOf(5),
          two.pow(maxBitLength - 1).subtract(two),
          two.pow(maxBitLength - 1).subtract(BigInteger.ONE),
          BigInteger.valueOf(-1),
          BigInteger.ONE,
          BigInteger.valueOf(-1)
      );
      this.expected = computeExpected(openLeft, openRight);
    }

    private static List<BigInteger> computeExpected(List<BigInteger> openLeft,
        List<BigInteger> openRight) {
      if (openLeft.size() != openRight.size()) {
        throw new IllegalStateException("Incorrect test spec!");
      }
      List<BigInteger> expected = new ArrayList<>(openLeft.size());
      for (int i = 0; i < openLeft.size(); i++) {
        boolean lessThan = openLeft.get(i).compareTo(openRight.get(i)) < 0;
        expected.add(lessThan ? BigInteger.ONE : BigInteger.ZERO);
      }
      return expected;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric numeric = builder.numeric();
            List<DRes<SInt>> left = openLeft.stream().map(numeric::known).collect(Collectors.toList());
            List<DRes<SInt>> right = openRight.stream().map(numeric::known).collect(Collectors.toList());
            List<DRes<SInt>> actualInner = new ArrayList<>(left.size());
            for (int i = 0; i < left.size(); i++) {
              actualInner.add(Comparison.using(builder).compareLT(left.get(i), right.get(i),
                  Comparison.Algorithm.LOG_ROUNDS));
            }
            DRes<List<DRes<BigInteger>>> opened = Collections.using(builder).openList(() -> actualInner);
            return () -> opened.out().stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigInteger> actual = runApplication(app);
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestCompareEQModulusTooSmall<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final int bitlength;

    public TestCompareEQModulusTooSmall(int bitlength) {
      this.bitlength = bitlength;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            DRes<SInt> x = input.known(BigInteger.valueOf(3));
            DRes<SInt> y = input.known(BigInteger.valueOf(5));
            Comparison comparison = Comparison.using(builder);
            DRes<SInt> result = comparison.equals(x, y, bitlength);
            return input.open(result);
          };
          runApplication(app);
        }
      };
    }
  }

  public static class TestCompareZeroInputTooLarge<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final int bitlength;

    public TestCompareZeroInputTooLarge(int bitlength) {
      this.bitlength = bitlength;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            DRes<SInt> x = input.known(BigInteger.valueOf(3));
            Comparison comparison = Comparison.using(builder);
            DRes<SInt> result = comparison.compareZero(x, bitlength);
            return input.open(result);
          };
          runApplication(app);
        }
      };
    }
  }

  public static class TestCompareLTModulusTooSmall<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            DRes<SInt> x = input.known(BigInteger.valueOf(3));
            DRes<SInt> y = input.known(BigInteger.valueOf(5));
            Comparison comparison = Comparison.using(builder);
            DRes<SInt> result = comparison.compareLT(x, y);
            return input.open(result);
          };
          runApplication(app);
        }
      };
    }
  }

  public static class TestCompareLTUnsupportedAlgorithm<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            DRes<SInt> x = input.known(BigInteger.valueOf(3));
            DRes<SInt> y = input.known(BigInteger.valueOf(5));
            Comparison comparison = Comparison.using(builder);
            DRes<SInt> result = comparison.compareLT(x, y, Algorithm.CONST_ROUNDS);
            return input.open(result);
          };
          runApplication(app);
        }
      };
    }
  }

  public static class TestCompareZero<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            DRes<SInt> x = input.input(BigInteger.valueOf(4), 1);
            Comparison comparison = Comparison.using(builder);
            DRes<SInt> compResult1 = comparison.compareZero(x, 2);
            DRes<SInt> compResult2 = comparison.compareZero(x, 3);
            Numeric open = builder.numeric();
            DRes<BigInteger> res1;
            DRes<BigInteger> res2;
            res1 = open.open(compResult1);
            res2 = open.open(compResult2);
            return () -> Arrays.asList(res1.out(), res2.out());
          };
          List<BigInteger> output = runApplication(app);
          Assert.assertEquals(BigInteger.ONE, output.get(0));
          Assert.assertEquals(BigInteger.ZERO, output.get(1));
        }
      };
    }
  }

  public static class TestCompareZeroAlgorithms<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            Comparison comparison = Comparison.using(builder);
            List<DRes<SInt>> results = new ArrayList<>();
            results.add(comparison.compareZero(input.known(1), builder.getBasicNumericContext().getMaxBitLength(), Algorithm.CONST_ROUNDS));
            results.add(comparison.compareZero(input.known(-1), builder.getBasicNumericContext().getMaxBitLength(), Algorithm.CONST_ROUNDS));
            results.add(comparison.compareZero(input.known(0), builder.getBasicNumericContext().getMaxBitLength(), Algorithm.CONST_ROUNDS));
            results.add(comparison.compareZero(input.known(1), builder.getBasicNumericContext().getMaxBitLength(), Algorithm.LOG_ROUNDS));
            results.add(comparison.compareZero(input.known(-1), builder.getBasicNumericContext().getMaxBitLength(), Algorithm.LOG_ROUNDS));
            results.add(comparison.compareZero(input.known(0), builder.getBasicNumericContext().getMaxBitLength(), Algorithm.LOG_ROUNDS));
            List<DRes<BigInteger>> open = results.stream().map(input::open).collect(Collectors.toList());
            return () -> open.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigInteger> output = runApplication(app);
          Assert.assertArrayEquals(
              new int[]{0, 0, 1, 0, 0, 1}, output.stream().mapToInt(BigInteger::intValue).toArray());
        }
      };
    }
  }

  public static class TestHammingDistance<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            List<DRes<SInt>> results = new ArrayList<>();
            results.add(new HammingDistance(Arrays.asList(input.known(0)), BigInteger.ONE).buildComputation(builder));
            results.add(new HammingDistance(Arrays.asList(input.known(1), input.known(0)), BigInteger.ONE).buildComputation(builder));
            results.add(new HammingDistance(Arrays.asList(input.known(1), input.known(1)), BigInteger.ONE).buildComputation(builder));
            results.add(new HammingDistance(Arrays.asList(input.known(1), input.known(1)), BigInteger.valueOf(3)).buildComputation(builder));
            results.add(new HammingDistance(Arrays.asList(input.known(0), input.known(0), input.known(0), input.known(0)), BigInteger.valueOf(15)).buildComputation(builder));
            List<DRes<BigInteger>> open = results.stream().map(input::open).collect(Collectors.toList());
            return () -> open.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigInteger> output = runApplication(app);
          Assert.assertArrayEquals(
              new int[]{1, 0, 1, 0, 4}, output.stream().mapToInt(BigInteger::intValue).toArray());
        }
      };
    }
  }

}