package dk.alexandra.fresco.tools.ot.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class DummyRotBatch implements RotBatch<StrictBitVector> {

  private Rot<StrictBitVector> dummyRot;

  public DummyRotBatch(int otherID, Network network, Random rand) {
    super();
    this.dummyRot = new DummyRot(otherID, network, rand);
  }

  @Override
  public List<Pair<StrictBitVector, StrictBitVector>> send(int numMessages,
      int size) throws MaliciousOtException, FailedOtException {
    List<Pair<StrictBitVector, StrictBitVector>> messagePairs = new ArrayList<>();
    for (int i = 0; i < numMessages; i++) {
      messagePairs.add(dummyRot.send(size));
    }
    return messagePairs;
  }

  @Override
  public List<StrictBitVector> receive(StrictBitVector choiceBits,
      int messageSize)
      throws MaliciousOtException, FailedOtException {
    List<StrictBitVector> choiceMessages = new ArrayList<>();
    for (int b = 0; b < choiceBits.getSize(); b++) {
      choiceMessages.add(dummyRot.receive(choiceBits.getBit(b)));
    }
    return choiceMessages;
  }

}
