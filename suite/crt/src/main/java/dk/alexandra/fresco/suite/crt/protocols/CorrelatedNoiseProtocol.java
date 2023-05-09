package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTCombinedPad;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;

/** Generate a pair of correlated noise, eg <i>(r, r + ep)</i> for some <i>0 &le; e &le; n</i>. */
public class CorrelatedNoiseProtocol<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> extends
        CRTComputation<CRTCombinedPad, ResourcePoolA, ResourcePoolB> {

  @Override
  public DRes<CRTCombinedPad> buildComputation(ProtocolBuilderNumeric builder, CRTNumericContext<ResourcePoolA, ResourcePoolB> context) {
    return context.getResourcePool().getDataSupplier().getCorrelatedNoise(builder);
  }
}
