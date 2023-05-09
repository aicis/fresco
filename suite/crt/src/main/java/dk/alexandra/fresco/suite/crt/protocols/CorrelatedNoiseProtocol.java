package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTCombinedPad;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTNativeProtocol;

/** Generate a pair of correlated noise, eg <i>(r, r + ep)</i> for some <i>0 &le; e &le; n</i>. */
public class CorrelatedNoiseProtocol<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool, NoiseT> extends
        CRTComputation<CRTCombinedPad, ResourcePoolA, ResourcePoolB> {

  @Override
  public DRes<CRTCombinedPad> buildComputation(ProtocolBuilderNumeric builder, CRTNumericContext<ResourcePoolA, ResourcePoolB> context) {
    return context.getResourcePool().getDataSupplier().getCorrelatedNoise(builder);
  }
}
