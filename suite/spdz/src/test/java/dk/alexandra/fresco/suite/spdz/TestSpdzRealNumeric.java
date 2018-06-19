package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.real.BasicFixedPointTests.TestMultIsolated;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzRealNumeric extends AbstractSpdzTest {

  @Test
  public void test_Real_Mults_Isolated() {
    runTest(new TestMultIsolated<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

}
