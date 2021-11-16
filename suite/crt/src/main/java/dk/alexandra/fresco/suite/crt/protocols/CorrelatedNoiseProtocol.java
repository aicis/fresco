package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;

public class CorrelatedNoiseProtocol<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> extends
    CRTNativeProtocol<SInt, ResourcePoolA, ResourcePoolB> {

  private CRTSInt r;

  @Override
  public EvaluationStatus evaluate(int round,
      CRTResourcePool<ResourcePoolA, ResourcePoolB> resourcePool, Network network) {

    this.r = resourcePool.getDataSupplier().getCorrelatedNoise();
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return r;
  }
}
