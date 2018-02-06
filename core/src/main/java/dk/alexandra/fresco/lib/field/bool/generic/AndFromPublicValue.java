package dk.alexandra.fresco.lib.field.bool.generic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * This protocol implements
 *
 * a AND b
 *
 * as
 *
 * if (b) then a ELSE false
 */
public class AndFromPublicValue implements Computation<SBool, ProtocolBuilderBinary> {

  private DRes<SBool> inA;
  private boolean inB;

  public AndFromPublicValue(DRes<SBool> inA, boolean inB) {
    this.inA = inA;
    this.inB = inB;
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderBinary builder) {
    if (inB) {
      return inA;
    } else {
      return builder.binary().known(false);
    }
  }
}
