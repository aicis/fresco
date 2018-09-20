package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.DefaultAdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kTruncationPairProtocol;

/**
 * Spdz2k-specific advanced numeric functionality.
 */
public class Spdz2kAdvancedNumeric extends DefaultAdvancedNumeric {

  Spdz2kAdvancedNumeric(
      BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder) {
    super(factoryNumeric, builder);
  }

  @Override
  public DRes<TruncationPair> generateTruncationPair(int d) {
    return builder.seq(seq -> seq.append(new Spdz2kTruncationPairProtocol<>(d)));
  }

}
