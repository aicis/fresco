package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.security.MessageDigest;

/**
 * The resource pool for Spdz. Represents the resources used for on invocation of the
 * spdz protocol suite.
 */
public interface SpdzResourcePool extends NumericResourcePool {

  /**
   * Gets the message digest for this protocol suite invocation.
   *
   * @return the message digest
   */
  MessageDigest getMessageDigest();

  /**
   * Gets the Spdz store.
   *
   * @return the store
   */
  SpdzStorage getStore();


}
