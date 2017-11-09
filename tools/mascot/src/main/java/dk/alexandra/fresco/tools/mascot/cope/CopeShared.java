package dk.alexandra.fresco.tools.mascot.cope;

import java.math.BigInteger;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.utils.DummyPRF;
import dk.alexandra.fresco.tools.mascot.utils.PRF;
import dk.alexandra.fresco.tools.ot.base.DummyOTBatch;
import dk.alexandra.fresco.tools.ot.base.OTBatch;

public class CopeShared {

  protected BigInteger modulus;
  protected int kBitLength;
  protected int otherID;
  protected int lambdaSecurityParam;
  protected BigInteger counter;
  protected Random rand;
  protected OTBatch<BigInteger> ot;
  protected boolean initialized;
  protected PRF prf;
  protected Network network;
  
  public CopeShared(int otherID, int kBitLength, int lambdaSecurityParam, Random rand,
      Network network, BigInteger prime) {
    super();
    this.otherID = otherID;
    this.kBitLength = kBitLength;
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.counter = BigInteger.valueOf(0);
    this.rand = rand;
    this.ot = new DummyOTBatch(otherID, network);
    this.initialized = false;
    this.prf = new DummyPRF();
    this.modulus = prime;
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

}
