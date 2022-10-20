package dk.alexandra.fresco.suite.crt.protocols.framework;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;

public abstract class CRTComputation<OutputT,
    ResourcePoolA extends NumericResourcePool,
    ResourcePoolB extends NumericResourcePool> implements
    Computation<OutputT, ProtocolBuilderNumeric> {

  public abstract DRes<OutputT> buildComputation(ProtocolBuilderNumeric builder, CRTNumericContext context);

  @Override
  public DRes<OutputT> buildComputation(ProtocolBuilderNumeric builder) {
    return buildComputation(builder,
        (CRTNumericContext) builder.getBasicNumericContext());
  }
}
