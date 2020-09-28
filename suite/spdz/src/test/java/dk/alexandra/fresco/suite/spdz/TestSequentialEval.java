package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.suite.dummy.arithmetic.ParallelAndSequenceTests.TestSumAndProduct;
import org.junit.Test;

/**
 * Tests the SCE's methods to evaluate application using sequential strategy.
 */
public class TestSequentialEval extends AbstractSpdzTest {

  @Test
  public void testSequentialEval() {
    runTestSequential(new TestSumAndProduct<>());
  }

}
