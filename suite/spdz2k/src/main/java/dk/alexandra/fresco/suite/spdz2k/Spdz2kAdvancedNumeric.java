package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.DefaultAdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.advanced.TruncateSpdz2k;

public class Spdz2kAdvancedNumeric extends DefaultAdvancedNumeric {

  Spdz2kAdvancedNumeric(
      BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder) {
    super(factoryNumeric, builder);
  }

  @Override
  public DRes<SInt> truncate(DRes<SInt> input, int shifts) {
//    return new Sp
    return builder.seq(new TruncateSpdz2k<>(input, shifts));
  }

}
