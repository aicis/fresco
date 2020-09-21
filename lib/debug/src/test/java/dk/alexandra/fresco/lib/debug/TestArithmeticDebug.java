package dk.alexandra.fresco.lib.debug;

import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.junit.Test;

public class TestArithmeticDebug extends AbstractDummyArithmeticTest {

  @Test
  public void test_debug_tools() {
    runTest(
        new ArithmeticDebugTests.TestArithmeticOpenAndPrint<>(),
        new TestParameters().numParties(2));
  }
}
