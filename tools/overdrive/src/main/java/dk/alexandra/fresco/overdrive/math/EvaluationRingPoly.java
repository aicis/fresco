package dk.alexandra.fresco.overdrive.math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A representation of a {@link RingPoly} which represents the polynomial <i>P</i> as a list of
 * values <i>P(r<sup>i</sup>)</i> for <i> i &in; Z<sub>m</sub><sup>*</sup></i> and <i>r</i> a
 * primitive root of unity in <i>Z<sub>q</sub></i>. This is useful as it allows efficient
 * multiplication of polynomials (<i>O(m)</i> rather than <i>O(m<sup>2</sup>)</i>). The conversion
 * to and from coefficient representation can be done efficiently using the fast Fourier transform
 * (actually the number theoretic transform) in <i>O(m*log(m))</i>.
 *
 * <p>
 * Note: since <i>m</i> is taken to be a two-power, Z<sub>m</sub><sup>*</sup> is really just the odd
 * numbers between <i>0</i> and <i>m</i>. I.e., the length of the list representing the poly in this
 * representation is equal to that of the coefficient representation.
 * </p>
 *
 *
 */
public final class EvaluationRingPoly implements RingPoly<EvaluationRingPoly> {

  private final List<BigInteger> evaluations;
  private final BigInteger modulus;
  private BigInteger root;

  private EvaluationRingPoly(List<BigInteger> evaluations, BigInteger root, BigInteger modulus) {
    this.evaluations = evaluations;
    this.root = root;
    this.modulus = modulus;
  }

  /**
   * Constructs a polynomial in evaluation representation directly from the evaluations.
   *
   * @param evaluations the list of evaluations, the m parameter will be inferred from the length of
   *        this list
   * @param root the root of unity in which the polynomial was evaluated (this is need for
   *        conversion back to coefficient representation)
   * @param modulus the modulus
   * @return a new polynomial in evaluation representation
   * @throws IllegalArgumentException
   *         if any arguments are null or the list of evaluations is a non-two-power size.
   */
  public static EvaluationRingPoly fromEvaluations(List<BigInteger> evaluations, BigInteger root,
      BigInteger modulus) {
    Objects.requireNonNull(evaluations);
    Objects.requireNonNull(root);
    Objects.requireNonNull(modulus);
    isTwoPower(evaluations.size());
    return new EvaluationRingPoly(evaluations, root, modulus);
  }

  /**
   * Converts a coefficient presentation of a polynomial to evaluation representation.
   *
   * @param coefficients the coefficients of the polynomial
   * @param root the root of unity to use for conversion
   * @param modulus the modulus
   * @return a new polynomial in evaluation representation
   * @throws IllegalArgumentException
   *         if any arguments are null or the list of coefficient is a non-two-power size.

   */
  public static EvaluationRingPoly fromCoefficients(List<BigInteger> coefficients, BigInteger root,
      BigInteger modulus) {
    Objects.requireNonNull(root);
    Objects.requireNonNull(modulus);
    int size = Objects.requireNonNull(coefficients).size();
    isTwoPower(size);
    /*
     * We need to evaluate the polynomial in r, r^3, ... , r^m-1. Using the Fourier Transform
     * naively only gives us the the polynomial evaluated in 1, r, r^2, r^3, ..., r^(m/2 -1).
     *
     * So to hack the transform we add m/2 zeroes to the coefficients to get 1, r, r^2, r^3, ...,
     * r^(m - 1) from the transform. We then use every second entry in this list as the evaluation
     * representation.
     *
     * Note, this is could probably be optimized by modifying the transform instead.
     */
    coefficients.addAll(Collections.nCopies(coefficients.size(), BigInteger.ZERO));
    List<BigInteger> evals = NntCt.getInstance(root, modulus).nnt(coefficients);
    List<BigInteger> res = new ArrayList<>(coefficients.size() / 2);
    for (int i = 1; i < evals.size(); i += 2) {
      res.add(evals.get(i));
    }
    return new EvaluationRingPoly(res, root, modulus);
  }

  private static void isTwoPower(int size) {
    boolean isTwoPower = size > 1 && (size & (size - 1)) == 0;
    if (!isTwoPower) {
      throw new IllegalArgumentException(
          "Number of coefficients must be larger than 1 and a power of two, but was " + size);
    }
  }



  @Override
  public List<BigInteger> getCoefficients() {
    /*
     * See fromCoefficients(...) to see how the trick used there. Here to do the trick in reverse we
     * pad every entry with a zero and use the inverse transform.
     *
     * Again, this can probably be optimized trivially.
     */
    List<BigInteger> padded = new ArrayList<>(evaluations.size() * 2);
    for (BigInteger e : evaluations) {
      padded.add(BigInteger.ZERO);
      padded.add(e);
    }
    padded = NntCt.getInstance(root, modulus).nntInverse(padded);
    ArrayList<BigInteger> result = new ArrayList<>(evaluations.size());
    for (int i = 0; i < evaluations.size(); i++) {
      BigInteger reduced =
          padded.get(i).subtract(padded.get(i + evaluations.size())).mod(modulus);
      result.add(reduced);
    }
    return result;
  }

