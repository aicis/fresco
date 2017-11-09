package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.framework.network.Network;

public class MultiPartyProtocol extends BaseProtocol {

  protected List<Integer> partyIds;

  public MultiPartyProtocol(Integer myId, List<Integer> partyIds, BigInteger modulus,
      int kBitLength, Network network, ExecutorService executor, Random rand) {
    super(myId, modulus, kBitLength, network, executor, rand);
    this.partyIds = partyIds;
  }

}
