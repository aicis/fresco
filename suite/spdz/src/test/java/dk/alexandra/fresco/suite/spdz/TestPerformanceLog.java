package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.compare.CompareTests;
import org.junit.Test;

public class TestPerformanceLog extends AbstractSpdzTest {

  @Test
  public void test_log_network() {
    runTestWithLogging(new CompareTests.TestCompareLT<>());
  }

}
