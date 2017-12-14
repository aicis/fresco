package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;

import java.util.ArrayList;
import java.util.List;

/**
 * Superclass containing the common variables and methods for the sender and
 * receiver parties of correlated OT with errors.
 * 
 * @author jot2re
 *
 */
public class CoteShared {
  // Constructor arguments
  protected int myId;
  protected int otherId;
  protected int kbitLength;
  protected int lambdaSecurityParam;
  protected Drbg rand;
  protected Network network;
  // Internal state variables
  protected boolean initialized;
  protected Ot ot;

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
    this.ot = new DummyOt(otherId, network);
    this.network = network;
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

  /**
   * Sends a list of StrictBitVectors to the default (0) channel.
   * 
   * @param list
   *          List to send
   * @return Returns true if the transmission was successful
   */
  protected boolean sendList(List<StrictBitVector> list) {
    for (StrictBitVector currentArr : list) {
      network.send(otherId, currentArr.toByteArray());
    }
    return true;
  }

  /**
   * Receives a list of StrictBitVectors from the default (0) channel
   * 
   * @param size
   *          Amount of elements in vector to receive
   * @return The list of received elements, or null in case an error occurred.
   */
  protected List<StrictBitVector> receiveList(int size) {
    List<StrictBitVector> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      byte[] byteBuffer = network.receive(otherId);
      StrictBitVector currentArr = new StrictBitVector(byteBuffer,
          byteBuffer.length * 8);
      list.add(currentArr);
    }
    return list;
  }

  /**
   * Construct a PRG based on a StrictBitVector seed.
   * 
   * @param seed
   *          The seed to use in the PRG
   * @return A new PRG based on the seed
   */
  protected Drbg makePrg(StrictBitVector seed) {
    // TODO make sure this is okay!
    Drbg prg = new PaddingAesCtrDrbg(seed.toByteArray(), 256);
    return prg;
  }
}
