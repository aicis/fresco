package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.common.collections.sort.NumericSortingTests.TestOddEvenMergeSort;
import dk.alexandra.fresco.lib.common.collections.sort.NumericSortingTests.TestOddEvenMergeSortDifferentValueLength;
import dk.alexandra.fresco.lib.common.math.integer.TestProductAndSum.TestSum;
import dk.alexandra.fresco.lib.common.math.integer.binary.BinaryOperationsTests.TestGenerateRandomBitMask;
import dk.alexandra.fresco.lib.common.math.integer.binary.BinaryOperationsTests.TestRightShift;
import dk.alexandra.fresco.lib.common.math.integer.division.DivisionTests;
import dk.alexandra.fresco.lib.common.math.integer.linalg.LinAlgTests.TestInnerProductClosed;
import dk.alexandra.fresco.lib.common.math.integer.linalg.LinAlgTests.TestInnerProductOpen;
import dk.alexandra.fresco.lib.common.math.integer.mod.Mod2mTests.TestMod2m;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.MathTests;
import dk.alexandra.fresco.lib.fixed.MathTests.TestLog;
import dk.alexandra.fresco.lib.fixed.MathTests.TestRandom;
import dk.alexandra.fresco.lib.fixed.MathTests.TestSqrt;
import dk.alexandra.fresco.lib.fixed.NormalizeTests.TestNormalizePowerSFixed;
import dk.alexandra.fresco.lib.fixed.NormalizeTests.TestNormalizeSFixed;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestCorrelatedNoise;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestFixedPointDivision;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestFixedPointInput;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestFixedPointMultiplication;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestFixedPointSecretDivision;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestInput;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestLiftPQ;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestLiftQP;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestProjectionLeft;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestProjectionRight;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestRandomModP;
import dk.alexandra.fresco.suite.crt.BasicCRTTests.TestTruncp;
import dk.alexandra.fresco.suite.crt.fixed.CRTAdvancedFixedNumeric;
import dk.alexandra.fresco.suite.crt.fixed.CRTFixedNumeric;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests.TestLotsMult;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests.TestOutputToSingleParty;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests.TestRandomBit;
import dk.alexandra.fresco.suite.dummy.arithmetic.BasicArithmeticTests.TestSumAndMult;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Before;
import org.junit.Test;

public class TestCRT {

  @Before
  public void setup() {
    FixedNumeric.load(CRTFixedNumeric::new);
    AdvancedFixedNumeric.load(CRTAdvancedFixedNumeric::new);
  }

  @Test
  public void testInput() {
    new AbstractSpdzCRTTest().runTest(new TestInput<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testKnown() {
    new AbstractSpdzCRTTest().runTest(new BasicArithmeticTests.TestKnownSInt<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
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
  public void testDivision() {
    new AbstractSpdzCRTTest().runTest(new DivisionTests.TestDivision<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testDivisionKnownDivisor() {
    new AbstractSpdzCRTTest().runTest(new DivisionTests.TestKnownDivisorDivision<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testRightShift() {
    new AbstractSpdzCRTTest().runTest(new TestRightShift<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testMod() {
    new AbstractSpdzCRTTest().runTest(new TestMod2m<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testMults() {
    new AbstractSpdzCRTTest().runTest(new TestLotsMult<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void testCorrelatedNoise() {
    new AbstractSemiHonestDummyCRTTest()
        .runTest(new TestCorrelatedNoise<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }


  @Test
  public void testGenerateRandomBitMask() {
    new AbstractSemiHonestDummyCRTTest().runTest(new TestGenerateRandomBitMask<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testRandomBit() {
    new AbstractSemiHonestDummyCRTTest().runTest(new TestRandomBit<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testLiftPQ() {
    new AbstractSemiHonestDummyCRTTest().runTest(new TestLiftPQ<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testLiftQP() {
    new AbstractSemiHonestDummyCRTTest().runTest(new TestLiftQP<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testProjectionLeft() {
    new AbstractSemiHonestDummyCRTTest()
        .runTest(new TestProjectionLeft<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testProjectionRight() {
    new AbstractSemiHonestDummyCRTTest()
        .runTest(new TestProjectionRight<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testCovertCorrelatedNoise() {
    new AbstractCovertDummyCRTTest()
            .runTest(new TestCorrelatedNoise<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testCovertGenerateRandomBitMask() {
    new AbstractCovertDummyCRTTest().runTest(new TestGenerateRandomBitMask<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testCovertRandomBit() {
    new AbstractCovertDummyCRTTest().runTest(new TestRandomBit<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testCovertLiftPQ() {
    new AbstractCovertDummyCRTTest().runTest(new TestLiftPQ<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testCovertLiftQP() {
    new AbstractCovertDummyCRTTest().runTest(new TestLiftQP<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testCovertProjectionLeft() {
    new AbstractCovertDummyCRTTest()
            .runTest(new TestProjectionLeft<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void testCovertProjectionRight() {
    new AbstractCovertDummyCRTTest()
            .runTest(new TestProjectionRight<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }


  @Test
  public void testTruncp() {
    new AbstractSpdzCRTTest()
        .runTest(new TestTruncp<>(), EvaluationStrategy.SEQUENTIAL, PreprocessingStrategy.MASCOT, 2);
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
