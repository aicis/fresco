package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;

public class CRTSemiHonestDataSupplier<L extends NumericResourcePool,
    R extends NumericResourcePool>
        extends CRTDataSupplier<L, R> {

  public CRTSemiHonestDataSupplier(CRTResourcePool<L,R> resourcePool) {
    super(new SemiHonestNoiseGenerator<>(DEFAULT_BATCH_SIZE, DEFAULT_STATSECURITY), resourcePool);
  }
}
