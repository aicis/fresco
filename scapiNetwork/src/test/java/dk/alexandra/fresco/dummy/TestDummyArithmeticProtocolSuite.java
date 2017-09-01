package dk.alexandra.fresco.dummy;

import dk.alexandra.fresco.comparison.ComparisonTests;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuite extends AbstractDummyArithmeticTest {

  @Test
  public void test_compareLT_Sequential() throws Exception {
    runTest(new ComparisonTests.TestCompareLT(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, 2);
  }

  @Test
  public void test_compareEQ_Sequential() throws Exception {
    runTest(new ComparisonTests.TestCompareEQ(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, 2);
  }

}
