package dk.alexandra.fresco.tools.mascot;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.RotList;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestMascotResourcePoolImpl {

  @Test(expected = UnsupportedOperationException.class)
  public void testGetSerializer() {
    MascotResourcePool resourcePool = new MascotResourcePoolImpl(0, Collections
        .singletonList(1), 1, new AesCtrDrbg(new byte[32]), null, null, 0, 0, 0,
        0);
    resourcePool.getSerializer();
  }

  @Test
  public void testCreateRot() {
    Map<Integer, RotList> seedOtsMap = new HashMap<>();
    Ot ot = new DummyOt(2, new MascotMockSupplier().getNetwork());
    RotList seedOts = new RotList(new AesCtrDrbg(new byte[32]), 8);
    seedOts.send(ot);
    seedOts.receive(ot);
    seedOtsMap.put(2, seedOts);
    MascotResourcePool resourcePool = new MascotResourcePoolImpl(1, Collections
        .singletonList(1), 1, new AesCtrDrbg(new byte[32]), seedOtsMap, new BigInteger(
            "251"), 8, 8, 8, 8);
    RotBatch rot =
        resourcePool.createRot(2, new MascotMockSupplier().getNetwork());
    assertTrue(rot instanceof RotBatch);
  }

}
