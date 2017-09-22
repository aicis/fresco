package dk.alexandra.fresco.dummy;

import dk.alexandra.fresco.framework.PerformanceLogger.Flag;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.ComparisonTests;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuite extends AbstractSpdzTest {

  @Test
  public void test_compareEQ_Sequential() throws Exception {
    runTest(new ComparisonTests.TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL, 2, null);
  }

  @Test
  public void test_compareLT_Sequential_performance() throws Exception {
    runTest(new ComparisonTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL, 2, Flag.ALL_OPTS);
  }
}
