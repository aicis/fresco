package dk.alexandra.fresco.suite.dummy.bool;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests.TestBasicProtocols;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests.TestInputDifferentSender;
import dk.alexandra.fresco.lib.bool.BasicBooleanTests.TestMultipleAnds;
import dk.alexandra.fresco.logging.binary.BinaryLoggingDecorator;
import org.junit.Test;

/**
 * Various tests of the dummy protocol suite.
 *
 * Currently, we simply test that AES works using the dummy protocol suite.
 */
public class TestDummyProtocolSuite extends AbstractDummyBooleanTest {

  // Basic tests for boolean suites
  @Test
  public void test_basic_logic() {
    runTest(new BasicBooleanTests.TestInput<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    runTest(new BasicBooleanTests.TestInputDifferentSender<>(true),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        true, 2);
    runTest(new BasicBooleanTests.TestXOR<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    runTest(new BasicBooleanTests.TestAND<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    runTest(new BasicBooleanTests.TestNOT<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    runTest(new BasicBooleanTests.TestRandomBit<>(true), EvaluationStrategy.SEQUENTIAL_BATCHED,
        true);

    assertThat(performanceLoggers.get(1).get(2).getLoggedValues()
        .get(BinaryLoggingDecorator.BINARY_BASIC_XOR), is((long) 4));
    assertThat(performanceLoggers.get(1).get(3).getLoggedValues()
        .get(BinaryLoggingDecorator.BINARY_BASIC_AND), is((long) 4));
    assertThat(performanceLoggers.get(1).get(5).getLoggedValues()
        .get(BinaryLoggingDecorator.BINARY_BASIC_RANDOM), is((long) 1));
  }

  @Test
  public void test_basic_protocol() {
    runTest(new TestBasicProtocols<>(true), EvaluationStrategy.SEQUENTIAL);
  }

  @Test
  public void test_multiple_ands() {
    runTest(new TestMultipleAnds<>(true, 16), EvaluationStrategy.SEQUENTIAL, false, 2);
  }

  @Test
  public void test_input_different_sender() {
    runTest(new TestInputDifferentSender<>(true), EvaluationStrategy.SEQUENTIAL, false, 2);
  }

}
