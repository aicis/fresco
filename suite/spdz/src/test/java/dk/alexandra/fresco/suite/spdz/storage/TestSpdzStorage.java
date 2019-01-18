package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import java.math.BigInteger;
import org.junit.Test;

public class TestSpdzStorage {

  @Test
  public void testDataSupplierModFoundTwice() {
    FilebasedStreamedStorageImpl storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
    storage.putNext("valid" + SpdzStorageDataSupplier.MODULUS_KEY, BigInteger.ONE);
    SpdzStorageDataSupplier supplier = new SpdzStorageDataSupplier(storage, "valid", 2);
    supplier.getFieldDefinition();
    supplier.getFieldDefinition();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDataSupplierModNotFound() {
    SpdzStorageDataSupplier supplier =
        new SpdzStorageDataSupplier(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
            "invalid", 2);
    supplier.getFieldDefinition();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDataSupplierSskNotFound() {
    SpdzStorageDataSupplier supplier =
        new SpdzStorageDataSupplier(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
            "invalid", 2);
    supplier.getSecretSharedKey();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDataSupplierTripleNotFound() {
    SpdzStorageDataSupplier supplier =
        new SpdzStorageDataSupplier(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
            "invalid", 2);
    supplier.getNextTriple();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDataSupplierExpPipeNotFound() {
    SpdzStorageDataSupplier supplier =
        new SpdzStorageDataSupplier(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
            "invalid", 2);
    supplier.getNextExpPipe();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDataSupplierInputMaskNotFound() {
    SpdzStorageDataSupplier supplier =
        new SpdzStorageDataSupplier(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
            "invalid", 2);
    supplier.getNextInputMask(1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDataSupplierNextBitNotFound() {
    SpdzStorageDataSupplier supplier =
        new SpdzStorageDataSupplier(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
            "invalid", 2);
    supplier.getNextBit();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDataSupplierRandomElmNotFound() {
    SpdzStorageDataSupplier supplier =
        new SpdzStorageDataSupplier(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
            "invalid", 2);
    supplier.getNextRandomFieldElement();
  }
}
