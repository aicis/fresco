package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.storage.MarlinStorage;

public interface MarlinResourcePool<T extends BigUInt<T>> extends NumericResourcePool {

  int getOperationalBitLength();

  int getEffectiveBitLength();

  MarlinStorage getStorage();

}
