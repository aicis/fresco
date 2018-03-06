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
   * Calcualte the exponential function of a secret input x.
   * 
   * @param x Secret value
   * @return A deferred result computing computing e<sup>x</sup>
   */
  DRes<SReal> exp(DRes<SReal> x);

  /**
   * Create a random value between 0 and 1.
   * 
   * @return The random value
   */
  DRes<SReal> random();

  /**
   * Calculate the natural logarithm of a secret value. Works best for small inputs (< 40), so
   * larger inputs should be scaled (utilize that log(x * b<sup>e</sup>) = log(x) + e log(b)).
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
