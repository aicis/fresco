package dk.alexandra.fresco.lib.crypto.mimc;

import java.math.BigInteger;
import org.junit.Test;

public class MiMCConstantsTest {

  @Test
  public void constructor() throws Exception {
    new MiMCConstants();
    MiMCConstants.getConstant(0, BigInteger.ONE);
  }
}