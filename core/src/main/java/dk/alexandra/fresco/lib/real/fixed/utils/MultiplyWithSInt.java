package dk.alexandra.fresco.lib.real.fixed.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;

/**
 * This computes the product of a secret real number and a secret integer.
 */
public class MultiplyWithSInt implements Computation<SReal, ProtocolBuilderNumeric> {

  private DRes<SReal> x;
  private DRes<SInt> s;

  public MultiplyWithSInt(DRes<SReal> x, DRes<SInt> s) {
    this.x = x;
    this.s = s;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      SFixed xFixed = (SFixed) x.out();
      return new SFixed(seq.numeric().mult(s, xFixed.getSInt()));
    });
  }

}
