package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

public class Logarithm implements Computation<SReal, ProtocolBuilderNumeric> {

  private final DRes<SReal> x;

  public Logarithm(DRes<SReal> x) {
    this.x = x;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      return seq.realAdvanced().normalize(x);
    }).seq((seq, norm) -> {

      DRes<SReal> g = seq.realNumeric().mult(norm.getFirst(), x);
      DRes<SReal> f = seq.realAdvanced().polynomialEvalutation(g, ApproximationPolynomials.LOG);

      double log2 = 0.69314718055994530;

      g = seq.realNumeric().sub(f,
          seq.realNumeric().mult(log2, seq.realNumeric().fromSInt(norm.getSecond())));
      return g;
    });
  }

}
