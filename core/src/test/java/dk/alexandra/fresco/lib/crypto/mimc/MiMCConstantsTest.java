package dk.alexandra.fresco.lib.crypto.mimc;

import java.math.BigInteger;
import org.junit.Test;

public class MiMCConstantsTest {

  @Test
  public void constructor() throws Exception {
    new MiMCConstants();
    for (int i = 0; i < 100; i++) {
      MiMCConstants.getConstant(100, BigInteger.ONE);
    }
  }
}