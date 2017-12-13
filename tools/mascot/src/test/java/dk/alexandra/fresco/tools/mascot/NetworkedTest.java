package dk.alexandra.fresco.tools.mascot;

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
  protected int lambdaSecurity;
  protected int prgSeedLength;
  protected int numLeftFactors;

  public NetworkedTest(BigInteger modulus, int modBitLength, int lambdaSecurity, int prgSeedLength,
      int numLeftFactors) {
    super();
    this.modulus = modulus;
    this.modBitLength = modBitLength;
    this.lambdaSecurity = lambdaSecurity;
    this.prgSeedLength = prgSeedLength;
    this.numLeftFactors = numLeftFactors;
  }

  public NetworkedTest() {
    this(new BigInteger("65521"), 16, 16, 256, 3);
  }

  public void initContexts(List<Integer> partyIds) {
    contexts = testRuntime.initializeContexts(partyIds, modulus, modBitLength, lambdaSecurity,
        prgSeedLength, numLeftFactors);
  }

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
