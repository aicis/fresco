package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

/**
 * Evaluate a polynomial with public coefficients and a secret input.
 */
public class PolynomialEvaluation implements Computation<SReal, ProtocolBuilderNumeric> {

  private double[] p;
  private DRes<SReal> x;

  /**
   * Create a new polynomial evalutation computation. The array contains the coefficients with the
   * first being the coefficient of the 0th degree term.
   * 
   * @param x Secret value
   * @param polynomial The coefficients for the polynomial
   */
  public PolynomialEvaluation(DRes<SReal> x, double ... polynomial) {
    this.p = polynomial;
    this.x = x;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    if (p.length == 0) {
      return builder.realNumeric().known(0);
    }
    
    if (p.length == 1) {
      return builder.realNumeric().known(p[0]);
    }

    int n = p.length - 1;
    DRes<SReal> b = builder.realNumeric().add(p[n - 1], builder.realNumeric().mult(p[n], x));
    for (int i = n - 2; i >= 0; i--) {
      b = builder.realNumeric().add(p[i], builder.realNumeric().mult(b, x));
    }
    return b;
  }

}