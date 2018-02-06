package dk.alexandra.fresco.dummy;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.compare.CompareTests;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuite extends AbstractSpdzTest {

  @Test
  public void test_compareEQ_Sequential() throws Exception {
    runTest(new CompareTests.TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL, 2, false);
  }

  @Test
  public void test_compareLT_Sequential_performance() throws Exception {
    runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL, 2, true);
  }
}
