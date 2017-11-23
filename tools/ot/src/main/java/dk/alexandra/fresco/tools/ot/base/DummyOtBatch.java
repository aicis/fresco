package dk.alexandra.fresco.tools.ot.base;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.BitVector;
import dk.alexandra.fresco.framework.util.Pair;

public class DummyOtBatch implements OtBatch<BitVector> {

  private Ot<BitVector> dummyOT;

  public DummyOtBatch(Integer otherId, int messageBitLength, Network network) {
    super();
    this.dummyOT = new DummyOt(otherId, messageBitLength, network);
  }

  @Override
  public void send(List<Pair<BitVector, BitVector>> messagePairs) {
    for (Pair<BitVector, BitVector> pair : messagePairs) {
      dummyOT.send(pair.getFirst(), pair.getSecond());
    }
  }

  @Override
  public List<BitVector> receive(BigInteger choiceBits, int numBits) {
    List<BitVector> choiceMessages = new ArrayList<>();
    for (int b = 0; b < numBits; b++) {
      choiceMessages.add(dummyOT.receive(choiceBits.testBit(b)));
    }
    return choiceMessages;
  }
  
}
