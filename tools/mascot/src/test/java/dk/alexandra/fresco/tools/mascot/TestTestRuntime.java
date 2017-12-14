package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;
import java.util.Arrays;

public class TestTestRuntime {

  // maskot parameters
  protected BigInteger modulus = new BigInteger("251");
  protected int modBitLength = 8;
  protected int lambdaSecurityParam = 8;
  protected int numLeftFactors = 3;
  protected int prgSeedLength = 256;

  public void test() {
    // TODO bug in network connect logic!
    for (int i = 0; i < 100; i++) {
      System.out.println(i);
      TestRuntime testRuntime = new TestRuntime();
      testRuntime.initializeContexts(Arrays.asList(1, 2, 3), modulus, modBitLength,
          lambdaSecurityParam, numLeftFactors, prgSeedLength);
      testRuntime.shutdown();
    }
  }
  
}
