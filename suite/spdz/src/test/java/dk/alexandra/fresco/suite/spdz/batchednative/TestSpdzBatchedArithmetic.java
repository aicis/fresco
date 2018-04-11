package dk.alexandra.fresco.suite.spdz.batchednative;

import dk.alexandra.fresco.lib.collections.batch.TestBatchArithmetic;
import org.junit.Test;

public class TestSpdzBatchedArithmetic extends BatchedNativeSpdzTest {

  @Test
  public void testInput() {
    runTest(new TestBatchArithmetic.TestBatchedMultiply<>());
  }

}
