package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.math.integer.binary.BinaryOperationsTests.TestRightShift;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzRightShift extends AbstractSpdzTest {

  @Test
  public void testRightShiftTwoParties() throws Exception {
    runTest(new TestRightShift<>(),
        PreprocessingStrategy.DUMMY, 2);
  }
}
