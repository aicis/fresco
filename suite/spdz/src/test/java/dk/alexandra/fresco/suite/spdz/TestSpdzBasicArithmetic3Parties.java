package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

/**
 * Basic arithmetic tests using the SPDZ protocol suite with 3 parties. Have to hardcode the number
 * of parties for now, since the storage is currently build to handle a fixed number of parties.
 * 
 */
public class TestSpdzBasicArithmetic3Parties extends AbstractSpdzTest {

  @Test
  public void test_Input_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 3);
  }

  @Test
  public void test_Input_SequentialBatched() throws Exception {
    runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 3);
  }

  @Test
  public void test_Sum_And_Output_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestSumAndMult<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 3);
  }

  @Test
  public void test_Lots_Of_Mults_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestLotsMult<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 3);
  }


  @Test
  public void test_Lots_Of_Mults_Sequential_Batched() throws Exception {
    runTest(new BasicArithmeticTests.TestLotsMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 3);
  }

  @Test
  public void test_Alternating_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestAlternatingMultAdd<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 3);
  }

  @Test
  public void test_Alternating_Sequential_Batched() throws Exception {
    runTest(new BasicArithmeticTests.TestAlternatingMultAdd<>(),
        EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 3);
  }
}
