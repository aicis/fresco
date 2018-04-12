package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.lib.collections.batch.TestBatchArithmetic.TestBatchMultiply;
import dk.alexandra.fresco.lib.collections.io.CloseListTests.TestCloseAndOpenList;
import dk.alexandra.fresco.lib.collections.io.CloseListTests.TestCloseEmptyList;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import org.junit.Test;

public abstract class Spdz2kCollectionsTestSuite<Spdz2kResourcePoolT extends Spdz2kResourcePool<?>>
    extends AbstractSpdz2kTest<Spdz2kResourcePoolT> {

  @Test
  public void testBatchMultiply() {
    runTest(new TestBatchMultiply<>());
  }

  @Test
  public void testCloseEmptyList() {
    runTest(new TestCloseEmptyList<>());
  }

  @Test
  public void testCloseAndOpenList() {
    runTest(new TestCloseAndOpenList<>());
  }


}
