package dk.alexandra.fresco.lib.fixed.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.DefaultAdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.DefaultFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

/**
 * Compute the logarithm of a secret fixed value.
 */
public class Logarithm implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final DRes<SFixed> x;

  /**
   * p2607 from "Computer Approximations" by Hart et al. which approximates the natural logarithm on
   * the interval [0.5, 1].
   */
  private static final double[] POLYNOMIAL = new double[] {-0.30674666858e1, 0.1130516183486e2,
      -0.2774666470302e2, 0.5149518504454e2, -0.6669583732238e2, 0.5853503340958e2,
      -0.3320167436859e2, 0.1098927015084e2, -0.161300738935e1};
  
  public Logarithm(DRes<SFixed> x) {
    this.x = x;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(
        r1 -> AdvancedFixedNumeric.using(r1).normalize(x)
    ).seq((r2, norm) -> {

      FixedNumeric fixedNumeric = FixedNumeric.using(r2);

      DRes<SFixed> g = fixedNumeric.mult(norm.getFirst(), x);
      DRes<SFixed> f = AdvancedFixedNumeric.using(r2).polynomialEvalutation(g, POLYNOMIAL);

      g = fixedNumeric.sub(f,
          fixedNumeric.mult(Math.log(2), fixedNumeric.fromSInt(norm.getSecond())));
      
      return g;
    });
  }

}
