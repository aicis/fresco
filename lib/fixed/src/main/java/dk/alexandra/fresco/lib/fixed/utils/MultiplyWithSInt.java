package dk.alexandra.fresco.lib.fixed.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;

/**
 * This computes the product of a secret fixed number and a secret integer.
 */
public class MultiplyWithSInt implements Computation<SFixed, ProtocolBuilderNumeric> {

  private DRes<SFixed> x;
  private DRes<SInt> s;

  public MultiplyWithSInt(DRes<SFixed> x, DRes<SInt> s) {
    this.x = x;
    this.s = s;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      SFixed xFixed = x.out();
      return new SFixed(seq.numeric().mult(s, xFixed.getSInt()));
    });
  }

}
