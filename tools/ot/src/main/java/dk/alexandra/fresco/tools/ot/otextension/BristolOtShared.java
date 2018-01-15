package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;

/**
 * Superclass containing the common variables and methods for the sender and
 * receiver parties of a Bristol OT extension.
 */
public class BristolOtShared {
  private final RotShared rot;
  private final int batchSize;

  /**
   * Constructs a Bristol OT extension super-class using an underlying random OT
   * object, using this to preprocess batches of {@code batchSize} random OTs.
   *
   * @param rot
   *          The underlying correlated OT with errors
   * @param batchSize
   *          The amount of random OTs to internally preprocess in a given batch
   */
  public BristolOtShared(RotShared rot, int batchSize) {
    super();
    this.rot = rot;
    this.batchSize = batchSize;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public int getOtherId() {
    return rot.getOtherId();
  }

  public Drbg getRand() {
    return rot.getRand();
  }

  public Network getNetwork() {
    return rot.getNetwork();
  }
}
