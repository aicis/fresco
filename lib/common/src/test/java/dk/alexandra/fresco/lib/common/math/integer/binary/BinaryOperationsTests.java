package dk.alexandra.fresco.lib.common.math.integer.binary;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric.RightShiftResult;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;


/**
 * Generic test cases for basic finite field operations.
 * <p>
 * Can be reused by a test case for any protocol suite that implements the basic field protocol
 * factory.
 * </p>
 */
public class BinaryOperationsTests {

  /**
   * Test binary right shift of a shared secret.
   */
  public static class TestRightShift<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final BigInteger input = BigInteger.valueOf(12332157);
        private final int shifts = 3;

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              (ProtocolBuilderNumeric builder) -> {
                AdvancedNumeric rightShift = AdvancedNumeric.using(builder);
                DRes<SInt> encryptedInput = builder.numeric().known(input);
                DRes<RightShiftResult> shiftedRight =
                    rightShift.rightShiftWithRemainder(encryptedInput, shifts);
                DRes<BigInteger> openResult =
                    builder.numeric().open(() -> shiftedRight.out().getResult());
                DRes<BigInteger> openRemainder =
                    builder.numeric().open(() -> shiftedRight.out().getRemainder());
                return () -> Arrays.asList(openResult.out(), openRemainder.out());
              };
              List<BigInteger> output = runApplication(app);

          Assert.assertEquals(input.shiftRight(shifts), output.get(0));
          Assert.assertEquals(input.mod(BigInteger.ONE.shiftLeft(shifts)), output.get(1));
        }
      };
    }
  }


  /**
   * Test binary right shift of a shared secret.
   */
  public static class TestBitLength<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final BigInteger input = BigInteger.valueOf(5);

        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            DRes<SInt> sharedInput = builder.numeric().known(input);
            AdvancedNumeric bitLengthBuilder = AdvancedNumeric.using(builder);
            DRes<SInt> bitLength = bitLengthBuilder.bitLength(sharedInput, input.bitLength() * 2);
            return builder.numeric().open(bitLength);
          };
          BigInteger result = runApplication(app);

          Assert.assertEquals(BigInteger.valueOf(input.bitLength()), result);
        }
      };
    }
  }

  /**
   * Test binary right shift of a shared secret.
   */
  public static class TestBits<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final BigInteger input = BigInteger.valueOf(12345);
        private final int max = 16;

        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              producer -> producer.seq(builder -> {
                DRes<SInt> sharedInput = builder.numeric().known(input);
                return AdvancedNumeric.using(builder).toBits(sharedInput, max);
              }).seq((seq, result) -> {
                List<DRes<BigInteger>> outs =
                    result.stream().map(seq.numeric()::open).collect(Collectors.toList());
                return () -> outs.stream().map(DRes::out).collect(Collectors.toList());
              });

          List<BigInteger> result = runApplication(app);
          for (int i = 0; i < max; i++) {
            Assert.assertEquals(result.get(i).testBit(0), input.testBit(i));
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
              .of(-1234567, -12345, -123, -1, 1, 123, 12345, 1234567, 123456789)
              .map(BigInteger::valueOf).collect(Collectors.toList());

      int l = 16;

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<Pair<List<BigInteger>, List<BigInteger>>, ProtocolBuilderNumeric> app =
              builder -> builder.seq(producer -> {

                List<DRes<SInt>> closed1 =
                    openInputs.stream().map(producer.numeric()::known).collect(Collectors.toList());

                List<DRes<Pair<DRes<SInt>, DRes<SInt>>>> result = new ArrayList<>();
                for (DRes<SInt> inputX : closed1) {
                  result.add(AdvancedNumeric.using(producer).normalize(inputX, l));
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
            int expected = Math.max(0, l - input.bitLength());

            Assert.assertEquals(expected, output.getSecond().get(i).intValue());

            Assert.assertEquals(
                BigInteger.ONE.shiftLeft(expected).multiply(BigInteger.valueOf(input.signum())),
                output.getFirst().get(i));
          }
        }
      };
    }
  }

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
              result.add(AdvancedNumeric.using(producer).truncate(inputX, shifts));
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

  public static class TestTruncationTrivial<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      List<BigInteger> openInputs = Stream.of(123, 1234, 12345, 123456, 1234567, 12345678)
          .map(BigInteger::valueOf).collect(Collectors.toList());
      int shifts = 32;
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> {

            List<DRes<SInt>> closed1 =
                openInputs.stream().map(producer.numeric()::known).collect(Collectors.toList());

            List<DRes<SInt>> result = new ArrayList<>();
            for (DRes<SInt> inputX : closed1) {
              result.add(new Truncate(inputX, shifts, 30).buildComputation(producer));
            }

            List<DRes<BigInteger>> opened =
                result.stream().map(producer.numeric()::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          };
          List<BigInteger> output = runApplication(app);

          for (BigInteger x : output) {
            Assert.assertEquals(BigInteger.ZERO, x);
          }
        }
      };
    }
  }

  public static class TestGenerateRandomBitMask<ResourcePoolT extends NumericResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        private int numBits = -1;
        private BigInteger modulus;

        private BigInteger recombine(List<BigInteger> bits) {
          BigInteger result = BigInteger.ZERO;
          for (int i = 0; i < bits.size(); i++) {
            result = result.add(BigInteger.ONE.shiftLeft(i).multiply(bits.get(i)).mod(modulus));
          }
          return result.mod(modulus);
        }

        @Override
        public void test() {
          Application<Pair<DRes<BigInteger>, List<DRes<BigInteger>>>, ProtocolBuilderNumeric> app =
              root -> {
                numBits = root.getBasicNumericContext().getMaxBitLength() - 1;
                modulus = root.getBasicNumericContext().getModulus();
                return root.seq(seq -> AdvancedNumeric.using(seq).additiveMask(numBits))
                    .seq((seq, mask) -> {
                      DRes<BigInteger> rec = seq.numeric().open(mask.value);
                      DRes<List<DRes<BigInteger>>> bits = Collections.using(seq)
                          .openList(DRes.of(mask.bits));
                      return () -> new Pair<>(rec, bits.out());
                    });
              };
          Pair<DRes<BigInteger>, List<DRes<BigInteger>>> actual = runApplication(app);
          BigInteger recombined = actual.getFirst().out();
          List<BigInteger> bits = actual.getSecond().stream()
              .map(DRes::out)
              .collect(Collectors.toList());
          Assert.assertEquals(numBits, bits.size());
          Assert.assertEquals(recombine(bits), recombined);
        }
      };
    }
  }

}
