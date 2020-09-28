package dk.alexandra.fresco.lib.debug;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests;
import dk.alexandra.fresco.suite.dummy.bool.AbstractDummyBooleanTest;
import org.junit.Test;

/**
 * Various tests of the dummy protocol suite.
 *
 * <p>Currently, we simply test that AES works using the dummy protocol suite.
 */
public class TestBinaryDebug extends AbstractDummyBooleanTest {

  @Test
  public void test_basic_logic_all_in_one() {
    runTest(
        new BasicBooleanTests.TestBasicProtocols<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Debug_Marker() {
    runTest(new BinaryDebugTests.TestBinaryOpenAndPrint<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_Debug_OpenAndPrintSysout() {
    runTest(
        new BinaryDebugTests.TestBinaryDebugToNullStream<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

}
