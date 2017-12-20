package dk.alexandra.fresco.tools.ot.base;

import java.util.ArrayList;
import java.util.List;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class DummyRotBatch implements RotBatch {

  private final Ot dummyOt;
  private final Drbg rand;

  public DummyRotBatch(int otherID, Network network, Drbg rand) {
    super();
    this.dummyOt = new DummyOt(otherID, network);
    this.rand = rand;
  }

  @Override
  public List<Pair<StrictBitVector, StrictBitVector>> send(int numMessages, int size) {
    List<Pair<StrictBitVector, StrictBitVector>> messagePairs = new ArrayList<>();
    for (int i = 0; i < numMessages; i++) {
      StrictBitVector messageZero = new StrictBitVector(size, this.rand);
      StrictBitVector messageOne = new StrictBitVector(size, this.rand);
      this.dummyOt.send(messageZero, messageOne);
      messagePairs.add(new Pair<>(messageZero, messageOne));
    }
    return messagePairs;
  }

  @Override
  public List<StrictBitVector> receive(StrictBitVector choiceBits, int messageSize) {
    List<StrictBitVector> choiceMessages = new ArrayList<>();
    for (int b = 0; b < choiceBits.getSize(); b++) {
      choiceMessages.add(dummyOt.receive(choiceBits.getBit(b)));
    }
    return choiceMessages;
  }

}
