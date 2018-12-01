package dk.alexandra.fresco.overdrive.math;

import java.math.BigInteger;
import java.util.List;

/**
 * Represents a polynomial in the polynomial ring <i>R<sub>q</sub> =
 * Z<sub>q</sub>[X]/&Phi;<sub>m</sub>(X)</i> where <i>m</i> is some two-power and
 * <i>&Phi;<sub>m</sub>(X)</i> is the m'th cyclotomic polynomial and <i>q</i> is some prime modulus
 * <i>q &in; Z<sub>q</sub></i> so that <i>Z<sub>q</sub></i> contains an <i>m</i>'th primitive root
 * of unity.
 *
 * <p>
 * We note that parameters as specified above implies that
 * </p>
 *
 * <ol>
 *
 * <li>&Phi;<sub>m</sub>(X)</i>=X<sup>m/2</sup> + 1, as <i>m</i> is a power of two.
 * <li>From 1. we have that the polynomials will have degree at most <i>m/2 - 1</i>, i.e., they have
 * <i>m</i> coefficients
 * <li>For <i>Z<sub>q</sub></i> to have an <i>m</i>'th primitive root of unity we must pick <i>q</i>
 * so that <i>m|q-1</i>.
 * <li>Btw. an <i>m</i>'th primitive root of unity just means that the is an element <i>r &in;
 * Z<sub>q</sub></i> so that <i>r<sup>m</sup> = 1</i> and <i>r<sup>k</sup> &ne; m</i> for <i>1 &leq;
 * k &lt; m </i>
 *
 * </ol>
 *
 *
 * @param <T> the type of the implementing class
 */
public interface RingPoly<T extends RingPoly<T>> {

  /**
   * Gets a list of <i>m/2</i> coefficients defining the polynomial. The list should be ordered with
   * the lowest degree coefficient first and the highest degree coefficient last (including
   * coefficient equal to zero).
   *
   * <p>
   * Note that regardless of the degree of the polynomial this method should return a list of length
   * exactly <i>m/2</i> (with <i>m</i> defined by {@link #getM()}) elements. For polynomials of
   * lower degree the upper degree coefficients should be set to zero.
   * </p>
   *
   * @return a list of coefficients
   */
  List<BigInteger> getCoefficients();

  /**
   * Gets the parameter <i>m</i> for the polynomial.
   *
   * <p>
   * Note, that the polynomial can have degree at most <i>m/2 - 1</i>.
   * </p>
   *
   * @return the <i>m</i> parameter.
   */
  int getM();

  /**
   * Gets the integer modulus for coefficients of this polynomial.
   *
   * @return the modulus
   */
  BigInteger getModulus();

  /**
   * Adds a polynomial to this polynomial (i.e., mutates this polynomial)
   *
   * @param other the other polynomial
   * @return this polynomial (to support chaining operations)
   */
  T add(final T other);

  /**
   * Computes the result of adding this polynomial to an other polynomial and gives the result as a
   * new polynomial (i.e, without mutating any of the operands).
   *
   * @param other the other polynomial
   * @return the resulting polynomial
   */
  T plus(final T other);

  /**
   * Subtracts a polynomial from this polynomial (i.e., mutates this polynomial)
   *
   * @param other the other polynomial
   * @return this polynomial (to support chaining operations)
   */
  T subtract(final T other);

  /**
   * Computes the result of subtracting a polynomial from this polynomial and gives the result as a
   * new polynomial (i.e, without mutating any of the operands).
   *
   * @param other the other polynomial
   * @return the resulting polynomial
   */
  T minus(final T other);


  /**
   * Multiplies this polynomial with an other polynomial (i.e., mutates this polynomial)
   *
   * @param other the other polynomial
   * @return this polynomial (to support chaining operations)
   */
  T multiply(final T other);

  /**
   * Computes the result of multiplying this polynomial with an other polynomial and gives the
   * result as a new polynomial (i.e, without mutating any of the operands).
   *
   * @param other the other polynomial
   * @return the resulting polynomial
   */
  T times(final T other);

  /*
   * TODO: Consider how the modulus q should be represented, and if we should be able to ask a ring
   * poly for this public BigInteger getModulus();
   */

}
