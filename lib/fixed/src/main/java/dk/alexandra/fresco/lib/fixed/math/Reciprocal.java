package dk.alexandra.fresco.lib.fixed.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

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
    return builder.seq(r1 ->
        AdvancedFixedNumeric.using(r1).normalize(x)
    ).seq((r2, norm) -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(r2);

      DRes<SFixed> normalized = fixedNumeric.mult(norm.getFirst(), x);

      // From https://en.wikipedia.org/wiki/Division_algorithm
      double a = 140.0 / 33.0;
      double b = -64.0 / 11.0;
      double c = 256.0 / 99.0;

      // Initial approximation
      DRes<SFixed> xi = AdvancedFixedNumeric.using(r2).polynomialEvalutation(normalized, a, b, c);

      // Number of iterations
      int n = (int) Math.ceil(
          log((r2.getBasicNumericContext().getDefaultFixedPointPrecision() + 1) / log(99, 2), 3));

      return new State(0, n, xi, normalized, norm.getFirst());
    }).whileLoop(state -> state.i < state.n, (seq, state) -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(seq);

      DRes<SFixed> e = fixedNumeric.sub(1, fixedNumeric.mult(state.xi, state.normalizedX));
      DRes<SFixed> y = fixedNumeric.mult(state.xi, e);
      DRes<SFixed> xi = fixedNumeric
          .add(state.xi, fixedNumeric.add(y, fixedNumeric.mult(y, e)));

      return new State(state.i + 1, state.n, xi, state.normalizedX, state.norm);
    }).seq((seq, state) -> FixedNumeric.using(seq).mult(state.xi, state.norm));
  }

  private static class State implements DRes<State> {

    private final int i, n;
    private final DRes<SFixed> xi, normalizedX;
    private final DRes<SFixed> norm;

    private State(int i, int n, DRes<SFixed> xi, DRes<SFixed> normalizedX,
        DRes<SFixed> norm) {
      this.i = i;
      this.n = n;
      this.xi = xi;
      this.normalizedX = normalizedX;
      this.norm = norm;
    }

    @Override
    public State out() {
      return this;
    }
  }

}
