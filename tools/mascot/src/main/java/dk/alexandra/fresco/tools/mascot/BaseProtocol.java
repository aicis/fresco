package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.framework.network.Network;

public class BaseProtocol {

  protected List<Integer> partyIDs;
  protected Integer myID;
  protected BigInteger modulus;
  protected int kBitLength;
  protected Network network;
  protected ExecutorService executor;
  protected Random rand;
  
  public BaseProtocol(List<Integer> partyIDs, Integer myID, BigInteger modulus, int kBitLength,
      Network network, ExecutorService executor, Random rand) {
    super();
    this.partyIDs = partyIDs;
    this.myID = myID;
    this.modulus = modulus;
    this.kBitLength = kBitLength;
    this.network = network;
    this.executor = executor;
    this.rand = rand;
  }
  
}
