package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;

public class BaseProtocol {

  protected Integer myId;
  protected BigInteger modulus;
  protected int kBitLength;
  protected ExtendedNetwork network;
  protected ExecutorService executor;
  protected Random rand;

  public BaseProtocol(Integer myId, BigInteger modulus, int kBitLength, ExtendedNetwork network,
      ExecutorService executor, Random rand) {
    super();
    this.myId = myId;
    this.modulus = modulus;
    this.kBitLength = kBitLength;
    this.network = network;
    this.executor = executor;
    this.rand = rand;
  }

  public Integer getMyId() {
    return myId;
  }

  public void setMyId(Integer myId) {
    this.myId = myId;
  }

  public BigInteger getModulus() {
    return modulus;
  }

  public void setModulus(BigInteger modulus) {
    this.modulus = modulus;
  }

  public int getkBitLength() {
    return kBitLength;
  }

  public void setkBitLength(int kBitLength) {
    this.kBitLength = kBitLength;
  }

}
