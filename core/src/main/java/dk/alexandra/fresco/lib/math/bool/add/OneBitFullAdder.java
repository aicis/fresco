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

  private DRes<SBool> valueA = null;
  private DRes<SBool> valueB = null;
  private DRes<SBool> carry = null;
  private DRes<SBool> xor1 = null;
  private DRes<SBool> xor2 = null;
  private DRes<SBool> xor3 = null;
  private DRes<SBool> and1 = null;
  private DRes<SBool> and2 = null;

  public OneBitFullAdder(DRes<SBool> a, DRes<SBool> b, DRes<SBool> c) {
    this.valueA = a;
    this.valueB = b;
    this.carry = c;
  }

  @Override
  public DRes<Pair<SBool, SBool>> buildComputation(ProtocolBuilderBinary builder) {
    return builder.par(par -> {
      xor1 = par.binary().xor(valueA, valueB);
      and1 = par.binary().and(valueA, valueB);
      return () -> (par);
    }).par((par, pair) -> {
      xor2 = par.binary().xor(xor1, carry);
      and2 = par.binary().and(xor1, carry);
      return () -> (par);
    }).par((par, pair) -> {
      xor3 = par.binary().xor(and2, and1);
      return () -> new Pair<SBool, SBool>(xor2.out(), xor3.out());
    });
  }
}
