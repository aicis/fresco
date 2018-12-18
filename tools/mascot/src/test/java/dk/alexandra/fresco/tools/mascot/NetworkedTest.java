package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import java.math.BigInteger;
import java.util.Map;
import org.junit.After;
import org.junit.Before;

public abstract class NetworkedTest {

  protected Map<Integer, MascotTestContext> contexts;
  protected TestRuntime testRuntime;

  private final MascotSecurityParameters defaultParameters = new MascotSecurityParameters(16,
      256, 3);
  private FieldDefinition fieldDefinition = new BigIntegerFieldDefinition(
      ModulusFinder.findSuitableModulus(16));

  public void initContexts(int noOfParties) {
    initContexts(noOfParties, 16, defaultParameters);
  }

  public void initContexts(int noOfParties, MascotSecurityParameters securityParameters) {
    initContexts(noOfParties, 16, securityParameters);
  }

  public void initContexts(int noOfParties, int bitLength,
      MascotSecurityParameters securityParameters) {
    contexts = testRuntime.initializeContexts(
        noOfParties, 1, securityParameters,
        new BigIntegerFieldDefinition(ModulusFinder.findSuitableModulus(bitLength)));
  }

  public void initContexts(int noOfParties, FieldDefinition fieldDefinition,
      MascotSecurityParameters securityParameters) {
    this.fieldDefinition = fieldDefinition;
    contexts = testRuntime.initializeContexts(noOfParties, 1, securityParameters,
        fieldDefinition);
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
    return getFieldDefinition().getModulus();
  }

  protected FieldDefinition getFieldDefinition() {
    return fieldDefinition;
  }

  protected MascotSecurityParameters getDefaultParameters() {
    return defaultParameters;
  }
}
