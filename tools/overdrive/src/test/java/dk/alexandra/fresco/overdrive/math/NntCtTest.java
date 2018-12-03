package dk.alexandra.fresco.overdrive.math;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class NntCtTest {

  private BigInteger modulus;
  private BigInteger root;
  private CoefficientRingPoly polyA;
  private CoefficientRingPoly polyB;


  /**
   * Sets up a few polynomials to use for tests.
   *
   * <p>
   * The polynomials are
   * <ul>
   * <li><i>polyA = 2 + 4x + 16x^2 + 15x^3 + 12x^4 + 4x^5 + 5x^6 + 14x^7</i> and
   * <li><i>polyB = 3 + 10x + 16x^2 + 6x^3 + 8x^4 + 4x^5 + 11x^6 + 2x^7</i>
   * </ul>
   * For these we use the root <i>9</i> and modulus <i>17</i>.
   *
   * Note: we are using a different root here than for the other tests! This is because here, we are
   * not padding the poly's with zeros, as we do when computing the evaluation representation.
   *
   * </p>
   */
  @Before
  public void setUp() {
    this.modulus = BigInteger.valueOf(17);
    this.root = BigInteger.valueOf(9);
    // 2 + 4x + 16x^2 + 15x^3 + 12x^4 + 4x^5 + 5x^6 + 14x^7
    List<BigInteger> coeffsA = Arrays.asList(2, 4, 16, 15, 12, 4, 5, 14).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    this.polyA = new CoefficientRingPoly(new ArrayList<>(coeffsA), this.modulus);
    // 3 + 10x + 16x^2 + 6x^3 + 8x^4 + 4x^5 + 11x^6 + 2x^7
    List<BigInteger> coeffsB = Arrays.asList(3, 10, 16, 6, 8, 4, 11, 2).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    this.polyB = new CoefficientRingPoly(coeffsB, this.modulus);
  }

  @Test
  public void test() {
    // Forwards
    NumberTheoreticTransform naiveNnt = new NaiveNnt(root, modulus);
    NumberTheoreticTransform nntCt = NntCt.getInstance(root, modulus);
    List<BigInteger> expectedA = naiveNnt.nnt(polyA.getCoefficients());
    assertEquals(expectedA, nntCt.nnt(polyA.getCoefficients()));
    List<BigInteger> expectedB = naiveNnt.nnt(polyB.getCoefficients());
    assertEquals(expectedB, nntCt.nnt(polyB.getCoefficients()));

    // Backwards
    assertEquals(polyA.getCoefficients(), nntCt.nntInverse(expectedA));
    assertEquals(polyB.getCoefficients(), nntCt.nntInverse(expectedB));
  }

  @Test
  public void testExpandingPowerTable() {
    NumberTheoreticTransform nntCt = NntCt.getInstance(root, modulus);
    nntCt.nnt(Arrays.asList(BigInteger.ONE, BigInteger.ONE));
    nntCt.nnt(Arrays.asList(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroCoeffsForward() {
    NumberTheoreticTransform nntCt = NntCt.getInstance(root, modulus);
    nntCt.nnt(Collections.emptyList());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroCoeffsBackwards() {
    NumberTheoreticTransform nntCt = NntCt.getInstance(root, modulus);
    nntCt.nntInverse(Collections.emptyList());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonTwoPower() {
    NumberTheoreticTransform nntCt = NntCt.getInstance(root, modulus);
    nntCt.nntInverse(Arrays.asList(BigInteger.ONE));
  }

}
