package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;

public class CRTSemiHonestDataSupplier<L extends NumericResourcePool,
    R extends NumericResourcePool>
        extends CRTDataSupplier<L, R, CRTSInt> {

  public CRTSemiHonestDataSupplier(CRTResourcePool<L,R> resourcePool) {
    super(new SemiHonestNoiseGenerator<>(10), resourcePool);
  }
}
