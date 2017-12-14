package dk.alexandra.fresco.tools.mascot;

import java.util.Collections;

import org.junit.Test;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;

public class TestMascotResourcePoolImpl {

  @Test(expected = UnsupportedOperationException.class)
  public void testGetSerializer() {
    MascotResourcePool resourcePool = new MascotResourcePoolImpl(0, Collections.singletonList(1), new AesCtrDrbg(new byte[32]), null, 0, 0, 0, 0);
    resourcePool.getSerializer();
  }
  
}