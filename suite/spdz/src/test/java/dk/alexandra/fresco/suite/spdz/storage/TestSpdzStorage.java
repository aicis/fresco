package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import org.junit.Test;

public class TestSpdzStorage {

  @Test(expected = MPCException.class)
  public void testDataSupplierModNotFound() {
    DataSupplierImpl supplier =
        new DataSupplierImpl(new FilebasedStreamedStorageImpl(new InMemoryStorage()), "invalid", 2);
    supplier.getModulus();
  }

  @Test(expected = MPCException.class)
  public void testDataSupplierSskNotFound() {
    DataSupplierImpl supplier =
        new DataSupplierImpl(new FilebasedStreamedStorageImpl(new InMemoryStorage()), "invalid", 2);
    supplier.getSecretSharedKey();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDummyDataSupplierTooManyPlayers() {
    new DummyDataSupplierImpl(1, 4);
  }
}
