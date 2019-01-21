package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.lib.compare.CompareTests;
import dk.alexandra.fresco.lib.compare.CompareTests.TestLessThanLogRounds;
import dk.alexandra.fresco.lib.compare.lt.CarryOutTests;
import dk.alexandra.fresco.lib.compare.lt.CarryOutTests.TestCarryOut;
import dk.alexandra.fresco.lib.compare.lt.PreCarryTests.TestPreCarryBits;
import dk.alexandra.fresco.lib.list.EliminateDuplicatesTests.TestFindDuplicatesOne;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest.TestParameters;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.storage.InitializeStorage;
import java.util.Random;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TestSpdzComparison extends AbstractSpdzTest {

  @Test
  public void test_compareLT_Sequential() {
    runTest(new CompareTests.TestCompareLT<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_compareLTEdge_Sequential() {
    runTest(new CompareTests.TestCompareLTEdgeCases<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testPreCarry() {
    runTest(new TestPreCarryBits<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testCarryOutZero() {
    runTest(new TestCarryOut<>(0x00000000, 0x00000000), PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testCarryOutOne() {
    runTest(new TestCarryOut<>(0x80000000, 0x80000000), PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testCarryOutAllOnes() {
    runTest(new TestCarryOut<>(0xffffffff, 0xffffffff), PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testCarryOutOneFromCarry() {
    runTest(new TestCarryOut<>(0x40000000, 0xc0000000), PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testCarryOutRandom() {
    runTest(new TestCarryOut<>(new Random(42).nextInt(), new Random(1).nextInt()),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  @Ignore("This is not tested on windows and does not work here")
  public void test_compareLT_Sequential_static() throws Exception {
    int noOfThreads = 1;
    try {
      InitializeStorage.initStreamedStorage(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
          2, noOfThreads, 1000, 100, 10000, 100);
      runTest(new CompareTests.TestCompareLT<>(),
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
    runTest(new CompareTests.TestCompareEQ<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testCompareEQEdgeCasesSequential() {
    runTest(new CompareTests.TestCompareEQEdgeCases<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_find_duplicates() {
    runTest(new TestFindDuplicatesOne<>(),
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
    runTest(new CompareTests.TestCompareEQSimple<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.MASCOT, 2, 64, 2, 1);
  }

}
