package dk.alexandra.fresco.tools.mascot;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;

public class MultiPartyProtocol extends BaseProtocol {

  protected List<Integer> partyIds;

  public MultiPartyProtocol(Integer myId, List<Integer> partyIds, BigInteger modulus,
      int kBitLength, ExtendedNetwork network, ExecutorService executor, Random rand) {
    super(myId, modulus, kBitLength, network, executor, rand);
    this.partyIds = partyIds;
  }

}
