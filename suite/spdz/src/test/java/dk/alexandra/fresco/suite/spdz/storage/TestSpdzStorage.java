package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import java.io.File;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class TestSpdzStorage {

  /**
   * Removes all files in this directory with given file names.
   */
  private void removeFiles(List<String> fileNames) {
    for (String fileName : fileNames) {
      new File(fileName).deleteOnExit();
    }
  }

  @Test
  public void testDataSupplierModFoundTwice() {
    FilebasedStreamedStorageImpl storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
    String fileName = "valid" + SpdzStorageDataSupplier.MODULUS_KEY;
    storage.putNext(fileName, BigInteger.ONE);
    SpdzStorageDataSupplier supplier = new SpdzStorageDataSupplier(storage, "valid", 2);
    supplier.getFieldDefinition();
    supplier.getFieldDefinition();
    removeFiles(Collections.singletonList(fileName));
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
