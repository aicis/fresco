package dk.alexandra.fresco.lib.common.math.field;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.common.dummy.bool.AbstractDummyBooleanTest;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import org.junit.Test;


/**
 * Various tests of the dummy protocol suite.
 *
 * Currently, we simply test that AES works using the dummy protocol suite.
 */
public class TestDummyProtocolSuite extends AbstractDummyBooleanTest {


  // lib.field.bool.generic
  // Slightly more advanced protocols for lowlevel logic operations
  @Test
  public void test_XNor() {
    runTest(new FieldBoolTests.TestXNorFromXorAndNot<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new FieldBoolTests.TestXNorFromOpen<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_OR() {
    runTest(new FieldBoolTests.TestOrFromXorAnd<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new FieldBoolTests.TestOrFromCopyConst<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void testOpen() {
    runTest(new FieldBoolTests.TestOpen<>(), EvaluationStrategy.SEQUENTIAL_BATCHED, true);
    assertThat(performanceLoggers.get(1).get(0).getLoggedValues()
        .get(NetworkLoggingDecorator.NETWORK_TOTAL_BYTES), is((long) 4));
  }

  @Test
  public void test_NAND() {
    runTest(new FieldBoolTests.TestNandFromAndAndNot<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
    runTest(new FieldBoolTests.TestNandFromOpen<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

  @Test
  public void test_AndFromCopy() {
    runTest(new FieldBoolTests.TestAndFromCopyConst<>(), EvaluationStrategy.SEQUENTIAL_BATCHED);
  }

}
