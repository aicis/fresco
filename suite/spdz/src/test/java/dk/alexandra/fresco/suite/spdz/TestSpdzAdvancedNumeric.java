package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.common.math.AdvancedNumericTests.TestModulus;
import dk.alexandra.fresco.lib.common.math.integer.division.DivisionTests.TestDivision;
import dk.alexandra.fresco.lib.common.math.integer.division.DivisionTests.TestKnownDivisorDivision;
import dk.alexandra.fresco.lib.common.math.integer.exp.ExponentiationTests.TestExponentiation;
import dk.alexandra.fresco.lib.common.math.polynomial.PolynomialTests.TestPolynomialEvaluator;
import dk.alexandra.fresco.lib.fixed.MathTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzAdvancedNumeric extends AbstractSpdzTest {

  @Test
  public void test_Division() {
    runTest(new TestDivision<>(),
        PreprocessingStrategy.DUMMY, 2, 256, 150, 16);
  }

  @Test
  public void testRealLog() {
    runTest(new MathTests.TestLog<>(), EvaluationStrategy.SEQUENTIAL_BATCHED,
        PreprocessingStrategy.DUMMY, 2, 512, 200, 16);
  }

  @Test
  public void test_Division_Known_Denominator() {
    runTest(new TestKnownDivisorDivision<>(),
        PreprocessingStrategy.MASCOT, 2);
  }

  @Test
  public void test_Modulus() {
    runTest(new TestModulus<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_exponentiation() {
    runTest(new TestExponentiation<>(),
        PreprocessingStrategy.DUMMY, 2, 512, 150, 16);
  }

  @Test
  public void test_polynomial() {
    runTest(new TestPolynomialEvaluator<>(),
        PreprocessingStrategy.DUMMY, 2);
  }
}
