package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.arithmetic.AdvancedNumericTests;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationTests.TestExponentiation;
import dk.alexandra.fresco.lib.math.polynomial.PolynomialTests.TestPolynomialEvaluator;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzAdvancedNumeric extends AbstractSpdzTest {

  @Test
  public void test_Division() {
    int[][] examples = new int[][]{
        new int[]{9, 4},
        new int[]{82, 2},
        new int[]{3, 3},
        new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE},
        new int[]{1, Integer.MAX_VALUE},
        new int[]{-9, 4},
        new int[]{9, -4},
        new int[]{-9, -4},
    };
    for (int[] example : examples) {
      test_Division(example[0], example[1]);
    }
  }

  private void test_Division(int numerator, int denominator) {
    runTest(new AdvancedNumericTests.TestDivision<>(numerator, denominator),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Division_Known_Denominator() throws Exception {
    int[][] examples = new int[][]{
        new int[]{9, 4},
        new int[]{82, 2},
        new int[]{3, 3},
        new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE},
        new int[]{1, Integer.MAX_VALUE},
        new int[]{-9, 4},
        new int[]{9, -4},
        new int[]{-9, -4}
    };
    for (int[] example : examples) {
      test_DivisionWithKnownDenominator(example[0], example[1]);
    }
  }

  private void test_DivisionWithKnownDenominator(int numerator, int denominator) {
    runTest(new AdvancedNumericTests.TestDivisionWithKnownDenominator<>(numerator, denominator),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_Modulus() {
    runTest(new AdvancedNumericTests.TestModulus<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_exponentiation() {
    runTest(new TestExponentiation<>(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_polynomial() {
    runTest(new TestPolynomialEvaluator<>(),
        PreprocessingStrategy.DUMMY, 2);
  }
}
