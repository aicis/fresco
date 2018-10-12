package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.arithmetic.SearchingTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestLookUpProtocol extends AbstractSpdzTest {

  @Test
  public void test_lookup_is_sorted() {
    runTest(new SearchingTests.TestIsSorted<>(),
        PreprocessingStrategy.DUMMY, 2);
  }


}
