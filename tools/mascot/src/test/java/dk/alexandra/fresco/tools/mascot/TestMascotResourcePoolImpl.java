package dk.alexandra.fresco.tools.mascot;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import java.math.BigInteger;
import java.util.Collections;

import org.junit.Test;

public class TestMascotResourcePoolImpl {

  @Test(expected = UnsupportedOperationException.class)
  public void testGetSerializer() {
    MascotResourcePool resourcePool = new MascotResourcePoolImpl(0, Collections.singletonList(1),
        new AesCtrDrbg(new byte[32]), null, 0, 0, 0, 0);
    resourcePool.getSerializer();
  }

  @Test
  public void testCreateRot() {
    MascotResourcePool resourcePool = new MascotResourcePoolImpl(1, Collections.singletonList(1),
        new AesCtrDrbg(new byte[32]), new BigInteger("251"), 8, 8, 8, 8);
    RotBatch rot =
        resourcePool.createRot(2, new MascotMockSupplier().getNetwork());
    assertTrue(rot instanceof RotBatch);
  }

}
