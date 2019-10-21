package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
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
  private static double[] POLYNOMIAL =
      new double[] {0.22906994529, 1.300669049, -0.9093210498, 0.5010420763, -0.1214683824};

  public SquareRoot(DRes<SReal> x) {
    this.x = x;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(r1 -> {
      return r1.realAdvanced().normalize(x);
    }).par((r2, norm) -> {
      DRes<SReal> a = r2.seq(r2Sub1 -> {
        DRes<SReal> g = r2Sub1.realNumeric().mult(norm.getFirst(), x);
        return r2Sub1.realAdvanced().polynomialEvalutation(g, POLYNOMIAL);
      });

      DRes<SInt> kHalf = r2.seq(r2Sub2 -> {
        DRes<SInt> k = norm.getSecond();
        DRes<SInt> kSign = r2Sub2.comparison().sign(k);

        return r2Sub2.numeric().mult(kSign,
            r2Sub2.advancedNumeric().rightShift(r2Sub2.numeric().mult(kSign, k)));
      });
      return () -> new Pair<>(a, new Pair<>(norm.getSecond(), kHalf));
    }).seq((r3, params) -> {

      DRes<SInt> k = params.getSecond().getFirst();
      DRes<SInt> kHalf = params.getSecond().getSecond();

      // Result if k is even
      DRes<SReal> a = r3.realNumeric().mult(params.getFirst(),
          new TwoPower(r3.numeric().sub(0, kHalf)).buildComputation(r3));

      DRes<SInt> kOddSigned = r3.numeric().sub(k, r3.numeric().mult(2, kHalf));

      // Result if k is odd - multiply with 1/sqrt(2) if k negative and sqrt(2) if k non-negative
      DRes<SReal> sqrt2recip = r3.realNumeric().sub(1.060660171779821,
          new MultiplyWithSInt(r3.realNumeric().known(0.353553390593274), kOddSigned)
              .buildComputation(r3));
      
      DRes<SReal> aPrime = r3.realNumeric().mult(sqrt2recip, a);

      DRes<SInt> kOdd = r3.numeric().mult(kOddSigned, kOddSigned);

      return r3.realAdvanced().condSelect(kOdd, aPrime, a);
    });
  }

}
