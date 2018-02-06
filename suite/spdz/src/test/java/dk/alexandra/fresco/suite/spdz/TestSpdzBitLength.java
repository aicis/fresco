package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.math.integer.binary.BinaryOperationsTests.TestBitLength;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzBitLength extends AbstractSpdzTest {

  @Test
  public void testBitLengthTwoParties() throws Exception {
    runTest(new TestBitLength<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }
}
