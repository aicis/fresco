package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.spdz.gates.batched.SpdzBatchedNumeric;

/**
 * Builder that uses batched native protocols for the SPDZ protocol suite.
 */
public class SpdzBatchedBuilder extends SpdzBuilder {

  SpdzBatchedBuilder(BasicNumericContext basicNumericContext,
      RealNumericContext realNumericContext) {
    super(basicNumericContext, realNumericContext);
  }

  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric protocolBuilder) {
    if (protocolBuilder.isParallel()) {
      return new SpdzBatchedNumeric(protocolBuilder);
    } else {
      return new SpdzNumeric(protocolBuilder);
    }
  }

}
