package dk.alexandra.fresco.lib.field.bool.generic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * An efficient way of OR'ing an SBool with a public value if we can construct SBools of constants.
 */
public class OrFromPublicValue implements Computation<SBool, ProtocolBuilderBinary> {

  private DRes<SBool> inA;
  private boolean inB;

  public OrFromPublicValue(DRes<SBool> inA, boolean inB) {
    this.inA = inA;
    this.inB = inB;
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderBinary builder) {
    if (inB) {
      return builder.binary().known(true);
    } else {
      return inA;
    }
  }
}
