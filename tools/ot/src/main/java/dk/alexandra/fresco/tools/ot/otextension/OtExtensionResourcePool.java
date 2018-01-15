package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;

import java.security.MessageDigest;

public interface OtExtensionResourcePool extends ResourcePool {

  /**
   * Gets the ID of the other party.
   *
   * @return The ID of the other party
   */
  int getOtherId();

  /**
   * Get the computational security parameter.
   *
   * @return The computational security parameter.
   */
  int getComputationalSecurityParameter();

  /**
   * Gets OT security parameter num bits (lambda in Mascot paper).
   *
   * @return lambda security parameter
   */
  int getLambdaSecurityParam();

  /**
   * Gets the {@code MessageDigest} object implementing the internally used hash
   * algorithm.
   *
   * @return The {@code MessageDigest} object implementing the internally used
   *         hash algorithm.
   */
  MessageDigest getDigest();

  /**
   * Gets the instance ID of this resource pool.
   *
   * @return The instance ID of this resource poo
   */
  int getInstanceId();

  /**
   * Gets the seed OTs.
   *
   * @return The seed OTs.
   */
  RotList getSeedOts();

  /**
   * Gets the coin tossing instance.
   *
   * @return The coin tossing instance.
   */
  CoinTossing getCoinTossing();
}
