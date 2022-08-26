package dk.alexandra.fresco.lib.fixed.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.lib.fixed.utils.MultiplyWithSInt;

/**
 * Compute the reciprocal of a secret fixed value.
 */
public class Reciprocal implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final DRes<SFixed> x;

  public Reciprocal(DRes<SFixed> x) {
    this.x = x;
  }

  private static double log(double x, double base) {
    return Math.log(x) / Math.log(base);
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(r1 -> Pair.lazy(AdvancedFixedNumeric.using(r1).normalize(x), AdvancedFixedNumeric.using(r1).sign(x))
    ).seq((r2, normAndSign) -> {
      Pair<DRes<SFixed>, DRes<SInt>> norm = normAndSign.getFirst().out();

      FixedNumeric fixedNumeric = FixedNumeric.using(r2);

      DRes<SFixed> normalized = fixedNumeric.mult(norm.getFirst(), x);

      // From https://en.wikipedia.org/wiki/Division_algorithm

      double a = 140.0 / 33.0;
      double b = -64.0 / 11.0;
      double c = 256.0 / 99.0;
      DRes<SFixed> xi = AdvancedFixedNumeric.using(r2).polynomialEvalutation(normalized, a, b, c);

      int n = (int) Math.ceil(log(r2.getBasicNumericContext().getDefaultFixedPointPrecision() + 1, 3) / log(99, 2));

      for (int i = 0; i < n; i++) {
        DRes<SFixed> e = fixedNumeric.sub(1, fixedNumeric.mult(xi, normalized));
        DRes<SFixed> y = fixedNumeric.mult(xi, e);
        xi = fixedNumeric
            .add(xi, fixedNumeric.add(y, fixedNumeric.mult(y, e)));
      }

      DRes<SFixed> nPrime = AdvancedFixedNumeric.using(r2).twoPower(norm.getSecond());

      return new MultiplyWithSInt(fixedNumeric.mult(xi, nPrime), normAndSign.getSecond()).buildComputation(r2);
    });
  }

}
