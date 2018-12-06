package dk.alexandra.fresco.overdrive.math;

import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
   * <p>
   * Note: this is the distribution called <i>U</i> in the Overdrive paper.
   * </p>
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
   * <p>
   * Note: this is the distribution called <i>U</i> in the Overdrive paper.
   * </p>
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
   * <p>
   * Note: this is the distribution called <i>ZO(0.5)</i> in the Overdrive paper.
   * </p>
   *
   * @param length the number of coefficients in the polynomial. Note, this defines the degree of
   *        the polynomial modulus in the ring to be <code>length</code>, and the return polynomial
   *        will have degree at most <code>length -1</code>.
   * @param modulus the modulus for the coefficients
   * @return a new polynomial with small coefficients
   */
  CoefficientRingPoly lowWeightPoly(int length, BigInteger modulus) {
    // We can sample each coefficient by sampling two uniformly random bits and then map 01 to 1, 10
    // to -1 and 00 and 11 to 0.
    List<BigInteger> coefficients = new ArrayList<>(length);
    int halfByte = Byte.SIZE / 2;
    byte[] bytes = new byte[(length + halfByte - 1) / halfByte];
    drbg.nextBytes(bytes);
    int mask = 0b00000011;
    for (int i = 0; i < length; i += halfByte) {
      coefficients.add(BigInteger.valueOf(translate((bytes[i] & mask))));
      coefficients.add(BigInteger.valueOf(translate((bytes[i] >> 2 & mask))));
      coefficients.add(BigInteger.valueOf(translate((bytes[i] >> 4 & mask))));
      coefficients.add(BigInteger.valueOf(translate((bytes[i] >> 6 & mask))));
    }
    int size = coefficients.size();
    for (int i = 0; i < length - size; i++) {
      coefficients.add(
          BigInteger.valueOf(translate((bytes[bytes.length - 1] >> 2 * i & mask))).mod(modulus));
    }
    return new CoefficientRingPoly(coefficients, modulus);
  }

  /**
   * Samples a ring polynomial with random coefficients in the set <i>{-1, 0, 1}</i> under the
   * restriction that there must be exactly some given number of non-zero coefficients.
   *
   * <p>
   * Note: this is basically the distribution called HWT(h) in the Overdrive paper. Only in
   * Overdrive they specify it as sampling a poly with <i>at least</i> h non-zero coefficients. I
   * believe this is a mistake, and we take it to be exactly h non-zero coefficients, which is the
   * specification in earlier definitions of this distribution.
   * </p>
   *
   * @param length the number of coefficients in the polynomial. Note, this defines the degree of
   *        the polynomial modulus in the ring to be <code>length</code>, and the return polynomial
   *        will have degree at most <code>length -1</code>.
   * @param weight the number of non-zero coefficients
   * @param modulus the modulus
   * @return a ring poly with exactly <i>weight</i> non-zero coefficients.
   */
  CoefficientRingPoly fixedWeightPoly(int length, int weight, BigInteger modulus) {
    if (weight > length) {
      throw new IllegalArgumentException(
          "Weight cannot exceed the number of coefficients. Weight was " + weight
              + " while length was " + length);
    }
    // Sample the indices that should be either non-zero or zero (depending on which there should be
    // most of)
    int numIndices = weight < length / 2 ? weight : length - weight;
    Set<Integer> randomIndices = new HashSet<>(numIndices);
    for (int i = 0; i < numIndices; i++) {
      int idx = drng.nextInt(length);
      while (randomIndices.contains(idx)) {
        idx = drng.nextInt(length);
      }
      randomIndices.add(idx);
    }
    // For the non-zero indices sample either one or minus one.
    List<BigInteger> coefficients = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      int coeff;
      if (weight < length / 2) { // there are more zero than non-zero indicies
        if (randomIndices.contains(i)) {
          coeff = drng.nextInt(2) == 0 ? -1 : 1;
        } else {
          coeff = 0;
        }
      } else { // there are more non-zero than zero indicies (typically, this would not be the case)
        if (randomIndices.contains(i)) {
          coeff = 0;
        } else {
          coeff = drng.nextInt(2) == 0 ? -1 : 1;
        }
      }
      coefficients.add(BigInteger.valueOf(coeff).mod(modulus));
    }
    return new CoefficientRingPoly(coefficients, modulus);
  }

  /**
   * Samples a polynomial where each coefficient is sampled from from a gaussian distribution with
   * some variance, and rounded to the nearest integer.
   * <p>
   * A paper on how to do that can be found here https://www.doc.ic.ac.uk/~wl/papers/07/csur07dt.pdf
   * Note: This is the distribution called <i>DG(&sigma;<sup>2</sup>)</i> in the Overdrive paper.
   * </p>
   *
   * @param length
   * @param variance
   * @param modulus
   * @return
   */
  CoefficientRingPoly gaussianPoly(int length, int variance, BigInteger modulus) {
    throw new UnsupportedOperationException("Not Implemented");
  }



  /**
   * Translate a value in 0, 1, 2, 3 to 0, 1, -1, 0 respectively.
   *
   * @param val the value to translate
   */
  private int translate(int val) {
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
