package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.arithmetic.ParallelAndSequenceTests.TestSumAndProduct;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

/**
 * Tests the SCE's methods to evaluate application using sequential strategy.
 */
public class TestSequentialEval extends AbstractSpdzTest {

  @Test
  public void testSumAndProduct() {
    runTestSequential(new TestSumAndProduct<>(),
        PreprocessingStrategy.DUMMY, 2, false, 128, 32, 3);
  }
}
