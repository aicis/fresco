package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.real.fixed.FixedNumeric;

/**
 * Evaluate a polynomial with public coefficients and a secret real input.
 */
public class PolynomialEvaluation implements Computation<SReal, ProtocolBuilderNumeric> {

  private final DRes<SReal> x;
  private final double[] p;

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

      RealNumeric realNumeric = new FixedNumeric(seq);
      if (p.length == 1) {
        return realNumeric.known(p[0]);
      }

      int n = p.length - 1;
      DRes<SReal> b = realNumeric.add(p[n - 1], realNumeric.mult(p[n], x));
      for (int i = n - 2; i >= 0; i--) {
        b = realNumeric.add(p[i], realNumeric.mult(b, x));
      }
      return b;
    });
  }

}
