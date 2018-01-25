package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.suite.spdz.AbstractSpdzTest;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;

public class TestStorage extends AbstractSpdzTest {

  /**
   * Tests that we successfully can initialize the InMemory version of the storage with spdz
   * preprocessed data. Cannot currently easily be used within an actual MPC test since we now only
   * support a streamed version.
   */
  @Test
  public void testInitInMemoryStorageAndDoubleFetch() {
    InitializeStorage.initStorage(new Storage[]{new InMemoryStorage()}, 2, 10, 10, 100, 10);
  }

  @Test
  public void testMultipleCallsAndRandomElm() throws Throwable {
    int noOfThreads = 1;
    FilebasedStreamedStorageImpl storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
    try {
      InitializeStorage.initStreamedStorage(storage, 2, noOfThreads, 1, 1, 1, 1);
      int noOfThreadsUsed = 1;
      int myId = 1;
      String storageName =
          SpdzStorageDataSupplier.STORAGE_NAME_PREFIX + noOfThreadsUsed + "_" + myId + "_" + 0
              + "_";
      SpdzDataSupplier supplier = new SpdzStorageDataSupplier(storage, storageName, 2);
      BigInteger m1 = supplier.getModulus();
      BigInteger m2 = supplier.getModulus();
      Assert.assertEquals(m1, m2);

      BigInteger ssk1 = supplier.getSecretSharedKey();
      BigInteger ssk2 = supplier.getSecretSharedKey();
      Assert.assertEquals(ssk1, ssk2);

      supplier.getNextRandomFieldElement();

    } finally {
      storage.shutdown();
      InitializeStorage.cleanup();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingTriple() throws Throwable {
    int noOfThreads = 1;
    FilebasedStreamedStorageImpl storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
    try {
      InitializeStorage.initStreamedStorage(storage,
          2, noOfThreads, 1, 100, 10000, 10);
      runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
          PreprocessingStrategy.STATIC, 2);
    } catch (Exception e) {
      throw e.getCause().getCause();
    } finally {
      storage.shutdown();
      InitializeStorage.cleanup();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingInput() throws Throwable {
    int noOfThreads = 1;
    FilebasedStreamedStorageImpl storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
    try {
      InitializeStorage.initStreamedStorage(storage,
          2, noOfThreads, 1000, 1, 10000, 10);
      runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
          PreprocessingStrategy.STATIC, 2);
    } catch (Exception e) {
      throw e.getCause().getCause();
    } finally {
      storage.shutdown();
      InitializeStorage.cleanup();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingBit() throws Throwable {
    int noOfThreads = 1;
    FilebasedStreamedStorageImpl storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
    try {
      InitializeStorage.initStreamedStorage(storage,
          2, noOfThreads, 1000, 10, 1, 10);
      runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
          PreprocessingStrategy.STATIC, 2);
    } catch (Exception e) {
      throw e.getCause().getCause();
    } finally {
      storage.shutdown();
      InitializeStorage.cleanup();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingExpPipe() throws Throwable {
    int noOfThreads = 1;
    FilebasedStreamedStorageImpl storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
    try {
      InitializeStorage.initStreamedStorage(storage,
          2, noOfThreads, 1000, 10, 10000, 1);
      runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
          PreprocessingStrategy.STATIC, 2);
    } catch (Exception e) {
      throw e.getCause().getCause();
    } finally {
      storage.shutdown();
      InitializeStorage.cleanup();
    }
  }
}
