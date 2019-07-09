package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

public class Reciprocal implements Computation<SReal, ProtocolBuilderNumeric> {

  private DRes<SReal> x;

  public Reciprocal(DRes<SReal> x) {
    this.x = x;
  }

  private static double log(double x, double base) {
    return Math.log(x) / Math.log(base);
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      return seq.realAdvanced().normalize(x);
    }).seq((seq, norm) -> {
      DRes<SReal> normalized = seq.realNumeric().mult(norm.getFirst(), x);

      // From https://en.wikipedia.org/wiki/Division_algorithm

      double a = 140.0 / 33.0;
      double b = -64.0 / 11.0;
      double c = 256.0 / 99.0;
      DRes<SReal> xi = seq.realAdvanced().polynomialEvalutation(normalized, a, b, c);

      int n = (int) Math.ceil(log(seq.getRealNumericContext().getPrecision() + 1, 3) / log(99, 2));

      for (int i = 0; i < n; i++) {
        DRes<SReal> e = seq.realNumeric().sub(1, seq.realNumeric().mult(xi, normalized));
        DRes<SReal> y = seq.realNumeric().mult(xi, e);
        xi = seq.realNumeric().add(xi, seq.realNumeric().add(y, seq.realNumeric().mult(y, e)));
      }

      DRes<SReal> nPrime = seq.realAdvanced().twoPower(norm.getSecond());

      return seq.realNumeric().mult(xi, nPrime);
    });
  }

}
