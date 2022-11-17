package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTNativeProtocol;

/** Generate a pair of correlated noise, eg <i>(r, r + ep)</i> for some <i>0 &le; e &le; n</i>. */
public class CorrelatedNoiseProtocol<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> extends
    CRTNativeProtocol<SInt, ResourcePoolA, ResourcePoolB> {

  private final ProtocolBuilderNumeric builder;
  private CRTSInt r;

  public CorrelatedNoiseProtocol(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }
  @Override
  public EvaluationStatus evaluate(int round,
      CRTResourcePool<ResourcePoolA, ResourcePoolB> resourcePool, Network network) {

    this.r = resourcePool.getDataSupplier().getCorrelatedNoise(builder);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return r;
  }
}
