package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import java.math.BigInteger;
import org.junit.Test;

public class TestMascotResourcePoolImpl {

  @Test(expected = UnsupportedOperationException.class)
  public void testGetSerializer() {
    MascotResourcePool resourcePool = new MascotResourcePoolImpl(0, 1, 1,
        new AesCtrDrbg(new byte[32]), null, new BigInteger("251"), 0, 0, 0,
        0);
    resourcePool.getSerializer();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateRotForSelf() {
    MascotResourcePool resourcePool = new MascotResourcePoolImpl(1, 1, 1,
        new AesCtrDrbg(new byte[32]), null, new BigInteger("251"), 0, 0, 0,
        0);
    resourcePool.createRot(1, null);
  }
}
