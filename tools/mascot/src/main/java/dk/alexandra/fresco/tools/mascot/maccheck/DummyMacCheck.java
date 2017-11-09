package dk.alexandra.fresco.tools.mascot.maccheck;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.MultiPartyProtocol;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class DummyMacCheck extends MultiPartyProtocol implements MacCheck {

  public DummyMacCheck(Integer myId, List<Integer> partyIds, BigInteger modulus, int kBitLength,
      Network network, ExecutorService executor, Random rand) {
    super(myId, partyIds, modulus, kBitLength, network, executor, rand);
    // TODO Auto-generated constructor stub
  }

  @Override
  public boolean check(FieldElement opened, FieldElement macKeyShare, FieldElement macShare) {
    // TODO Auto-generated method stub
    return false;
  }

}
