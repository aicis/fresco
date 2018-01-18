package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.util.ModulusFinder;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;

public abstract class NetworkedTest {

  protected TestRuntime testRuntime;
  protected Map<Integer, MascotTestContext> contexts;

  // maskot parameters
  protected BigInteger modulus;
  protected int modBitLength;
  protected int lambdaSecurityParam;
  protected int numLeftFactors;
  private int prgSeedLength;

  private NetworkedTest(BigInteger modulus, int modBitLength, int lambdaSecurity,
      int numLeftFactors,
      int prgSeedLength) {
    super();
    this.modulus = modulus;
    this.modBitLength = modBitLength;
    this.lambdaSecurityParam = lambdaSecurity;
    this.numLeftFactors = numLeftFactors;
    this.prgSeedLength = prgSeedLength;
  }

  public NetworkedTest() {
    this(ModulusFinder.findSuitableModulus(16), 16, 16, 3, 256);
  }

  public void initContexts(List<Integer> partyIds) {
    contexts = testRuntime.initializeContexts(partyIds, 1, modulus,
        modBitLength, lambdaSecurityParam, numLeftFactors, prgSeedLength);
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

}