  /**
   * Gets the evaluations used to represent the polynomial.
   *
   * <p>
   * The evaluations are the values of the polynomial evaluated at <i>r, r<sup>3</sup>, ... r<sup>m
   * - 1</sup></i> where <i>r</i> is the root of unity used for this representation. In other words
   * the polynomial evaluated in all points in <i>Z<sub>m</sub><sup>*</sup></i>
   * </p>
   *
   * @return a list of evaluations
   */
  List<BigInteger> getEvaluations() {
    return new ArrayList<>(evaluations);
  }

  @Override
  public int getM() {
    return evaluations.size() * 2;
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public EvaluationRingPoly add(EvaluationRingPoly other) {
    checkCompatibility(other);
    for (int i = 0; i < this.evaluations.size(); i++) {
      this.evaluations.set(i, this.evaluations.get(i).add(other.evaluations.get(i).mod(modulus)));
    }
    return this;
  }

  @Override
  public EvaluationRingPoly plus(EvaluationRingPoly other) {
    checkCompatibility(other);
    List<BigInteger> resEvals = new ArrayList<>(this.evaluations.size());
    for (int i = 0; i < this.evaluations.size(); i++) {
      resEvals.add(this.evaluations.get(i).add(other.evaluations.get(i).mod(modulus)));
    }
    return new EvaluationRingPoly(resEvals, root, modulus);
  }

  @Override
  public EvaluationRingPoly subtract(EvaluationRingPoly other) {
    checkCompatibility(other);
    for (int i = 0; i < this.evaluations.size(); i++) {
      this.evaluations.set(i,
          this.evaluations.get(i).subtract(other.evaluations.get(i).mod(modulus)));
    }
    return this;
  }

  @Override
  public EvaluationRingPoly minus(EvaluationRingPoly other) {
    checkCompatibility(other);
    List<BigInteger> resEvals = new ArrayList<>(this.evaluations.size());
    for (int i = 0; i < this.evaluations.size(); i++) {
      resEvals.add(this.evaluations.get(i).subtract(other.evaluations.get(i).mod(modulus)));
    }
    return new EvaluationRingPoly(resEvals, root, modulus);
  }

  @Override
  public EvaluationRingPoly multiply(EvaluationRingPoly other) {
    checkCompatibility(other);
    for (int i = 0; i < this.evaluations.size(); i++) {
      this.evaluations.set(i,
          this.evaluations.get(i).multiply(other.evaluations.get(i).mod(modulus)));
    }
    return this;
  }

  @Override
  public EvaluationRingPoly times(EvaluationRingPoly other) {
    checkCompatibility(other);
    List<BigInteger> resEvals = new ArrayList<>(this.evaluations.size());
    for (int i = 0; i < this.evaluations.size(); i++) {
      resEvals.add(this.evaluations.get(i).multiply(other.evaluations.get(i).mod(modulus)));
    }
    return new EvaluationRingPoly(resEvals, root, modulus);
  }

  /**
   * Checks a polynomial to see if it is compatible with this polynomial, i.e., if they use the same
   * modulus and <i>m</i> parameter
   *
   * @param other the other polynomial
   */
  void checkCompatibility(EvaluationRingPoly other) {
    Objects.requireNonNull(other);
    if (other.getM() != this.getM()) {
      throw new IllegalArgumentException("Non-equal m-parameters. m-parameter of first polynomial "
          + this.getM() + " while second polynimial was " + other.getM());
    }
    if (!other.getModulus().equals(this.getModulus())) {
      throw new IllegalArgumentException("Non-equal moduli. Modulus of first polynomial "
          + this.getModulus() + " while the second polynomials was " + other.getModulus());
    }

    if (!other.root.equals(this.root)) {
      throw new IllegalArgumentException("Non-equal roots. Root of first polynomial "
          + this.getModulus() + " while the second polynomials was " + other.getModulus());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof EvaluationRingPoly)) {
      return false;
    }
    EvaluationRingPoly poly = (EvaluationRingPoly) o;
    return poly.modulus.equals(this.modulus) && poly.root.equals(this.root)
        && poly.evaluations.equals(this.evaluations);
  }

  @Override
  public int hashCode() {
    int result = evaluations.hashCode();
    result = 31 * result + modulus.hashCode();
    result = 31 * result + root.hashCode();
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[");
    List<String> evals = new ArrayList<>(evaluations.size());
    for (BigInteger e : evaluations) {
      evals.add(e.toString());
    }
    sb.append(String.join(", ", evals));
    sb.append("]:");
    sb.append(root);
    sb.append(":");
    sb.append(modulus);
    return sb.toString();
  }

}
