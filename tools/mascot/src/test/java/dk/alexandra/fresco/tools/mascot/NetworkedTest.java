package dk.alexandra.fresco.tools.mascot;

import org.junit.After;
import org.junit.Before;

public abstract class NetworkedTest {
  protected TestRuntime testRuntime;

  @Before
  public void initializeRuntime() {
    testRuntime = new TestRuntime();
  }

  @After
  public void teardownRuntime() {
    if (testRuntime != null && testRuntime.isExecutorInitialized()) {
      testRuntime.shutdown();
      testRuntime = null;
    }
  }
}
