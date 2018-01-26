package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.util.ModulusFinder;
import java.math.BigInteger;
import java.util.Map;
import org.junit.After;
import org.junit.Before;

public abstract class NetworkedTest {

  protected Map<Integer, MascotTestContext> contexts;
  protected TestRuntime testRuntime;

  private final MascotSecurityParameters defaultParameters = new MascotSecurityParameters(16, 16,
      256, 3);
  private BigInteger modulus = ModulusFinder.findSuitableModulus(16);

  public void initContexts(int noOfParties) {
    initContexts(noOfParties, defaultParameters);
  }

  public void initContexts(int noOfParties, MascotSecurityParameters securityParameters) {
    modulus = ModulusFinder.findSuitableModulus(securityParameters.getModBitLength());
    contexts = testRuntime.initializeContexts(noOfParties, 1,
        securityParameters);
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

  protected BigInteger getModulus() {
    return modulus;
  }

  protected MascotSecurityParameters getDefaultParameters() {
    return defaultParameters;
  }

}
