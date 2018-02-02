package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;

public interface MarlinResourcePool extends NumericResourcePool {

  int getOperationalBitLength();

  int getEffectiveBitLength();

}
