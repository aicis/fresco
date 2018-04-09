package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.AdvancedNumericTests.TestMinInfFrac;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.math.integer.division.DivisionTests.TestKnownDivisorDivision;
import dk.alexandra.fresco.lib.math.integer.division.DivisionTests.TestDivision;
import dk.alexandra.fresco.lib.math.integer.log.LogTests.TestLogarithm;
import dk.alexandra.fresco.lib.math.integer.sqrt.SqrtTests.TestSquareRoot;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Basic arithmetic tests using the SPDZ protocol suite with 2 parties. Have to hardcode the number
 * of parties for now, since the storage is currently build to handle a fixed number of parties.
 */
public class TestSpdzBasicArithmetic2Parties extends AbstractSpdzTest {

  // Fix error before activating
  // TODO PFF Consider deleting or changing test data to avoid the failure?
  @Ignore
  @Test
  public void test_Division_Sequential_Batched() {
    runTest(new TestKnownDivisorDivision<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Secret_Shared_Division_Sequential_Batched() {
    runTest(new TestDivision<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Log_Sequential_Batched() {
    runTest(new TestLogarithm<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Sqrt_Sequential_Batched() {
    runTest(new TestSquareRoot<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Input_Sequential() {
    runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_OutputToTarget_Sequential() {
    runTest(new BasicArithmeticTests.TestOutputToSingleParty<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_AddPublicValue_Sequential() {
    runTest(new BasicArithmeticTests.TestAddPublicValue<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testOpenWithConversion() {
    runTest(new BasicArithmeticTests.TestOpenWithConversion<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_MultAndAdd_Sequential() {
    runTest(new BasicArithmeticTests.TestSimpleMultAndAdd<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Sum_And_Output_Sequential() {
    runTest(new BasicArithmeticTests.TestSumAndMult<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_MinInfFrac_Sequential() {
    runTest(new TestMinInfFrac<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_MinInfFrac_SequentialBatched() {
    runTest(new TestMinInfFrac<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Input_SequentialBatched_Mascot() {
    runTest(new BasicArithmeticTests.TestInput<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.MASCOT, 2, 16, 16, 16);
  }

  @Test
  public void testInputFromAllMascot() {
    runTest(new BasicArithmeticTests.TestInputFromAll<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.MASCOT, 2, 16, 16, 16);
  }


  @Test
  public void test_Lots_Of_Mults_Sequential_Batched_Different_Modulus() {
    runTest(new BasicArithmeticTests.TestLotsMult<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2, false, 256, 128, 16);
  }

  @Test
  public void testOpenWithConversionMascot() {
    runTest(new BasicArithmeticTests.TestOpenWithConversion<>(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.MASCOT, 2, 16, 16, 16);
  }
  
}
