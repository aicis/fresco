package dk.alexandra.fresco.lib.common.math.logical;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.common.logical.Logical;
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
                List<DRes<SInt>> leftClosed = left.stream().map(root.numeric()::known).collect(Collectors.toList());
                DRes<List<DRes<SInt>>> xored = Logical.using(root)
                    .pairWiseXorKnown(right, DRes.of(leftClosed));
                return Collections.using(root).openList(xored);
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
                List<DRes<SInt>> leftClosed = left.stream().map(root.numeric()::known).collect(Collectors.toList());
                DRes<List<DRes<SInt>>> anded = Logical.using(root)
                    .pairWiseAndKnown(right, DRes.of(leftClosed));
                return Collections.using(root).openList(anded);
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
                List<DRes<SInt>> leftClosed = left.stream().map(root.numeric()::known).collect(Collectors.toList());
                List<DRes<SInt>> rightClosed = right.stream().map(root.numeric()::known).collect(Collectors.toList());
                DRes<List<DRes<SInt>>> anded = Logical.using(root)
                    .pairWiseAnd(DRes.of(leftClosed), DRes.of(rightClosed));
                return Collections.using(root).openList(anded);
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
                List<DRes<SInt>> leftClosed = left.stream().map(root.numeric()::known).collect(Collectors.toList());
                List<DRes<SInt>> rightClosed = right.stream().map(root.numeric()::known).collect(Collectors.toList());
                DRes<List<DRes<SInt>>> anded = Logical.using(root).pairWiseOr(DRes.of(leftClosed), DRes.of(rightClosed));
                return Collections.using(root).openList(anded);
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

  public static class TestOrNeighbors<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> list = Arrays.asList(
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.ZERO,
            BigInteger.ZERO);

        @Override
        public void test() {
          Application<List<DRes<BigInteger>>, ProtocolBuilderNumeric> app =
              root -> {
                List<DRes<SInt>> leftClosed = list.stream().map(root.numeric()::known).collect(Collectors.toList());
                return root.seq(seq -> {
                  DRes<List<DRes<SInt>>> orred = Logical.using(seq).orNeighbors(leftClosed);
                  return Collections.using(seq).openList(orred);
                });
              };
          List<BigInteger> actual = runApplication(app).stream().map(DRes::out)
              .collect(Collectors.toList());
          List<BigInteger> expected = Arrays.asList(
              BigInteger.ONE,
              BigInteger.ONE,
              BigInteger.ZERO
          );
          Assert.assertEquals(expected, actual);
        }
      };
    }
  }

  public static class TestOrList<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<BigInteger> input1 = Arrays.asList(BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE);
        private final List<BigInteger> input2 = Arrays.asList(BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        private final List<BigInteger> input3 = Arrays.asList(BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO);
        private final List<BigInteger> input4 = Arrays.asList(BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO, BigInteger.ONE);

        @Override
        public void test() {
          List<List<BigInteger>> inputLists = Arrays.asList(input1, input2,
              input3, input4);
          List<BigInteger> expectedOutput = Arrays.asList(BigInteger.ONE,
              BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);

          Application<List<BigInteger>, ProtocolBuilderNumeric> app = root -> {
            List<DRes<BigInteger>> results = inputLists.stream().map(
                current -> root.numeric().open(Logical.using(root).orOfList(DRes.of(current.stream().map(root.numeric()::known).collect(
                    Collectors.toList()))))).collect(Collectors.toList());
            return () -> results.stream().map(DRes::out).collect(Collectors
                .toList());
          };
          List<BigInteger> actual = runApplication(app);
          Assert.assertArrayEquals(expectedOutput.toArray(), actual.toArray());
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
                DRes<List<DRes<SInt>>> bits = DRes.of(Arrays.asList(root.numeric().known(BigInteger.ONE), root.numeric().known(BigInteger.ZERO)));
                DRes<List<DRes<SInt>>> notted = Logical.using(root).batchedNot(bits);
                return Collections.using(root).openList(notted);
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