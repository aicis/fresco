package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;

/**
 * Superclass containing the common variables and methods for the sender and
 * receiver parties of correlated OT with errors.
 * 
 * @author jot2re
 *
 */
public class CoteShared {
  // Constructor arguments
  private final int myId;
  private final int otherId;
  private final int kbitLength;
  private final int lambdaSecurityParam;
  private final Drbg rand;
  private final Network network;
  // Internal state variables
  private boolean initialized = false;

  /**
   * Constructs a correlated OT extension with errors super-class.
   * 
   * @param myId
   *          The ID of the calling party
   * @param otherId
   *          ID of the other party to execute with
   * @param kbitLength
   *          The computational security parameter
   * @param lambdaSecurityParam
   *          The statistical security parameter
   * @param rand
   *          The current party's cryptographically secure randomness generator
   * @param network
   *          The network object used to communicate with the other party
   */
  public CoteShared(int myId, int otherId, int kbitLength,
      int lambdaSecurityParam, Drbg rand, Network network) {
    super();
    if (kbitLength < 1 || lambdaSecurityParam < 1
        || rand == null || network == null) {
      throw new IllegalArgumentException("Illegal constructor parameters");
    }
    if (kbitLength % 8 != 0) {
      throw new IllegalArgumentException(
          "Computational security parameter must be divisible by 8");
    }
    if (lambdaSecurityParam % 8 != 0) {
      throw new IllegalArgumentException(
          "Statistical security parameter must be divisible by 8");
    }
    this.myId = myId;
    this.otherId = otherId;
    this.kbitLength = kbitLength;
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.rand = rand;
    this.network = network;
  }

  public void initialize() {
    initialized = true;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public int getMyId() {
    return myId;
  }

  public int getOtherId() {
    return otherId;
  }

  public int getkBitLength() {
    return kbitLength;
  }

  public int getLambdaSecurityParam() {
    return lambdaSecurityParam;
  }

  public Drbg getRand() {
    return rand;
  }

  public Network getNetwork() {
    return network;
  }
}
