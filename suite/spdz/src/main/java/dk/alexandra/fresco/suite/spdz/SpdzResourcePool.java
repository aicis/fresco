package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;

/**
 * The resource pool for Spdz. Represents the resources used for on invocation of the
 * spdz protocol suite.
 */
public interface SpdzResourcePool extends NumericResourcePool {

  /**
   * Gets the Spdz store.
   *
   * @return the store
   */
  SpdzStorage getStore();


}
