package dk.alexandra.fresco.lib.math.bool.add;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * Uses a straightforward way of computing the addition plus the carry.
 */
public class OneBitHalfAdder
    implements Computation<Pair<SBool, SBool>, ProtocolBuilderBinary> {

  private DRes<SBool> left, right;

  public OneBitHalfAdder(DRes<SBool> left, DRes<SBool> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public DRes<Pair<SBool, SBool>> buildComputation(ProtocolBuilderBinary builder) {
    return builder.par(par -> {
      DRes<SBool> res = par.binary().xor(left, right);
      DRes<SBool> carry = par.binary().and(left, right);
      return () -> new Pair<SBool, SBool>(res.out(), carry.out());
    });
  }

}
