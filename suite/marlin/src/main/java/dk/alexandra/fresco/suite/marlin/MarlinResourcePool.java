package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.storage.MarlinDataSupplier;
import dk.alexandra.fresco.suite.marlin.storage.MarlinOpenedValueStore;

public interface MarlinResourcePool<T extends BigUInt<T>> extends NumericResourcePool {

  /**
   * Returns instance of {@link MarlinOpenedValueStore} which tracks all opened, unchecked values.
   */
  MarlinOpenedValueStore<T> getOpenValueStore();

  /**
   * Returns instance of {@link MarlinDataSupplier} which provides pre-processed material such as
   * multiplication triples.
   */
  MarlinDataSupplier<T> getDataSupplier();

  // TODO not clear that this belongs here
  int getOperationalBitLength();

  // TODO not clear that this belongs here
  int getEffectiveBitLength();

}
