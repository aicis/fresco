package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.math.integer.logical.LogicalOperationsTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzLogical extends AbstractSpdzTest {

  @Test
  public void testXorKnown() {
    runTest(new LogicalOperationsTests.TestXorKnown<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testAndKnown() {
    runTest(new LogicalOperationsTests.TestAndKnown<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testAnd() {
    runTest(new LogicalOperationsTests.TestAnd<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testOr() {
    runTest(new LogicalOperationsTests.TestOr<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testOrOfList() {
    runTest(new LogicalOperationsTests.TestOrList<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testOrNeighbors() {
    runTest(new LogicalOperationsTests.TestOrNeighbors<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testNot() {
    runTest(new LogicalOperationsTests.TestNot<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

}
