package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.suite.crt.datatypes.CRTNoise;

public class CRTSemiHonestDataSupplier<L extends NumericResourcePool,
    R extends NumericResourcePool>
        extends CRTDataSupplier<L, R, CRTNoise> {

  public CRTSemiHonestDataSupplier(CRTResourcePool<L,R> resourcePool) {
    super(new SemiHonestNoiseGenerator<>(10), resourcePool);
  }
}
