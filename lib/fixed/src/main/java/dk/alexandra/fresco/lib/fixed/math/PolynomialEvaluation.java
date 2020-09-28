package dk.alexandra.fresco.lib.fixed.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

/**
 * Evaluate a polynomial with public coefficients and a secret fixed input.
 */
public class PolynomialEvaluation implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final DRes<SFixed> x;
  private final double[] p;

  /**
   * Create a new polynomial evalutation computation. The array contains the coefficients with the
   * first being the coefficient of the 0th degree term.
   * 
   * @param x Secret value
   * @param polynomial The coefficients for the polynomial
   */
  public PolynomialEvaluation(DRes<SFixed> x, double... polynomial) {
    this.x = x;
    this.p = polynomial;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {

      FixedNumeric fixedNumeric = FixedNumeric.using(seq);
      if (p.length == 1) {
        return fixedNumeric.known(p[0]);
      }

      int n = p.length - 1;
      DRes<SFixed> b = fixedNumeric.add(p[n - 1], fixedNumeric.mult(p[n], x));
      for (int i = n - 2; i >= 0; i--) {
        b = fixedNumeric.add(p[i], fixedNumeric.mult(b, x));
      }
      return b;
    });
  }

}
