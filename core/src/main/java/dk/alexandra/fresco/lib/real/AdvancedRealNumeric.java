package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
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
   * @return The random value
   */
  DRes<SReal> random();

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
   * @return A deferred result computing &radic;x
   */
  DRes<SReal> sqrt(DRes<SReal> x);

  /**
   * Calculate a power of two <i>c = 2<sup>-k</sup></i> such that the product <i>x * c</i> is in the
   * interval <i>[0.5, 1]</i>.
   * 
   * @param x A secret value
   * @return A deferred result computing the pair <i>(c, k)</i> as decribed above.
   */
  DRes<Pair<DRes<SReal>, DRes<SInt>>> normalize(DRes<SReal> x);
  
  /**
   * Calculate the reciprocal of a secret value.
   * 
   * @param x A secret value
   * @return A deferred result computing 1/x
   */
  DRes<SReal> reciprocal(DRes<SReal> x);

  /**
   * Calculate 2 to a secret integer power which may be negative.
   * 
   * @param x A secret value.
   * @return A deferred result computing <i>2<sup>x</sup></i>
   */
  DRes<SReal> twoPower(DRes<SInt> x);

  /**
   * Evaluate a polynomial with public coefficients and a secret input.
   *
   * @param input A secret value
   * @param polynomial The coefficients for the polynomial in increaseing order of degree.
   * @return A deferred result computing the value of the polynomial on the input.
   */
  DRes<SReal> polynomialEvalutation(DRes<SReal> input, double ... polynomial);
  
  /**
   * Pick one of two inputs depending on a condition. The first is picked if the condition is 1 and
   * the other if the condition is 0.
   * 
   * @param condition A secret value which is 0 or 1
   * @param first A secret value
   * @param second A secret value
   * @return
   */
  DRes<SReal> condSelect(DRes<SInt> condition, DRes<SReal> first, DRes<SReal> second);
  
  /**
   * Compute the floor of a secret input.
   * 
   * @param x A secret value.
   * @return A deferred result computing &lfloor; x &rfloor;
   */
  DRes<SInt> floor(DRes<SReal> x);
  
  /**
   * Compute the sign of a secret input.
   * 
   * @param x A secret value.
   * @return A deferred result computing the sign for x (0 has positive sign).
   */
  DRes<SInt> sign(DRes<SReal> x);
  
}
