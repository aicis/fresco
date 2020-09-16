package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.real.fixed.FixedNumeric;

/**
 * Compute the logarithm of a secret real value.
 */
public class Logarithm implements Computation<SReal, ProtocolBuilderNumeric> {

  private final DRes<SReal> x;

  /**
   * p2607 from "Computer Approximations" by Hart et al. which approximates the natural logarithm on
   * the interval [0.5, 1].
   */
  private static final double[] POLYNOMIAL = new double[] {-0.30674666858e1, 0.1130516183486e2,
      -0.2774666470302e2, 0.5149518504454e2, -0.6669583732238e2, 0.5853503340958e2,
      -0.3320167436859e2, 0.1098927015084e2, -0.161300738935e1};
  
  public Logarithm(DRes<SReal> x) {
    this.x = x;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(
        r1 -> new AdvancedFixedNumeric(r1).normalize(x)
    ).seq((r2, norm) -> {

      FixedNumeric fixedNumeric = new FixedNumeric(r2);
      DRes<SReal> g = fixedNumeric.mult(norm.getFirst(), x);
      DRes<SReal> f = new AdvancedFixedNumeric(r2).polynomialEvalutation(g, POLYNOMIAL);

      g = fixedNumeric.sub(f,
          fixedNumeric.mult(Math.log(2), fixedNumeric.fromSInt(norm.getSecond())));
      
      return g;
    });
  }

}
