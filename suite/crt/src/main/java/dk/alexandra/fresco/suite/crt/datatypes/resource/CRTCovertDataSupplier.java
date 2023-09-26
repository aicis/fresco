package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;

public class CRTCovertDataSupplier<L extends NumericResourcePool, R extends NumericResourcePool>
        extends CRTDataSupplier<L, R> {

  public CRTCovertDataSupplier() {
    this(DEFAULT_BATCH_SIZE, DEFAULT_DETERRENCE_FACTOR, DEFAULT_STATSECURITY);
  }

  public CRTCovertDataSupplier(int batchSize, int deterrenceFactor, int securityParam) {
    super(new CovertNoiseGenerator<>(batchSize, deterrenceFactor, securityParam));
  }
}
