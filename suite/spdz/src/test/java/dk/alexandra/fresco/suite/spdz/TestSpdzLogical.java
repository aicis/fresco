package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.math.integer.logical.LogicalOperationsTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzLogical extends AbstractSpdzTest {

  @Test
  public void testXorKnown() {
    runTest(new LogicalOperationsTests.TestXorKnown<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testAndKnown() {
    runTest(new LogicalOperationsTests.TestAndKnown<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testAnd() {
    runTest(new LogicalOperationsTests.TestAnd<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testOr() {
    runTest(new LogicalOperationsTests.TestOr<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testOrOfList() {
    runTest(new LogicalOperationsTests.TestOrList<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testOrNeighbors() {
    runTest(new LogicalOperationsTests.TestOrNeighbors<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testNot() {
    runTest(new LogicalOperationsTests.TestNot<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

}
