package dk.alexandra.fresco.tools.bitTriples;

import java.util.Map;
import org.junit.After;
import org.junit.Before;

public abstract class NetworkedTest {

  protected Map<Integer, BitTriplesTestContext> contexts;
  protected TestRuntime testRuntime;

  private final BitTripleSecurityParameters defaultParameters =
        new BitTripleSecurityParameters(256, 16,
      256, 3);

  private final BitTripleSecurityParameters testParameters =
        new BitTripleSecurityParameters(16, 16,
      16, 3);

  protected BitTripleSecurityParameters parameters;

  public void initContexts(int noOfParties) {
    initContexts(noOfParties, testParameters);
  }

  public void initContexts(int noOfParties,
      BitTripleSecurityParameters securityParameters) {
    parameters = securityParameters;
    contexts = testRuntime.initializeContexts(
        noOfParties, 1, securityParameters);
  }
  @Before
  public void initializeRuntime() {
    testRuntime = new TestRuntime();
  }

  /**
   * Closes all resources allocated for tests and shuts down test runtime.
   */
  @After
  public void teardownRuntime() {
    if (testRuntime != null && testRuntime.isExecutorInitialized()) {
      testRuntime.shutdown();
      testRuntime = null;
    }
  }
  protected BitTripleSecurityParameters getParameters() {
    return parameters;
  }
}
