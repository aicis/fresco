package dk.alexandra.fresco.tools.ot.base;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;

public class DummyOTBatch implements OTBatch<BigInteger> {

  private OT<BigInteger> dummyOT;

  public DummyOTBatch(int otherID, Network network) {
    super();
    this.dummyOT = new DummyOT(otherID, network);
  }

  @Override
  public void send(List<Pair<BigInteger, BigInteger>> messagePairs) {
    for (Pair<BigInteger, BigInteger> pair : messagePairs) {
      dummyOT.send(pair.getFirst(), pair.getSecond());
    }
  }

  @Override
  public List<BigInteger> receive(BigInteger choiceBits, int numBits) {
    List<BigInteger> choiceMessages = new ArrayList<>();
    for (int b = 0; b < numBits; b++) {
      choiceMessages.add(dummyOT.receive(choiceBits.testBit(b)));
    }
    return choiceMessages;
  }
}
