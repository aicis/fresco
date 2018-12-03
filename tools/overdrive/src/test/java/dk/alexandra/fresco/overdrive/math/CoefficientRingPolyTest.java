package dk.alexandra.fresco.overdrive.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class CoefficientRingPolyTest {

  private CoefficientRingPoly polyA;
  private CoefficientRingPoly polyB;
  private CoefficientRingPoly equalsPolyB;

  private BigInteger modulus;
  private int parameterM;

  /**
   * Sets up a few polynomials to use for tests.
   *
   * <p>
   * The polynomials are
   * <ul>
   * <li><i>polyA = 2 + 4x + 16x^2 + 15x^3 + 12x^4 + 4x^5 + 5x^6 + 14x^7</i> and
   * <li><i>polyB = 3 + 10x + 16x^2 + 6x^3 + 8x^4 + 4x^5 + 11x^6 + 2x^7</i>
   * </ul>
   * For these we use the modulus <i>17</i>.
   * </p>
   */
  @Before
  public void setUp() {
    // The m parameter must be a power of two, here we use 2^4 = 16
    this.parameterM = 16;
    // The modulus must be a prime, p, so that m|p - 1, here we use m + 1 = 17
    this.modulus = BigInteger.valueOf(17);
    // 2 + 4x + 16x^2 + 15x^3 + 12x^4 + 4x^5 + 5x^6 + 14x^7
    List<BigInteger> coeffsA = Arrays.asList(2, 4, 16, 15, 12, 4, 5, 14).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    this.polyA = new CoefficientRingPoly(coeffsA, this.modulus);
    // 3 + 10x + 16x^2 + 6x^3 + 8x^4 + 4x^5 + 11x^6 + 2x^7
    List<BigInteger> coeffsB = Arrays.asList(3, 10, 16, 6, 8, 4, 11, 2).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    this.polyB = new CoefficientRingPoly(coeffsB, this.modulus);
    List<BigInteger> equalsCoeffsB = Arrays.asList(3, 10, 16, 6, 8, 4, 11, 2).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    this.equalsPolyB = new CoefficientRingPoly(equalsCoeffsB, this.modulus);
  }

  @Test
  public void testHashCode() {
    int hashA = polyA.hashCode();
    assertEquals(31 * polyA.getCoefficients().hashCode() + modulus.hashCode(), hashA);
    int hashB = polyB.hashCode();
    assertEquals(31 * polyB.getCoefficients().hashCode() + modulus.hashCode(), hashB);
    assertEquals(hashB, equalsPolyB.hashCode());
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void testEquals() {
    assertTrue(polyA.equals(polyA));
    assertTrue(polyB.equals(polyB));
    assertTrue(polyB.equals(equalsPolyB));
    assertTrue(equalsPolyB.equals(polyB));
    assertFalse(equalsPolyB == polyB);
    assertFalse(equalsPolyB.equals(polyA));
    assertFalse(polyB.equals(polyA));
    assertFalse(polyA.equals(null));
    assertFalse(polyA.equals("Test"));
    assertFalse("Test".equals(polyA));
    CoefficientRingPoly differentModPoly =
        new CoefficientRingPoly(polyA.getCoefficients(), modulus.add(BigInteger.TEN));
    List<BigInteger> shortCoeffs =
        Arrays.asList(2, 4, 16, 15).stream().map(BigInteger::valueOf).collect(Collectors.toList());
    CoefficientRingPoly shortPoly = new CoefficientRingPoly(shortCoeffs, modulus);
    List<BigInteger> longCoeffs =
        Arrays.asList(2, 4, 16, 15, 12, 4, 5, 14, 2, 4, 16, 15, 12, 4, 5, 14).stream()
            .map(BigInteger::valueOf).collect(Collectors.toList());
    CoefficientRingPoly longPoly = new CoefficientRingPoly(longCoeffs, modulus);
    assertFalse(polyA.equals(differentModPoly));
    assertFalse(polyA.equals(shortPoly));
    assertFalse(polyA.equals(longPoly));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNonTwoPowerCoeffs() {
    List<BigInteger> shortCoeffs =
        Arrays.asList(2, 4, 16).stream().map(BigInteger::valueOf).collect(Collectors.toList());
    new CoefficientRingPoly(shortCoeffs, modulus);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNoCoeffs() {
    new CoefficientRingPoly(new ArrayList<BigInteger>(), modulus);
  }

  @Test
  public void testToString() {
    assertEquals("2 + 4x^1 + 16x^2 + 15x^3 + 12x^4 + 4x^5 + 5x^6 + 14x^7 mod 17", polyA.toString());
    assertEquals("3 + 10x^1 + 16x^2 + 6x^3 + 8x^4 + 4x^5 + 11x^6 + 2x^7 mod 17", polyB.toString());
  }

  @Test
  public void testGetM() {
    assertEquals(this.parameterM, polyA.getM());
  }

  @Test
  public void testGetModulus() {
    assertEquals(modulus, polyA.getModulus());
    assertEquals(modulus, polyB.getModulus());
  }

  @Test
  public void testAdd() {
    List<BigInteger> coeffsSum = Arrays.asList(5, 14, 15, 4, 3, 8, 16, 16).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    CoefficientRingPoly polySum = new CoefficientRingPoly(coeffsSum, modulus);
    CoefficientRingPoly actualSum = polyA.add(polyB);
    assertEquals(polySum, actualSum);
    assertEquals(polySum, polyA);
    assertTrue(polyA == actualSum);
  }

  @Test
  public void testPlus() {
    List<BigInteger> coeffsSum = Arrays.asList(5, 14, 15, 4, 3, 8, 16, 16).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    CoefficientRingPoly polySum = new CoefficientRingPoly(coeffsSum, modulus);
    CoefficientRingPoly actualSum = polyA.plus(polyB);
    assertEquals(polySum, actualSum);
    assertNotEquals(polySum, polyA);
    assertFalse(polyA == actualSum);
  }

  @Test
  public void testSubtract() {
    List<BigInteger> coeffsDifference = Arrays.asList(16, 11, 0, 9, 4, 0, 11, 12).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    CoefficientRingPoly polyDiff = new CoefficientRingPoly(coeffsDifference, modulus);
    CoefficientRingPoly actualDiff = polyA.subtract(polyB);
    assertEquals(polyDiff, actualDiff);
    assertEquals(polyDiff, polyA);
    assertTrue(polyA == actualDiff);
  }

  @Test
  public void testMinus() {
    List<BigInteger> coeffsDifference = Arrays.asList(16, 11, 0, 9, 4, 0, 11, 12).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    CoefficientRingPoly polyDiff = new CoefficientRingPoly(coeffsDifference, modulus);
    CoefficientRingPoly actualDiff = polyA.minus(polyB);
    assertEquals(polyDiff, actualDiff);
    assertNotEquals(polyDiff, polyA);
    assertFalse(polyA == actualDiff);

  }

  @Test
  public void testMultiply() {
    List<BigInteger> coeffsProd = Arrays.asList(0, 11, 5, 13, 6, 4, 16, 1).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    CoefficientRingPoly polyProd = new CoefficientRingPoly(coeffsProd, modulus);
    CoefficientRingPoly actualProd = polyA.multiply(polyB);
    assertEquals(polyProd, actualProd);
    assertEquals(polyA, actualProd);
    assertTrue(polyA == actualProd);
  }

  @Test
  public void testTimes() {
    List<BigInteger> coeffsProd = Arrays.asList(0, 11, 5, 13, 6, 4, 16, 1).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    CoefficientRingPoly polyProd = new CoefficientRingPoly(coeffsProd, modulus);
    CoefficientRingPoly actualProd = polyA.times(polyB);
    assertEquals(polyProd, actualProd);
    assertNotEquals(polyA, actualProd);
    assertFalse(polyA == actualProd);

  }

  @Test
  public void testEval() {
    assertEquals(BigInteger.valueOf(2), polyA.eval(BigInteger.ZERO));
    assertEquals(BigInteger.valueOf(7), polyA.eval(BigInteger.valueOf(12)));
    assertEquals(BigInteger.valueOf(16), polyA.eval(BigInteger.valueOf(10)));
    assertEquals(BigInteger.valueOf(8), polyA.eval(BigInteger.valueOf(7)));
    assertEquals(BigInteger.valueOf(12), polyA.eval(BigInteger.valueOf(9)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testcheckCompatibilityDifferentMod() {
    CoefficientRingPoly differentModPoly =
        new CoefficientRingPoly(polyA.getCoefficients(), modulus.add(BigInteger.TEN));
    polyA.checkCompatibility(differentModPoly);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testcheckCompatibilityLongPoly() {
    List<BigInteger> longCoeffs =
        Arrays.asList(2, 4, 16, 15, 12, 4, 5, 14, 2, 4, 16, 15, 12, 4, 5, 14).stream()
            .map(BigInteger::valueOf).collect(Collectors.toList());
    CoefficientRingPoly longPoly = new CoefficientRingPoly(longCoeffs, modulus);
    polyA.checkCompatibility(longPoly);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testcheckCompatibilityShortPoly() {
    List<BigInteger> shortCoeffs =
        Arrays.asList(2, 4, 16, 15).stream().map(BigInteger::valueOf).collect(Collectors.toList());
    CoefficientRingPoly shortPoly = new CoefficientRingPoly(shortCoeffs, modulus);
    polyA.checkCompatibility(shortPoly);
  }



}
