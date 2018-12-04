package dk.alexandra.fresco.overdrive.math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A naive representation of a {@link RingPoly} which simply uses the coefficient representation of
 * the polynomial.
 */
public final class CoefficientRingPoly implements RingPoly<CoefficientRingPoly> {

  private final List<BigInteger> coeffs;
  private final BigInteger modulus;

  /**
   * A new polynomial represented as a list of <i>m/2</i> coefficients (lowest to highest degree).
   * For polynomials of degree lower than <i>m/2 - 1</i> the upper coefficients to be set to zero.
   *
   * @param coefficients a list of <i>m/2</i> coefficients
   * @param modulus the modulus
   * @throws IllegalArgumentException
   *         if any arguments are null or the list of coefficients is a non-two-power size.
   */
  CoefficientRingPoly(List<BigInteger> coefficients, BigInteger modulus) {
    this.coeffs = Objects.requireNonNull(coefficients);
    this.modulus = Objects.requireNonNull(modulus);
    int size = Objects.requireNonNull(coefficients).size();
    boolean isTwoPower = size > 1 && (size & (size - 1)) == 0;
    if (!isTwoPower) {
      throw new IllegalArgumentException(
          "Number of coefficients must be larger than 1 and a power of two, but was " + size);
    }
  }

  @Override
  public List<BigInteger> getCoefficients() {
    return new ArrayList<>(this.coeffs);
  }

  @Override
  public int getM() {
    return this.coeffs.size() * 2;
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public CoefficientRingPoly add(CoefficientRingPoly other) {
    checkCompatibility(other);
    for (int i = 0; i < this.coeffs.size(); i++) {
      this.coeffs.set(i, this.coeffs.get(i).add(other.coeffs.get(i)).mod(modulus));
    }
    return this;
  }

  @Override
  public CoefficientRingPoly plus(CoefficientRingPoly other) {
    checkCompatibility(other);
    List<BigInteger> resCoeffs = new ArrayList<>(this.coeffs.size());
    for (int i = 0; i < this.coeffs.size(); i++) {
      resCoeffs.add(this.coeffs.get(i).add(other.coeffs.get(i)).mod(modulus));

    }
    return new CoefficientRingPoly(resCoeffs, modulus);
  }

  @Override
  public CoefficientRingPoly subtract(CoefficientRingPoly other) {
    checkCompatibility(other);
    for (int i = 0; i < this.coeffs.size(); i++) {
      this.coeffs.set(i, this.coeffs.get(i).subtract(other.coeffs.get(i)).mod(modulus));
    }
    return this;
  }

  @Override
  public CoefficientRingPoly minus(CoefficientRingPoly other) {
    List<BigInteger> resCoeffs = new ArrayList<>(this.coeffs.size());
    for (int i = 0; i < this.coeffs.size(); i++) {
      resCoeffs.add(this.coeffs.get(i).subtract(other.coeffs.get(i)).mod(modulus));
    }
    return new CoefficientRingPoly(resCoeffs, modulus);
  }

  @Override
  public CoefficientRingPoly multiply(CoefficientRingPoly other) {
    List<BigInteger> tmp = productCoefficients(other);
    for (int i = 0; i < tmp.size(); i++) {
      this.coeffs.set(i, tmp.get(i).mod(modulus));
    }
    return this;
  }

  @Override
  public CoefficientRingPoly times(CoefficientRingPoly other) {
    List<BigInteger> tmp = productCoefficients(other);
    for (int i = 0; i < tmp.size(); i++) {
      tmp.set(i, tmp.get(i).mod(modulus));
    }
    return new CoefficientRingPoly(tmp, modulus);

  }

  /**
   * Evaluates the polynomial in some point.
   *
   * @param x the evaluation point
   * @return the value of the polynomial in the given point
   */
  public BigInteger eval(BigInteger x) {
    int n = coeffs.size() - 1;
    BigInteger y = coeffs.get(n);
    for (int i = n - 1; i >= 0; i--) {
      y = coeffs.get(i).add(x.multiply(y)).mod(modulus);
    }
    return y;
  }

  /**
   * Computes the coefficients of a polynomial resulting from the multiplication of this polynomial
   * with an other polynomial.
   *
   * <p>
   * Generally, we want to do polynomial multiplication using the evaluation representation for
   * efficiency. So not too much effort has been made to make this fast.
   * </p>
   *
   * @param other the other polynomial
   * @return the coefficients in the product polynomial
   */
  private List<BigInteger> productCoefficients(CoefficientRingPoly other) {
    checkCompatibility(other);
    List<BigInteger> tmp = new ArrayList<>(this.coeffs.size());
    for (int i = 0; i < this.coeffs.size(); i++) {
      tmp.add(BigInteger.ZERO);
    }
    for (int i = 0; i < this.coeffs.size(); i++) {
      BigInteger coeff = this.coeffs.get(i);
      for (int j = 0; j < other.coeffs.size(); j++) {
        int index = (i + j) % this.coeffs.size();
        // The following is due during the computation modulo x^(m/2) + 1
        if (index == i + j) {
          tmp.set(index, tmp.get(index).add(coeff.multiply(other.coeffs.get(j))));
        } else {
          tmp.set(index, tmp.get(index).subtract(coeff.multiply(other.coeffs.get(j))));
        }
      }
    }
    return tmp;
  }

  /**
   * Checks a polynomial to see if it is compatible with this polynomial, i.e., if they use the same
   * modulus and <i>m</i> parameter
   *
   * @param other the other polynomial
   */
  void checkCompatibility(CoefficientRingPoly other) {
    Objects.requireNonNull(other);
    if (other.getM() != this.getM()) {
      throw new IllegalArgumentException("Non-equal m-parameters. m-parameter of this polynomial "
          + this.getM() + " while other was " + other.getM());
    }
    if (!other.getModulus().equals(this.getModulus())) {
      throw new IllegalArgumentException("Non-equal moduli. This modulus was " + modulus
          + " while other was " + other.getModulus());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CoefficientRingPoly)) {
      return false;
    }
    CoefficientRingPoly poly = (CoefficientRingPoly) o;
    if (poly.coeffs.size() != this.coeffs.size()) {
      return false;
    }
    if (!poly.modulus.equals(this.modulus)) {
      return false;
    }
    return this.coeffs.equals(poly.coeffs);
  }

  @Override
  public int hashCode() {
    int result = coeffs.hashCode();
    result = 31 * result + modulus.hashCode();
    return result;
  }

  @Override
  public String toString() {
    List<String> terms = new ArrayList<>(coeffs.size());
    terms.add(coeffs.get(0).toString());
    for (int i = 1; i < coeffs.size(); i++) {
      terms.add(coeffs.get(i).toString() + "x^" + i);
    }
    return String.join(" + ", terms) + " mod " + modulus;
  }
}
