package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.nio.ByteBuffer;

/**
 * Superclass containing the common methods for the sender and
 * receiver parties of correlated OT with errors.
 */
public abstract class CoteShared {
  private final int instanceId;

  /**
   * Constructs a correlated OT extension with errors super-class.
   *
   * @param instanceId
   *          The instance Id of the given protocol
   */
  public CoteShared(int instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * Initializes a PRG based on a seed and the internal instance ID.
   *
   * @param originalSeed
   *          The seed to initialize the PRG from
   * @return The initialized PRG
   */
  Drbg initPrg(StrictBitVector originalSeed) {
    byte[] seedBytes = originalSeed.toByteArray();
    ByteBuffer idBuffer = ByteBuffer.allocate(seedBytes.length + Integer.BYTES);
    byte[] newSeed = idBuffer.putInt(instanceId).put(seedBytes).array();
    return AesCtrDrbgFactory.fromDerivedSeed(newSeed);
  }
}
