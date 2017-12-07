package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

public abstract class NetworkedTest {
  protected TestRuntime testRuntime;
  protected Map<Integer, MascotContext> contexts;
  
  // maskot parameters
  protected BigInteger modulus;
  protected int modBitLength;
  protected int lambdaSecurity;

  public NetworkedTest(BigInteger modulus, int modBitLenght, int lambdaSecurity) {
    super();
    this.modulus = modulus;
    this.modBitLength = modBitLenght;
    this.lambdaSecurity = lambdaSecurity;
  }

  public NetworkedTest() {
    this(new BigInteger("65521"), 16, 16);
  }

  public void initContexts(List<Integer> partyIds) {
    contexts = testRuntime.initializeContexts(partyIds);
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
