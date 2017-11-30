package dk.alexandra.fresco.tools.ot.base;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class DummyOtBatch implements OtBatch<StrictBitVector> {

  private Ot<StrictBitVector> dummyOt;

  public DummyOtBatch(Integer otherId, int messageBitLength, Network network) {
    super();
    this.dummyOt = new DummyOt(otherId, messageBitLength, network);
  }

  @Override
  public void send(List<Pair<StrictBitVector, StrictBitVector>> messagePairs)
      throws MaliciousOtException, FailedOtException {
    for (Pair<StrictBitVector, StrictBitVector> pair : messagePairs) {
      dummyOt.send(pair.getFirst(), pair.getSecond());
    }
  }

  @Override
  public List<StrictBitVector> receive(BigInteger choiceBits, int numBits)
      throws MaliciousOtException, FailedOtException {
    List<StrictBitVector> choiceMessages = new ArrayList<>();
    for (int b = 0; b < numBits; b++) {
      choiceMessages.add(dummyOt.receive(choiceBits.testBit(b)));
    }
    return choiceMessages;
  }
  
}
