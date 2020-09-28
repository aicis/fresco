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
 * Compute the exponential function of a secret fixed value.
 */
public class Exponential implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final DRes<SFixed> x;

  /**
   * p1045 from "Computer Approximations" by Hart et al. which approximates x -> 2^x on the interval
   * [0, 1].
   */
  private static final double[] POLYNOMIAL = new double[] {0.1000000077443021686e1,
      0.693147180426163827795756e0, 0.24022651071017064605384e0, .55504068620466379157744e-1,
      0.9618341225880462374977e-2, 0.1332730359281437819329e-2, 0.155107460590052573978e-3,
      0.14197847399765606711e-4, 0.1863347724137967076e-5};

  public Exponential(DRes<SFixed> x) {
    this.x = x;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {

    return builder.seq(r1 -> {

      DRes<SInt> signBit = FixedNumeric.using(r1).leq(x, FixedNumeric.using(r1).known(0.0));

      // e^x = 2^{log_2 e * x} = 2^{1.442695040889 * x}
      DRes<SFixed> X = FixedNumeric.using(r1).mult(1.442695040889, x);

      DRes<SInt> sign = r1.numeric().add(1, r1.numeric().mult(-2, signBit));
      DRes<SFixed> absX = new MultiplyWithSInt(X, sign).buildComputation(r1);

      DRes<SInt> xIntegerPart = AdvancedFixedNumeric.using(r1).floor(absX);
      DRes<SFixed> xFractionalPart = FixedNumeric.using(r1).sub(absX, FixedNumeric.using(r1).fromSInt(xIntegerPart));

      return () -> new Pair<>(new Pair<>(xIntegerPart, xFractionalPart), signBit);
    }).par((r2, xAndSignBit) -> {

      AdvancedFixedNumeric advanced = AdvancedFixedNumeric.using(r2);

      // 2^integer part
      DRes<SFixed> f = advanced.twoPower(xAndSignBit.getFirst().getFirst());

      // 2^fractional part
      DRes<SFixed> g =
          advanced.polynomialEvalutation(xAndSignBit.getFirst().getSecond(), POLYNOMIAL);

      return () -> new Pair<>(new Pair<>(f, g), xAndSignBit.getSecond());
    }).seq((r3, fgAndSign) -> {

      AdvancedFixedNumeric advanced = AdvancedFixedNumeric.using(r3);

      DRes<SFixed> h = FixedNumeric.using(r3).mult(fgAndSign.getFirst().getFirst(),
          fgAndSign.getFirst().getSecond());
      DRes<SFixed> hRecip = advanced.reciprocal(h);

      return advanced.condSelect(fgAndSign.getSecond(), hRecip, h);
    });
  }

}
