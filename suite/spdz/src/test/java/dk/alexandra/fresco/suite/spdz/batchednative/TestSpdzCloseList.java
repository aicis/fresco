package dk.alexandra.fresco.suite.spdz.batchednative;

import dk.alexandra.fresco.lib.collections.io.CloseListTests.TestCloseAndOpenList;
import dk.alexandra.fresco.lib.collections.io.CloseListTests.TestCloseEmptyList;
import org.junit.Test;

public class TestSpdzCloseList extends BatchedNativeSpdzTest {

  @Test
  public void testCloseEmptyList() {
    runTest(new TestCloseEmptyList<>());
  }

  @Test
  public void testCloseAndOpenList() {
    runTest(new TestCloseAndOpenList<>());
  }

}
