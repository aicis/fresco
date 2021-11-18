package dk.alexandra.fresco.suite.crt.protocols.framework;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;

public abstract class CRTComputation<OutputT> implements
    Computation<OutputT, ProtocolBuilderNumeric> {

  public abstract DRes<OutputT> buildComputation(ProtocolBuilderNumeric builder, CRTRingDefinition ring, CRTNumericContext context);

  @Override
  public DRes<OutputT> buildComputation(ProtocolBuilderNumeric builder) {
    return buildComputation(builder,
        (CRTRingDefinition) builder.getBasicNumericContext().getFieldDefinition(),
        (CRTNumericContext) builder.getBasicNumericContext());
  }
}
