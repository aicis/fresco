package dk.alexandra.fresco.suite.spdz.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestStorage {

  private static final int THREAD_COUNT = 1;
  private static final int MY_ID = 1;
  private static final String storageName =
      SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + THREAD_COUNT + "_" + MY_ID + "_0_";
  private StreamedStorage storage;

  @Before
  public void setup() {
    storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
  }

  @After
  public void teardown() {
    storage.shutdown();
    try {
      InitializeStorage.cleanup();
    } catch (IOException e) {
      fail();
      e.printStackTrace();
    }
  }

  /**
   * Tests that we successfully can initialize the InMemory version of the storage with spdz
   * preprocessed data. Cannot currently easily be used within an actual MPC test since we now only
   * support a streamed version.
   */
  @Test
  public void testInitInMemoryStorageAndDoubleFetch() {
    InitializeStorage.initStorage(new Storage[] { new InMemoryStorage() }, 2, 10, 10, 100, 10);
  }

  @Test
  public void testMultipleCallsAndRandomElm() throws Throwable {
    SpdzDataSupplier supplier = (new Initializer()).numTriples(1).init();
    BigInteger m1 = supplier.getModulus();
    BigInteger m2 = supplier.getModulus();
    assertEquals(m1, m2);
    BigInteger ssk1 = supplier.getSecretSharedKey();
    BigInteger ssk2 = supplier.getSecretSharedKey();
    assertEquals(ssk1, ssk2);
    supplier.getNextRandomFieldElement();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingModulus() throws Throwable {
    SpdzDataSupplier supplier = new SpdzStorageDataSupplier(storage, storageName, 2);
    supplier.getModulus();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingSecretSharedKey() throws Throwable {
    SpdzDataSupplier supplier = new SpdzStorageDataSupplier(storage, storageName, 2);
    supplier.getSecretSharedKey();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingTriple() throws Throwable {
    SpdzDataSupplier supplier = (new Initializer()).numTriples(1).init();
    try {
      supplier.getNextTriple();
    } catch (Exception e) {
      fail("There should be one triple available");
      e.printStackTrace();
    }
    supplier.getNextTriple();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingInput() throws Throwable {
    SpdzDataSupplier supplier = (new Initializer()).numInputs(1).numPlayers(2).init();
    try {
      supplier.getNextInputMask(2);
    } catch (Exception e) {
      fail("There should be one input mask available");
      e.printStackTrace();
    }
    supplier.getNextInputMask(2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingBit() throws Throwable {
    SpdzDataSupplier supplier = (new Initializer()).numBits(1).init();
    try {
      supplier.getNextBit();
    } catch (Exception e) {
      fail("There should be one bit available");
      e.printStackTrace();
    }
    supplier.getNextBit();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingExpPipe() throws Throwable {
    int noOfThreads = 1;
    SpdzStorageDataSupplier supplier = (new Initializer()).numExps(1).init();
    try {
      supplier.getNextExpPipe();
    } catch (Exception e) {
      fail("There should be one exp pipe available");
    }
    supplier.getNextExpPipe();
  }

  private class Initializer {

    int numPlayers = 1;
    int numThreads = 1;
    int numTriples = 0;
    int numInputs = 0;
    int numBits = 0;
    int numExps = 0;

    public Initializer numPlayers(int numPlayers) {
      this.numPlayers = numPlayers;
      return this;
    }

    public Initializer numThreads(int numThreads) {
      this.numThreads = numThreads;
      return this;
    }

    public Initializer numTriples(int numTriples) {
      this.numTriples = numTriples;
      return this;
    }

    public Initializer numInputs(int numInputs) {
      this.numInputs = numInputs;
      return this;
    }

    public Initializer numBits(int numBits) {
      this.numBits = numBits;
      return this;
    }

    public Initializer numExps(int numExps) {
      this.numExps = numExps;
      return this;
    }

    public SpdzStorageDataSupplier init() {
      InitializeStorage.initStreamedStorage(storage, numPlayers, numThreads, numTriples, numInputs, numBits, numExps);
      return new SpdzStorageDataSupplier(storage, storageName, 2);
    }
  }
}
