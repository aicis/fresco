package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;

public class TwoPartyProtocol extends BaseProtocol {

  protected Integer otherId;
  
  public TwoPartyProtocol(Integer myId, Integer otherId, BigInteger modulus, int kBitLength,
      ExtendedNetwork network, ExecutorService executor, Random rand) {
    super(myId, modulus, kBitLength, network, executor, rand);
    this.otherId = otherId;
  }

  public Integer getOtherId() {
    return otherId;
  }

  public void setOtherId(Integer otherId) {
    this.otherId = otherId;
  }
  
}
