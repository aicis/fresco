package dk.alexandra.fresco.suite.spdz2k.resource;

import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128Factory;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kOpenedValueStoreImpl;
import org.junit.Test;

public class TestSpdz2kResourcePoolImpl {

  private final Spdz2kResourcePool<CompUInt128> resourcePool = new Spdz2kResourcePoolImpl<>(1, 2,
      null, new Spdz2kOpenedValueStoreImpl<>(),
      new Spdz2kDummyDataSupplier<>(1, 2, null, new CompUInt128Factory()),
      new CompUInt128Factory());

  @Test(expected = UnsupportedOperationException.class)
  public void testGetSerializer() {
    resourcePool.getSerializer();
  }

  @Test(expected = IllegalStateException.class)
  public void testGetDrbgBeforeInit() {
    resourcePool.getRandomGenerator();
  }

}
