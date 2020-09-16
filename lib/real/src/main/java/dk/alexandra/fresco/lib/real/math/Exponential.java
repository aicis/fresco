package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.real.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.real.fixed.utils.MultiplyWithSInt;

/**
 * Compute the exponential function of a secret real value.
 */
public class Exponential implements Computation<SReal, ProtocolBuilderNumeric> {

  private final DRes<SReal> x;

  /**
   * p1045 from "Computer Approximations" by Hart et al. which approximates x -> 2^x on the interval
   * [0, 1].
   */
  private static final double[] POLYNOMIAL = new double[] {0.1000000077443021686e1,
      0.693147180426163827795756e0, 0.24022651071017064605384e0, .55504068620466379157744e-1,
      0.9618341225880462374977e-2, 0.1332730359281437819329e-2, 0.155107460590052573978e-3,
      0.14197847399765606711e-4, 0.1863347724137967076e-5};

  public Exponential(DRes<SReal> x) {
    this.x = x;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {

    return builder.seq(r1 -> {

      DRes<SInt> signBit = new FixedNumeric(r1).leq(x, new FixedNumeric(r1).known(0.0));

      // e^x = 2^{log_2 e * x} = 2^{1.442695040889 * x}
      DRes<SReal> X = new FixedNumeric(r1).mult(1.442695040889, x);

      DRes<SInt> sign = r1.numeric().add(1, r1.numeric().mult(-2, signBit));
      DRes<SReal> absX = new MultiplyWithSInt(X, sign).buildComputation(r1);

      DRes<SInt> xIntegerPart = new AdvancedFixedNumeric(r1).floor(absX);
      DRes<SReal> xFractionalPart = new FixedNumeric(r1).sub(absX, new FixedNumeric(r1).fromSInt(xIntegerPart));

      return () -> new Pair<>(new Pair<>(xIntegerPart, xFractionalPart), signBit);
    }).par((r2, xAndSignBit) -> {

      // 2^integer part
      DRes<SReal> f = new AdvancedFixedNumeric(r2).twoPower(xAndSignBit.getFirst().getFirst());

      // 2^fractional part
      DRes<SReal> g =
          new AdvancedFixedNumeric(r2).polynomialEvalutation(xAndSignBit.getFirst().getSecond(), POLYNOMIAL);

      return () -> new Pair<>(new Pair<>(f, g), xAndSignBit.getSecond());
    }).seq((r3, fgAndSign) -> {

      DRes<SReal> h = new FixedNumeric(r3).mult(fgAndSign.getFirst().getFirst(),
          fgAndSign.getFirst().getSecond());
      DRes<SReal> hRecip = new AdvancedFixedNumeric(r3).reciprocal(h);

      return new AdvancedFixedNumeric(r3).condSelect(fgAndSign.getSecond(), hRecip, h);
    });
  }

}
