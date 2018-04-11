package dk.alexandra.fresco.suite.spdz.batchednative;

import dk.alexandra.fresco.lib.collections.batch.TestBatchArithmetic.TestBatchMultiply;
import org.junit.Test;

public class TestSpdzBatchedArithmetic extends BatchedNativeSpdzTest {

  @Test
  public void testBatchMultiply() {
    runTest(new TestBatchMultiply<>());
  }

}
