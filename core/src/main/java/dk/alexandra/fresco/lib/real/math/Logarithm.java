package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

public class Logarithm implements Computation<SReal, ProtocolBuilderNumeric> {

  private final DRes<SReal> x;

  /**
   * p2607 from "Computer Approximations" by Hart et al. which approximates the natural logarithm on
   * the interval [0.5, 1].
   */
  private static double[] POLYNOMIAL = new double[] {-0.30674666858e1, 0.1130516183486e2,
      -0.2774666470302e2, 0.5149518504454e2, -0.6669583732238e2, 0.5853503340958e2,
      -0.3320167436859e2, 0.1098927015084e2, -0.161300738935e1};
  
  public Logarithm(DRes<SReal> x) {
    this.x = x;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(r1 -> {
      return r1.realAdvanced().normalize(x);
    }).seq((r2, norm) -> {

      DRes<SReal> g = r2.realNumeric().mult(norm.getFirst(), x);
      DRes<SReal> f = r2.realAdvanced().polynomialEvalutation(g, POLYNOMIAL);

      // Natural log of 2
      double log2 = 0.69314718055994530;

      g = r2.realNumeric().sub(f,
          r2.realNumeric().mult(log2, r2.realNumeric().fromSInt(norm.getSecond())));
      
      return g;
    });
  }

}
