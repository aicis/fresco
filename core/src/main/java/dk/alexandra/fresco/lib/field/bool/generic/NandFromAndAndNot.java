package dk.alexandra.fresco.lib.field.bool.generic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * This protocol implements
 *
 * a NXOR b
 *
 * as
 *
 * NOT ( a XOR b )
 */
public class NandFromAndAndNot implements
    dk.alexandra.fresco.framework.builder.Computation<SBool, ProtocolBuilderBinary> {

  private DRes<SBool> inA;
  private DRes<SBool> inB;

  public NandFromAndAndNot(DRes<SBool> inA, DRes<SBool> inB) {
    this.inA = inA;
    this.inB = inB;
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderBinary builder) {
    DRes<SBool> tmp = builder.binary().and(inA, inB);
    return builder.binary().not(tmp);
  }
}
