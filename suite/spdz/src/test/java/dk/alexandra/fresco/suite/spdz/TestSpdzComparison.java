package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.lib.arithmetic.SortingTests;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.lib.list.EliminateDuplicatesTests.TestFindDuplicatesOne;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.storage.InitializeStorage;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TestSpdzComparison extends AbstractSpdzTest {

  @Test
  public void test_compareLT_Sequential() throws Exception {
    runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }
  
  @Test
  @Ignore("This is not tested on windows and does not work here")
  public void test_compareLT_Sequential_static() throws Exception {
    int noOfThreads = 1;
    try {
      InitializeStorage.initStreamedStorage(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
          2, noOfThreads, 1000, 100, 10000, 100);
      runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
          PreprocessingStrategy.STATIC, 2);
    } catch (Exception e) {
      //Should not fail
      Assert.fail();
    } finally {
      InitializeStorage.cleanup();
    }
  }

  @Test
  public void test_compareEQ_Sequential() throws Exception {
    runTest(new CompareTests.TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_isSorted() throws Exception {
    runTest(new SortingTests.TestIsSorted<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_compareAndSwap() throws Exception {
    runTest(new SortingTests.TestCompareAndSwap<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Sort() throws Exception {
    runTest(new SortingTests.TestSort<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Big_Sort() throws Exception {
    runTest(new SortingTests.TestBigSort<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_find_duplicates() throws Exception {
    runTest(new TestFindDuplicatesOne<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }
}
