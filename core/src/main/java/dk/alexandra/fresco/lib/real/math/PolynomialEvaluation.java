package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

/**
 * Evaluate a polynomial with public coefficients and a secret real input.
 */
public class PolynomialEvaluation implements Computation<SReal, ProtocolBuilderNumeric> {

  private DRes<SReal> x;
  private double[] p;

  /**
   * Create a new polynomial evalutation computation. The array contains the coefficients with the
   * first being the coefficient of the 0th degree term.
   * 
   * @param x Secret value
   * @param polynomial The coefficients for the polynomial
   */
  public PolynomialEvaluation(DRes<SReal> x, double... polynomial) {
    this.x = x;
    this.p = polynomial;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {

      if (p.length == 1) {
        return seq.realNumeric().known(p[0]);
      }

      int n = p.length - 1;
      DRes<SReal> b = seq.realNumeric().add(p[n - 1], seq.realNumeric().mult(p[n], x));
      for (int i = n - 2; i >= 0; i--) {
        b = seq.realNumeric().add(p[i], seq.realNumeric().mult(b, x));
      }
      return b;
    });
  }

}
