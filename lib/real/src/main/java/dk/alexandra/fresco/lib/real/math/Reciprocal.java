package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.real.fixed.FixedNumeric;

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
      new AdvancedFixedNumeric(r1).normalize(x)
    ).seq((r2, norm) -> {
      DRes<SReal> normalized = new FixedNumeric(r2).mult(norm.getFirst(), x);

      // From https://en.wikipedia.org/wiki/Division_algorithm

      double a = 140.0 / 33.0;
      double b = -64.0 / 11.0;
      double c = 256.0 / 99.0;
      DRes<SReal> xi = new AdvancedFixedNumeric(r2).polynomialEvalutation(normalized, a, b, c);

      int n = (int) Math.ceil(log(r2.getBasicNumericContext().getPrecision() + 1, 3) / log(99, 2));

      for (int i = 0; i < n; i++) {
        DRes<SReal> e = new FixedNumeric(r2).sub(1, new FixedNumeric(r2).mult(xi, normalized));
        DRes<SReal> y = new FixedNumeric(r2).mult(xi, e);
        xi = new FixedNumeric(r2).add(xi, new FixedNumeric(r2).add(y, new FixedNumeric(r2).mult(y, e)));
      }

      DRes<SReal> nPrime = new AdvancedFixedNumeric(r2).twoPower(norm.getSecond());

      return new FixedNumeric(r2).mult(xi, nPrime);
    });
  }

}
