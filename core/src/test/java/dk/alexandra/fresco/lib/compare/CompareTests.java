package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class CompareTests {

  public static class CompareAndSwapTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public CompareAndSwapTest() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {
        @Override
        public void test() throws Exception {
          List<Boolean> rawLeft = Arrays.asList(ByteAndBitConverter.toBoolean("ee"));
          List<Boolean> rawRight = Arrays.asList(ByteAndBitConverter.toBoolean("00"));

          Application<List<List<Boolean>>, ProtocolBuilderBinary> app =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> left =
                    rawLeft.stream().map(seq.binary()::known).collect(Collectors.toList());
                List<DRes<SBool>> right =
                    rawRight.stream().map(seq.binary()::known).collect(Collectors.toList());

                DRes<List<List<DRes<SBool>>>> compared =
                    new CompareAndSwap(left, right).buildComputation(seq);
                return compared;
              }).seq((seq, opened) -> {
                List<List<DRes<Boolean>>> result = new ArrayList<>();
                for (List<DRes<SBool>> entry : opened) {
                  result.add(entry.stream().map(DRes::out).map(seq.binary()::open)
                      .collect(Collectors.toList()));
                }

                return () -> result;
              }).seq((seq, opened) -> {
                List<List<Boolean>> result = new ArrayList<>();
                for (List<DRes<Boolean>> entry : opened) {
                  result.add(entry.stream().map(DRes::out).collect(Collectors.toList()));
                }

                return () -> result;
              });

          List<List<Boolean>> res = runApplication(app);

          Assert.assertEquals("00", ByteAndBitConverter.toHex(res.get(0)));
          Assert.assertEquals("ee", ByteAndBitConverter.toHex(res.get(1)));
        }
      };
    }
  }

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
            Comparison comparison = builder.comparison();
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
   * Compares the two numbers 3 and 5 and checks that 3 == 3. Also checks that 3 != 5
   */
  public static class TestCompareEQ<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric input = builder.numeric();
            DRes<SInt> x = input.known(BigInteger.valueOf(3));
            DRes<SInt> y = input.known(BigInteger.valueOf(5));
            Comparison comparison = builder.comparison();
            DRes<SInt> compResult1 = comparison.equals(x, x);
            DRes<SInt> compResult2 = comparison.equals(x, y);
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
            Comparison comparison = builder.comparison();
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
            DRes<List<DRes<BigInteger>>> opened = builder.collections().openList(() -> comps);

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
            Comparison comparison = builder.comparison();
            BigInteger modulus = builder.getBasicNumericContext().getModulus();
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
            DRes<List<DRes<BigInteger>>> opened = builder.collections().openList(() -> comps);

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

}
