package dk.alexandra.fresco.decimal;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.util.List;

public interface AdvancedRealNumeric extends ComputationDirectory {

  /**
   * Compare two secret values. Returns a secret int that is 1 if x \leq y and 0 otherwise.
   *
   * @param x Secret value 1
   * @param y Secret value 2
   * @return A secret int that is 1 if x \leq y and 0 otherwise.
   */
  DRes<SInt> leq(DRes<SReal> x, DRes<SReal> y);

  /**
   * Calculate the sum of all terms in a list.
   * 
   * @param terms
   * @return
   */
  DRes<SReal> sum(List<DRes<SReal>> terms);

  /**
   * Calculate the inner product of two secret vectors.
   * 
   * @param a Secret value 1
   * @param b Secret value 1
   * @return
   */
  DRes<SReal> innerProduct(List<DRes<SReal>> a, List<DRes<SReal>> b);

  /**
   * Calculate the inner product of a public and a secret vector.
   * 
   * @param a Public value
   * @param b Secret value
   * @return
   */
  DRes<SReal> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SReal>> b);

  /**
   * Calcualte the exponential function of a secret input x.
   * 
   * @param x
   * @return
   */
  DRes<SReal> exp(DRes<SReal> x);

  /**
   * Create a random value between 0 and 1.
   * 
   * @return The random value
   */
  DRes<SReal> random();

}
