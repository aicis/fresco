package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import java.util.Map;
import org.junit.Test;

public class TestMascotResourcePoolImpl {


  @Test(expected = IllegalArgumentException.class)
  public void testCreateRotForSelf() {
    MascotResourcePool resourcePool = new MascotResourcePoolImpl(1, 1, 1,
        new AesCtrDrbg(new byte[32]), Map.of(), new MascotSecurityParameters(),
        new BigIntegerFieldDefinition(ModulusFinder.findSuitableModulus(128)));
    resourcePool.createRot(1, null);
  }
}
