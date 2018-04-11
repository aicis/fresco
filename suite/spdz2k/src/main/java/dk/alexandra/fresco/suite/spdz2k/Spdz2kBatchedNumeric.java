package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;

public class Spdz2kBatchedNumeric<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNumeric<PlainT> {

  Spdz2kBatchedNumeric(ProtocolBuilderNumeric builder,
      CompUIntFactory<PlainT> factory) {
    super(builder, factory);
  }

}
