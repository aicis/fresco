package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.junit.Test;

public class TestExponentiationPipe extends AbstractDummyArithmeticTest {

  @Test
  public void test_exponentiation_pipe_preprocessed() {
    runTest(new ExponentiationPipeTests.TestPreprocessedValues<>(), new TestParameters());
  }

}
