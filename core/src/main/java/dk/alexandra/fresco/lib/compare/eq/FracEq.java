package dk.alexandra.fresco.lib.compare.eq;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * <p>
 * Implements the comparison <i>a/b = c/d</i> by computing the related comparison
 * <i>ad=bc</i>. Note: this means that that a fraction with zero denominator will
 * be judged equal to zero, which is technically wrong.
 * </p>
 */
public class FracEq implements ComputationBuilder<SInt, ProtocolBuilderNumeric> {

  private final Computation<SInt> n0, d0, n1, d1;

  /**
   * @param n0 numerator of first fraction
   * @param d0 denominator of first fraction
   * @param n1 numerator of first fraction
   * @param d1 denominator of first fraction
   */
  public FracEq(Computation<SInt> n0, Computation<SInt> d0, Computation<SInt> n1,
      Computation<SInt> d1) {
    super();
    this.n0 = n0;
    this.d0 = d0;
    this.n1 = n1;
    this.d1 = d1;
  }

  @Override
  public Computation<SInt> build(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      NumericBuilder numeric = par.numeric();
      return Pair.lazy(
          numeric.mult(d0, n1),
          numeric.mult(d1, n0)
      );
    }).seq((pair, seq) ->
        seq.comparison().equals(pair.getFirst(), pair.getSecond())
    );
  }

}
