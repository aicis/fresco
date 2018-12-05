package dk.alexandra.fresco.overdrive.math;

import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class to sample polynomials from various distributions.
 */
public class PolySampler {

  private Drng drng;
  private Drbg drbg;

  /**
   * Create a new sampler.
   *
   * @param drbg a deterministic random byte generator
   */
  PolySampler(Drbg drbg) {
    this.drng = new DrngImpl(drbg);
    this.drbg = drbg;
  }

  /**
   * Samples a uniformly random ring polynomial for a given modulus, in coefficient representation.
   *
   * @param length the number of coefficients in the polynomial. Note, this defines the degree of
   *        the polynomial modulus in the ring to be <code>length</code>, and the return polynomial
   *        will have degree at most <code>length -1</code>.
   * @param modulus the modulus for the coefficients
   * @return a uniformly random polynomial in coefficient representation.
   */
  CoefficientRingPoly uniformCoefficientPoly(int length, BigInteger modulus) {
    List<BigInteger> coefficients = sampleList(length, modulus);
    return new CoefficientRingPoly(coefficients, modulus);
  }

  /**
   * Samples a uniformly random polynomial for a given modulus, in evaluation representation.
   *
   * @param length the number of coefficients in the polynomial. Note, this defines the degree of
   *        the polynomial modulus in the ring to be <code>length</code>, and the return polynomial
   *        will have degree at most <code>length -1</code>.
   * @param root the root of unity used for the evaluation. This is assumed to be a
   *        <code>length*2</code> root of unity relative to <code>modulus</code>.
   * @param modulus the modulus for the coefficients
   * @return a uniformly random polynomial in evaluation representation.
   */
  EvaluationRingPoly uniformEvaluationPoly(int length, BigInteger root, BigInteger modulus) {
    List<BigInteger> evaluations = sampleList(length, modulus);
    return EvaluationRingPoly.fromEvaluations(evaluations, root, modulus);
  }

  /**
   * Samples a polynomial with small coefficients.
   *
   * <p>
   * Specifically, each coefficient is chosen as <i>-1, 0</i> or <i>1</i> with probabilities <i>1/4,
   * 1/2</i> and <i>1/4</i> respectively.
   * </p>
   *
   * @param length the number of coefficients in the polynomial. Note, this defines the degree of
   *        the polynomial modulus in the ring to be <code>length</code>, and the return polynomial
   *        will have degree at most <code>length -1</code>.
   * @param modulus the modulus for the coefficients
   * @return a new polynomial with small coefficients
   */
  CoefficientRingPoly lowWeightCoefficientPoly(int length, BigInteger modulus) {
    List<BigInteger> coefficients = new ArrayList<>(length);
    int halfByte = Byte.SIZE / 2;
    byte[] bytes = new byte[(length + halfByte - 1) / halfByte];
    drbg.nextBytes(bytes);
    int mask = 0b00000011;
    for (int i = 0; i < length; i += halfByte) {
      coefficients.add(BigInteger.valueOf(map((bytes[i] & mask))));
      coefficients.add(BigInteger.valueOf(map((bytes[i] >> 2 & mask))));
      coefficients.add(BigInteger.valueOf(map((bytes[i] >> 4 & mask))));
      coefficients.add(BigInteger.valueOf(map((bytes[i] >> 6 & mask))));
    }
    // TODO: handle lengths not aligned
    return new CoefficientRingPoly(coefficients, modulus);
  }

  /**
   * Maps a value in 0, 1, 2, 3 to 0, 1, -1, 0 respectively.
   *
   * @param val the value to map
   */
  private int map(int val) {
    return (val & 0b00000001) == 0 ? val : val - 3;
  }

  /**
   * Samples a list of BigIntegers given some modulus from the drbg.
   *
   * @param length the length of the list
   * @param modulus the modulus
   * @return list of numbers
   */
  private List<BigInteger> sampleList(int length, BigInteger modulus) {
    return IntStream.range(0, length).mapToObj(i -> drng.nextBigInteger(modulus))
        .collect(Collectors.toCollection(ArrayList::new));
  }

}
