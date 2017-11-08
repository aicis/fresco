package dk.alexandra.fresco.tools.ot.otextension;

import java.math.BigInteger;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.ot.base.DummyOTBatch;
import dk.alexandra.fresco.tools.ot.base.OTBatch;

/**
 * Superclass containing the common variables and methods 
 * for the sender and receiver parties of correlated OT with errors 
 * @author jot2re
 *
 */
public class COTeShared {
  // Constructor arguments
  protected int otherID;
  protected int kBitLength;
  protected int lambdaSecurityParam;
  protected Random rand;
  protected Network network;
  // Internal state variables
  protected boolean initialized;
  protected OTBatch<BigInteger> ot;

  public COTeShared(int otherID, int kBitLength, int lambdaSecurityParam, Random rand, 
      Network network) {
    super();
    if (kBitLength < 1 | lambdaSecurityParam < 1 | rand == null
        | network == null)
      throw new IllegalArgumentException("Illegal constructor parameters");
    this.otherID = otherID;
    this.kBitLength = kBitLength;
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.rand = rand;
    this.ot = new DummyOTBatch(otherID, network);
    this.network = network;
  }

  public int getOtherID() {
    return otherID;
  }

  public void setOtherID(int otherID) {
    this.otherID = otherID;
  }

  public int getkBitLength() {
    return kBitLength;
  }

  public void setkBitLength(int kBitLength) {
    this.kBitLength = kBitLength;
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

}
