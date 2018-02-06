package dk.alexandra.fresco.lib.field.bool.generic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * This protocol implements
 *
 * a OR b
 *
 * as
 *
 * (a AND b) XOR a XOR b
 */
public class OrFromXorAnd implements
    dk.alexandra.fresco.framework.builder.Computation<SBool, ProtocolBuilderBinary> {

  private DRes<SBool> inA;
  private DRes<SBool> inB;

  public OrFromXorAnd(DRes<SBool> inA, DRes<SBool> inB) {
    this.inA = inA;
    this.inB = inB;
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderBinary builder) {

    DRes<SBool> t0 = builder.binary().and(inA, inB);
    DRes<SBool> t1 = builder.binary().xor(inA, inB);
    
    return builder.binary().xor(t0, t1);
  }
}
