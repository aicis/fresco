package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.DefaultLogical;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;

/**
 * Logical operators for Spdz2k on boolean shares. <p>NOTE: requires that inputs have previously
 * been converted to boolean shares!</p>
 */
public class Spdz2kLogicalBooleanMode<PlainT extends CompUInt<?, ?, PlainT>> extends
    DefaultLogical {

  protected Spdz2kLogicalBooleanMode(
      ProtocolBuilderNumeric builder) {
    super(builder);
  }

  @Override
  public DRes<SInt> and(DRes<SInt> bitA, DRes<SInt> bitB) {
    return super.and(bitA, bitB);
  }

}
