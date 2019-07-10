package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.utils.Scaling;

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
    return builder.seq(seq -> {
      return seq.realAdvanced().normalize(x);
    }).par((par, norm) -> {
      DRes<SReal> a = par.seq(subBuilder -> {
        DRes<SReal> g = subBuilder.realNumeric().mult(norm.getFirst(), x);
        return subBuilder.realAdvanced().polynomialEvalutation(g, POLYNOMIAL);
      });

      DRes<SInt> kHalf = par.seq(subBuilder -> {
        DRes<SInt> k = norm.getSecond();
        DRes<SInt> kSign = subBuilder.comparison().sign(k);

        return subBuilder.numeric().mult(kSign,
            subBuilder.advancedNumeric().rightShift(subBuilder.numeric().mult(kSign, k)));
      });
      return () -> new Pair<>(a, new Pair<>(norm.getSecond(), kHalf));
    }).seq((seq, params) -> {

      DRes<SInt> k = params.getSecond().getFirst();
      DRes<SInt> kHalf = params.getSecond().getSecond();

      // Result if k is even
      DRes<SReal> a = seq.realNumeric().mult(params.getFirst(),
          new TwoPower(seq.numeric().sub(0, kHalf)).buildComputation(seq));

      DRes<SInt> kOddSigned = seq.numeric().sub(k, seq.numeric().mult(2, kHalf));

      // Result if k is odd
      DRes<SReal> sqrt2recip = seq.realNumeric().sub(1.060660171779821,
          new Scaling(seq.realNumeric().known(0.353553390593274), kOddSigned)
              .buildComputation(seq));
      
      DRes<SReal> aPrime = seq.realNumeric().mult(sqrt2recip, a);

      DRes<SInt> kOdd = seq.numeric().mult(kOddSigned, kOddSigned);

      return seq.realAdvanced().condSelect(kOdd, aPrime, a);
    });
  }

}
