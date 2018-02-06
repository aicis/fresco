package dk.alexandra.fresco.lib.math.polynomial;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public class PolynomialImpl implements Polynomial {

  private final List<DRes<SInt>> coefficients;

  /**
   * Create a new polynomial with the given coefficients.
   * 
   * @param coefficients
   *            The coefficients of the polynomial,
   *            <code>coefficients[n]</code> being the coefficient for the
   *            term of degree <code>n</code>.
   */
  public PolynomialImpl(List<DRes<SInt>> coefficients) {
    this.coefficients = coefficients;
  }

  @Override
  public DRes<SInt> getCoefficient(int n) {
    return coefficients.get(n);
  }

  @Override
  public int getMaxDegree() {
    return coefficients.size();
  }

}
