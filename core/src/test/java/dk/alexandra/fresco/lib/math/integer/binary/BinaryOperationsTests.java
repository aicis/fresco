package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RightShiftResult;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
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
          Application<Pair<BigInteger, List<BigInteger>>, ProtocolBuilderNumeric> app =
              (ProtocolBuilderNumeric builder) -> {
            AdvancedNumeric rightShift = builder.advancedNumeric();
            DRes<SInt> encryptedInput = builder.numeric().known(input);
            DRes<RightShiftResult> shiftedRight =
                rightShift.rightShiftWithRemainder(encryptedInput, shifts);
            Numeric NumericBuilder = builder.numeric();
            DRes<BigInteger> openResult = NumericBuilder.open(() -> shiftedRight.out().getResult());
            DRes<List<DRes<BigInteger>>> openRemainders = builder.seq((innerBuilder) -> {
              Numeric innerOpenBuilder = innerBuilder.numeric();
              List<DRes<BigInteger>> opened = shiftedRight.out().getRemainder().stream()
                  .map(innerOpenBuilder::open).collect(Collectors.toList());
              return () -> opened;
            });
            return () -> new Pair<>(openResult.out(),
                openRemainders.out().stream().map(DRes::out).collect(Collectors.toList()));
          };
          Pair<BigInteger, List<BigInteger>> output = runApplication(app);
          BigInteger result = output.getFirst();
          List<BigInteger> remainders = output.getSecond();

          Assert.assertEquals(result, input.shiftRight(3));
          BigInteger lastRound = input;
          for (BigInteger remainder : remainders) {
            Assert.assertEquals(lastRound.mod(BigInteger.valueOf(2)), remainder);
            lastRound = lastRound.shiftRight(1);
          }
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
            AdvancedNumeric bitLengthBuilder = builder.advancedNumeric();
            DRes<SInt> bitLength = bitLengthBuilder.bitLength(sharedInput, input.bitLength() * 2);
            return builder.numeric().open(bitLength);
          };
          BigInteger result = runApplication(app);

          Assert.assertEquals(BigInteger.valueOf(input.bitLength()), result);
        }
      };
    }
  }
}
