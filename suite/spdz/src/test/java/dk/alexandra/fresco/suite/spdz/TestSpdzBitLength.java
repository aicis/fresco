package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.common.math.integer.binary.BinaryOperationsTests.TestBitLength;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzBitLength extends AbstractSpdzTest {

  @Test
  public void testBitLengthTwoParties() {
    runTest(new TestBitLength<>(),
        PreprocessingStrategy.DUMMY, 2);
  }
}
