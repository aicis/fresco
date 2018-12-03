package dk.alexandra.fresco.overdrive.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class EvaluationRingPolyTest {

  private BigInteger modulus;
  private BigInteger root;
  private CoefficientRingPoly polyA;
  private CoefficientRingPoly polyB;
  private EvaluationRingPoly evalPolyA;
  private EvaluationRingPoly evalPolyB;
  private EvaluationRingPoly equalsEvalPolyB;

  /**
   * Sets up a few polynomials to use for tests.
   *
   * <p>
   * The polynomials are
   * <ul>
   * <li><i>polyA = 2 + 4x + 16x^2 + 15x^3 + 12x^4 + 4x^5 + 5x^6 + 14x^7</i> and
   * <li><i>polyB = 3 + 10x + 16x^2 + 6x^3 + 8x^4 + 4x^5 + 11x^6 + 2x^7</i>
   * </ul>
   * For these we use the root <i>3</i> and modulus <i>17</i>.
   * </p>
   */
  @Before
  public void setUp() {
    // The modulus must be a prime, p, so that m|p - 1, here we use m + 1 = 17
    this.modulus = BigInteger.valueOf(17);
    this.root = BigInteger.valueOf(3);
    // 2 + 4x + 16x^2 + 15x^3 + 12x^4 + 4x^5 + 5x^6 + 14x^7
    List<BigInteger> coeffsA = Arrays.asList(2, 4, 16, 15, 12, 4, 5, 14).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    this.polyA = new CoefficientRingPoly(new ArrayList<>(coeffsA), this.modulus);
    this.evalPolyA = EvaluationRingPoly.fromCoefficients(new ArrayList<>(coeffsA), root, modulus);
    // 3 + 10x + 16x^2 + 6x^3 + 8x^4 + 4x^5 + 11x^6 + 2x^7
    List<BigInteger> coeffsB = Arrays.asList(3, 10, 16, 6, 8, 4, 11, 2).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    this.polyB = new CoefficientRingPoly(coeffsB, this.modulus);
    this.evalPolyB = EvaluationRingPoly.fromCoefficients(new ArrayList<>(coeffsB), root, modulus);
    List<BigInteger> equalsCoeffsB = Arrays.asList(3, 10, 16, 6, 8, 4, 11, 2).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    this.equalsEvalPolyB =
        EvaluationRingPoly.fromCoefficients(new ArrayList<>(equalsCoeffsB), root, modulus);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonTwoPowerLength() {
    BigInteger one = BigInteger.ONE;
    EvaluationRingPoly.fromCoefficients(Arrays.asList(one, one, one), root, modulus);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLengthOne() {
    EvaluationRingPoly.fromCoefficients(Arrays.asList(BigInteger.ONE), root, modulus);
  }

  @Test
  public void testHashCode() {
    int hashA = evalPolyA.hashCode();
    assertEquals(
        31 * (31 * evalPolyA.getEvaluations().hashCode() + modulus.hashCode()) + root.hashCode(),
        hashA);
    int hashB = evalPolyB.hashCode();
    assertEquals(
        31 * (31 * evalPolyB.getEvaluations().hashCode() + modulus.hashCode()) + root.hashCode(),
        hashB);
    assertEquals(hashB, equalsEvalPolyB.hashCode());
  }

  @Test
  public void testFromEvaluations() {
    // Compute the evaluations naively
    List<BigInteger> expectedValues = new ArrayList<>(polyA.getCoefficients().size());
    BigInteger tmp = root;
    for (int i = 0; i < polyA.getCoefficients().size(); i++) {
      expectedValues.add(polyA.eval(tmp));
      tmp = tmp.multiply(root);
      tmp = tmp.multiply(root).mod(modulus);
    }
    EvaluationRingPoly e = EvaluationRingPoly.fromEvaluations(expectedValues, root, modulus);
    assertEquals(expectedValues, e.getEvaluations());
    assertEquals(modulus, e.getModulus());
    assertEquals(polyA.getCoefficients(), e.getCoefficients());
  }

  @Test
  public void testFromCoefficients() {
    // Compute the evaluations naively
    List<BigInteger> expectedValues = new ArrayList<>(polyA.getCoefficients().size());
    BigInteger tmp = root;
    for (int i = 0; i < polyA.getCoefficients().size(); i++) {
      expectedValues.add(polyA.eval(tmp));
      tmp = tmp.multiply(root);
      tmp = tmp.multiply(root).mod(modulus);
    }
    assertEquals(expectedValues, evalPolyA.getEvaluations());
    assertEquals(polyA.getCoefficients(), evalPolyA.getCoefficients());
  }

  @Test
  public void testFromCoefficientsLong() {
    // Note: Doing this naively would take a real long time, so we just check that we can convert
    // back and forth.

    // Make a long polynomial
    List<BigInteger> coeffsLong = new ArrayList<>(1 << 15);
    Random rand = new Random();
    for (int i = 0; i < (1 << 15); i++) {
      coeffsLong.add(new BigInteger(16, rand));
    }
    CoefficientRingPoly polyLong =
        new CoefficientRingPoly(coeffsLong, BigInteger.valueOf((1 << 16) + 1));
    BigInteger modLong = BigInteger.valueOf((1 << 16) + 1);
    // Convert representation
    EvaluationRingPoly longE =
        EvaluationRingPoly.fromCoefficients(polyLong.getCoefficients(), root, modLong);
    assertEquals(polyLong.getCoefficients(), longE.getCoefficients());
  }

  @Test
  public void testGetM() {
    assertEquals(evalPolyA.getM(), polyA.getCoefficients().size() * 2);
    assertEquals(evalPolyB.getM(), polyB.getCoefficients().size() * 2);
  }

  @Test
  public void testGetModulus() {
    assertEquals(modulus, evalPolyA.getModulus());
    assertEquals(modulus, evalPolyB.getModulus());
  }

  @Test
  public void testAdd() {
    EvaluationRingPoly sum = evalPolyA.add(evalPolyB);
    assertEquals(polyA.plus(polyB).getCoefficients(), sum.getCoefficients());
    assertEquals(polyA.plus(polyB).getCoefficients(), evalPolyA.getCoefficients());
  }

  @Test
  public void testPlus() {
    EvaluationRingPoly sum = evalPolyA.plus(evalPolyB);
    assertEquals(polyA.plus(polyB).getCoefficients(), sum.getCoefficients());
    assertNotEquals(polyA.plus(polyB).getCoefficients(), evalPolyA.getCoefficients());
  }

  @Test
  public void testSubtract() {
    EvaluationRingPoly diff = evalPolyA.subtract(evalPolyB);
    assertEquals(polyA.minus(polyB).getCoefficients(), diff.getCoefficients());
    assertEquals(polyA.minus(polyB).getCoefficients(), evalPolyA.getCoefficients());
  }

  @Test
  public void testMinus() {
    EvaluationRingPoly diff = evalPolyA.minus(evalPolyB);
    assertEquals(polyA.minus(polyB).getCoefficients(), diff.getCoefficients());
    assertNotEquals(polyA.minus(polyB).getCoefficients(), evalPolyA.getCoefficients());
  }

  @Test
  public void testMultiply() {
    EvaluationRingPoly prod = evalPolyA.multiply(evalPolyB);
    assertEquals(polyA.times(polyB).getCoefficients(), prod.getCoefficients());
    assertEquals(polyA.times(polyB).getCoefficients(), evalPolyA.getCoefficients());
  }

  @Test
  public void testTimes() {
    EvaluationRingPoly prod = evalPolyA.times(evalPolyB);
    assertEquals(polyA.times(polyB).getCoefficients(), prod.getCoefficients());
    assertNotEquals(polyA.times(polyB).getCoefficients(), evalPolyA.getCoefficients());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckCompatibilityWrongMod() {
    EvaluationRingPoly wrongMod =
        EvaluationRingPoly.fromEvaluations(evalPolyA.getCoefficients(), root, BigInteger.TEN);
    evalPolyA.checkCompatibility(wrongMod);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckCompatibilityWrongRoot() {
    EvaluationRingPoly wrongRoot =
        EvaluationRingPoly.fromEvaluations(evalPolyA.getCoefficients(), BigInteger.TEN, modulus);
    evalPolyA.checkCompatibility(wrongRoot);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckCompatibilityWrongLength() {
    EvaluationRingPoly wrongLength = EvaluationRingPoly
        .fromEvaluations(Arrays.asList(BigInteger.TEN, BigInteger.ONE), BigInteger.TEN, modulus);
    evalPolyA.checkCompatibility(wrongLength);
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void testEqualsObject() {
    assertTrue(evalPolyA.equals(evalPolyA));
    assertTrue(evalPolyB.equals(evalPolyB));
    assertTrue(evalPolyB.equals(equalsEvalPolyB));
    assertTrue(equalsEvalPolyB.equals(evalPolyB));
    assertFalse(equalsEvalPolyB == evalPolyB);
    assertFalse(equalsEvalPolyB.equals(evalPolyA));
    assertFalse(evalPolyB.equals(evalPolyA));
    assertFalse(evalPolyA.equals(null));
    assertFalse(evalPolyA.equals("Test"));
    assertFalse("Test".equals(evalPolyA));
    EvaluationRingPoly wrongLength = EvaluationRingPoly
        .fromEvaluations(Arrays.asList(BigInteger.TEN, BigInteger.ONE), BigInteger.TEN, modulus);
    assertFalse(evalPolyA.equals(wrongLength));
    EvaluationRingPoly wrongRoot =
        EvaluationRingPoly.fromEvaluations(evalPolyA.getCoefficients(), BigInteger.TEN, modulus);
    assertFalse(evalPolyA.equals(wrongRoot));
    EvaluationRingPoly wrongMod =
        EvaluationRingPoly.fromEvaluations(evalPolyA.getCoefficients(), root, BigInteger.TEN);
    assertFalse(evalPolyA.equals(wrongMod));
  }

  @Test
  public void testToString() {
    System.out.println(evalPolyA.toString());
    assertEquals("[16, 16, 7, 0, 7, 8, 7, 6]:3:17", evalPolyA.toString());
  }

}
