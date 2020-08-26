package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

/**
 * Compute the reciprocal of a secret real value.
 */
public class Reciprocal implements Computation<SReal, ProtocolBuilderNumeric> {

  private final DRes<SReal> x;

  public Reciprocal(DRes<SReal> x) {
    this.x = x;
  }

  private static double log(double x, double base) {
    return Math.log(x) / Math.log(base);
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(r1 ->
      r1.realAdvanced().normalize(x)
    ).seq((r2, norm) -> {
      DRes<SReal> normalized = r2.realNumeric().mult(norm.getFirst(), x);

      // From https://en.wikipedia.org/wiki/Division_algorithm

      double a = 140.0 / 33.0;
      double b = -64.0 / 11.0;
      double c = 256.0 / 99.0;
      DRes<SReal> xi = r2.realAdvanced().polynomialEvalutation(normalized, a, b, c);

      int n = (int) Math.ceil(log(r2.getRealNumericContext().getPrecision() + 1, 3) / log(99, 2));

      for (int i = 0; i < n; i++) {
        DRes<SReal> e = r2.realNumeric().sub(1, r2.realNumeric().mult(xi, normalized));
        DRes<SReal> y = r2.realNumeric().mult(xi, e);
        xi = r2.realNumeric().add(xi, r2.realNumeric().add(y, r2.realNumeric().mult(y, e)));
      }

      DRes<SReal> nPrime = r2.realAdvanced().twoPower(norm.getSecond());

      return r2.realNumeric().mult(xi, nPrime);
    });
  }

}
