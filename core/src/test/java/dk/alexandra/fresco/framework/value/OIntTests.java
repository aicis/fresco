package dk.alexandra.fresco.framework.value;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public class OIntTests {
  public static class TestConstantCheck<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<Boolean, ProtocolBuilderNumeric> app = producer -> {
            DRes<OInt> input = producer.getOIntArithmetic().one();
            Assert.assertTrue(producer.getOIntArithmetic().isOne(input.out()));
            Assert.assertFalse(producer.getOIntArithmetic().isZero(input.out()));
            return () -> true;
          };
          boolean output = runApplication(app);

          Assert.assertEquals(true, output);
        }
      };
    }
  }

  public static class TestTwoPowers<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          int rightShiftVal = 15;
          int rightShiftPos = 1;
          int twoPower = 2;
          Application<BigInteger, ProtocolBuilderNumeric> app = producer -> {
            DRes<OInt> input = producer.getOIntFactory().fromBigInteger(BigInteger.valueOf(
                rightShiftVal));
            DRes<OInt> shiftedInput = producer.getOIntArithmetic().shiftRight(input.out(),
                rightShiftPos);
            DRes<OInt> val = producer.getOIntArithmetic().modTwoTo(shiftedInput.out(), twoPower);
            return () -> producer.getOIntFactory().toBigInteger(val.out());
          };
          BigInteger output = runApplication(app);

          int expected = (rightShiftVal >> rightShiftPos) % (1 << twoPower);
          Assert.assertEquals(BigInteger.valueOf(expected), output);
        }
      };
    }
  }

  public static class TestBigPower<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> {
            int twoPower = 3 * producer.getOIntFactory().getMaxBitLength();
            // Force computation of something that isn't cached
            DRes<OInt> input = producer.getOIntArithmetic().twoTo(twoPower);
            List<OInt> bits = producer.getOIntArithmetic().toBits(input.out(), 1 + twoPower);
            return () -> bits.stream().map(val -> producer.getOIntFactory().toBigInteger(val))
                .collect(Collectors.toList());
          };
          List<BigInteger> output = runApplication(app);
          // The most significant bit should be 1
          Assert.assertEquals(BigInteger.ONE, output.get(0));
          // The rest should be 0, since we do shifting
          for (int i = 1; i < output.size(); i++) {
            Assert.assertEquals(BigInteger.ZERO, output.get(i));
          }
        }
      };
    }
  }

  public static class TestFromBigInteger<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          int inputVal = 2;
          Application<List<BigInteger>, ProtocolBuilderNumeric> app = producer -> {
            BigInteger nullInteger = null;
            BigInteger two = BigInteger.valueOf(inputVal);
            List<OInt> input = producer.getOIntFactory().fromBigInteger(Arrays.asList(
                nullInteger, two));
            return () -> input.stream().map(val -> producer.getOIntFactory().toBigInteger(val))
                .collect(Collectors.toList());
          };
          List<BigInteger> output = runApplication(app);

          Assert.assertNull(output.get(0));
          Assert.assertEquals(BigInteger.valueOf(inputVal), output.get(1));
        }
      };
    }
  }
}
