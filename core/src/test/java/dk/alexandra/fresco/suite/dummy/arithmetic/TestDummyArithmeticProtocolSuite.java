package dk.alexandra.fresco.suite.dummy.arithmetic;

import org.junit.Test;

import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.lib.arithmetic.ComparisonTests;
import dk.alexandra.fresco.lib.arithmetic.LogicTests;
import dk.alexandra.fresco.lib.arithmetic.SortingTests;
import dk.alexandra.fresco.lib.math.integer.division.DivisionTests;
import dk.alexandra.fresco.lib.math.integer.sqrt.SqrtTests;
import dk.alexandra.fresco.lib.math.integer.stat.StatisticsTests;

public class TestDummyArithmeticProtocolSuite extends AbstractDummyArithmeticTest {

  @Test
  public void test_Copy_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestCopyProtocol(), EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, 2);
  }

  @Test
  public void test_Input_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestInput(), EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, 2);
  }

  @Test
  public void test_OutputToTarget_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestOutputToSingleParty(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, 2);
  }

  @Test
  public void test_AddPublicValue_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestAddPublicValue(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET, 2);
  }

  @Test
  public void test_Lots_Of_Inputs_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestLotsOfInputs(), EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET,
        2);
  }

  @Test
  public void test_MultAndAdd_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestSimpleMultAndAdd(), EvaluationStrategy.SEQUENTIAL,
        NetworkingStrategy.KRYONET,2);
  }

  @Test
  public void test_Sum_And_Output_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestSumAndMult(), EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET,
        2);
  }

  @Test
  public void test_Lots_Of_Inputs_SequentialBatched() throws Exception {
    runTest(new BasicArithmeticTests.TestLotsOfInputs(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        NetworkingStrategy.KRYONET, 2);
  }

  //Comparisons
/*
  @Test
  public void test_MinInfFrac_Sequential() throws Exception {
    runTest(new BasicArithmeticTests.TestMinInfFrac(), EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET,
        2);
  }

  @Test
  public void test_MinInfFrac_SequentialBatched() throws Exception {
    runTest(new BasicArithmeticTests.TestMinInfFrac(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        NetworkingStrategy.KRYONET, 2);
  }
*/
  @Test
  public void test_compareLT_Sequential() throws Exception {
    runTest(new ComparisonTests.TestCompareLT(),
        EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, 2);
  }

  @Test
  public void test_compareEQ_Sequential() throws Exception {
    runTest(new ComparisonTests.TestCompareEQ(),
        EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, 2);
  }

  @Test
  public void test_isSorted() throws Exception {
    runTest(new SortingTests.TestIsSorted(),
        EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, 2);
  }

  @Test
  public void test_compareAndSwap() throws Exception {
    runTest(new SortingTests.TestCompareAndSwap(),
        EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, 2);
  }
  @Test
  public void test_Sort() throws Exception {
    runTest(new SortingTests.TestSort(),
        EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, 2);
  }
  @Test
  public void test_Big_Sort() throws Exception {
    runTest(new SortingTests.TestBigSort(),
        EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, 2);
  }
  @Test
  public void test_logic() throws Exception {
    runTest(new LogicTests.TestLogic(),
        EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, 2);
  }
  
  //Math tests
  
  @Test
  public void test_euclidian_division() throws Exception {
    runTest(new DivisionTests.TestEuclidianDivision(), EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET, 2);
  }
  
  @Test
  public void test_ss_division() throws Exception {
    runTest(new DivisionTests.TestSecretSharedDivision(), EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET, 2);
  }
   
  @Test
  public void test_sqrt() throws Exception {
    runTest(new SqrtTests.TestSquareRoot(), EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET, 2);
  }
  
  //Statistics
  
  @Test
  public void test_Exiting_Variable_2_parties() throws Exception {        
      runTest(new StatisticsTests.TestStatistics(), EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET, 2);
  }

  @Test
  public void test_Exiting_Variable_3_parties() throws Exception {
      runTest(new StatisticsTests.TestStatistics(), EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET, 3);
  }
  
  
}
