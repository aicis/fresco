package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import java.math.BigDecimal;
import java.util.List;

public interface AdvancedRealNumeric extends ComputationDirectory {

  /**
   * Calculate the sum of all terms in a list.
   *
   * @param terms List of secret values
   * @return A deferred result computing the sum of the terms
   */
  DRes<SReal> sum(List<DRes<SReal>> terms);

  /**
   * Calculate the inner product of two secret vectors.
   *
   * @param a List of secret values
   * @param b List of secret values
   * @return A deferred result computing computing the inner product of the two lists
   */
  DRes<SReal> innerProduct(List<DRes<SReal>> a, List<DRes<SReal>> b);

  /**
   * Calculate the inner product of a public and a secret vector.
   *
   * @param a List of public values
   * @param b List of secret values
   * @return A deferred result computing computing the inner product of the two lists
   */
  DRes<SReal> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SReal>> b);

  /**
   * Calcualte the exponential function of a secret input x. The result is mmore precise for small
   * inputs, so the input should be scaled if possible (note that <i>exp(n x) =
   * exp(x)<sup>n</sup></i>).
   *
   * @param x Secret value
   * @return A deferred result computing computing e<sup>x</sup>
   */
  DRes<SReal> exp(DRes<SReal> x);

  /**
   * Create a random value between 0 and 1.
   *
   * <p>
   * As it is impossible to uniformly sample the infinite reals between 0 and 1, the number will be
   * sampled as <i>r * 2<sup>-n</sup></i> for a random positive <i>n</i> bit number <i>r</i>.
   * </p>
   *
   * @param bits the number of random bits used for the sample
   * @return The random value
   */
  DRes<SReal> random(int bits);

  /**
   * Calculate the natural logarithm of a secret value. Works best for small inputs (< 10), so
   * larger inputs should be scaled is possible (note that <i>log(x * b<sup>e</sup>) = log(x) + e
   * log(b)</i>).
   *
   * @param x Secret value
   * @return A deferred result computing computing log(x)
   */
  DRes<SReal> log(DRes<SReal> x);

  /**
   * Calculate the square root of a secret value.
   *
   * @param x Secret value
   * @return A deferred result computing computing &radic;x
   */
  DRes<SReal> sqrt(DRes<SReal> x);

}
