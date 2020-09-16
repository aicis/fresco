package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.DefaultComparison;
import dk.alexandra.fresco.lib.common.math.DefaultAdvancedNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.real.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.real.fixed.utils.MultiplyWithSInt;

/**
 * Compute the square root of a secret real value.
 */
public class SquareRoot implements Computation<SReal, ProtocolBuilderNumeric> {

  private final DRes<SReal> x;

  /**
   * p0132 from "Computer Approximations" by Hart et al. which approximates the square root on the
   * interval [0.5, 1].
   */
  private static final double[] POLYNOMIAL =
      new double[] {0.22906994529, 1.300669049, -0.9093210498, 0.5010420763, -0.1214683824};

  public SquareRoot(DRes<SReal> x) {
    this.x = x;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(r1 ->
      new AdvancedFixedNumeric(r1).normalize(x)
    ).par((r2, norm) -> {
      DRes<SReal> a = r2.seq(r2Sub1 -> {
        DRes<SReal> g = new FixedNumeric(r2Sub1).mult(norm.getFirst(), x);
        return new AdvancedFixedNumeric(r2Sub1).polynomialEvalutation(g, POLYNOMIAL);
      });

      DRes<SInt> kHalf = r2.seq(r2Sub2 -> {
        DRes<SInt> k = norm.getSecond();
        DRes<SInt> kSign = new DefaultComparison(r2Sub2).sign(k);

        return r2Sub2.numeric().mult(kSign,
            new DefaultAdvancedNumeric(r2Sub2).rightShift(r2Sub2.numeric().mult(kSign, k)));
      });
      return () -> new Pair<>(a, new Pair<>(norm.getSecond(), kHalf));
    }).seq((r3, params) -> {

      DRes<SInt> k = params.getSecond().getFirst();
      DRes<SInt> kHalf = params.getSecond().getSecond();

      FixedNumeric realNumeric = new FixedNumeric(r3);

      // Result if k is even
      DRes<SReal> a = realNumeric.mult(params.getFirst(),
          new TwoPower(r3.numeric().sub(0, kHalf)).buildComputation(r3));

      DRes<SInt> kOddSigned = r3.numeric().sub(k, r3.numeric().mult(2, kHalf));

      // Result if k is odd - multiply with 1/sqrt(2) if k negative and sqrt(2) if k non-negative
      DRes<SReal> sqrt2recip = realNumeric.sub(1.060660171779821,
          new MultiplyWithSInt(realNumeric.known(0.353553390593274), kOddSigned)
              .buildComputation(r3));
      
      DRes<SReal> aPrime = realNumeric.mult(sqrt2recip, a);

      DRes<SInt> kOdd = r3.numeric().mult(kOddSigned, kOddSigned);

      return new AdvancedFixedNumeric(r3).condSelect(kOdd, aPrime, a);
    });
  }

}
