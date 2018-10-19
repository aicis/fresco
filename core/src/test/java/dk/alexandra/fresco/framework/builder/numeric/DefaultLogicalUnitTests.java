package dk.alexandra.fresco.framework.builder.numeric;

import java.math.BigInteger;
import org.junit.Assert;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;

public class DefaultLogicalUnitTests {
  public static class TestLogicalAnd<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            DRes<SInt> one = builder.numeric().input(BigInteger.ONE, 1);
            DRes<SInt> zero = builder.numeric().input(BigInteger.ZERO, 1);
            DRes<SInt> resZero = builder.logical().and(one, zero);
            DRes<SInt> resOne = builder.logical().and(one, one);
            DRes<BigInteger> openResZero = builder.numeric().open(resZero);
            DRes<BigInteger> openResOne = builder.numeric().open(resOne);
            return () -> new Pair<BigInteger, BigInteger>(openResZero.out(), openResOne.out());
          };
          Pair<BigInteger, BigInteger> output = runApplication(app);
          Assert.assertEquals(BigInteger.ZERO, output.getFirst());
          Assert.assertEquals(BigInteger.ONE, output.getSecond());
        }
      };
    }
  }

  public static class TestLogicalXor<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            DRes<SInt> one = builder.numeric().input(BigInteger.ONE, 1);
            DRes<SInt> zero = builder.numeric().input(BigInteger.ZERO, 1);
            DRes<SInt> resZero = builder.logical().xor(one, one);
            DRes<SInt> resOne = builder.logical().xor(zero, one);
            DRes<BigInteger> openResZero = builder.numeric().open(resZero);
            DRes<BigInteger> openResOne = builder.numeric().open(resOne);
            return () -> new Pair<BigInteger, BigInteger>(openResZero.out(), openResOne.out());
          };
          Pair<BigInteger, BigInteger> output = runApplication(app);
          Assert.assertEquals(BigInteger.ZERO, output.getFirst());
          Assert.assertEquals(BigInteger.ONE, output.getSecond());
        }
      };
    }
  }

  public static class TestLogicalOr<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            DRes<SInt> one = builder.numeric().input(BigInteger.ONE, 1);
            DRes<SInt> zero = builder.numeric().input(BigInteger.ZERO, 1);
            DRes<SInt> resZero = builder.logical().or(zero, zero);
            DRes<SInt> resOne = builder.logical().or(one, zero);
            DRes<BigInteger> openResZero = builder.numeric().open(resZero);
            DRes<BigInteger> openResOne = builder.numeric().open(resOne);
            return () -> new Pair<BigInteger, BigInteger>(openResZero.out(), openResOne.out());
          };
          Pair<BigInteger, BigInteger> output = runApplication(app);
          Assert.assertEquals(BigInteger.ZERO, output.getFirst());
          Assert.assertEquals(BigInteger.ONE, output.getSecond());
        }
      };
    }
  }

  public static class TestLogicalNot<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            DRes<SInt> one = builder.numeric().input(BigInteger.ONE, 1);
            DRes<SInt> zero = builder.numeric().input(BigInteger.ZERO, 1);
            DRes<SInt> resZero = builder.logical().not(one);
            DRes<SInt> resOne = builder.logical().not(zero);
            DRes<BigInteger> openResZero = builder.numeric().open(resZero);
            DRes<BigInteger> openResOne = builder.numeric().open(resOne);
            return () -> new Pair<BigInteger, BigInteger>(openResZero.out(), openResOne.out());
          };
          Pair<BigInteger, BigInteger> output = runApplication(app);
          Assert.assertEquals(BigInteger.ZERO, output.getFirst());
          Assert.assertEquals(BigInteger.ONE, output.getSecond());
        }
      };
    }
  }
}
