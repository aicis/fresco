package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.exceptions.NoMoreElementsException;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.InitializeStorage;
import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestStorage extends AbstractSpdzTest{

  private BigInteger modulus;

  @Before
  public void init() {
    modulus = new BigInteger(
        "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
  }

  @Test
  public void testInMemoryStorage() {
    Storage storage = new InMemoryStorage();
    testStorage(storage);
    testStoreBigInteger(storage);
  }


  @Test
  public void testFilebasedStorage() throws NoMoreElementsException {
    StreamedStorage storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
    testStorage(storage);
    testStoreBigInteger(storage);
    testStreamedStorage(storage);
    File f = new File("testName");
    if (f.exists()) {
      f.delete();
    }
  }

  private void testStreamedStorage(StreamedStorage storage) throws NoMoreElementsException {
    storage.putNext("testName", BigInteger.TEN);
    Serializable o = storage.getNext("testName");
    Assert.assertEquals(BigInteger.TEN, o);
  }

  public void testStorage(Storage storage) {
    SpdzElement a = new SpdzElement(BigInteger.ONE, BigInteger.ZERO, modulus);
    SpdzTriple o1 = new SpdzTriple(a, a, a);

    storage.putObject("test", "key", o1);

    SpdzTriple o2 = storage.getObject("test", "key");
    Assert.assertEquals(o1, o2);
  }

  public void testStoreBigInteger(Storage storage) {
    BigInteger o1 = BigInteger.ONE;

    storage.putObject("test", "key", o1);

    BigInteger o2 = storage.getObject("test", "key");
    Assert.assertEquals(o1, o2);
  }
  
  /**
   * Tests that we successfully can initialize the InMemory version of the storage with spdz
   * preprocessed data. Cannot currently easily be used within an actual MPC test since we now only
   * support a streamed version.
   * 
   * @throws Exception If something fails
   */
  @Test
  public void testInitInMemoryStorage() throws Exception {
    InitializeStorage.initStorage(new Storage[] {new InMemoryStorage()},
        2, 10, 10, 100, 10);          
  }
  

  @Test(expected = MPCException.class)
  public void testMissingTriple() throws Throwable {
    int noOfThreads = 1;
    try {
      InitializeStorage.initStreamedStorage(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
          2, noOfThreads, 1, 100, 10000, 10);
      runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
          PreprocessingStrategy.STATIC, 2);
    } catch (Exception e) {
      throw e.getCause().getCause();
    } finally {
      InitializeStorage.cleanup();
    }
  }
  
  @Test(expected = MPCException.class)
  public void testMissingInput() throws Throwable {
    int noOfThreads = 1;
    try {
      InitializeStorage.initStreamedStorage(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
          2, noOfThreads, 1000, 1, 10000, 10);
      runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
          PreprocessingStrategy.STATIC, 2);
    } catch (Exception e) {
      throw e.getCause().getCause();
    } finally {
      InitializeStorage.cleanup();
    }
  }
  
  @Test(expected = MPCException.class)
  public void testMissingBit() throws Throwable {
    int noOfThreads = 1;
    try {
      InitializeStorage.initStreamedStorage(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
          2, noOfThreads, 1000, 10, 1, 10);
      runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
          PreprocessingStrategy.STATIC, 2);
    } catch (Exception e) {
      throw e.getCause().getCause();
    } finally {
      InitializeStorage.cleanup();
    }
  }
  
  @Test(expected = MPCException.class)
  public void testMissingExpPipe() throws Throwable {
    int noOfThreads = 1;
    try {
      InitializeStorage.initStreamedStorage(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
          2, noOfThreads, 1000, 10, 10000, 1);
      runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
          PreprocessingStrategy.STATIC, 2);
    } catch (Exception e) {
      throw e.getCause().getCause();
    } finally {
      InitializeStorage.cleanup();
    }
  }
}
