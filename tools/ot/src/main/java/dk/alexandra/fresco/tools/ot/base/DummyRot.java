package dk.alexandra.fresco.tools.ot.base;

import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class DummyRot implements Rot<StrictBitVector> {

  private Ot<StrictBitVector> dummyOt;
  private int messageBitLength;
  private Random rand;

  public DummyRot(int otherId, Network network, Random rand, int bitLength) {
    super();
    this.rand = rand;
    this.messageBitLength = bitLength;
    this.dummyOt = new DummyOt(otherId, messageBitLength, network);
  }

  @Override
  public StrictBitVector receive(Boolean choiceBit)
      throws MaliciousOtException, FailedOtException {
    return this.dummyOt.receive(choiceBit);
  }

  @Override
  public Pair<StrictBitVector, StrictBitVector> send()
      throws MaliciousOtException, FailedOtException {
    StrictBitVector messageZero = new StrictBitVector(this.messageBitLength, this.rand);
    StrictBitVector messageOne = new StrictBitVector(this.messageBitLength, this.rand);
    this.dummyOt.send(messageZero, messageOne);
    return new Pair<>(messageZero, messageOne);
  }

}
