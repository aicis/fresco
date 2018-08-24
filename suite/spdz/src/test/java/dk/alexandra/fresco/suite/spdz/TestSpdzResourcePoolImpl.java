package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import org.junit.Test;

public class TestSpdzResourcePoolImpl {

  @Test(expected = IllegalStateException.class)
  public void getRandomGenerator() {
    new SpdzResourcePoolImpl(1, 2, new SpdzOpenedValueStoreImpl(), new SpdzDummyDataSupplier(1, 2),
        null).getRandomGenerator();
  }

}
