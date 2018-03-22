package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.util.Drbg;
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
   * The DRBG is useful for protocols which needs a form of shared randomness where the random bytes
   * are not easily guessed by an adversary. This generator will provide exactly that. For explicit
   * security guarantees, we refer to implementations of {@link dk.alexandra.fresco.framework.util.Drbg}.
   *
   * @return An instance of a DRBG.
   */
  Drbg getRandomGenerator();

  /**
   * Gets the Spdz store.
   *
   * @return the store
   */
  SpdzStorage getStore();


}
