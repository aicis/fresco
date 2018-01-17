package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import java.util.Collections;

import org.junit.Test;

public class TestMascotResourcePoolImpl {

  @Test(expected = UnsupportedOperationException.class)
  public void testGetSerializer() {
    MascotResourcePool resourcePool = new MascotResourcePoolImpl(0, Collections
        .singletonList(1), 1, new AesCtrDrbg(new byte[32]), null, null, 0, 0, 0,
        0);
    resourcePool.getSerializer();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateRotForSelf() {
    MascotResourcePool resourcePool = new MascotResourcePoolImpl(1, Collections
        .singletonList(1), 1, new AesCtrDrbg(new byte[32]), null, null, 0, 0, 0,
        0);
    resourcePool.createRot(1, null);
  }
}
