package dk.alexandra.fresco.lib.collections.relational;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixTestUtils;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import java.math.BigInteger;
import java.util.Collections;

public class LeakyAggregationTests {

  public static class TestLeakyAggregationGeneric<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    final Matrix<BigInteger> input;
    final Matrix<BigInteger> expected;

    TestLeakyAggregationGeneric(Matrix<BigInteger> input, Matrix<BigInteger> expected) {
      this.input = input;
      this.expected = expected;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          // define functionality to be tested
          Application<Matrix<BigInteger>, ProtocolBuilderNumeric> testApplication = root -> {
            DRes<Matrix<DRes<SInt>>> closed = root.collections().closeMatrix(input, 1);
            DRes<Matrix<DRes<SInt>>> aggregated =
                root.collections().leakyAggregateSum(closed, 0, 1);
            DRes<Matrix<DRes<BigInteger>>> opened = root.collections().openMatrix(aggregated);
            return () -> new MatrixUtils().unwrapMatrix(opened);
          };
          Matrix<BigInteger> actual = runApplication(testApplication);
          // sort by key to undo shuffling
          // (keys are guaranteed to be unique)
          Collections.sort(actual.getRows(), (r1, r2) -> r1.get(0).compareTo(r2.get(0)));
          assertThat(actual.getRows(), is(expected.getRows()));
        }
      };
    }
  }

  public static <ResourcePoolT extends ResourcePool> TestLeakyAggregationGeneric<ResourcePoolT> aggregate() {
    MatrixTestUtils utils = new MatrixTestUtils();
    BigInteger[][] rawRows = {{BigInteger.valueOf(1), BigInteger.valueOf(7), BigInteger.valueOf(8)},
        {BigInteger.valueOf(1), BigInteger.valueOf(19), BigInteger.valueOf(20)},
        {BigInteger.valueOf(1), BigInteger.valueOf(10), BigInteger.valueOf(11)},
        {BigInteger.valueOf(1), BigInteger.valueOf(4), BigInteger.valueOf(5)},
        {BigInteger.valueOf(2), BigInteger.valueOf(13), BigInteger.valueOf(14)},
        {BigInteger.valueOf(2), BigInteger.valueOf(1), BigInteger.valueOf(2)},
        {BigInteger.valueOf(2), BigInteger.valueOf(22), BigInteger.valueOf(23)},
        {BigInteger.valueOf(2), BigInteger.valueOf(16), BigInteger.valueOf(17)}};
    Matrix<BigInteger> input = utils.getInputMatrix(rawRows);
    BigInteger[][] expectedRows = {{BigInteger.valueOf(1), BigInteger.valueOf(40)},
        {BigInteger.valueOf(2), BigInteger.valueOf(52)}};
    Matrix<BigInteger> expected = utils.getInputMatrix(expectedRows);
    return new TestLeakyAggregationGeneric<>(input, expected);
  }

  public static <ResourcePoolT extends ResourcePool> TestLeakyAggregationGeneric<ResourcePoolT> aggregateUniqueKeys() {
    MatrixTestUtils utils = new MatrixTestUtils();
    BigInteger[][] rawRows = {{BigInteger.valueOf(1), BigInteger.valueOf(7), BigInteger.valueOf(8)},
        {BigInteger.valueOf(2), BigInteger.valueOf(19), BigInteger.valueOf(20)},
        {BigInteger.valueOf(3), BigInteger.valueOf(10), BigInteger.valueOf(11)},
        {BigInteger.valueOf(4), BigInteger.valueOf(4), BigInteger.valueOf(5)},
        {BigInteger.valueOf(5), BigInteger.valueOf(13), BigInteger.valueOf(14)},
        {BigInteger.valueOf(6), BigInteger.valueOf(1), BigInteger.valueOf(2)},
        {BigInteger.valueOf(7), BigInteger.valueOf(22), BigInteger.valueOf(23)},
        {BigInteger.valueOf(8), BigInteger.valueOf(16), BigInteger.valueOf(17)}};
    Matrix<BigInteger> input = utils.getInputMatrix(rawRows);
    BigInteger[][] expectedRows = {{BigInteger.valueOf(1), BigInteger.valueOf(7)},
        {BigInteger.valueOf(2), BigInteger.valueOf(19)},
        {BigInteger.valueOf(3), BigInteger.valueOf(10)},
        {BigInteger.valueOf(4), BigInteger.valueOf(4)},
        {BigInteger.valueOf(5), BigInteger.valueOf(13)},
        {BigInteger.valueOf(6), BigInteger.valueOf(1)},
        {BigInteger.valueOf(7), BigInteger.valueOf(22)},
        {BigInteger.valueOf(8), BigInteger.valueOf(16)}};
    Matrix<BigInteger> expected = utils.getInputMatrix(expectedRows);
    return new TestLeakyAggregationGeneric<>(input, expected);
  }

  public static <ResourcePoolT extends ResourcePool> TestLeakyAggregationGeneric<ResourcePoolT> aggregateEmpty() {
    MatrixTestUtils utils = new MatrixTestUtils();
    Matrix<BigInteger> input = utils.getInputMatrix(0, 0);
    Matrix<BigInteger> expected = utils.getInputMatrix(0, 0);
    return new TestLeakyAggregationGeneric<>(input, expected);
  }
}
