package dk.alexandra.fresco.tools.ot.otextension;

import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;

/**
 * Superclass containing the common variables and methods for the sender and
 * receiver parties of a Bristol OT extension.
 * 
 * @author jot2re
 *
 */
public class BristolOtShared {
  protected RotShared rot;
  protected boolean initialized = false;
  protected int batchSize;

  /**
   * Constructs a Bristol OT extension super-class using an underlying random OT
   * object, using this to preprocess batches of "batchSize" random OTs.
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

  public int getOtherId() {
    return rot.getOtherId();
  }

  public int getKbitLength() {
    return rot.getKbitLength();
  }

  public int getLambdaSecurityParam() {
    return rot.getLambdaSecurityParam();
  }

  public Random getRand() {
    return rot.getRand();
  }

  public Network getNetwork() {
    return rot.getNetwork();
  }
}