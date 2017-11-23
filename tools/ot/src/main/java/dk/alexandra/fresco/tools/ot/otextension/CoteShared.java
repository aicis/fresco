package dk.alexandra.fresco.tools.ot.otextension;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.BitVector;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;

/** 
 * Superclass containing the common variables and methods 
 * for the sender and receiver parties of correlated OT with errors.
 * @author jot2re
 *
 */
public class CoteShared {
  // Constructor arguments
  protected int myId;
  protected int otherId;
  protected int kbitLength;
  protected int lambdaSecurityParam;
  protected Random rand;
  protected Network network;
  // Internal state variables
  protected boolean initialized;
  protected Ot<BitVector> ot;

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
  public CoteShared(int myId, int otherId, int kbitLength, int lambdaSecurityParam, Random rand, 
      Network network) {
    super();
    if (kbitLength < 1 || lambdaSecurityParam < 1
        || rand == null || network == null) {
      throw new IllegalArgumentException("Illegal constructor parameters");
    }
    if (kbitLength % 8 != 0) {
      throw new IllegalArgumentException(
          "Computational security parameter must be divisible by 8");
    }
    this.myId = myId;
    this.otherId = otherId;
    this.kbitLength = kbitLength;
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.rand = rand;
    this.ot = new DummyOt(otherId, kbitLength, network);
    this.network = network;
  }

  public int getMyId() {
    return myId;
  }

  public void setMyId(int myId) {
    this.myId = myId;
  }

  public int getOtherId() {
    return otherId;
  }

  public void setOtherId(int otherId) {
    this.otherId = otherId;
  }

  public int getkBitLength() {
    return kbitLength;
  }

  public void setkBitLength(int kbitLength) {
    this.kbitLength = kbitLength;
  }

  public int getLambdaSecurityParam() {
    return lambdaSecurityParam;
  }

  public void setLambdaSecurityParam(int lambdaSecurityParam) {
    this.lambdaSecurityParam = lambdaSecurityParam;
  }

  public Random getRand() {
    return rand;
  }

  public void setRand(Random rand) {
    this.rand = rand;
  }

  public Network getNetwork() {
    return network;
  }

  public void setNetwork(Network network) {
    this.network = network;
  }

  /**
   * Sends a list of byte arrays to the default (0) channel.
   * 
   * @param vector
   *          Vector to send
   * @return Returns true if the transmission was successful
   */
  protected boolean sendList(List<BitVector> vector) {
    for (BitVector currentArr : vector) {
      network.send(otherId, currentArr.asByteArr());
    }
    return true;
  }

  /**
   * Receives a list of byte arrays from the default (0) channel
   * 
   * @param vector
   *          Vector to receive
   * @param size
   *          Amount of elements in vector to receive
   * @return The list of received elements, or null in case an error occurred.
   */
  protected List<BitVector> receiveList(int size) {
    List<BitVector> vector = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      byte[] byteBuffer = network.receive(otherId);
      BitVector currentArr = new BitVector(byteBuffer, byteBuffer.length * 8);
      vector.add(currentArr);
    }
    return vector;
  }
}
