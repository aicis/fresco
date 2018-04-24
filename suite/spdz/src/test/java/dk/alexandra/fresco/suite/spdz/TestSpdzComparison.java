package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.lib.compare.CompareTests.TestLessThanLogRounds;
import dk.alexandra.fresco.lib.list.EliminateDuplicatesTests.TestFindDuplicatesOne;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.storage.InitializeStorage;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TestSpdzComparison extends AbstractSpdzTest {

  @Test
  public void test_compareLT_Sequential() {
    runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_compareLTEdge_Sequential() {
    runTest(new CompareTests.TestCompareLTEdgeCases<>(), EvaluationStrategy.SEQUENTIAL,
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
  public void test_compareEQ_Sequential() {
    runTest(new CompareTests.TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testCompareEQEdgeCasesSequential() {
    runTest(new CompareTests.TestCompareEQEdgeCases<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_find_duplicates() {
    runTest(new TestFindDuplicatesOne<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testCompareLTBatchedMascot() {
    runTest(new CompareTests.TestCompareLT<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.MASCOT, 2, 64, 2, 1);
  }

  @Test
  public void testLessThanLogRounds() {
    int maxBitLength = 64;
    runTest(new TestLessThanLogRounds<>(maxBitLength),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY,
        2, 128, 64, 32);
  }

  @Test
  public void testCompareEQSequentialBatchedMascot() {
    runTest(new CompareTests.TestCompareEQ<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.MASCOT, 2, 64, 2, 1);
  }

  @Test
  public void testCompareEQEdgeCasesBatchedMascot() {
    runTest(new CompareTests.TestCompareEQEdgeCases<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.MASCOT, 2, 64, 2, 1);
  }

}
