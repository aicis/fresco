package dk.alexandra.fresco.lib.math.integer.logical;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class LogicalOperationsTests {

  public static class TestXorKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> left = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.ZERO);
        private final List<BigInteger> right = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ZERO);

        @Override
        public void test() {
          Application<List<DRes<BigInteger>>, ProtocolBuilderNumeric> app =
              root -> {
                DRes<List<DRes<SInt>>> leftClosed = root.numeric().knownAsDRes(left);
                List<DRes<OInt>> rightOInts = root.getOIntFactory().fromBigInteger(right);
                DRes<List<DRes<SInt>>> xored = root.logical()
                    .pairWiseXorKnown(() -> rightOInts, leftClosed);
                return root.collections().openList(xored);
              };
          List<BigInteger> actual = runApplication(app).stream().map(DRes::out)
              .collect(Collectors.toList());
          List<BigInteger> expected = Arrays.asList(
              BigInteger.ZERO,
              BigInteger.ONE,
              BigInteger.ONE,
              BigInteger.ZERO
          );
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestAndKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> left = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.ZERO);
        private final List<BigInteger> right = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ZERO);

        @Override
        public void test() {
          Application<List<DRes<BigInteger>>, ProtocolBuilderNumeric> app =
              root -> {
                DRes<List<DRes<SInt>>> leftClosed = root.numeric().knownAsDRes(left);
                List<DRes<OInt>> rightOInts = root.getOIntFactory().fromBigInteger(right);
                DRes<List<DRes<SInt>>> anded = root.logical()
                    .pairWiseAndKnown(() -> rightOInts, leftClosed);
                return root.collections().openList(anded);
              };
          List<BigInteger> actual = runApplication(app).stream().map(DRes::out)
              .collect(Collectors.toList());
          List<BigInteger> expected = Arrays.asList(
              BigInteger.ONE,
              BigInteger.ZERO,
              BigInteger.ZERO,
              BigInteger.ZERO
          );
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestAnd<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> left = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.ZERO);
        private final List<BigInteger> right = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ZERO);

        @Override
        public void test() {
          Application<List<DRes<BigInteger>>, ProtocolBuilderNumeric> app =
              root -> {
                DRes<List<DRes<SInt>>> leftClosed = root.numeric().knownAsDRes(left);
                DRes<List<DRes<SInt>>> rightClosed = root.numeric().knownAsDRes(right);
                DRes<List<DRes<SInt>>> anded = root.logical()
                    .pairWiseAnd(leftClosed, rightClosed);
                return root.collections().openList(anded);
              };
          List<BigInteger> actual = runApplication(app).stream().map(DRes::out)
              .collect(Collectors.toList());
          List<BigInteger> expected = Arrays.asList(
              BigInteger.ONE,
              BigInteger.ZERO,
              BigInteger.ZERO,
              BigInteger.ZERO
          );
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestOr<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> left = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.ZERO);
        private final List<BigInteger> right = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ZERO);

        @Override
        public void test() {
          Application<List<DRes<BigInteger>>, ProtocolBuilderNumeric> app =
              root -> {
                DRes<List<DRes<SInt>>> leftClosed = root.numeric().knownAsDRes(left);
                DRes<List<DRes<SInt>>> rightClosed = root.numeric().knownAsDRes(right);
                DRes<List<DRes<SInt>>> anded = root.logical().pairWiseOr(leftClosed, rightClosed);
                return root.collections().openList(anded);
              };
          List<BigInteger> actual = runApplication(app).stream().map(DRes::out)
              .collect(Collectors.toList());
          List<BigInteger> expected = Arrays.asList(
              BigInteger.ONE,
              BigInteger.ONE,
              BigInteger.ONE,
              BigInteger.ZERO
          );
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestNot<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() {
          Application<List<DRes<BigInteger>>, ProtocolBuilderNumeric> app =
              root -> {
                DRes<SInt> notOne = root.logical().not(root.numeric().known(BigInteger.ONE));
                DRes<SInt> notZero = root.logical().not(root.numeric().known(BigInteger.ZERO));
                List<DRes<SInt>> notted = Arrays.asList(notOne, notZero);
                return root.collections().openList(() -> notted);
              };
          List<BigInteger> actual = runApplication(app).stream().map(DRes::out)
              .collect(Collectors.toList());
          List<BigInteger> expected = Arrays.asList(
              BigInteger.ZERO,
              BigInteger.ONE
          );
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

}
