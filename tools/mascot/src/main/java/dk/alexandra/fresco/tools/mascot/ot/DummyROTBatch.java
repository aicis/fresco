package dk.alexandra.fresco.tools.mascot.ot;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DummyROTBatch implements ROTBatch<BigInteger> {

  private ROT<BigInteger> dummyROT;

  public DummyROTBatch(int otherID, Network network, Random rand, int bitLength) {
    super();
    this.dummyROT = new DummyROT(otherID, network, rand, bitLength);
  }

  @Override
  public List<Pair<BigInteger, BigInteger>> send(int numMessages) {
    List<Pair<BigInteger, BigInteger>> messagePairs = new ArrayList<>();
    for (int i = 0; i < numMessages; i++) {
      messagePairs.add(dummyROT.send());
    }
    return messagePairs;
  }

  @Override
  public List<BigInteger> receive(List<Boolean> choiceBits) {
    List<BigInteger> choiceMessages = new ArrayList<>();
    for (Boolean choiceBit : choiceBits) {
      choiceMessages.add(dummyROT.receive(choiceBit));
    }
    return choiceMessages;
  }

}
