package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerModulus;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import java.math.BigInteger;
import org.junit.Test;

public class TestSpdzResourcePoolImpl {

  @Test(expected = IllegalStateException.class)
  public void getRandomGenerator() {
    BigInteger modulus = ModulusFinder.findSuitableModulus(512);
    new SpdzResourcePoolImpl(1, 2, new SpdzOpenedValueStoreImpl(), new SpdzDummyDataSupplier(1, 2,
        new BigIntegerFieldDefinition(new BigIntegerModulus(modulus)), modulus), null)
        .getRandomGenerator();
  }
}
