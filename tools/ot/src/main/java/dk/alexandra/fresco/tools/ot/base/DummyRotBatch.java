package dk.alexandra.fresco.tools.ot.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.BitVector;
import dk.alexandra.fresco.framework.util.Pair;

public class DummyRotBatch implements RotBatch<BitVector> {

  private Rot<BitVector> dummyRot;

  public DummyRotBatch(int otherID, Network network, Random rand, int bitLength) {
    super();
    this.dummyRot = new DummyRot(otherID, network, rand, bitLength);
  }

  @Override
  public List<Pair<BitVector, BitVector>> send(int numMessages) {
    List<Pair<BitVector, BitVector>> messagePairs = new ArrayList<>();
    for (int i = 0; i < numMessages; i++) {
      messagePairs.add(dummyRot.send());
    }
    return messagePairs;
  }

  @Override
  public List<BitVector> receive(BitVector choiceBits, int numBits) {
    List<BitVector> choiceMessages = new ArrayList<>();
    for (int b = 0; b < numBits; b++) {
      choiceMessages.add(dummyRot.receive(choiceBits.get(b)));
    }
    return choiceMessages;
  }

}
