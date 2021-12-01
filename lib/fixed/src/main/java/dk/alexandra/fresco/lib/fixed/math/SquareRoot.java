package dk.alexandra.fresco.lib.fixed.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.lib.fixed.utils.MultiplyWithSInt;

/**
 * Compute the square root of a secret fixed value.
 */
public class SquareRoot implements Computation<SFixed, ProtocolBuilderNumeric> {

  /**
   * p0132 from "Computer Approximations" by Hart et al. which approximates the square root on the
   * interval [0.5, 1].
   */
  private static final double[] POLYNOMIAL =
      new double[]{0.22906994529, 1.300669049, -0.9093210498, 0.5010420763, -0.1214683824};
  private final DRes<SFixed> x;

  public SquareRoot(DRes<SFixed> x) {
    this.x = x;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq ->
        AdvancedFixedNumeric.using(seq).normalize(x)
    ).pairInPar(
        (seq, norm) -> {
          DRes<SFixed> g = FixedNumeric.using(seq).mult(norm.getFirst(), x);
          return AdvancedFixedNumeric.using(seq).polynomialEvalutation(g, POLYNOMIAL);
        }, (seq, norm) -> {
          DRes<SInt> k = norm.getSecond();
          DRes<SInt> kSign = Comparison.using(seq).sign(k);
          DRes<SInt> kHalf = AdvancedNumeric.using(seq).rightShift(seq.numeric().mult(kSign, k));
          return Pair.lazy(k, seq.numeric().mult(kSign, kHalf));
        }
    ).seq((seq, params) -> {
      FixedNumeric fixed = FixedNumeric.using(seq);

      DRes<SFixed> unscaledResult = params.getFirst();
      DRes<SInt> k = params.getSecond().getFirst();
      DRes<SInt> kHalf = params.getSecond().getSecond();

      // Result if k is even
      DRes<SFixed> a = fixed.mult(unscaledResult,
          new TwoPower(seq.numeric().sub(0, kHalf)).buildComputation(seq));

      // This is +/- 1 if k is odd and 0 if k is even
      DRes<SInt> kOddSigned = seq.numeric().sub(k, seq.numeric().mult(2, kHalf));

      // Result if k is odd - multiply with 1/sqrt(2) if k negative and sqrt(2) if k non-negative
      DRes<SFixed> sqrt2reciprocal = fixed.sub(1.060660171779821,
          new MultiplyWithSInt(fixed.known(0.353553390593274), kOddSigned)
              .buildComputation(seq));

      // Result if k is odd
      DRes<SFixed> aPrime = fixed.mult(sqrt2reciprocal, a);

      // This is +1 if k is odd and 0 if k is even
      DRes<SInt> kOdd = seq.numeric().mult(kOddSigned, kOddSigned);

      return AdvancedFixedNumeric.using(seq).condSelect(kOdd, aPrime, a);
    });
  }

}
