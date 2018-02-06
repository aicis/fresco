package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.math.integer.binary.BinaryOperationsTests.TestRightShift;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzRightShift extends AbstractSpdzTest {

  @Test
  public void testRightShiftTwoParties() throws Exception {
    runTest(new TestRightShift<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }
}
