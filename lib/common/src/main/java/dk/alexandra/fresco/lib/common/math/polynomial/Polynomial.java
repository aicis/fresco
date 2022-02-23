package dk.alexandra.fresco.lib.common.math.polynomial;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public interface Polynomial {

  /**
   * Get an upper bound for the degree of this polynomial.
   * 
   * @return The upper bound of the degree of the polynomial 
   */
  int getMaxDegree();

  /**
   * Get the coefficient of the term of degree <code>n</code> of this
   * polynomial.
   * 
   * @param n The degree
   * @return The coefficient
   */
  DRes<SInt> getCoefficient(int n);

}
