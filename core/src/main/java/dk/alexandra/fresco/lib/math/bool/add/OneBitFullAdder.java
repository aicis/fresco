package dk.alexandra.fresco.lib.math.bool.add;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * Uses a straightforward way of computing the addition of two bits and a previous carry.
 */
public class OneBitFullAdder
    implements Computation<Pair<SBool, SBool>, ProtocolBuilderBinary> {

  private DRes<SBool> a, b, c;
  private DRes<SBool> xor1, xor2, xor3, and1, and2 = null;

  public OneBitFullAdder(DRes<SBool> a, DRes<SBool> b, DRes<SBool> c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  @Override
  public DRes<Pair<SBool, SBool>> buildComputation(ProtocolBuilderBinary builder) {
    return builder.par(par -> {
      xor1 = par.binary().xor(a, b);
      and1 = par.binary().and(a, b);
      return () -> (par);
    }).par((par, pair) -> {
      xor2 = par.binary().xor(xor1, c);
      and2 = par.binary().and(xor1, c);
      return () -> (par);
    }).par((par, pair) -> {
      xor3 = par.binary().xor(and2, and1);
      return () -> new Pair<SBool, SBool>(xor2.out(), xor3.out());
    });
  }
}
