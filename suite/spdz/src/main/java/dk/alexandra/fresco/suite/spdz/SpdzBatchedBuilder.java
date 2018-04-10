package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.PreprocessedValues;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.spdz.gates.batched.BatchedNumeric;

/**
 * Basic native builder for the SPDZ protocol suite.
 */
public class SpdzBatchedBuilder extends SpdzBuilder {

  SpdzBatchedBuilder(BasicNumericContext basicNumericContext,
      RealNumericContext realNumericContext) {
    super(basicNumericContext, realNumericContext);
  }

  @Override
  public PreprocessedValues createPreprocessedValues(ProtocolBuilderNumeric protocolBuilder) {
    return pipeLength -> {
      SpdzExponentiationPipeProtocol spdzExpPipeProtocol =
          new SpdzExponentiationPipeProtocol(pipeLength);
      return protocolBuilder.append(spdzExpPipeProtocol);
    };
  }

  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric protocolBuilder) {
    if (protocolBuilder.isParallel()) {
      return new BatchedNumeric(protocolBuilder);
    } else {
      return new SpdzNumeric(protocolBuilder);
    }
  }

}
