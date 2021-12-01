package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.common.collections.shuffle.ShuffleRowsTests;
import dk.alexandra.fresco.lib.common.collections.shuffle.ShuffleRowsTests.TestShuffleRowsGeneric;
import dk.alexandra.fresco.lib.common.collections.sort.NumericSortingTests;
import dk.alexandra.fresco.lib.common.collections.sort.NumericSortingTests.TestOddEvenMergeSort;
import dk.alexandra.fresco.lib.common.collections.sort.NumericSortingTests.TestOddEvenMergeSortDifferentValueLength;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.integer.TestProductAndSum.TestSum;
import dk.alexandra.fresco.lib.common.math.integer.binary.BinaryOperationsTests.TestRightShift;
import dk.alexandra.fresco.lib.common.math.integer.linalg.LinAlgTests.TestInnerProductClosed;
import dk.alexandra.fresco.lib.common.math.integer.linalg.LinAlgTests.TestInnerProductOpen;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.MathTests;
import dk.alexandra.fresco.lib.fixed.MathTests.TestLog;
import dk.alexandra.fresco.lib.fixed.MathTests.TestRandom;
import dk.alexandra.fresco.lib.fixed.MathTests.TestSqrt;
import dk.alexandra.fresco.lib.fixed.NormalizeTests;
import dk.alexandra.fresco.lib.fixed.NormalizeTests.TestNormalizePowerSFixed;
import dk.alexandra.fresco.lib.fixed.NormalizeTests.TestNormalizeSFixed;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestBitDecomposition;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestBitLength;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestCorrelatedNoise;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestDivision;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestFixedPointDivision;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestFixedPointInput;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestFixedPointMultiplication;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestFixedPointSecretDivision;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestInput;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestLEQ;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestLiftPQ;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestLiftQP;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestMaskAndOpen;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestMixedAdd;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestNormalization;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestProjectionLeft;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestProjectionRight;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestRandomModP;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestTruncp;
import dk.alexandra.fresco.suite.crt.comparison.CRTComparison;
import dk.alexandra.fresco.suite.crt.fixed.CRTAdvancedFixedNumeric;
import dk.alexandra.fresco.suite.crt.fixed.CRTFixedNumeric;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests.TestLotsMult;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests.TestOutputToSingleParty;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests.TestSumAndMult;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Before;
import org.junit.Test;

public class TestCRT {

  @Before
  public void setup() {
    FixedNumeric.load(CRTFixedNumeric::new);
    AdvancedFixedNumeric.load(CRTAdvancedFixedNumeric::new);
    Comparison.load(CRTComparison::new);
  }

  @Test
  public void testInput() {
    new AbstractSpdzCRTTest().runTest(new TestInput<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testSum() {
    new AbstractSpdzCRTTest().runTest(new TestSum<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testInnerProduct() {
    new AbstractSpdzCRTTest().runTest(new TestInnerProductOpen<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testInnerProductClosed() {
    new AbstractSpdzCRTTest().runTest(new TestInnerProductClosed<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testOutputToSingleParty() {
    new AbstractSpdzCRTTest().runTest(new TestOutputToSingleParty<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testMultAndAdd() {
    new AbstractSpdzCRTTest().runTest(new TestSumAndMult<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.MASCOT, 2);
  }

  @Test
  public void testRightShift() {
    new AbstractSpdzCRTTest().runTest(new TestRightShift<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testMults() {
    new AbstractSpdzCRTTest().runTest(new TestLotsMult<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testCorrelatedNoise() {
    new AbstractDummyCRTTest()
        .runTest(new TestCorrelatedNoise<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testMixedAdd() {
    new AbstractDummyCRTTest().runTest(new TestMixedAdd<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }


  @Test
  public void testMaskAndOpen() {
    new AbstractDummyCRTTest().runTest(new TestMaskAndOpen<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testLiftPQ() {
    new AbstractDummyCRTTest().runTest(new TestLiftPQ<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testLiftQP() {
    new AbstractDummyCRTTest().runTest(new TestLiftQP<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testProjectionLeft() {
    new AbstractDummyCRTTest()
        .runTest(new TestProjectionLeft<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testProjectionRight() {
    new AbstractDummyCRTTest()
        .runTest(new TestProjectionRight<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testTruncp() {
    new AbstractSpdzCRTTest()
        .runTest(new TestTruncp<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.MASCOT, 2);
  }

  @Test
  public void testDivision() {
    new AbstractSpdzCRTTest()
        .runTest(new TestDivision<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.MASCOT, 2);
  }

  @Test
  public void testLEQ() {
    new AbstractDummyCRTTest()
        .runTest(new TestLEQ<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testBitDecomposition() {
    new AbstractSpdzCRTTest()
        .runTest(new TestBitDecomposition<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testBitLength() {
    new AbstractSpdzCRTTest()
        .runTest(new TestBitLength<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testNormalization() {
    new AbstractSpdzCRTTest()
        .runTest(new TestNormalization<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testFixedPointInput() {
    new AbstractSpdzCRTTest()
        .runTest(new TestFixedPointInput<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testFixedPointMultiplication() {
    new AbstractSpdzCRTTest()
        .runTest(new TestFixedPointMultiplication<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testFixedPointDivision() {
    new AbstractSpdzCRTTest()
        .runTest(new TestFixedPointDivision<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testFixedPointSecretDivision() {
    new AbstractSpdzCRTTest()
        .runTest(new TestFixedPointSecretDivision<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testSort() {
    new AbstractSpdzCRTTest()
        .runTest(new TestOddEvenMergeSort<>(83, 4, 8), EvaluationStrategy.SEQUENTIAL,
            PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testSortDifferentValueLength() {
    new AbstractSpdzCRTTest()
        .runTest(new TestOddEvenMergeSortDifferentValueLength<>(), EvaluationStrategy.SEQUENTIAL,
            PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testExp() {
    new AbstractSpdzCRTTest()
        .runTest(new MathTests.TestExp<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testReciprocal() {
    new AbstractSpdzCRTTest()
        .runTest(new MathTests.TestReciprocal<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testTwoPower() {
    new AbstractSpdzCRTTest()
        .runTest(new MathTests.TestTwoPower<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testNormalize() {
    new AbstractSpdzCRTTest()
        .runTest(new TestNormalizeSFixed<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testNormalizePower() {
    new AbstractSpdzCRTTest()
        .runTest(new TestNormalizePowerSFixed<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testSqrt() {
    new AbstractSpdzCRTTest()
        .runTest(new TestSqrt<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testShift() {
    new AbstractSpdzCRTTest()
        .runTest(new TestRightShift<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testLog() {
    new AbstractSpdzCRTTest()
        .runTest(new TestLog<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testRandomModP() {
    new AbstractSpdzCRTTest()
        .runTest(new TestRandomModP<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testRandom() {
    new AbstractSpdzCRTTest()
        .runTest(new TestRandom<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

}
