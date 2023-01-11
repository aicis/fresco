package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;

public class CRTCovertDataSupplier<L extends NumericResourcePool, R extends NumericResourcePool>
        extends CRTDataSupplier<L,R,CovertNoiseGenerator<L,R>> {

  public CRTCovertDataSupplier(CRTResourcePool<L,
      R> resourcePool) {
    this(resourcePool, 8, 2, 40);
  }

  public CRTCovertDataSupplier(CRTResourcePool<L, R> resourcePool,
                               int batchSize, int deterrenceFactor, int securityParam) {
    super(new CovertNoiseGenerator<>(batchSize, deterrenceFactor, securityParam), resourcePool);
  }
}
