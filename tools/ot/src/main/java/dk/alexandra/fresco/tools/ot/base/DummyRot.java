package dk.alexandra.fresco.tools.ot.base;

import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class DummyRot implements Rot<StrictBitVector> {

  private Ot<StrictBitVector> dummyOt;
  private Random rand;

  public DummyRot(int otherId, Network network, Random rand) {
    super();
    this.rand = rand;
    this.dummyOt = new DummyOt(otherId, network);
  }

  @Override
  public StrictBitVector receive(Boolean choiceBit)
      throws MaliciousOtException, FailedOtException {
    return this.dummyOt.receive(choiceBit);
  }

  @Override
  public Pair<StrictBitVector, StrictBitVector> send(int size)
      throws MaliciousOtException, FailedOtException {
    StrictBitVector messageZero = new StrictBitVector(size, this.rand);
    StrictBitVector messageOne = new StrictBitVector(size, this.rand);
    this.dummyOt.send(messageZero, messageOne);
    return new Pair<>(messageZero, messageOne);
  }

}
